({
	key      : 'showhide',
	text     : "i18n:obrowser.showhide",
	type     : "boolean",
	icon_on  : '<i class="fa fa-check"></i>',
    icon_off : '<i class="fa fa"></i>',
	init : function(mi){
		mi.on = this.browser.getHiddenObjVisibility() == "show";
	}
})