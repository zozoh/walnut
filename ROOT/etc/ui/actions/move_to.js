({
	text : "i18n:moveTo",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		var objs = UI.browser.getChecked();

		//console.log(objs);

		// 没有对象被选中
		if (objs.length == 0) {
			alert(UI.msg("noselected"));
			return;
		}

		// 得到 id 列表
		var ids = _.pluck(objs, "id");
		//console.log(ids);

		// 总结一下类型列表
		var memo = {};
		var tps = [];
		for (var i = 0; i < objs.length; i++) {
			var obj = objs[i];
			var tp = Wn.objTypeName(obj);
			if (!memo[tp]) {
				tps.push(tp);
				memo[tp] = true;
			}
		}

		// 查询移动到的限制
		var cmdText = "app-move-to type:" + tps.join(" type:");
		//console.log(cmdText);

		Wn.exec(cmdText, function(re) {
			var moveTo = $z.fromJson(re) || {};
			// 预编译正则表达式
			if (moveTo.filter && moveTo.filter.length > 0) {
				for (var i = 0; i < moveTo.filter.length; i++) {
					moveTo.filter[i] = new RegExp(moveTo.filter[i]);
				}
			}
			// 清空
			else {
				moveTo.filter = [];
			}

			seajs.use('ui/pop/pop_browser', function(PopUI) {
				new PopUI({
					base : moveTo.base || "~",
					multi : false,
					filter : function(o) {
						// 必须是目录
						if ('DIR' != o.race)
							return false;

						// 不能是已选
						if (ids.indexOf(o.id) >= 0)
							return false;

						if (moveTo.filter.length > 0) {
							var tp = Wn.objTypeName(o);
							for (var i = 0; i < moveTo.filter.length; i++) {
								if (!moveTo.filter[i].test(tp)) {
									return false;
								}
							}
						}
						// 嗯通过
						return true;
					},
					on_ok : function(oTa) {
						//console.log("ta:", oTa)
						// 组合命令
						var cmdText = "";
						for (var i = 0; i < ids.length; i++) {
							cmdText += 'mv id:' + ids[i] + ' id:' + oTa[0].id + ";";
						}
						//console.log(cmdText);
						// 执行
						Wn.exec(cmdText, function() {
							UI.browser.refresh();
						});

					}
				}).render();
			});
		});
	}
})