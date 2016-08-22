({
	text : "i18n:hmaker.pub_site",
	tip  : "i18n:hmaker.pub_site_tip",
	type : "button",
	handler : function($ele, a) {
		var cmdText = "hmaker publish id:" + this.getSiteHomeId();
		Wn.logpanel(cmdText);
	}
})