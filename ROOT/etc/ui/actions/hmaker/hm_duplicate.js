({
	icon : '<i class="far fa-clone"></i>',
	text : "i18n:duplicate",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		
		// 标记图标
		var jIcon = $ele.find("i.far");
		
		// 得到旧的 class
		var oldClassName = jIcon[0].className;
		jIcon[0].className = "fa fa-spinner fa-pulse";
		
		// 获取当前的对象
		var o = $z.invoke(this, "getCurrentEditObj");
		// 未定义，那么就表示 context 不提供这个方法
		if(_.isUndefined(o)){
			UI.alert(UI.msg("e.act.noapi_obj") + " :-> getCurrentEditObj");
			jIcon[0].className = oldClassName;
			return;
		}
		
		// 获取当前的文本内容
		var content = $z.invoke(this, "getCurrentTextContent", [true]);
		// 未定义，那么就表示 context 不提供这个方法
		if(_.isUndefined(content)){
			UI.alert(UI.msg("e.act.noapi_content") + " :-> getCurrentTextContent");
			jIcon[0].className = oldClassName;
			return;
		}
		
		// 创建一个新页面
		UI.prompt('i18n:hmaker.action.dup_tip', {
			dfttext : o.nm, 
			ok : function(newName){
				//console.log(newName)
				var o2 = $z.pick(o, /^(pid|tp|mime)$/);
				o2.nm = newName;
				var cmdText = $z.tmpl("obj -o -new \'<%=obj%>\'")($z.toJson(o2));
				Wn.exec(cmdText, function(re){
					// 检查错误
					if(/^e./.test(re)) {
						UI.alert(re);
						jIcon[0].className = oldClassName;
						return;
					}
					// 保存新内容
					var o3 = $z.fromJson(re);
					Wn.exec("hmaker save -o id:" + o3.id, content, function(re){
						if(/^e./.test(re)) {
							UI.alert(re);
							jIcon[0].className = oldClassName;
							return;
						}
						var o4 = $z.fromJson(re);
						
						// 更新缓存
						Wn.saveToCache(o4);
						
						// 准备刷新站点资源
						var uiRes = UI.resourceUI();
				        var oAt   = uiRes.getActived() || oHome;
				        // 如果当前选中的是一个文件，那么选择父目录
				        if('DIR' != oAt.race) {
				            oAt = Wn.getById(oAt.pid, true) || oHome;
				        }
				        
				        // 刷新站点资源后恢复图标
						UI.fire("reload:folder", oAt, function(){
							jIcon[0].className = oldClassName;
						});
						
					});
				})
			}
		});
	}
})