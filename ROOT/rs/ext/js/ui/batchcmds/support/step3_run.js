(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/cmd_log'
], function(ZUI, Wn, CmdLogUI){
//==============================================
var html = function(){/*
<div class="ui-arena bc-step3-run" ui-fitparent="yes" ui-gasket="log"></div>
*/};
//==============================================
return ZUI.def("ui.ext.bc_step3_run", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI = this;

        UI.__log_list = [];

        new CmdLogUI({
            parent : UI,
            gasketName : "log",
            welcome : '正在执行中 ...',
            // 偷偷记录一下日志给 done 用
            formatMessage : function(str) {
                UI.__log_list.push(str);
                return str;
            },
            done : function(){
                UI.parent.saveData();
                UI.parent.gotoStep(1);
            }
        }).render(function(){
            UI.defer_report("log");
        });

        return ["log"];
    },
    //...............................................................
    isDataReady : function() {
        return true;
    },
    //...............................................................
    getData : function() {
        //console.log("step3.getData")
        return {
            importLog : this.__log_list
        };
    },
    //...............................................................
    setData : function(data) {
        var UI  = this;
        var opt = UI.options;
        console.log(data);
        
        // 循环拼装命令
        var cmds = opt.thing.UI.invokeExtCommand({
            method : opt.cmdBy,
            args : [data.targets, data.params]
        });
        console.log(cmds.join(";\n"));
        

        // 执行命令
        var cmdText = "echo 发送命令中...;" + cmds.join(";");
        console.log(cmdText)
        UI.gasket.log.runCommand(cmdText);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);