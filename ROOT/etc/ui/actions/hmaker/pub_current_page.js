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
        
        // 得到主目录
        var oHome = UI.getHomeObj();
		
        // 得到当前页
		var oPage = UI.getCurrentEditObj();
		var rph = Wn.getRelativePath(oHome, oPage);
		
		// 执行命令
		var cmdText = "hmaker publish id:" + this.getHomeObjId() + " -src '"+rph+"'";
		console.log(cmdText)
		Wn.logpanel(cmdText);
	}
})