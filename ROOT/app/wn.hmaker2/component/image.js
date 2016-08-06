(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hmc-image"></div>
*/};
//==============================================
return ZUI.def("app.wn.hm_com_image", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        'click .hmc-text' : function(){
            console.log("Hi you click me!")
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        console.log("C:image redraw")
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 保存属性
        UI.setPropToDom(com);

        // 准备更新的样式
        var css = {
            "background-image"  : 'url(/a/load/wn.hmaker2/img_blank.jpg)',
            "background-color"  : "#000",
            "background-repeat" : "norepeat",
            "background-size"   : "100% 100%",
        };

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
            "width"  : "100%",
            "height" : "100%",
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
    getProp : function() {
        return this.getPropFromDom();
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
                        base : oHome
                    }
                }]
            }
        }
    }
});
//===================================================================
});
})(window.NutzUtil);