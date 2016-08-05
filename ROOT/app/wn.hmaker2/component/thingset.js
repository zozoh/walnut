(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hmc-thingset">
    I am thingset
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_com_thingset", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        console.log("I am com.thingset redraw")
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    setupProp : function(){

    },
    //...............................................................
    getProp : function() {

    },
    //...............................................................
    paint : function(com) {

    }
});
//===================================================================
});
})(window.NutzUtil);