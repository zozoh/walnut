(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/quartz/edit_quartz',
    'ui/pop/pop_quartz',
    'ui/pop/pop_browser'
], function(ZUI, Wn, QuartzUI, PopQuartz, PopBrowser){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes">
<button class="do-pop-qz">POP Quartz</button> <button class="do-pop-br">POP Browser</button>
<div ui-gasket="mymain" style="width:100%; height:100%; padding:20px;">
</div>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_test0", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .do-pop-qz" : function(){
            var UI = this;
            var qz = UI.mymain.getData();
            new PopQuartz({
                data : qz,
                on_ok : function(qz){
                    UI.mymain.setData(qz);
                }
            }).render();
        },
        "click .do-pop-br" : function(){
            var UI = this;
            var qz = UI.mymain.getData();
            new PopBrowser({
                data : qz,
                on_ok : function(o){
                    console.log(o)
                }
            }).render();
        }
    },
    //...............................................................
    update : function(o){
        var UI = this;
        UI.mymain = new QuartzUI({
            parent : UI,
            gasketName : "mymain"
        }).render(function(){
            this.setData("0 0 0 1-5 * ?");
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);