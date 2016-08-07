({
	text : "i18n:ui.view_text",
	type : "button",
	handler : function($ele, a) {
		// 获取当前的文本内容
		var content = $z.invoke(this, "getCurrentTextContent");

		// 未定义，那么就表示 context 不提供这个方法
		if (_.isUndefined(content)) {
			alert(_C.msg("e.act.noapi_content") + " :-> getCurrentTextContent");
			return;
		}

		var MaskUI = require("ui/mask/mask");
		new MaskUI({
			width : "90%",
			height : "90%"
		}).render(function() {
			var jPre = $('<pre>').appendTo(this.$main);
			jPre.css({
				"width" : "100%",
				"height" : "100%",
				"padding" : "20px",
				// "word-wrap" : "break-word",
				// "word-break" : "break-all",
				"overflow" : "auto"
			}).text(content);
		});
	}
})