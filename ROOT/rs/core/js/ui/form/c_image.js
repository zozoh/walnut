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
    target : "xxxx"

    // 返回的是图片的路径/ID/还是完整对象
    // - obj  : 完整对象
    // - path : obj.ph
    // - id   : obj.id
    // - idph : "id:" + obj.id
    dataType : "obj|path|id|idph"

    // 检查上传文件的合法性，默认会动态的根据 target 来判断
    validate : {c}F(file, UI):Boolean

    // 回调
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
    'ui/form/support/form_c_methods'
], function(ZUI, Wn, FormCMethods){
//==============================================
var dft_src = "/gu/rs/core/js/ui/form/img_blank.jpg";
var html = `<div class="ui-arena com-image">
    <div class="comi-image"><img></div>
    <div class="comi-select">
        <input type="file">
        <b>{{com.image.select}}</b>
        <span class="comi-process-con">
            <span class="comi-process-inner"></span>
        </span>
        <span class="comi-process-info">0%</span>
        <span class="comi-msg" st="done"><i class="zmdi zmdi-check-circle"></i></span>
        <span class="comi-msg" st="fail"><i class="zmdi zmdi-alert-triangle"></i><em>abddd</em></span>
    </div>
</div>`;
//===================================================================
return ZUI.def("ui.form_com_image", {
    //...............................................................
    dom  : html,
    //...............................................................
    init : function(opt){
        FormCMethods(this);

        function fixJPG(suffix) {
            if (suffix == "jpeg") {
                return "jpg";
            }
            return suffix;
        }

        // 默认根据 target 来检查上传的文件类型
        $z.setUndefined(opt, "validate", function(file, UI){
            var opt = UI.options;
            if(opt.target) {
                var o = Wn.get(opt.target, true);
                // 根据路径判断
                if(!o) {
                    var sn_o = fixJPG(($z.getSuffixName(opt.target) || "").toLowerCase());
                    var sn_f = fixJPG(($z.getSuffixName(file.name) || "").toLowerCase());
                    return sn_o == sn_f;
                }
                // 根据文件对象判断
                return o.mime == file.type;
            }
            // 没有目标，试图根据 data 来判断
            if(UI.__OBJ) {
                return o.mime == file.type;
            }

            // 还木有，那么返回 false
            return false;
        });
    },
    //...............................................................
    events : {
        'click .comi-select b' : function(){
            this.arena.find('.comi-select input[type="file"]').click();
        },
        'change .comi-select input[type="file"]' : function(e) {
            if(e.currentTarget.files.length > 0){
                this.__do_upload(e.currentTarget.files[0]);
                // 清除文件选择框的记忆
                this.arena.find('input[type="file"]').val('');
            }
        },
        'dragover .comi-image' : function(e) {
            e.stopPropagation();
            e.preventDefault();
            $(e.currentTarget).attr("drag-show", "yes");
        },
        'dragleave .comi-image' : function(e) {
            $(e.currentTarget).removeAttr("drag-show");
        },
        'drop .comi-image' : function(e) {
            e.stopPropagation();
            e.preventDefault();

            var UI = this;

            // 只能允许一个文件
            if (e.originalEvent.dataTransfer.files.length != 1) {
                alert(UI.msg("com.image.err_multi"));
                return;
            }

            // 移除属性
            $(e.currentTarget).removeAttr("drag-show");

            // 执行上传
            UI.__do_upload(e.originalEvent.dataTransfer.files[0]);
        },
        "click img" : function(){
            alert(this.options.target)
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

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

        UI.arena.find("img").css({
            width  : opt.width,
            height : opt.height
        });

        UI._set_data();
    },
    //...............................................................
    setTarget : function(ta){
        this.options.target = ta;
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
        if(!$z.invoke(opt, "validate", [f, UI], context)){
            alert(UI.msg("com.image.invalideFile") + " : " + f.name);
            return;
        }

        // 准备上传的 URL
        var ta = opt.target;
        var url = "/o/upload/";
        // 如果已经有了指向的图片，则
        if (UI.__OBJ) {
            url += "id:" + UI.__OBJ.id + "?";
        }
        // 否则可以自动创建
        else if(_.isString(opt.target)){
            url += opt.target + "?race=FILE&cie=true&";
            // FIXME 绝对目录
            if (opt.target.substr(0, 1) == '/') {
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
        url += "nm={{file.name}}&sz={{file.size}}&mime={{file.type}}";

        // 执行上传
        $z.uploadFile({
            url: url,
            file: f,
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
                // 标记错误信息
                UI.arena.attr({
                    "upload-status" : "fail"
                }).find('.comi-msg[st="fail"] em').text(re.msg);
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

        if("idph" == opt.dataType)
            return "id:" + UI.__OBJ.id;

        if("path" == opt.dataType)
            return UI.__OBJ.ph;

        return UI.__OBJ;
    },
    //...............................................................
    _set_data : function(o, notifyChange) {
        var UI = this;

        if(o && _.isString(o)){
            o = Wn.get(o);
        }
        // 记录数据
        UI.__OBJ = o || null;

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
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return UI._get_data();
        });
    },
    //...............................................................
    setData : function(obj, jso){
        var UI = this;
        this.ui_parse_data(obj, function(o){
            UI._set_data(o);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);