/*
调用方法

{
    width  : undefined     // 「选」图片固定宽度
    height : undefined     // 「选」图片固定高度

    // 上传目标，可以有以下形式
    //  "id:xxx"  - 指定了文件的 ID
    //  "/path/to/file" - 绝对路径
    //  "~/path/to/file" - 指定主目录下的路径
    //  "id:xxxx/path/to/file" - 指定某ID文件夹下的文件
    // 所指向的文件必须存在，如果不存在，会自动创建一个（id:xxx) 形式的除外，因为无法指定
    target : "xxxx",

    // 如果上传目标指定的是一个目录，通常会在这个目录写入本地同名文件
    // 如果你想指定这个文件名，可以通过定制本段
    // 你可以指定一个回调函数，从而直接指定文件名
    // 或者直接给一个字符串指定文件名也是可以的
    fileName : F(f):String

    // 返回的是图片的路径/ID/还是完整对象
    // - obj  : 完整对象
    // - path : obj.ph
    // - id   : obj.id
    // - idph : "id:" + obj.id
    dataType : "obj|path|id|idph"

    // 检查上传文件的合法性，默认会动态的根据 target 来判断
    // 如果这个值是一个正则表达式，或者 `^` 开头的字符串，则对文件名进行正则表达式匹配
    // 如果就是字符串，则表示用半角逗号分隔的文件扩展名
    // 同理，你可以直接写一个数组
    validate : {c}F(file, UI):Boolean

    // 删除回调
    // 本控件的删除操作，仅仅清空自己的数据记录，
    // 如果想在服务器上删除文件等，需要指定删除操作的回调
    // 回调函数是异步的，如果执行完毕，请主动调用 callback
    remove   : {c}F(obj, callback)

    // 上传回调
    done     : {c}F(re)
    fail     : {c}F(re)
    complete : {c}F(re, status)
}

getData 返回标准的 WnObj
setData 接受 WnObj 或者文件路径
*/
(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/support/form_ctrl'
], function(ZUI, Wn, FormCMethods){
//==============================================
var dft_src = "/gu/rs/core/js/ui/form/img_blank.jpg";
var html = `<div class="ui-arena com-image">
    <div class="comi-image">
        <img>
        <input type="file" accept=".jpg, .jpeg">
        <div class="comi-select">
            <b>{{com.image.select}}</b>
            <span class="comi-process-con">
                <span class="comi-process-inner"></span>
            </span>
            <span class="comi-process-info">0%</span>
        </div>
        <div a="remove"><i class="fas fa-trash-alt"></i></div>
    </div>
</div>`;
//===================================================================
return ZUI.def("ui.form_com_image", {
    //...............................................................
    dom  : html,
    css  : "ui/form/theme/component-{{theme}}.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt){
        FormCMethods(this);

        function fixJPG(suffix) {
            if (suffix == "jpeg") {
                return "jpg";
            }
            return suffix;
        }

        // 设置默认的删除函数
        $z.setUndefined(opt, "remove", function(obj, callback){
            callback();
        });

        // 默认根据 target 来检查上传的文件类型
        $z.setUndefined(opt, "validate", function(file, UI){
            var opt = UI.options;
            var ta  = UI.__eval_target();
            if(ta) {
                var o = Wn.get(ta, true);
                // 根据路径判断
                if(!o) {
                    var sn_o = fixJPG(($z.getSuffixName(ta) || "").toLowerCase());
                    var sn_f = fixJPG(($z.getSuffixName(file.name) || "").toLowerCase());
                    return sn_o == sn_f;
                }
                // 根据文件对象判断
                return o.mime == file.type;
            }
            // 没有目标，试图根据 data 来判断
            if(UI.__OBJ) {
                return UI.__OBJ.mime == file.type;
            }

            // 还木有，那么返回 false
            return false;
        });

        console.log("readonly", opt.readonly)

        // 如果过 validate 是一个字符串...
        if(_.isString(opt.validate)) {
            // ^ 开头的就变成一个正则表达式
            if(/^\^/.test(opt.validate)) {
                opt.validate = new RegExp(opt.validate);
            }
            // 否则就变成数组
            else {
                opt.validate = opt.validate.split(',');
            }
        }
    },
    //...............................................................
    events : {
        'click .comi-select b' : function(){
            if(this.options.readonly)
                return;
            this.arena.find('input[type="file"]').click();
        },
        'click div[a="remove"]' : function(){
            if(this.options.readonly)
                return;
            this.__do_remove();
        },
        'change input[type="file"]' : function(e) {
            if(this.options.readonly)
                return;
            if(e.currentTarget.files.length > 0){
                this.__do_upload(e.currentTarget.files[0]);
                // 清除文件选择框的记忆
                this.arena.find('input[type="file"]').val('');
            }
        },
    },
    //...............................................................
    dragAndDrop : true,
    on_drop : function(fs) {
        var UI = this;
        var opt = UI.options;

        // 只读什么都不做
        if(opt.readonly)
            return;

        // 只能允许一个文件
        if (fs.length != 1) {
            UI.alert(UI.msg("com.image.err_multi"));
            return;
        }
        // 执行上传
        UI.__do_upload(fs[0]);
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        //console.log("c_image", opt)

        // UI.$el.find(".comi-image").bind("drop",function(e){
        //     e.stopPropagation();
        //     e.preventDefault();

        //     // 只能允许一个文件
        //     if (e.originalEvent.dataTransfer.files.length > 1) {
        //         alert(UI.msg("com.image.err_multi"));
        //         return;
        //     }

        //     alert("haha")
        // });
        //console.log(opt.width, opt.height)

        // 标识一下只读
        if(opt.readonly) {
            UI.arena.attr('is-readonly', 'yes');
        } else {
            UI.arena.attr('is-editable', 'yes');
        }

        // 限制宽度
        if(!_.isUndefined(opt.width)) {
            UI.arena.find(">.comi-image").css('width', opt.width);
            UI.arena.find(">.comi-image img").css('width', "100%");
        }
        // 限制高度
        if(!_.isUndefined(opt.height)) {
            UI.arena.find(">.comi-image").css('height', opt.height);
            UI.arena.find(">.comi-image img").css('height', "100%");
        }

        UI._set_data();
    },
    //...............................................................
    __do_remove : function() {
        var UI  = this;
        var opt = UI.options;
        var obj = UI.__OBJ;
        var context = opt.context || UI;
        var jB = UI.arena.find('div[a="remove"]');

        if(obj) {
            // 显示操作中
            jB.html('<i class="zmdi zmdi-rotate-right zmdi-hc-spin"></i>');
            // 执行删除
            opt.remove.apply(context, [obj, function(o){
                UI._set_data(o, true);
                jB.html('<i class="fas fa-trash-alt"></i>');
            }]);
        }
    },
    //...............................................................
    setTarget : function(ta){
        this.options.target = ta;
    },
    //...............................................................
    __eval_target : function(){
        var UI = this;
        var ta = UI.options.target;

        // 如果是动态决定的
        if(_.isFunction(ta)) {
            var myFormData = {};
            if(UI.parent) {
                myFormData = $z.invoke(UI.parent, "getData", []) || {};
            }
            return ta.call(UI, myFormData);
        }

        return ta;
    },
    //...............................................................
    __is_validate : function(f) {
        var UI  = this;
        var opt = UI.options;

        // 正则表达式
        if(_.isRegExp(opt.validate)) {
            return opt.validate.test(f.name);
        }
        // 如果是数组，那么久判断一下文件扩展名
        if(_.isArray(opt.validate)) {
            var ftp = $z.getSuffixName(f.name);
            return opt.validate.indexOf(ftp) >= 0;
        }
        // 函数的话，就直接调用了
        if(_.isFunction(opt.validate)){
            var context = opt.context || UI;
            return opt.validate.apply(context, [f, UI]);
        }
        // 其他的情况，是什么鬼，直接返回错误
        return false;
    },
    //...............................................................
    __do_upload : function(f) {
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 必须有文件
        if(!f){
            alert(UI.msg("com.image.noFile"));
            return;
        }

        // 校验
        if(!UI.__is_validate(f)){
            alert(UI.msg("com.image.invalideFile") + " : " + f.name);
            return;
        }

        // 准备上传的 URL
        var ta = UI.__eval_target();

        // 分析 URL
        var url = "/o/upload/";
        // 如果已经有了指向的图片，则
        if (UI.__OBJ) {
            url += "id:" + UI.__OBJ.id + "?";
        }
        // 否则可以自动创建
        else if(_.isString(ta)){
            // 路径如果以 `/` 结尾，表示目录
            var taRace = /\/$/.test(ta) ? 'DIR' : 'FILE';
            url += ta + "?race="+taRace+"&cie=true&";
            // FIXME 绝对目录
            if (ta.substr(0, 1) == '/') {
                url += "aph=true&"
            } else {
                url += "aph=false&"
            }
        }
        // 不知道传到哪里啊，大锅
        else {
            alert(UI.msg("com.image.invalidTarget"));
            return;
        }

        // 准备文件名等信息
        url += "sz={{file.size}}&mime={{file.type}}";
        // 指定了文件名
        if(opt.fileName) {
            // 函数
            if(_.isFunction(opt.fileName)) {
                var fnm = opt.fileName.apply(context, [f]);
                url += '&nm=' + fnm;
            }
            // 字符串
            else {
                url += '&nm=' + opt.fileName + ".{{suffixName}}";
            }
        }
        // 默认
        else {
            url += '&nm={{file.name}}';
        }

        // 先压缩后上传
        if(opt.iosfix || opt.compress){
            $z.compressImageFile(f, function (nf) {
                UI._exec_upload(nf, url);
            }, opt.iosfix == true, opt.compress || 100);
        }
        // 直接执行上传
        else {
            UI._exec_upload(f, url);
        }

    },
    _exec_upload: function (f, url) {
        var UI = this;
        var opt = UI.options;
        var context = opt.context || UI;

        $z.uploadFile({
            url: url,
            file: f,
            suffixName : $z.getSuffixName(f.name),
            progress: function (e) {
                UI.__update_progress(UI, e.loaded, e.total);
            },
            // 标识上传开始
            beforeSend: function (xhr) {
                UI.arena.attr({
                    "ing": "yes",
                    "upload-status" : null,
                });
            },
            evalReturn: "ajax",
            done: function (re) {
                // 更新显示
                UI._set_data(re, true);
                // 标记成功图标
                UI.arena.attr({
                    "upload-status" : "done"
                });
                // 调用回调
                $z.invoke(opt, "done", [re], context);
            },
            fail: function (re) {
                // 调用回调
                $z.invoke(opt, "fail", [re], context);
            },
            complete: function (re, status) {
                // 移除上传标记
                UI.arena.removeAttr("ing");
                // 调用回调
                $z.invoke(opt, "complete", [re, status], context);
            }
        });
    },
    //...............................................................
    __update_progress : function (jq, loaded, total) {
        var UI = this;
        var jOuter = UI.arena.find(".comi-process-con");
        var jInner = jOuter.find(".comi-process-inner");
        var jNb = UI.arena.find(".comi-process-info");
        var W = jOuter.width();
        var per = loaded / total;
        jNb.text(parseInt(per * 1000) / 10 + "%");
        jInner.css("width", W * per);
    },
    //...............................................................
    _get_data : function() {
        var UI  = this;
        var opt = UI.options;

        if(!UI.__OBJ)
            return null;

        if("id" == opt.dataType)
            return UI.__OBJ.id;

        if("nm" == opt.dataType)
            return UI.__OBJ.nm;

        if("idph" == opt.dataType)
            return "id:" + UI.__OBJ.id;

        if("path" == opt.dataType)
            return UI.__OBJ.ph;

        return UI.__OBJ;
    },
    //...............................................................
    _set_data : function(o, notifyChange) {
        var UI  = this;
        var opt = UI.options;

        if(o && _.isString(o)){
            var oph = o;
            // 如果保存的仅仅是名称，那么就需要 target 是一个目录咯
            // 如果不是目录就截取到目录
            if("nm" == opt.dataType) {
                var ta = UI.__eval_target();
                var tpos = ta.lastIndexOf('/');
                if(tpos > 0) {
                    oph = ta.substring(0, tpos) + "/" + o;
                } else {
                    oph = ta + "/" + o;
                }
            }

            // 得到对象
            o = Wn.get(oph);
        }
        // 记录数据
        UI.__OBJ = o || null;

        // console.log("image._set_data", o);
        // 标记状态
        UI.arena.attr('is-empty', UI.__OBJ ? null : "yes");

        // 修改显示（如果是通知方式，则加入时间戳更新缓存)
        UI.arena.find("img").attr({
            "src" : o ? "/o/read/id:" + o.id + "?_t="+$z.timestamp()
                      : dft_src
        });

        // 通知回调
        if(notifyChange)
            UI.__on_change();
    },
    //...............................................................
    // 总是不相等才好
    __equals : function(v1, v2) {
        return false;
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);