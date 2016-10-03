([ {
	title : "i18n:favorites",
	items : [ {
		ph : '~',
		icon : '<i class="zmdi zmdi-home"></i>',
		text : 'i18n:home',
		dynamic : false
	}, {
		type : "objs",
		cmd : "obj ~ -match 'tp:\"^d-\"' -l -json -hide"
	} ]
} ])