(function($z){
    $z.declare([
        'zui',
        'wn/util',
        'ui/wizard/wizard',
    ], function(ZUI, Wn, WizardUI){
    //==============================================
    var html = function(){/*
    <div class="ui-arena th3-media-download th3-wizard"
        ui-fitparent="true"
        ui-gasket="wizard"></div>
    */};
    //==============================================
    return ZUI.def("ui.th3_media_download", {
        dom  : $z.getFuncBodyAsStr(html.toString()),
        //..............................................
        redraw : function() {
            var UI  = this;
            var opt = UI.options;
       
            // 必须有 thingSetId
            if(!opt.oT || !opt.dirName) {
                alert("opt.oTh or dirName without defined!");
                return;
            }
    
            // 准备步骤的配置文件
            var steps = {};
            // Step1:选择文件
            steps["step1"] = {
                text : "i18n:th3.data.down_pickname",
                next : true,
                uiType : "ui/thing3/wiz/media_download/step1_pickname",
                uiConf : opt
            };
            // Step2:导出进度
            steps["step2"] = {
                text : "i18n:th3.data.down_gen_zip",
                prev : false,
                next : false,
                uiType : "ui/thing3/wiz/media_download/step2_gen_zip",
                uiConf : opt
            };
            // Step3: 导出完成
            steps["step3"] = {
                text : "i18n:th3.data.down_show_down",
                done : function(){
                    $z.invoke(opt, "done");
                },
                uiType : "ui/thing3/wiz/media_download/step3_show_down",
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