({
	icon : '<i class="fa fa-upload"></i>',
	text : "i18n:obrowser.action.upload",
	type : "button",
	handler : function($ele, a) {
		var uiBw = this;
		while(uiBw && !_.isFunction(uiBw.showUploader)){
			uiBw = uiBw.parent;
		}
		
		if(uiBw)
			uiBw.showUploader({
				finish : function(){
					UIBrowser.refresh();
					this.parent.close();
				}
			});
	}
})