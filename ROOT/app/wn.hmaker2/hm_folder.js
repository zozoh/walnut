(function($z){
$z.declare([
    'zui',
], function(ZUI){
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
    update : function(o) {
        this.arena.find("b").text(o.ph);
    }
});
//===================================================================
});
})(window.NutzUtil);