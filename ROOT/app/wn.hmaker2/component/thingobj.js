(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hmc-thingobj">
    I am thingobj
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_com_thingobj", {
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

        console.log("I am com.thingobj redraw")
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    setupProp : function(){

    }
});
//===================================================================
});
})(window.NutzUtil);