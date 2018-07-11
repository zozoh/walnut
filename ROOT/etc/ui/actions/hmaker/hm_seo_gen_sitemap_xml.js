({
	icon : '<i class="fas fa-sitemap"></i>',
	text : "i18n:hmaker.seo_gen_sitemap_xml",
	type : "button",
	handler : function($ele, a) {
		this.hmaker().doGenSiteMap();
	}
})