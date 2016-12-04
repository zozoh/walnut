({
	text : "i18n:hmaker.pub_current_page",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		
		// 没有数据接口 
        if(!_.isFunction(UI.getCurrentEditObj)){
        	alert(UI.msg("e.act.noapi_obj"));
        	return;
        }
        
        // 得到当前页
		var oPage = UI.getCurrentEditObj();
		
		// 执行
		UI.hmaker().doPublish(oPage);
	}
})