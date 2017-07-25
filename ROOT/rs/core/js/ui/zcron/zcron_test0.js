(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/zcron/edt_zcron',
    'ui/pop/pop',
], function(ZUI, Wn, ZCronUI, POP){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes">
<button class="do-pop-zcron">POP ZCron</button>
<div ui-gasket="mymain" style="width:100%; height:100%; padding:20px;">
</div>
</div>
*/};
//===================================================================
return ZUI.def("ui.zcron_test0", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click .do-pop-qz" : function(){
            var UI = this;
            var qz = UI.mymain.getData();
            POP.openUIPanel({
                setup : {
                    uiType : 'ui/zcron/edt_zcron',
                    uiConf : {}
                }
            }, this);
        },
    },
    //...............................................................
    update : function(o){
        var UI = this;
        UI.mymain = new ZCronUI({
            parent : UI,
            gasketName : "mymain"
        }).render(function(){
            this.setData("T[00:00,23:59]{0/30m} * * ?");
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);