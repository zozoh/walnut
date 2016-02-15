(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/quartz/edit_quartz'
], function(ZUI, Wn, QuartzUI){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes">
<div ui-gasket="mymain" style="width:100%; height:100%; padding:20px;">
</div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_test0", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    update : function(o){
        new QuartzUI({
            parent : this,
            gasketName : "mymain"
        }).render(function(){
            this.setData("0 0 0 * * 1-3,5");
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);