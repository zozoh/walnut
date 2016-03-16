({
	icon : '<i class="fa fa-gear"></i>',
	text : "i18n:properties",
	type : "button",
	handler : function($ele, a) {
		var _C = this;
		
		// 获取当前的对象
		var o = $z.invoke(this, "getActived");
		
		if(!o){
			o = $z.invoke(this, "getCurrentObj");
		}
		
		if(!o){
			alert(_C.msg("e.act.noapi.obj") + " :-> getCurrentObj");
			return;
		}
		
		alert($z.toJson(o, null, '    '));
	}
})