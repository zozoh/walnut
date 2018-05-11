({
	icon : '<i class="fa fa-save"></i>',
	text : "i18n:save",
	type : "button",
	shortcut : [83, "command"],
	handler : function($ele, a) {
		var UI = this;
		
		// 标记图标
		var jIcon = $ele.find("i.fa");
		jIcon.removeClass("fa-save")
			 .addClass("fa-spinner fa-pulse");
		
		// 获取当前的对象
		var o = $z.invoke(this, "getCurrentEditObj");
		// 未定义，那么就表示 context 不提供这个方法
		if(_.isUndefined(o)){
			UI.alert(UI.msg("e.act.noapi_obj") + " :-> getCurrentEditObj");
			return;
		}
		
		// 获取当前的文本内容
		var content = $z.invoke(this, "getCurrentTextContent");
		// 未定义，那么就表示 context 不提供这个方法
		if(_.isUndefined(content)){
			UI.alert(UI.msg("e.act.noapi_content") + " :-> getCurrentTextContent");
			return;
		}
		
		
		// 准备提交
		window.setTimeout(function(){
			Wn.write(o, content, function(o2){
				// 更新缓存
				Wn.saveToCache(o2);
				// 恢复图标
				jIcon.removeClass("fa-spinner fa-pulse")
				 	 .addClass("fa-save");
			});
		}, 100)
	}
})