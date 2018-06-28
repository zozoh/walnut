(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing/support/th_methods',
    'ui/wizard/wizard',
], function(ZUI, Wn, ThMethods, WizardUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-import th-wizard"
    ui-fitparent="true"
    ui-gasket="wizard"></div>
*/};
//==============================================
return ZUI.def("ui.th_import", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 准备参数
        $z.setUndefined(opt, "thingSetId", null);
        $z.setUndefined(opt, "accept", ".csv, .xls");
        $z.setUndefined(opt, "uniqueKey", null);
        $z.setUndefined(opt, "mapping", null);
        $z.setUndefined(opt, "fixedForm", null);
        $z.setUndefined(opt, "afterCommand", null);

        // 必须有 thingSetId
        if(!opt.thingSetId) {
            alert("opt.thingSetId without defined!");
            return;
        }

        // 准备步骤的配置文件
        var steps = {};
        if(opt.fixedForm) {
            steps["step0"] = {
                text : "i18n:thing.import.step0",
                next : true,
                uiType : "ui/thing/support/import/step0_fixed_form",
                uiConf : opt
            }
        }
        // Step1:选择文件
        steps["step1"] = {
            text : "i18n:thing.import.step1",
            next : false,
            uiType : "ui/thing/support/import/step1_choose_file",
            uiConf : opt
        };
        // Step2:上传进度
        steps["step2"] = {
            text : "i18n:thing.import.step2",
            prev : false,
            next : false,
            uiType : "ui/thing/support/import/step2_uploading",
            uiConf : opt
        };
        // Step3: 分析数据执行命令
        steps["step3"] = {
            text : "i18n:thing.import.step3",
            prev : false,
            next : false,
            uiType : "ui/thing/support/import/step3_import",
            uiConf : opt
        };
        // Step4: 显示上传成功
        steps["step4"] = {
            text : "i18n:thing.import.step4",
            done : function(){
                $z.invoke(opt, "done");
            },
            uiType : "ui/thing/support/import/step4_done",
        },

        /*
        向导收集的对象为:
        {
            theFile   : File   // 本地文件对象,
            oTmpFile  : {..}   // 服务器端的临时文件
            fixedData : {..}   // 固定字段值
        }
        */

        new WizardUI({
            parent : UI,
            gasketName : "wizard",
            headMode : "all",
            startPoint : "step0",
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