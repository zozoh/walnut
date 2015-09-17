/*
在 $pel 内创建一组 DOM，用来提供用户的文件上传界面
对应的 options 可以有以下选项
..............................................................
使用方法:

var UploadUI = require("ui/upload/upload");
new UploadUI({
    $pel  : $(document.body),
    //.......................................
    // 设置标题，如果 false 将隐藏标题栏
    // 自定义标题支持字符串模板，占位符为 target 的所有字段 
    title : false|true|"自定义",
    //.......................................
    // 指定多文件上传，默认为 false
    multi : true | false,
    //.......................................
    // 一个 WnObj 的实例表示上传目标
    // 如果是个目录就是多文件上传，如果是文件就是单文件上传
    target  : {
        id : "34acde..",     // 如果指定了 ID 则必须是已经存在的目标
        ph : "/path/to/ta",  // 否则采用 Path, 不存在就创建
        race : "DIR"         // 特指 ph 对应对象的类型
    },
    // 仅对单文件上传有效，没有声明，则不显示文件预览
    // 如果给的是一个 url，则位对象默认预览图
    // 如果 target.id 存在，则用 target.id 预览
    preview : true | "/url/to/target",
    //.......................................
    // 如果是多文件上传，重名文件是否自动替换
    // 默认 false
    replaceable : false | true,
    //.......................................
    // 声明校验函数，或者一个正则表达式验证文件名
    validate : function(file){
        throw "如果错误抛出错误消息"
    } || "^.+[.]png$" || /^.+[.]png$/
    //.......................................
    // 声明某个文件上传后的处理
    // this 为 Upload 的 UI 本身
    // 当 status == "done" 时 re 为 WnObj 的 JSON
    // 当 status == "fail" 时 re 为 AjaxReturn 的 JSOn
    // status 为 "done" 或 "fail"
    done : function(re){..}
    fail : function(re){..}
    complete : function(re, status) {..}
    //.......................................
    // 上传不在工作时调用，批量上传，最后一个文件处理完毕后调用
    // 它和你声明的 complete 不冲突，会在它之后调用
    // this 位 Upload 的 UI 本身 
    finish : function(){..}
    // 多文件上传，当最后一个文件处理完了才会调用
}).render();

*/
define(function(require, exports, module) {
//===================================================================
function gen_url(opt) {
    var ta = opt.target;
    var url = "/o/upload/";
    // 指定的 ID，所以对象必须存在
    if(ta.id){
        url += "id:"+ta.id+"?";
    }
    // 否则可以自动创建
    else {
        url += ta.ph+"?race="+ta.race+"&cie=true&";
    }
    url += "nm={{file.name}}&sz={{file.size}}&mime={{file.type}}";

    if(!opt.replaceable)
        url += "&dupp=${major}(${nb})${suffix}";

    return url;
}
//===================================================================
    var ZUI = require("zui");
    module.exports = ZUI.def("ui.upload", {
        //...............................................................
        dom  : "ui/upload/upload.html",
        css  : "ui/upload/upload.css",
        i18n : "ui/upload/i18n/{{lang}}.js",
        //...............................................................
        init : function(options){
            var UI = this;
            //console.log("jjjjj init uploadd")
            if(options.validate && !_.isFunction(options.validate)){
                var regex;
                if(_.isString(options.validate)){
                    regex = new RegExp(options.validate);
                }else if(_.isRegExp(options.validate)){
                    regex = options.validate;
                }else{
                    throw "invalid params validate";
                }
                options.validate = function(file){
                    if(!regex.test(file.name)){
                        throw UI.msg("upload.invalid", file);
                    }
                };
            }
        },
        //...............................................................
        redraw : function() {
            var UI    = this;
            var opt   = UI.options;
            var title = opt.title;
            var ta    = opt.target;
            // 默认根据目标的 race 判断模式
            if(typeof opt.multi == "undefined"){
                opt.multi = (ta.race == 'DIR');
            }
            // 多文件上传
            if(opt.multi){
                UI.$el.on("dragover",  ".ui-upload-arena", UI.multi.on_dragover);
                UI.$el.on("dragleave", ".ui-upload-arena", UI.multi.on_dragleave);
                UI.$el.on("drop",      ".ui-upload-arena", UI.multi.on_drop);
                if(title === true || title == undefined)
                    title = UI.msg("upload.multi.sky");
                UI.arena.find(".ui-upload-arena").append(UI.ccode("multi.main"));
            }
            // 单文件上传
            else{
                UI.$el.on("dragover",  ".ui-upload-arena", UI.single.on_dragover);
                UI.$el.on("dragleave", ".ui-upload-arena", UI.single.on_dragleave);
                UI.$el.on("drop",      ".ui-upload-arena", UI.single.on_drop);
                if(title === true || title == undefined)
                    title = UI.msg("upload.single.sky");
                UI.arena.find(".ui-upload-arena").append(UI.ccode("single.main"));
                // 如果上传目标是图片
                if(opt.preview || /^image\//.test(ta.mime)){
                    var bgurl = ta.id ? "/o/read/id:"+ta.id : opt.preview;
                    bgurl += "?" + $z.timestamp();
                    UI.arena.find(".ui-upload-single").css({
                        "background-image" : "url("+bgurl+")",
                        "background-size"  : opt.backgroundSize || "cover"
                    });
                }
            }
            // 更新标题 
            if(title){
                title = (_.template(title))(ta);
                UI.$el.find(".ui-upload-sky").html(title);
            }else{
                UI.$el.find(".ui-upload-sky").remove();
            }
            // 更新帮助
            if(opt.tip) {
                UI.arena.find(".ui-upload-tip").html(opt.tip);
            }
        },
        depose : function(){},
        //...............................................................
        events : {
            "dblclick .ui-upload-multi" : function(){
                var jList = this.arena.find(".ui-upload-multi");
                var jTip = jList.find(".ui-upload-multi-tip").show();
                jList.children().not(".ui-upload-multi-tip").remove();
            }
        },
        //...............................................................
        resize : function(){
            var UI = this;
            var jSky    = UI.arena.find(".ui-upload-sky");
            var jMain   = UI.arena.find(".ui-upload-arena");
            var jFooter = UI.arena.find(".ui-upload-footer");
            var H       = UI.arena.height();
            var hSky    = jSky.outerHeight();
            var hFooter = jFooter.outerHeight(); 
            // console.log(H, hSky, hFooter)
            jMain.css("height", H - hSky - hFooter);
        },
        //...............................................................
        single : {
            on_dragover : function(e){
                e.stopPropagation();
                e.preventDefault();
                $(this).attr("drag","over");
            },
            on_dragleave : function(e){
                $(this).removeAttr("drag");
            },
            on_drop : function(e) {
                e.stopPropagation();
                e.preventDefault();
                var UI = ZUI.checkInstance(this);
                var opt = UI.options;
                var jSingle = $(this).removeAttr("drag").find(".ui-upload-single");
                if("yes" == jSingle.attr("ing")) {
                    alert(UI.msg("upload.single.err_ing"));
                    return;
                }
                var UI = ZUI.checkInstance(this);
                var ta = UI.options.target;
                // 只能允许一个文件
                if(e.originalEvent.dataTransfer.files.length != 1) {
                    alert(UI.msg("upload.single.err_multi"));
                    return;
                }
                // 得到这个文件
                var file = e.originalEvent.dataTransfer.files[0];
                if(!UI._do_validate(file))
                    return;
                // 开始上传
                jSingle.attr("ing", "yes");
                $z.uploadFile({
                    url : gen_url(opt),
                    file : file,
                    progress : function(e){  
                        UI.updateProgress.call(UI, jSingle, e.loaded, e.total);
                    },
                    evalReturn : "ajax",
                    done : function(re){
                        $z.invoke(opt, "done", [re], UI);
                    },
                    fail : function(re){
                        alert(re.msg);
                        $z.invoke(opt, "fail", [re], UI);
                    },
                    complete : function(re, status){
                        jSingle.attr("ing", "no")
                            .find(".inner").css("width", "1px");
                        // 修改显示
                        if(opt.preview || /^image\//.test(ta.mime)){
                            UI.arena.find(".ui-upload-single").css({
                                "background-image" : "url(/o/read/id:"+re.id+"?"+$z.timestamp()+")"
                            });
                        }
                        // 调用回调
                        $z.invoke(opt, "complete", [re, status], UI);
                        $z.invoke(opt, "finish", [], UI);
                    }
                });
            }
        },
        //...............................................................
        multi : {
            on_dragover : function(e){
                e.stopPropagation();
                e.preventDefault();
                $(this).attr("drag","over");
            },
            on_dragleave : function(e){
                $(this).removeAttr("drag");
            },
            on_drop : function(e) {
                $(this).removeAttr("drag");
                e.stopPropagation();
                e.preventDefault();
                var UI = ZUI.checkInstance(this);
                if(!UI.multi.addFiles.call(UI, e.originalEvent.dataTransfer.files))
                    return;
                UI.multi.doUpload.call(UI);
            },
            addFiles : function(files) {
                var UI = this;
                var jList = this.arena.find(".ui-upload-multi");
                var jTip = jList.find(".ui-upload-multi-tip").hide();
                jList.children().not(".ui-upload-multi-tip").remove();
                for(var i=0;i<files.length;i++){
                    var file = files[i];
                    if(!UI._do_validate(file))
                        return false;
                    var jItem = this.ccode("list.item");
                    jItem.data("FILE", file).find(".fname").text(file.name);
                    jList.append(jItem);
                }
                this.resize();
                return true;
            },
            doUpload : function(){
                var UI  = this;
                var opt = UI.options;
                var jItem  = this.arena.find(".ui-upload-item")
                                .not(".ui-upload-item-done")
                                .not(".ui-upload-item-fail")
                                .first();
                // 没有更多项目了，返回
                if(jItem.size()<=0) {
                    $z.invoke(opt, "finish", [], UI);
                    return;
                }
                jItem[0].scrollIntoView();
                // 开始上传
                $z.uploadFile({
                    url : gen_url(opt),
                    file : jItem.data("FILE"),
                    progress : function(e){  
                        UI.updateProgress.call(UI, jItem, e.loaded, e.total);
                    },
                    // 上传前修改提示图标的标签
                    beforeSend : function(xhr){
                        jItem.find(".thumbnail .fa").prop("className","fa fa-spinner fa-spin");
                        jItem.find(".remote .fa").addClass("fa-spin");
                    },
                    evalReturn : "ajax",
                    done : function(re){
                        jItem.addClass("ui-upload-item-done");
                        jItem.find(".thumbnail .fa").prop("className", "fa fa-check-circle");
                        jItem.find(".rname").text(re.nm);
                        $z.invoke(opt, "done", [re], UI);
                    },
                    fail : function(re){
                        jItem.addClass("ui-upload-item-fail");
                        jItem.find(".thumbnail .fa").prop("className", "fa fa-warning");
                        jItem.find(".rname").text(re.msg)
                        $z.invoke(opt, "fail", [re], UI);
                    },
                    complete : function(re, status){
                        jItem.find(".remote .fa").removeClass("fa-spin");
                        $z.invoke(opt, "complete", [re, status], UI);
                        UI.multi.doUpload.call(UI);
                    }
                });
            }
        },
        _do_validate : function(file){
            try{
                $z.invoke(this.options, "validate", [file]);
                return true;
            }catch(e){
                alert(e);
                return false;
            }
        },
        //...............................................................
        updateProgress : function(jq, loaded, total){
            var jOuter = jq.find(".outer");
            var jInner = jq.find(".inner");
            var jNb    = jq.find(".nb");
            var W = jOuter.width();
            var per = loaded / total;
            jNb.text(parseInt(per * 1000) / 10 + "%");
            jInner.css("width", W * per);
        }
    });
//===================================================================
});