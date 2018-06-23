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
            welcome : 'i18n:thing.import.in_welcome',
            done : function(){
               UI.parent.gotoStep(1);
            }
        }).render(function(){
            UI.defer_report("log");
        });

        return ["log"];
    },
    //...............................................................
    setData : function(data) {
        var UI  = this;
        var opt = UI.options;
        // console.log(data);
        // console.log(opt.cmdText)
        
        var cmd = $z.tmpl(opt.cmdText)({
            f    : data.oTmpFile,
            tsId : opt.thingSetId,
        });

        UI.gasket.log.runCommand(cmd);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);