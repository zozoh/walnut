({
	icon : '<i class="fa fa-upload"></i>',
	text : "i18n:obrowser.action.upload",
	type : "button",
	handler : function($ele, a) {
		var UIBrowser = this;
		UIBrowser.showUploader({
			finish : function(){
				UIBrowser.refresh();
				this.parent.close();
			}
		});
	}
})