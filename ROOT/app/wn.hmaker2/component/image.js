(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hmc-image hm-del-save"></div>
*/};
//==============================================
return ZUI.def("app.wn.hm_com_image", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        //console.log("C:image redraw")
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // console.log("image", com)

        // 准备更新的样式
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
        // 指定
        if(com.src) {
            //UI.$el.attr("image-src", com.src);
            css["background-image"] = 'url(/o/read/'+com.src+')';    
        }
        // // 清除
        // else {
        //     UI.$el.removeAttr("image-src");
        // }

        // 大小
        UI.arena.css({
            "width"  : com.width,
            "height" : com.height,
        });

        // // 链接
        // // 指定 
        // if(com.href) {
        //     UI.$el.attr("image-href", com.href);
        // }
        // // 清除
        // else {
        //     UI.$el.removeAttr("image-href");
        // }

        // 最后更新显示
        UI.arena.css(css);

    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    setupProp : function(){
        var UI = this;
        var oHome = UI.getHomeObj();
        return {
            uiType : 'ui/form/form',
            uiConf : {
                uiWidth: "all",
                fields : [{
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
                    editAs : "link",
                    uiConf : {
                        body : {
                            setup : {
                                defaultPath : oHome
                            }
                        }
                    }
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
                }]
            }
        }
    },
    //...............................................................
    formatSize : function(prop, com, fromMode) {
        var UI = this;
        
        //console.log("I am img.formatSize:before", prop)

        // 组件默认还是要 100% 的
        $z.setUndefined(com, "width",  "100%");
        $z.setUndefined(com, "height", "100%");
        
        // 模式变了，我要参与一下
        if(fromMode != prop.mode) {
            // 根据属性计算出矩形
            var rect = UI.getBlockRectByProp(prop);

            // 绝对位置，把宽高设置回 posVal
            if("abs" == prop.mode) {
                // 将块属性的宽高，设置回矩形里
                rect.width  = prop.width;
                rect.height = prop.height;

                prop.posVal = UI.transRectToPosVal(rect, prop.posBy);
               
            }
            // 绝对位置，从 posVal 里把宽高 copy 出来
            else {
                // 将矩形的宽高设置到块属性里
                prop.width  = rect.width;
                prop.height = rect.height;
            }
        }
        
        //console.log("I am img.formatSize:after", prop)
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);