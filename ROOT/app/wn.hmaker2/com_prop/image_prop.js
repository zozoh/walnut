(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
    'ui/mask/mask'
], function(ZUI, Wn, HmMethods, FormUI, MaskUI){
//==============================================
var html = `
<div class="ui-arena hmc-image-prop" ui-fitparent="yes" ui-gasket="form">
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_image_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        HmMethods(this);
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

         // 通用样式设定
        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "all",
            on_update : function(com) {
                UI.uiCom.saveData("panel", com);
            },
            autoLineHeight : true,
            fields : [{
                title  : "i18n:hmaker.com.image.tt_image",
                fields : UI.IMG_FIELDS()
            }, {
                title  : "i18n:hmaker.com.image.tt_text",
                fields : UI.TXT_FIELDS()
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
    IMG_FIELDS : function(){
        var oHome = this.getHomeObj();
        return [{
            key    : "src",
            title  : "i18n:hmaker.prop.img_src",
            type   : "string",
            dft    : null,
            uiType : "ui/picker/opicker",
            uiConf : {
                base : oHome,
                setup : {
                    lastObjId : "hmaker_pick_media",
                    filter    : function(o) {
                        if('DIR' == o.race)
                            return true;
                        return /^image/.test(o.mime);
                    }
                },
                parseData : function(str){
                    var m = /id:(\w+)/.exec(str);
                    return m ? Wn.getById(m[1]) : null;
                },
                formatData : function(o){
                    return o ? "id:"+o.id : null;
                }
            }
        }, {
            key    : "href",
            title  : "i18n:hmaker.prop.href",
            type   : "string",
            uiWidth : "all",
            // editAs : "link",
            // uiConf : {
            //     body : {
            //         setup : {
            //             defaultPath : oHome
            //         }
            //     }
            // }
        }, {
            key    : "objectFit",
            title  : "i18n:hmaker.prop.objectFit",
            type   : "string",
            editAs : "link",
            editAs : "switch", 
            uiConf : {
                items : [{
                    text : 'i18n:hmaker.prop.objectFit_fill',
                    val  : 'fill',
                }, {
                    text : 'i18n:hmaker.prop.objectFit_contain',
                    val  : 'contain',
                }, {
                    text : 'i18n:hmaker.prop.objectFit_cover',
                    val  : 'cover',
                }]
            }
        }];
    },
    //...............................................................
    TXT_FIELDS : function(){
        return [{
            key    : "text.content",
            title  : "i18n:hmaker.com.image.text",
            type   : "string",
            dft    : null,
            emptyAsNull : true,
            editAs : "text",
        }, {
            key    : "text.pos",
            title  : "i18n:hmaker.com.image.text_pos",
            type   : "string",
            editAs : "switch",
            uiConf : {
                items : [{
                    text  : "i18n:hmaker.com.image.text_pos_N",
                    value : "N"
                }, {
                    text  : "i18n:hmaker.com.image.text_pos_S",
                    value : "S"
                }, {
                    text  : "i18n:hmaker.com.image.text_pos_W",
                    value : "W"
                }, {
                    text  : "i18n:hmaker.com.image.text_pos_E",
                    value : "E"
                }]
            }
        }, {
            key   : "text.textAlign",
            title : 'i18n:hmaker.com.text.textAlign',
            type  : "string",
            editAs : "switch",
            uiConf : {
                items : [{
                    icon : '<i class="fa fa-align-left">',
                    val  : 'left',
                }, {
                    icon : '<i class="fa fa-align-center">',
                    val  : 'center',
                }, {
                    icon : '<i class="fa fa-align-right">',
                    val  : 'right',
                }]
            }
        }, {
            key    : "text.size",
            title  : "i18n:hmaker.com.image.text_size",
            type   : "string",
            uiWidth : 80, 
            editAs : "input",
        }, {
            key    : "text.padding",
            title  : "i18n:hmaker.com.image.text_padding",
            type   : "string",
            uiWidth : 80, 
            editAs : "input",
        }, {
            key    : "text.color",
            title  : "i18n:hmaker.com.image.text_color",
            type   : "string",
            editAs : "color",
        }, {
            key    : "text.background",
            title  : "i18n:hmaker.com.image.text_background",
            type   : "string",
            nullAsUndefined : true,
            editAs : "background",
            uiConf : this.getBackgroundImageEditConf()
        }, {
            key   : "text.lineHeight",
            title : 'i18n:hmaker.com.text.lineHeight',
            type  : "string",
            editAs : "input",
        }, {
            key   : "text.letterSpacing",
            title : 'i18n:hmaker.com.text.letterSpacing',
            type  : "string",
            editAs : "input",
        }, {
            key   : "text.fontSize",
            title : 'i18n:hmaker.com.text.fontSize',
            type  : "string",
            editAs : "input",
        }, {
            key   : "text.textShadow",
            title : 'i18n:hmaker.com.text.textShadow',
            type  : "string",
            editAs : "input",
        }];
    },
    //...............................................................
    update : function(com) {
        this.gasket.form.setData(com);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);