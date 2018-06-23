(function($z){
$z.declare([
    'zui',
    'wn/util',
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena th-import-4-done" ui-fitparent="yes">
    <header>
        <i class="zmdi zmdi-check-circle"></i>
    </header>
    <section>{{thing.import.in_done}}</section>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thi_4_done", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
});
//===================================================================
});
})(window.NutzUtil);