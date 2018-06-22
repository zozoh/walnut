(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/cmd_log'
], function(ZUI, Wn, CmdLogUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-import-3-import"    
    ui-fitparent="yes"
    ui-gasket="log"></div>
*/};
//==============================================
return ZUI.def("app.wn.thi_3_import", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI = this;

        new CmdLogUI({
            parent : UI,
            gasketName : "log",
            done : function(){
                console.log("I am done")
            }
        }).render(function(){
            UI.defer_report("log");
        });

        return ["log"];
    },
    //...............................................................
    getData : function(){
        
    },
    //...............................................................
    setData : function(data) {
        var UI = this;
        console.log(data);
        

        UI.gasket.log.runCommand("output hello -n 100 -ti -interval 10");
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);