({
    icon : '<i class="fa fa-download"></i>',
    text : "i18n:download",
    type : "button",
    handler : function($ele, a) {
    	var UI = this.browser();
        var o  = $z.invoke(UI, "getCurrentEditObj");
        if(!o){
        	alert(UI.msg("e.act.noapi_obj"));
        	return;
        }
        
        $z.openUrl("/o/read/id:" + o.id);
    }
})