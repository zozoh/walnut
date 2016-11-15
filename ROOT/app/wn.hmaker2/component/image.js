(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `
<div class="ui-arena hmc-image hm-del-save">
    <div class="hmc-image-pic"></div>
    <div class="hmc-image-txt"></div>
    <div class="hmc-image-link-tip"><i class="zmdi zmdi-link"></i></div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_image", {
    dom  : html,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        //console.log("image", com)
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 指定链接
        if(com.href) {
            UI.arena.attr("image-href", "yes");
        }
        // 清除
        else {
            UI.arena.removeAttr("image-href");
        }
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 准备更新图片的样式
        var css = {
            "background-image"  : 'url(/a/load/wn.hmaker2/img_blank.jpg)',
            "background-repeat" : "no-repeat",
        };

        // 图片拉伸方式
        switch(com.scale) {
            case "contain":
            case "cover"  :
                css["background-size"] = com.scale;
                css["background-position"] = "center center";
                break;
            case "tile" :
                css["background-size"] = "";
                css["background-repeat"] = "repeat";
                break;
            default:
                css["background-size"] = "100% 100%";
        }

        // 图片源
        if(com.src) {
            css["background-image"] = 'url(/o/read/'+com.src+')';    
        }

        // 大小
        css.width  = com.width  || "";
        css.height = com.height || "";

        // 更新图片显示
        UI.arena.children(".hmc-image-pic").css(css);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 准备更新文本样式
        var txt = com.text || {};
        if(txt.content) {
            //console.log(txt)
            // 计算文本的 CSS
            css = {};

            // 文本位置极其宽高，根据顶底左右不同，选择 txt.size 表示的是宽还是高
            switch(txt.pos) {
                // N: North 顶
                case "N":
                    css.top    = 0;
                    css.left   = 0;
                    css.right  = 0;
                    css.bottom = "";
                    css.width  = "";
                    css.height = txt.size || "";
                    break;
                // E: Weat 左
                case "W":
                    css.top    = 0;
                    css.left   = 0;
                    css.right  = "";
                    css.bottom = 0;
                    css.width  = txt.size || "";
                    css.height = "";
                    break;
                // E: East 右
                case "E":
                    css.top    = 0;
                    css.left   = "";
                    css.right  = 0;
                    css.bottom = 0;
                    css.width  = txt.size || "";
                    css.height = "";
                    break;
                // 默认 S: South 底
                default:
                    css.top    = "";
                    css.left   = 0;
                    css.right  = 0;
                    css.bottom = 0;
                    css.width  = "";
                    css.height = txt.size || "";
            }

            // 边距等其他属性
            css.padding    = txt.padding    || "";
            css.color      = txt.color      || "";
            css.background = txt.background || "";
            css.textAlign  = txt.textAlign  || "";
            css.lineHeight = txt.lineHeight  || "";
            css.letterSpacing  = txt.letterSpacing  || "";
            css.fontSize   = txt.fontSize   || "";
            css.textShadow = txt.textShadow || "";

            // 设置文本显示
            UI.arena.children(".hmc-image-txt")
                .text(txt.content)
                .css(css);

            // 标记显示文本
            UI.arena.attr("show-txt", "yes");
        }
        // 标记不显示文本
        else {
            UI.arena.removeAttr("show-txt");
        }

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
            key    : "scale",
            title  : "i18n:hmaker.prop.scale",
            type   : "string",
            editAs : "link",
            editAs : "switch", 
            uiConf : {
                items : [{
                    text : 'i18n:hmaker.prop.scale_full',
                    val  : 'full',
                }, {
                    text : 'i18n:hmaker.prop.scale_contain',
                    val  : 'contain',
                }, {
                    text : 'i18n:hmaker.prop.scale_cover',
                    val  : 'cover',
                }, {
                    text : 'i18n:hmaker.prop.scale_tile',
                    val  : 'tile',
                }]
            }
        }, {
            key    : "width",
            title  : "i18n:hmaker.prop.width",
            type   : "string",
            uiWidth : 80, 
            editAs : "input",
        }, {
            key    : "height",
            title  : "i18n:hmaker.prop.height",
            type   : "string",
            uiWidth : 80, 
            editAs : "input",
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
    getBlockPropFields : function() {
        return ["margin","padding","border","borderRadius",
                "boxShadow","overflow"];
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        var UI = this;
        return {
            uiType : 'ui/form/form',
            uiConf : {
                uiWidth: "all",
                fields : [{
                    title  : "i18n:hmaker.com.image.tt_image",
                    fields : UI.IMG_FIELDS()
                }, {
                    title  : "i18n:hmaker.com.image.tt_text",
                    fields : UI.TXT_FIELDS()
                }]
            }
        };
    },
    //...............................................................
    getDefaultData : function(){
        return {
            text : {
                content : "hahah"
            }
        };
    },
    //...............................................................
    getDefaultBlock : function(){
        return {
            mode : "abs",
            posBy   : "top,left,width,height",
            posVal  : "10px,10px,200px,200px",
            width   : "",
            height  : "",
            padding : "",
            border : "" ,   // "1px solid #000",
            borderRadius : "",
            overflow : "",
            blockBackground : "",
        };
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);