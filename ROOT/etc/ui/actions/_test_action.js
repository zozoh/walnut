({
	text : "~测试命令~",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		
		UI.browser().setData("~/demo_site1", "hmaker2")
	}
})