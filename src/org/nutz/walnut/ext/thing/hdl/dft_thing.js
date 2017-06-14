({
	fields : [ {
		key : "id",
		hide : true,
		title : "i18n:thing.key.id",
		type : "string",
		editAs : "label"
	}, {
		key : "thumb"
	}, {
		key : "th_nm",
		title : "i18n:thing.key.th_nm",
		type : "string",
		editAs : "input"
	}, {
		key : "lbls",
		title : "i18n:thing.key.lbls",
		type : "object",
		editAs : "input"
	}, {
		key : "__brief_and_content__"
	}, {
		key : "__media__",
		multi : false,
		hide : false
	}, {
		key : "__attachment__",
		multi : true,
		hide : false
	}, {
		key : "th_enabled",
		title : "生效",
		type : "boolean",
		editAs : "toggle",
		hide : false,
	}, {
		key : "pubat",
		title : "发布日期",
		multi : false,
		editAs : "datepicker",
		hide : false,
		type : "datetime"
	}, {
		key : "lm",
		title : "i18n:thing.key.lm",
		type : "datetime",
		editAs : "label",
		multi : false,
		hide : true
	}, {
		key : "ct",
		title : "i18n:thing.key.ct",
		hide : true,
		type : "datetime",
		editAs : "label"
	} ]
})