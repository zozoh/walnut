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

        // 准备上下文
        var c = {
            f       : data.oTmpFile,
            tsId    : opt.thingSetId
        };

        var str = 'sheet id:{{f.id}} -tpo json';
        if(opt.mapping)
            str += ' -mapping \'' + opt.mapping + '\'';
        str += ' | thing {{tsId}} create -fields -process "${P} ${th_nm?-未知-} : ${phone?-未设定-}"';
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
        UI.gasket.log.runCommand(cmdText);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);