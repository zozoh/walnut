(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing/support/th_methods',
    'ui/wizard/wizard',
], function(ZUI, Wn, ThMethods, WizardUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-export th-wizard"
    ui-fitparent="true"
    ui-gasket="wizard"></div>
*/};
//==============================================
return ZUI.def("ui.th_export", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 准备参数
        $z.setUndefined(opt, "thingSetId", null);
        $z.setUndefined(opt, "exportType", "csv");
        $z.setUndefined(opt, "pageRange", false);
        $z.setUndefined(opt, "pageBegin", 1);
        $z.setUndefined(opt, "pageEnd", -1);
        $z.setUndefined(opt, "audoDownload", false);
        $z.setUndefined(opt, "mapping", null);
        $z.setUndefined(opt, "processTmpl", "${P} ${th_nm?-未知-} : ${phone?-未设定-}");

        // 必须有 thingSetId
        if(!opt.thingSetId) {
            alert("opt.thingSetId without defined!");
            return;
        }

        // 准备步骤的配置文件
        var steps = {};
        // Step1:选择文件
        steps["step1"] = {
            text : "i18n:thing.export.step1",
            next : true,
            uiType : "ui/thing/support/export/step1_setup",
            uiConf : opt
        };
        // Step2:导出进度
        steps["step2"] = {
            text : "i18n:thing.export.step2",
            prev : false,
            next : false,
            uiType : "ui/thing/support/export/step2_export",
            uiConf : opt
        };
        // Step3: 导出完成
        steps["step3"] = {
            text : "i18n:thing.export.step3",
            done : function(){
                $z.invoke(opt, "done");
            },
            uiType : "ui/thing/support/export/step3_done",
        },

        /*
        向导收集的对象为:
        {
            setup     : {..}   // 导出的设定
            oTmpFile  : {..}   // 服务器端的临时文件
            exportLog : [..]   // 记录导出的日志输出
        }
        */

        new WizardUI({
            parent : UI,
            gasketName : "wizard",
            headMode : "all",
            //startPoint : "step3",
            steps : steps
        }).render(function(){
            UI.defer_report("wizard")
        });

        return ["wizard"];
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);