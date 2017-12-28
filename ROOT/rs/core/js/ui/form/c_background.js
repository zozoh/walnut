(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
    'ui/support/cssp',
    'ui/support/edit_color',
    'ui/form/form'
], function(ZUI, FormMethods, CssP, EditColorUI, FormUI){
//==============================================
var html = `
<div class="ui-arena com-background com-square-drop">
    <div class="cc-box"><div class="ccb-preview"></div></div>
    <div class="cc-opt">
        <a action="bgimg">{{com.background.image}}</a>
        <a action="clean"
            data-balloon="{{com.background.clean}}"
            data-balloon-pos="up"><i class="zmdi zmdi-close"></i></a>
    </div>
    <div class="cc-edit">
        <div class="cce-mask"></div>
        <div class="cce-con" ui-gasket="edit"></div>
    </div>
</div>
`;
//===================================================================
return ZUI.def("ui.form_com_background", {
    //...............................................................
    dom  : html,
    css  : "ui/form/theme/component-{{theme}}.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        var UI = FormMethods(this);

        // 测试代码
        // CssP.__test_background();

        // backgroundImage 的 UI 选择配置
        $z.setUndefined(opt, "imageBy", {
            editAs : "input"
        });

        // ESC 键，将会隐藏自己
        UI.watchKey(27, function(e){
            // 如果有遮罩，也先不关闭
            if($(document.body).children().last().hasClass("ui-mask"))
                return;
            // 执行隐藏
            UI.hideDrop();
        });

        // 背景图编辑的表单控件
        // {
        //     key    : "backgroundColor",
        //     title  : "i18n:com.background.color",
        //     type   : "string",
        //     editAs : "color",
        // }
        UI.__bgimg_fields = [_.extend({
            key    : "backgroundImage",
            title  : "i18n:com.background.image",
            type   : "string",
            dft    : "",
        }, opt.imageBy), {
            key    : "backgroundPositionX",
            title  : "i18n:com.background.positionX",
            type   : "string",
            uiWidth : 120,
            editAs : "input",
            uiConf : {
                assist : {
                    icon : '<i class="zmdi zmdi-more"></i>',
                    uiType : 'ui/form/c_list',
                    uiConf : {
                        escapeHtml : false,
                        items : ["left", "center", "right"],
                        text  : function(s) {
                            return '<span>' + s + "</span><em>"
                                    + UI.msg("com.background.pos_" + s)
                                    + '</em>';
                        },
                        value : function(s) {return s;}
                    }
                }
            } 
        },{
            key    : "backgroundPositionY",
            title  : "i18n:com.background.positionY",
            type   : "string",
            uiWidth : 120,
            editAs : "input",
            uiConf : {
                assist : {
                    icon : '<i class="zmdi zmdi-more"></i>',
                    uiType : 'ui/form/c_list',
                    uiConf : {
                        escapeHtml : false,
                        items : ["top", "center", "bottom"],
                        text  : function(s) {
                            return '<span>' + s + "</span><em>"
                                    + UI.msg("com.background.pos_" + s)
                                    + '</em>';
                        },
                        value : function(s) {return s;}
                    }
                }
            } 
        },{
            key    : "backgroundSize",
            title  : "i18n:com.background.size",
            type   : "string",
            editAs : "input",
            uiConf : {
                assist : {
                    icon : '<i class="zmdi zmdi-more"></i>',
                    uiType : 'ui/form/c_list',
                    uiConf : {
                        escapeHtml : false,
                        items : ["cover", "contain", "auto", "100% 100%"],
                        text  : function(s) {
                            return '<span>' + s + "</span><em>"
                                    + UI.msg("com.background.size_" + s)
                                    + '</em>';
                        },
                        value : function(s) {return s;}
                    }
                }
            } 
        },{
            key    : "backgroundRepeat",
            title  : "i18n:com.background.repeat",
            type   : "string",
            dft    : null,
            editAs : "switch",
            uiConf : {
                singleKeepOne : false,
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
            dft    : null,
            editAs : "switch",
            uiConf : {
                singleKeepOne : false,
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
            dft    : null,
            editAs : "switch",
            uiConf : {
                singleKeepOne : false,
                items : [{
                    text : 'i18n:com.background.attachment_scroll',
                    val  : 'scroll',
                }, {
                    text : 'i18n:com.background.attachment_fixed',
                    val  : 'fixed',
                }]
            }
        }];
    },
    //...............................................................
    events : {
        // 显示颜色提取器
        'click .com-background > .cc-box' : function() {
            var UI = this;
            UI.showDrop("color", function(afterShow){
                new EditColorUI(_.extend({}, UI.options.color||{}, {
                    parent : UI,
                    gasketName : "edit",
                    parseData  : null,
                    formatData : null,
                    dataType   : "string",
                    on_change  : function(v){
                        UI.__update({
                            backgroundColor : v || ""
                        }, true);
                        UI.__on_change();
                    }
                })).render(function(){
                    var bg = UI.__get_background();
                    this.setData(bg.backgroundColor);
                    afterShow();
                });
            });
        },
        // 显示背景图片编辑
        'click .com-background > .cc-opt a[action="bgimg"]' : function() {
            var UI = this;
            UI.showDrop("bgimg", function(afterShow){
                // 显示 form
                new FormUI({
                    parent : UI,
                    gasketName : "edit",
                    mergeData : true,
                    displayMode : "compact",
                    fitparent : false,
                    on_change : function(){
                        UI.__update(this.getData(), true);
                        UI.__on_change();
                    },
                    uiWidth : "all",
                    fields  : UI.__bgimg_fields
                }).render(function(){
                    var bg = UI.__get_background();
                    this.setData(bg);
                    afterShow();
                });
            });
        },
        // 清除背景图
        'click .com-background > .cc-opt a[action="clean"]' : function() {
            this.__update(null);
            this.__on_change();
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
    __update : function(bg, mergeData) {
        var UI = this;

        // 解析，确保是对象
        var bgo = CssP.parseBackground(bg);
        //console.log("A:", bgo);

        // 与老的属性融合
        if(mergeData) {
            bgo = _.extend({}, UI.__get_background(), bgo);
        }
        //console.log("B:", bgo);

        // 记录
        UI.$el.data("@BG", bgo);

        // 显示
        var bgStyle = CssP.strBackground(bgo);
        UI.arena.find("> .cc-box > .ccb-preview").css("background", bgStyle);
    },
    //...............................................................
    _get_data : function(){
        var UI = this;
        var opt = UI.options;
        var bgo = UI.__get_background();
        //console.log(opt)
        if("string" == opt.dataType)
            return CssP.strBackground(bgo);
        return bgo;
    },
    //...............................................................
    _set_data : function(val, jso){
        this.__update(val)
     },
    //...............................................................
    showDrop : function(dropMode, callback) {
        var UI  = this;
        var opt = UI.options;
        
        // 显示
        var jBox  = UI.arena.find(".cc-box");
        var jDrop = UI.arena.find(".cce-con");
        UI.arena.attr("show", "yes");
        jDrop.attr("for", dropMode).css({"top":"", "left":""});

        // 调用回调
        callback(function(){
            UI.adjustDrop();
        });
    },
    //...............................................................
    adjustDrop : function(){
        var UI  = this;
        var jBox  = UI.arena.find(".cc-box");
        var jDrop = UI.arena.find(".cce-con");
        
        // 停靠
        $z.dock(jBox,jDrop,"H");

        // 下面不要让下拉框超出窗口
        var rect = $D.rect.gen(jDrop);
        var viewport = $z.winsz();
        var rect2 = $D.rect.boundaryIn(rect, viewport);
        jDrop.css($z.pick(rect2, "top,left"));
    },
    //...............................................................
    // 隐藏颜色提取器 
    hideDrop : function() {
        var UI = this;
        UI.arena.removeAttr("show");
        UI.arena.find(">.cc-edit>.cce-con").css({"top":"", "left":""});
        // 释放
        if(UI.gasket.edit)
            UI.gasket.edit.destroy();
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