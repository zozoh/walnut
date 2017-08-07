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
        "click .do-pop-zcron" : function(){
            var UI = this;
            var qz = UI.mymain.getData();
            POP.zcron(null, null, this);
        },
    },
    //...............................................................
    update : function(o){
        var UI = this;
        UI.mymain = new ZCronUI({
            parent : UI,
            gasketName : "mymain"
        }).render(function(){
            //this.setData("T[03:00,08:59]{0/30m} T[13:00,18:59]{0/30m} * * ?");
            this.setData("T[10:00,12:00]{0/15m} 4L * 3-6");
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);