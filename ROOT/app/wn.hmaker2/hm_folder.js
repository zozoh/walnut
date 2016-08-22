(function($z){
$z.declare([
    'zui',
    'app/wn.hmaker2/hm__methods',
], function(ZUI, HmMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hm-unknown" ui-fitparent="yes">
    我是文件夹 : <b></b>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_folder", {
    dom : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    update : function(o) {
        this.arena.find("b").text(o.ph);

        this.fire("active:folder", o);
    }
});
//===================================================================
});
})(window.NutzUtil);