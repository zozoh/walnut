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
        var cmdText = opt.cmdText;

        /*
        向导收集的对象为:
        {
            theFile  : File   // 本地文件对象,
            oTmpFile : {..}   // 服务器端的临时文件
        }
        */

        new WizardUI({
            parent : UI,
            gasketName : "wizard",
            headMode : "all",
            startPoint : "step3",
            steps : {
                // Step1:选择文件
                "step1" : {
                    text : "选择文件",
                    next : false,
                    uiType : "ui/thing/support/import/step1_choose_file",
                    uiConf : {
                        accept: ".csv, .xls"
                    }
                },
                // Step2:上传进度
                "step2" : {
                    text : "上传",
                    prev : false,
                    next : false,
                    uiType : "ui/thing/support/import/step2_uploading",
                    uiConf : {
                        thingSetId : opt.thingSetId
                    }
                },
                // Step3: 分析数据执行命令
                "step3" : {
                    text : "执行导入",
                    prev : false,
                    next : false,
                    uiType : "ui/thing/support/import/step3_import",
                    uiConf : {
                        
                    }
                },
                // Step4: 显示上传成功
                "step4" : {
                    text : "成功",
                    done : function(){
                        console.log("I am done");
                    },
                    uiType : "ui/thing/support/import/step4_done",
                    uiConf : {
                        
                    }
                },

            }
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