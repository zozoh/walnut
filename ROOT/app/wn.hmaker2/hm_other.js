(function($z){
$z.declare([
    'zui',
    'app/wn.hmaker2/hm__methods',
], function(ZUI, HmMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hm-other" ui-fitparent="yes">
    不知道如何打开这个文件 : <b></b>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_other", {
    dom : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    update : function(o) {
        this.arena.find("b").text(o.ph);

        this.fire("active:other", o);
    }
});
//===================================================================
});
})(window.NutzUtil);