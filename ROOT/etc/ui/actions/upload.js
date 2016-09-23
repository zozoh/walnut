({
	icon : '<i class="fa fa-upload"></i>',
	text : "i18n:obrowser.action.upload",
	type : "button",
	handler : function($ele, a) {
		var UI = this;
		UI.browser().showUploader({
			finish : function(){
				UI.browser().refresh();
				this.parent.close();
			}
		});
	}
})