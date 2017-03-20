({
	icon : '<i class="zmdi zmdi-settings-square"></i>',
	text : "i18n:properties",
	type : "button",
	handler : function($ele, a) {
		var UI = this.browser();
		console.log("haha")
		
		// 获取当前的对象
		var o = $z.invoke(UI, "getActived");
		
		if(!o){
			o = $z.invoke(UI, "getCurrentObj");
		}
		
		if(!o){
			UI.alert(UI.msg("e.act.noapi_obj") + " :-> getCurrentObj");
			return;
		}
		
		alert($z.toJson(o, null, '    '));
	}
})