(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hmc-text">
    I am text
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_com_text", {
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

    }
});
//===================================================================
});
})(window.NutzUtil);