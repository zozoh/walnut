({
	icon : '<i class="fa fa-trash"></i>',
	text : "i18n:delete",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		
		// 没有数据接口 
        if(!_.isFunction(UI.getCurrentEditObj)){
        	alert(UI.msg("e.act.noapi_obj"));
        	return;
        }
        
        // 得到要删除的对象
        var theObj = UI.getCurrentEditObj();
        var list;
        		
		// 是目录
		if("DIR" == theObj.race) {
			// 看看是否有选择的内容
			if(_.isFunction(UI.getChecked)){
				list = UI.getChecked();
			}
		}
		
		// 默认删除当前编辑的项目
		if (!list || list.length == 0) {
			list = [theObj];
		}
		
		// 有目录
		var hasFolder = false;
		for(var o of list){
			if("DIR" == o.race){
				hasFolder = true;
				break;
			}
		}
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
		
		// 刷新界面
		$z.invoke(UI, "refresh");

		// 调用在资源面板上移除
		$z.invoke(UI.resourceUI(), "remove", [o.id]);
	}
})