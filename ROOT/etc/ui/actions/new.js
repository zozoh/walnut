({
	icon : '<i class="zmdi zmdi-flare"></i>',
	text : "i18n:new",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		
		// 得到当前对象
		var o = UI.browser().getCurrentObj();
		
		// 显示新建文件对象面板
		Wn.createPanel(o, function(newObj){
			UI.browser().refresh(function(){
				UI.browser().setActived(newObj.id);
			});
		});
	}
})