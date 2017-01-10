({
    icon : '<i class="fa fa-download"></i>',
    text : "i18n:download",
    type : "button",
    handler : function($ele, a) {
    	var UI = this;
        var o  = $z.invoke(UI, "getCurrentEditObj");
        if(!o){
        	UI.alert(UI.msg("e.act.noapi_obj"));
        	return;
        }
        
        $z.openUrl("/o/read/id:" + o.id, "_blank", "GET", {d:true});
    }
})