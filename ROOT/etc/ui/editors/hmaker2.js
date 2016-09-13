({
	key : "hmaker2",
	text : "hMaker2",
	icon : '<i class="fa fa-flash"></i>',
	outline : false,
	actions : ["@::hmaker/hm_create", 
               "~",
               "::hmaker/pub_site",
               "~",
               "::hmaker/hm_site_conf",
               "~",
               "::zui_debug",
               "::open_console"],
	uiType : "app/wn.hmaker2/hmaker",
	uiConf : {}
})