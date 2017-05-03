({
	fields : [ {
		key : "id",
		hide : true,
		title : "i18n:thing.key.id",
		type : "string",
		editAs : "label"
	}, {
		key : "th_nm",
		title : "i18n:thing.key.th_nm",
		type : "string",
		editAs : "input"
	}, {
		key : "th_brief",
		title : "i18n:thing.key.th_brief",
		hide : true,
		type : "string",
		editAs : "text"
	}, {
		key : "icon",
		title : "i18n:thing.key.icon",
		hide : true,
		type : "string",
		editAs : "input"
	}, {
		key : "lbls",
		title : "i18n:thing.key.lbls",
		type : "object",
		editAs : "input"
	}, {
		key : "th_cate",
		title : "i18n:thing.key.th_cate",
		type : "string",
		editAs : "input"
	}, {
		key : "th_ow",
		title : "i18n:thing.key.th_ow",
		hide : true,
		type : "string",
		editAs : "input"
	}, {
		key : "th_live",
		title : "i18n:thing.key.th_live",
		hide : true,
		type : "int",
		dft : 1,
		editAs : "label",
		uiConf : {
			parseData : function(live, UI){
				return UI.text(live == -1 ? "i18n:thing.live_d" : "i18n:thing.live_a");
			}
		}
	}, {
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
})