({
	icon : '<i class="fa fa-save"></i>',
	text : "i18n:wxmp.save",
	type : "button",
	handler : function($ele, a) {
		var UI      = this;
		
		// 固定按钮宽度
		$ele.css("width", $ele.outerWidth());
		
		var jIcon   = $ele.find(".menu-item-icon");
		var jText   = $ele.find(".menu-item-text");
		
		// 标识状态
		jIcon.html('<i class="fa fa-spinner fa-pulse"></i>');
		jText.text(UI.msg("saving"));
		this.do_save(function(){
			window.setTimeout(function(){
				$ele.css("width", "");
				jIcon.html('<i class="fa fa-save"></i>');
				jText.text(UI.msg('wxmp.save'));
			}, 800);
		});
	}
})