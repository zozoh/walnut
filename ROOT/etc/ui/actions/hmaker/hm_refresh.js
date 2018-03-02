({
	icon : '<i class="zmdi zmdi-refresh"></i>',
	text : "i18n:refresh",
	type : "button",
	handler : function($ele, a) {
		this.fire("reload:folder");
	}
})