({
	icon : '<i class="fa fa-save"></i>',
	text : "i18n:save",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		var jIcon = $ele.find("i.fa");
		jIcon.removeClass("fa-save")
			 .addClass("fa-spinner fa-pulse");
		
		// 获取当前的对象
		var o = $z.invoke(this, "getCurrentObj");
		if(_.isUndefined(o)){
			alert(UI.msg("e.act.noapi.obj"));
			return;
		}
		//console.log(o);
		
		// 未定义，那么就表示 context 不提供这个方法
		var content = $z.invoke(this, "getCurrentTextContent");
		if(_.isUndefined(content)){
			alert(UI.msg("e.act.noapi.content"));
			return;
		}
		//console.log(content);
		
		// 准备提交
		window.setTimeout(function(){
			Wn.write(o, content, function(o2){
				jIcon.removeClass("fa-spinner fa-pulse")
				 	 .addClass("fa-save");
			});
		}, 100)
	}
})