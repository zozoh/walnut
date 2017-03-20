([ {
	title : "站点管理",
	items : [ {
		type : "objs",
		cmd : "obj -mine -match \"tp:'hmaker_site', race:'DIR'\" -json -l -sort 'nm:1'",
		defaultIcon : '<i class="fa fa-sitemap"></i>',
		editor : "hmaker2"
	} ]
}, {
	title : "数据管理",
	items : [ {
		type : "objs",
		cmd : "obj -mine -match \"tp:'thing_set'\" -json -l -sort 'nm:1'",
		defaultIcon : '<i class="fa fa-cubes"></i>',
		editor : "thing_set"
	} ]
} ])