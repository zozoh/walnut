define(function (require, exports, module) {
//===================================================================
function gen_url(opt) {
    var ta = opt.target;
    var url = "/o/upload/";
    // 指定的 ID，所以对象必须存在
    if (ta.id) {
        url += "id:" + ta.id + "?";
    }
    // 否则可以自动创建
    else {
        url += ta.ph + "?race=" + ta.race + "&cie=true&";
        // FIXME 绝对目录
        if (ta.ph.substr(0, 1) == '/') {
            url += "abpath=true&"
        } else {
            url += "abpath=false&"
        }
    }
    url += "nm={{file.name}}&sz={{file.size}}&mime={{file.type}}";

    if (!opt.replaceable)
        url += "&dupp=${major}(${nb})${suffix}";

    return url;
}

//===================================================================
var ZUI = require("zui");
module.exports = ZUI.def("ui.upload", {
    //...............................................................
    dom: "ui/upload/upload.html",
    css: "theme/ui/upload/upload.css",
    i18n: "ui/upload/i18n/{{lang}}.js",
    //...............................................................
    init: function (options) {
        var UI = this;
        //console.log("jjjjj init uploadd")
        if (options.validate && !_.isFunction(options.validate)) {
            var regex;
            if (_.isString(options.validate)) {
                regex = new RegExp(options.validate);
            } else if (_.isRegExp(options.validate)) {
                regex = options.validate;
            } else {
                throw "invalid params validate";
            }
            options.validate = function (file) {
                if (!regex.test(file.name)) {
                    throw UI.msg("upload.invalid", file);
                }
            };
        }
    },
    //...............................................................
    events: {
        "dblclick .ui-upload-multi": function () {
            var jList = this.arena.find(".ui-upload-multi");
            var jTip = jList.find(".ui-upload-multi-tip").show();
            jList.children().not(".ui-upload-multi-tip").remove();
        }
    },
    //...............................................................
    redraw: function () {
        var UI = this;
        var opt = UI.options;
        
        // 更新上传目标
        if(opt.target)
            UI.setTarget(opt.target);

        // 更新帮助
        if (opt.tip) {
            UI.arena.find(".ui-upload-tip").html(opt.tip);
        }
    },
    //...............................................................
    setTarget : function(ta) {
        var UI  = this;
        var opt = UI.options;
        opt.target = ta;
        var jTitle = UI.$el.find(".ui-upload-sky");

        // 默认根据目标的 race 判断模式
        if (_.isUndefined(opt.multi)) {
            UI.is_multi = (ta.race == 'DIR');
        }else{
            UI.is_multi = opt.multi ? true : false;
        }


        // 修改标题
        if(opt.title) {
            jTitle.css("display", "");
            // 得到目标路径名称
            var ph = ta.ph;
            if(!ph && ta.id) {
                var o = Wn.getById(ta.id);
                ph = o.ph;
            }
            // 生成路径字符串
            var title = UI.msg(UI.is_multi ? "upload.multi.sky" : "upload.single.sky", {
                ph : Wn.objDisplayPath(UI, ph, 3)
            });
            jTitle.html(title);
        }
        // 移除
        else {
            jTitle.css("display", "none");
        }

        // 多文件上传
        if (UI.is_multi) {
            UI.$el.on("dragover", ".ui-upload-arena", UI.multi.on_dragover);
            UI.$el.on("dragleave", ".ui-upload-arena", UI.multi.on_dragleave);
            UI.$el.on("drop", ".ui-upload-arena", UI.multi.on_drop);
            UI.arena.find(".ui-upload-arena").append(UI.ccode("multi.main"));
        }
        // 单文件上传
        else {
            UI.$el.on("dragover", ".ui-upload-arena", UI.single.on_dragover);
            UI.$el.on("dragleave", ".ui-upload-arena", UI.single.on_dragleave);
            UI.$el.on("drop", ".ui-upload-arena", UI.single.on_drop);
            UI.arena.find(".ui-upload-arena").append(UI.ccode("single.main"));
            // 如果上传目标是图片
            if (opt.preview || /^image\//.test(ta.mime)) {
                var bgurl = ta.id ? "/o/read/id:" + ta.id : opt.preview;
                bgurl += "?" + $z.timestamp();
                UI.arena.find(".ui-upload-single").css({
                    "background-image": "url(" + bgurl + ")",
                    "background-size": opt.backgroundSize || "cover"
                });
            }
        }

    },
    //...............................................................
    resize: function () {
        var UI = this;
        var jSky = UI.arena.find(".ui-upload-sky");
        var jMain = UI.arena.find(".ui-upload-arena");
        var jFooter = UI.arena.find(".ui-upload-footer");
        var H = UI.arena.height();
        var hSky = jSky.outerHeight();
        var hFooter = jFooter.outerHeight();
        // console.log(H, hSky, hFooter)
        jMain.css("height", H - hSky - hFooter);
    },
    //...............................................................
    single: {
        on_dragover: function (e) {
            e.stopPropagation();
            e.preventDefault();
            $(this).attr("drag", "over");
        },
        on_dragleave: function (e) {
            $(this).removeAttr("drag");
        },
        on_drop: function (e) {
            e.stopPropagation();
            e.preventDefault();
            var UI = ZUI.checkInstance(this);
            var opt = UI.options;
            var context = opt.context || UI;
            var jSingle = $(this).removeAttr("drag").find(".ui-upload-single");
            if ("yes" == jSingle.attr("ing")) {
                alert(UI.msg("upload.single.err_ing"));
                return;
            }
            var UI = ZUI.checkInstance(this);
            var ta = UI.options.target;
            // 只能允许一个文件
            if (e.originalEvent.dataTransfer.files.length != 1) {
                alert(UI.msg("upload.single.err_multi"));
                return;
            }
            // 得到这个文件
            var file = e.originalEvent.dataTransfer.files[0];
            if (!UI._do_validate(file))
                return;
            // 开始上传
            jSingle.attr("ing", "yes");
            $z.uploadFile({
                url: gen_url(opt),
                file: file,
                progress: function (e) {
                    UI.updateProgress.call(UI, jSingle, e.loaded, e.total);
                },
                evalReturn: "ajax",
                done: function (re) {
                    UI.__obj = re;  // 记录成功的对象以备 finish 使用
                    $z.invoke(opt, "done", [re], context);
                },
                fail: function (re) {
                    alert(re.msg);
                    $z.invoke(opt, "fail", [re], context);
                },
                complete: function (re, status) {
                    jSingle.attr("ing", "no")
                        .find(".inner").css("width", "1px");
                    // 修改显示
                    if (opt.preview || /^image\//.test(ta.mime)) {
                        UI.arena.find(".ui-upload-single").css({
                            "background-image": "url(/o/read/id:" + re.id + "?" + $z.timestamp() + ")"
                        });
                    }
                    // 调用回调
                    $z.invoke(opt, "complete", [re, status], context);
                    $z.invoke(opt, "finish", [UI.__obj], context);
                }
            });
        }
    },
    //...............................................................
    multi: {
        on_dragover: function (e) {
            e.stopPropagation();
            e.preventDefault();
            $(this).attr("drag", "over");
        },
        on_dragleave: function (e) {
            $(this).removeAttr("drag");
        },
        on_drop: function (e) {
            $(this).removeAttr("drag");
            e.stopPropagation();
            e.preventDefault();
            var UI = ZUI.checkInstance(this);
            if (!UI.multi.addFiles.call(UI, e.originalEvent.dataTransfer.files))
                return;
            // 记录成功的对象以备 finish 使用
            UI.__list = [];
            UI.multi.doUpload.call(UI);
        },
        addFiles: function (files) {
            var UI = this;
            var jList = this.arena.find(".ui-upload-multi");
            var jTip = jList.find(".ui-upload-multi-tip").hide();
            jList.children().not(".ui-upload-multi-tip").remove();
            for (var i = 0; i < files.length; i++) {
                var file = files[i];
                if (!UI._do_validate(file))
                    return false;
                var jItem = this.ccode("list.item");
                jItem.data("FILE", file).find(".fname").text(file.name);
                jList.append(jItem);
            }
            this.resize();
            return true;
        },
        doUpload: function () {
            var UI = this;
            var opt = UI.options;
            var context = opt.context || UI;
            var jItem = this.arena.find(".ui-upload-item")
                .not(".ui-upload-item-done")
                .not(".ui-upload-item-fail")
                .first();
            // 没有更多项目了，返回
            if (jItem.size() <= 0) {
                $z.invoke(opt, "finish", [UI.__list], context);
                return;
            }
            jItem[0].scrollIntoView();
            // 开始上传
            $z.uploadFile({
                url: gen_url(opt),
                file: jItem.data("FILE"),
                progress: function (e) {
                    UI.updateProgress.call(UI, jItem, e.loaded, e.total);
                },
                // 上传前修改提示图标的标签
                beforeSend: function (xhr) {
                    jItem.find(".thumbnail .fa").prop("className", "fa fa-spinner fa-spin");
                    jItem.find(".remote .fa").addClass("fa-spin");
                },
                evalReturn: "ajax",
                done: function (re) {
                    jItem.addClass("ui-upload-item-done");
                    jItem.find(".thumbnail .fa").prop("className", "fa fa-check-circle");
                    jItem.find(".rname").text(re.nm);
                    // 记录成功的对象以备 finish 使用
                    UI.__list.push(re);
                    // 调用回调
                    $z.invoke(opt, "done", [re], context);
                },
                fail: function (re) {
                    jItem.addClass("ui-upload-item-fail");
                    jItem.find(".thumbnail .fa").prop("className", "fa fa-warning");
                    jItem.find(".rname").text(re.msg)
                    $z.invoke(opt, "fail", [re], context);
                },
                complete: function (re, status) {
                    jItem.find(".remote .fa").removeClass("fa-spin");
                    $z.invoke(opt, "complete", [re, status], context);
                    UI.multi.doUpload.call(UI);
                }
            });
        }
    },
    _do_validate: function (file) {
        try {
            $z.invoke(this.options, "validate", [file]);
            return true;
        } catch (e) {
            alert(e);
            return false;
        }
    },
    //...............................................................
    updateProgress: function (jq, loaded, total) {
        var jOuter = jq.find(".outer");
        var jInner = jq.find(".inner");
        var jNb = jq.find(".nb");
        var W = jOuter.width();
        var per = loaded / total;
        jNb.text(parseInt(per * 1000) / 10 + "%");
        jInner.css("width", W * per);
    }
});
//===================================================================
});