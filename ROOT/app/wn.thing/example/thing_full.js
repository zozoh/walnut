({
	fields : [ {
		icon : '<i class="fa fa-cube"></i>',
		title : "i18n:thing.fld.general",
		fields : [ {
			key : "id",
			hide : true,
			title : "i18n:thing.key.id",
			type : "string",
			editAs : "label"
		}, {
			key : "thumb",
		}, {
			key : "th_nm",
		}, {
			key : "lbls",
		} ]
	}, {
		icon : '<i class="fa fa-rss" aria-hidden="true"></i>',
		title : "i18n:thing.fld.content",
		fields : [ {
			key   : "__brief_and_content__",
			title : "i18n:thing.key.brief_and_content",
			hide  : true
		}, {
			key : "__media__",
			hide : false,
		}, {
			key : "__attachment__",
			multi : true,
			hide : false,
		} ]
	}, {
		icon : '<i class="fa fa-bar-chart"></i>',
		title : "i18n:thing.fld.numerical",
		fields : [ {
			key : "ct",
			title : "i18n:thing.key.ct",
			hide : true,
			type : "datetime",
			editAs : "label"
		}, {
			key : "lm",
			title : "i18n:thing.key.lm",
			type : "datetime",
			editAs : "label"
		}, {
			key : "th_c_cmt",
			title : "i18n:thing.key.th_c_cmt",
			hide : true,
			type : "int",
			dft : 0,
			editAs : "label"
		}, {
			key : "th_c_view",
			title : "i18n:thing.key.th_c_view",
			hide : true,
			type : "int",
			dft : 0,
			editAs : "label"
		}, {
			key : "th_c_agree",
			title : "i18n:thing.key.th_c_agree",
			hide : true,
			type : "int",
			dft : 0,
			editAs : "label"
		} ]
	} ]
})