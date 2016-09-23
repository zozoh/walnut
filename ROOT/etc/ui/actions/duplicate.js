({
	icon : '<i class="zmdi zmdi-copy"></i>',
	text : "i18n:duplicate",
	type : "button",
	handler : function($ele, a) {
		var UIBrowser = this.browser();
		var list = UIBrowser.getChecked();
		// 没内容
		if (list.length == 0) {
			alert(UIBrowser.msg("obrowser.warn.empty"));
			return;
		}
		// 有目录
		var hasFolder = false;
		list.forEach(function(o) {
			hasFolder |= "DIR" == o.race;
		});
		if (hasFolder) {
			if (!window.confirm(UIBrowser.msg("obrowser.warn.dupdir"))) {
				return;
			}
		}

		// 执行
		var cmdText = "";
		list.forEach(function(o) {
			// 得到文件名
			var fnm = o.nm;
			var pos = fnm.lastIndexOf(".");
			var fMajorName, fSuffix;
			if (pos > 0) {
				fMajorName = fnm.substring(0, pos);
				fSuffix = fnm.substring(pos);
			}
			else {
				fMajorName = fnm;
				fSuffix = "";
			}

			// 组合命令
			cmdText += "cp -p id:" + o.id + " \"id:" + o.pid;
			cmdText += "/`fnm id:" + o.pid + " -tmpl '" + fMajorName + "副本(@{n})" + fSuffix + "'`\";";
		});

		console.log(cmdText)
		Wn.exec(cmdText);

		UIBrowser.refresh();
	}
})