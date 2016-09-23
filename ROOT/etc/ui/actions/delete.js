({
	icon : '<i class="zmdi zmdi-delete"></i>',
	text : "i18n:delete",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		
		var list = UI.browser().getChecked();
		// 没内容
		if (list.length == 0) {
			alert(UI.msg("obrowser.warn.empty"));
			return;
		}
		// 有目录
		var hasFolder = false;
		list.forEach(function(o) {
			hasFolder |= "DIR" == o.race;
		});
		if (hasFolder) {
			if (!window.confirm(UI.msg("obrowser.warn.rmdir"))) {
				return;
			}
		}

		// 执行
		var cmdText = "rm -rf ";
		list.forEach(function(o) {
			cmdText += " id:" + o.id;
		});

		Wn.exec(cmdText);

		UI.browser().refresh();
	}
})