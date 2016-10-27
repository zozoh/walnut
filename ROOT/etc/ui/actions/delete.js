({
	icon : '<i class="zmdi zmdi-delete"></i>',
	text : "i18n:delete",
	type : "button",
	handler : function($ele, a) {
		// 得到要操作的 UI，如果是 browser 的子 UI，则一定得到 browser 的顶级控件
		// 否则控件必须实现 "getChecked" 和 "refresh" 函数，以便本过程调用
		var UI = $z.invoke(this, "browser") || this;
		
		var list = UI.getChecked();
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

		UI.refresh();
	}
})