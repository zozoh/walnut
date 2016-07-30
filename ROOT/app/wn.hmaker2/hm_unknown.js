(function($z){
$z.declare([
    'zui',
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena hm-unknown" ui-fitparent="yes">
    不知道如何打开这个文件 : <b></b>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_unknown", {
    dom : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    update : function(o) {
        this.arena.find("b").text(o.ph);
    }
});
//===================================================================
});
})(window.NutzUtil);