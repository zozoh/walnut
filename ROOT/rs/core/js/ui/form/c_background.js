(function($z){
$z.declare([
    'zui',
    'ui/support/cssp',
    'ui/form/form'
], function(ZUI, CssP, FormUI){
//==============================================
var html = `
<div class="ui-arena com-background com-square-drop">
    <div class="cc-box"><div class="ccb-preview"></div></div>
    <div class="cc-edit">
        <div class="cce-mask"></div>
        <div class="cce-con" ui-gasket="form"></div>
    </div>
</div>
`;
//===================================================================
return ZUI.def("ui.form_com_background", {
    //...............................................................
    dom  : html,
    css  : "theme/ui/form/component.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        var UI = this;

        // 测试代码
        // CssP.__test_background();

        // backgroundImage 的 UI 选择配置
        $z.setUndefined(opt, "imageBy", {
            editAs : "input"
        });

        // ESC 键，将会隐藏自己
        UI.watchKey(27, function(e){
            // 如果打开自己内置的颜色选择器，无视
            if(UI.arena.find('.com-color').attr("show"))
                return;
            // 如果有遮罩，也先不关闭
            if($(document.body).children().last().hasClass("ui-mask"))
                return;
            UI.hideDrop();
        });
    },
    //...............................................................
    events : {
        // 显示颜色提取器
        'click .com-background > .cc-box' : function() {
            this.showDrop();
        },
        // 隐藏颜色提取器 
        'click .com-background > .cc-edit > .cce-mask' : function() {
            this.hideDrop();
        },
    },
    //...............................................................
    __get_background : function(){
        return this.$el.data("@BG") || {};
    },
    //...............................................................
    __update : function(bg) {
        var UI = this;

        // 解析，确保是对象
        var bgo = CssP.parseBackground(bg);

        // 记录
        UI.$el.data("@BG", bgo);

        // 显示
        var bgStyle = CssP.strBackground(bgo);
        UI.arena.find("> .cc-box > .ccb-preview").css("background", bgStyle);
    },
    //...............................................................
    __on_change : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI.parent;
        var v = UI.getData();
        $z.invoke(opt, "on_change", [v], context);
        UI.trigger("change", v);
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            var bgo = UI.__get_background();
            if("string" == opt.dataType)
                return CssP.strBackground(bgo);
            return bgo;
        });
    },
    //...............................................................
    setData : function(val, jso){
        //console.log(val)
        var UI = this;
        this.ui_parse_data(val, function(s){
            UI.__update(s)
        });
    },
    //...............................................................
    showDrop : function() {
        var UI  = this;
        var opt = UI.options;
        
        // 显示
        var jBox  = UI.arena.find(".cc-box");
        var jDrop = UI.arena.find(".cce-con");
        UI.arena.attr("show", "yes");
        jDrop.css({"top":"", "left":""});
        $z.dock(jBox,jDrop,"HA");

        // 下面不要让下拉框超出窗口
        var rect = $z.rect(jDrop);
        var viewport = $z.winsz();
        var rect2 = $z.rect_clip_boundary(rect, viewport);
        jDrop.css($z.rectObj(rect2, "top,left"));

        // 显示 form
        new FormUI({
            parent : UI,
            gasketName : "form",
            mergeData : false,
            on_change : function(){
                UI.__update(this.getData());
                UI.__on_change();
            },
            fields : [{
                key    : "backgroundColor",
                title  : "i18n:com.background.color",
                type   : "string",
                editAs : "color",
            }, _.extend({
                key    : "backgroundImage",
                title  : "i18n:com.background.image",
                type   : "string",
            }, opt.imageBy), {
                key    : "backgroundPosition",
                title  : "i18n:com.background.position",
                type   : "string",
                editAs : "switch",
                uiConf : {
                    items : [{
                        text : 'i18n:com.background.pos_lt',
                        val  : '0% 0%',
                    }, {
                        icon : '<i class="zmdi zmdi-format-align-left"></i>',
                        tip  : 'i18n:com.background.pos_left',
                        val  : 'left',
                    }, {
                        icon : '<i class="zmdi zmdi-border-inner"></i>',
                        tip  : 'i18n:com.background.pos_center',
                        val  : 'center',
                    }, {
                        icon : '<i class="zmdi zmdi-format-align-right"></i>',
                        tip  : 'i18n:com.background.pos_right',
                        val  : 'right',
                    }, {
                        icon : '<i class="zmdi zmdi-format-valign-top"></i>',
                        tip  : 'i18n:com.background.pos_top',
                        val  : 'top',
                    }, {
                        icon : '<i class="zmdi zmdi-format-valign-bottom"></i>',
                        tip  : 'i18n:com.background.pos_bottom',
                        val  : 'bottom',
                    }]
                }
            },{
                key    : "backgroundSize",
                title  : "i18n:com.background.size",
                type   : "string",
                editAs : "switch",
                uiConf : {
                    items : [{
                        text : 'i18n:com.background.size_full',
                        val  : '100% 100%',
                    }, {
                        text : 'i18n:com.background.size_contain',
                        val  : 'contain',
                    }, {
                        text : 'i18n:com.background.size_cover',
                        val  : 'cover',
                    }, {
                        text : 'i18n:com.background.size_auto',
                        val  : 'auto auto',
                    }]
                }
            },{
                key    : "backgroundRepeat",
                title  : "i18n:com.background.repeat",
                type   : "string",
                dft    : "no-repeat",
                editAs : "switch",
                uiConf : {
                    items : [{
                        text : 'i18n:com.background.repeat_yes',
                        val  : 'repeat',
                    }, {
                        text : 'i18n:com.background.repeat_no',
                        val  : 'no-repeat',
                    }]
                }
            },{
                key    : "backgroundOrigin",
                title  : "i18n:com.background.origin",
                type   : "string",
                dft    : "padding-box",
                editAs : "switch",
                uiConf : {
                    items : [{
                        text : 'i18n:com.background.origin_border',
                        val  : 'border-box',
                    }, {
                        text : 'i18n:com.background.origin_padding',
                        val  : 'padding-box',
                    }, {
                        text : 'i18n:com.background.origin_content',
                        val  : 'content-box',
                    }]
                }
            },{
                key    : "backgroundAttachment",
                title  : "i18n:com.background.attachment",
                type   : "string",
                dft    : "scroll",
                editAs : "switch",
                uiConf : {
                    items : [{
                        text : 'i18n:com.background.attachment_scroll',
                        val  : 'scroll',
                    }, {
                        text : 'i18n:com.background.attachment_fixed',
                        val  : 'fixed',
                    }]
                }
            }]
        }).render(function(){
            var bg = UI.__get_background();
            this.setData(bg);
        });
    },
    //...............................................................
    // 隐藏颜色提取器 
    hideDrop : function() {
        var UI = this;
        UI.arena.removeAttr("show");
        UI.arena.find(">.cc-edit>.cce-con").css({"top":"", "left":""});
        // 释放
        if(UI.gasket.form)
            UI.gasket.form.destroy();
    },
    //...............................................................
    resize : function() {
        // 改变大小的时候，一定要隐藏
        this.arena.removeAttr("show");
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);