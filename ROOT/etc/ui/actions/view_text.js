({
	text : "i18n:ui.view_text",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		// 获取当前的文本内容
		var content = $z.invoke(this, "getCurrentTextContent");
		
		// 未定义，那么就表示 context 不提供这个方法
		if(_.isUndefined(content)){
			UI.alert(UI.msg("e.act.noapi_content") + " :-> getCurrentTextContent");
			return;
		}
		
		var MaskUI = require("ui/mask/mask");
		new MaskUI({
			width  : "100%",
			height : "100%"
		}).render(function(){
			var jPre = $('<pre>').appendTo(this.$main);
			jPre.css({
				"width"  : "100%",
				"height" : "100%",
				"overflow" : "auto"
			}).text(content);
		});
	}
})