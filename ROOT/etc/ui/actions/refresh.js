({
	icon : '<i class="fa fa-refresh"></i>',
	text : "i18n:refresh",
	type : "button",
	handler : function($ele, a) {
		if(this.browser)
			this.browser.refresh();
		else if(_.isFunction(this.refresh))
			this.refresh();
	}
})