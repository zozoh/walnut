([ {
	title : "站点管理",
	items : [ {
		type : "objs",
		cmd : "obj ~ -match \"tp:'hmaker_site'\" -json -l",
		icon : '<i class="fa fa-sitemap"></i>',
		editor : "hmaker2"
	} ]
}, {
	title : "数据管理",
	items : [ {
		type : "objs",
		cmd : "obj ~/.thing -match \"tp:'thing_set'\" -json -l",
		icon : '<i class="fa fa-cubes"></i>',
		editor : "thing_set"
	} ]
} ])