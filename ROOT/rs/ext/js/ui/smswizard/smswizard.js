(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/wizard/wizard',
], function(ZUI, Wn, WizardUI){
//==============================================
var html = function(){/*
<div class="ui-arena smswizard" ui-fitparent="yes" ui-gasket="main">
</div>
*/};
//==============================================
return ZUI.def("ui.ext.smswizard", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "uix/smswizard/theme/smswizard-{{theme}}.css",
    i18n : "uix/smswizard/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt) {
    	$z.setUndefined(opt, 'nameKey', 'th_nm');
    	$z.setUndefined(opt, 'phoneKey', 'phone');
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

    	// 必须得有值啊
    	if(objs.length == 0) {
    		$('<div class="notarget">').text(UI.msg('smswizard.notarget'))
    			.appendTo(UI.arena);
    		return;
    	}

    	//--------------------------------------- 
        /*
        准备收集的数据格式为:
        {
			lang : "zh-cn",        // 语言
			tmplName : "signup",   // 模板名称
			params   : {..},       // 模板占位符
			targets  : [..],       // 发送目标对象列表
        }
        */
        //--------------------------------------- 
        // 准备步骤的配置文件
        var steps = {};
        // Step1:选择消息模板
        steps["step1"] = {
            text : "i18n:smswizard.picktmpl",
            next : true,
            uiType : "uix/smswizard/support/step1_picktmpl",
            uiConf : opt
        };
        // Step1:选择消息模板
        steps["step2"] = {
            text : "i18n:smswizard.confirmmsg",
            next : {
            	icon   : '<i class="zmdi zmdi-mail-send"></i>',
            	text   : "i18n:smswizard.confirmmsg_ok",
            	action : true,
            },
            uiType : "uix/smswizard/support/step2_confirmmsg",
            uiConf : opt
        };
        // Step1:选择消息模板
        steps["step3"] = {
            text : "i18n:smswizard.dosend",
            done : {
            	icon : '<i class="fas fa-sign-out-alt"></i>',
            	text : "i18n:smswizard.done",
            	action : true,
            },
            uiType : "uix/smswizard/support/step3_dosend",
            uiConf : opt
        };
        //--------------------------------------- 
        new WizardUI({
            parent : UI,
            gasketName : "main",
            headMode : "all",
            //startPoint : "step3",
            steps : steps,
            on_done : function(){
            	UI.parent.close();
            }
        }).render(function(){
            this.setData({
	    		targets : objs
	    	});
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);