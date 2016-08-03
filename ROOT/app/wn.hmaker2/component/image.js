(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com',
    'ui/form/form',
], function(ZUI, Wn, HmComMethods, MenuUI, TreeUI){
//==============================================
var html = function(){/*
<div class="ui-arena hmc-image">
    I am image
</div>
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

        console.log("I am com.text redraw")
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
                on_change : function(key, val) {
                    console.log(this.uiCom.uiName, key, val);
                },
                fields : [{
                    key    : "src",
                    title  : "i18n:hmaker.prop.img_src",
                    type   : "string",
                    uiType : "ui/picker/opicker",
                    uiConf : {
                        base : oHome,
                        setup : {
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