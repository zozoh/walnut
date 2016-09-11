({
	icon : '<i class="fa fa-refresh"></i>',
	text : "i18n:refresh",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		
		//console.log(UI.uiName)
		
		if(_.isFunction(UI.refresh)){
			UI.refresh();
		}
		else {
			alert(UI.msg("e.act.noapi_obj") + " :-> refresh");
		}

	}
})