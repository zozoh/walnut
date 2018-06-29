(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/cmd_log'
], function(ZUI, Wn, CmdLogUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-import-2-export"    
    ui-fitparent="yes"
    ui-gasket="log"></div>
*/};
//==============================================
return ZUI.def("app.wn.the_2_export", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI = this;

        UI.__log_list = [];

        new CmdLogUI({
            parent : UI,
            gasketName : "log",
            welcome : 'i18n:thing.export.out_welcome',
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
        // console.log(data);
        // console.log(opt.cmdText)

        // 准备上下文
        var c = {
            f       : data.oTmpFile,
            tsId    : opt.thingSetId
        };

        var str = 'sheet id:{{f.id}} -tpo json';
        if(opt.mapping)
            str += ' -mapping \'' + opt.mapping + '\'';
        str += ' | thing {{tsId}} create -fields';
        if(opt.processTmpl) {
            str += ' -process \'' + opt.processTmpl + '\'';
        }
        if(opt.uniqueKey) {
            str += ' -unique ' + opt.uniqueKey;
        }
        if(data.fixedData && !_.isEmpty(data.fixedData)) {
            str += ' -fixed \'' + $z.toJson(data.fixedData, function(k, v){
                if(!/^__/.test(k))
                    return v;

            }) + '\'';
        }
        if(opt.afterCommand) {
            str += ' -after \'' + opt.afterCommand + '\'';
        }
        
        var cmdText = $z.tmpl(str)(c);
        //UI.gasket.log.runCommand(cmdText);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);