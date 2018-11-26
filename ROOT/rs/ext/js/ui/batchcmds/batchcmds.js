(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/wizard/wizard',
], function(ZUI, Wn, WizardUI){
//==============================================
var html = function(){/*
<div class="ui-arena batchcmds" ui-fitparent="yes" ui-gasket="main">
    I am batch commmand
</div>
*/};
//==============================================
return ZUI.def("ui.ext.batchcmds", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "uix/batchcmds/theme/batchcmds-{{theme}}.css",
    i18n : "uix/batchcmds/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
        $z.setUndefined(opt, "nameKey", "th_nm");
    },
    //...............................................................
    update : function(objs) {
    	var UI = this;
        var opt = UI.options;

    	// 确保是数组
    	if(!objs) {
    		objs = [];
    	}
    	else if(!_.isArray(objs)){
    		objs = [objs]
    	}

    	// 注销子控件
    	if(UI.gasket.main)
            UI.gasket.main.destroy();
            
    	// 确保内容区为空
        UI.arena.empty();
        
        // 检查配置参数
        if(!opt.targetHome) {
            alert("!opt.targetHome");
            return;
        }

        // 检查配置参数
        if(!opt.targetHome) {
            alert("!opt.targetHome");
            return;
        }
        if(!opt.targetBy) {
            alert("!opt.targetBy");
            return;
        }
        if(!opt.listFieldsBy) {
            alert("!opt.listFieldsBy");
            return;
        }
        if(!opt.cmdBy) {
            alert("!opt.cmdBy");
            return;
        }
        

    	//--------------------------------------- 
        /*
        准备收集的数据格式为:
        {
			targets : [{WnObj}..],   // 一组目标对象
			params  : {..},          // 参数对象
        }
        */
        //--------------------------------------- 
        // 准备步骤的配置文件
        var steps = {};
        steps["step1"] = {
            text : "选择目标",
            next : true,
            uiType : "uix/batchcmds/support/step1_target",
            uiConf : opt
        };
        steps["step2"] = {
            text : "设置参数",
            prev : true,
            next : true,
            uiType : "uix/batchcmds/support/step2_params",
            uiConf : opt
        };
        steps["step3"] = {
            text : "批量执行",
            prev : true,
            next : true,
            uiType : "uix/batchcmds/support/step3_run",
            uiConf : opt
        };
        steps["step4"] = {
            text : "完成",
            done : {
            	icon : '<i class="fas fa-sign-out-alt"></i>',
            	text : "批量执行完成",
            	action : true,
            },
            uiType : "uix/batchcmds/support/step4_done",
            uiConf : opt
        };
        //--------------------------------------- 
        new WizardUI({
            parent : UI,
            gasketName : "main",
            headMode : "all",
            // startPoint : "step2",
            steps : steps,
            on_done : function(){
            	UI.parent.close();
            }
        }).render(function(){
            this.setData({
	    		targets : objs
            });
            // this.setData({
            //     targets : objs,
            //     lang : "zh-cn",
            //     params : {
            //         code: "3456", 
            //         min: "10", 
            //         app: "我们的服务"
            //     },
            //     tmplName : "login",
            //     exampleTarget : objs[0],
            //     exampleContent : "【零站服务】您的验证码是3456。有效期为10分钟，感谢您使用我们的服务"
            // });
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);