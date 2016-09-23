({
	icon : '<i class="zmdi zmdi-refresh"></i>',
	text : "i18n:refresh",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		
		//console.log(UI.uiName)
		
		if(_.isFunction(UI.refresh)){
			UI.refresh();
		}
		else if(UI.browser()){
			UI.browser().refresh();
		}
		else if(UI.parent && _.isFunction(UI.parent.refresh)){
			UI.parent.refresh();
		}
		else {
			alert(UI.msg("e.act.noapi_obj") + " :-> refresh");
		}

	}
})