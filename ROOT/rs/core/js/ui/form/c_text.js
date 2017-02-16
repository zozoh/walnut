(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_c_methods'
], function(ZUI, FormMethods){
//==============================================
var html = function(){/*
<div class="ui-arena com-text"><textarea spellcheck="false"></textarea></div>
*/};
//===================================================================
return ZUI.def("ui.form_com_text", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(opt){
        FormMethods(this);

        $z.setUndefined(opt, "trimData", true);
    },
    //...............................................................
    events : {
        "change textarea" : function(){
            this.__on_change();
        },
        "keydown textarea" : function(e){
            if(13 == e.which && (e.metaKey || e.ctrlKey)) {
                this.__on_change();
            }
        }
    },
    //...............................................................
    redraw : function(){
        var UI    = this;
        var opt   = UI.options;
        var jText = UI.arena.find("textarea");
        
        // 占位符显示
        if(opt.placeholder) {
            jText.attr("placeholder", opt.placeholder);
        }
    },
    //...............................................................
    getData : function(){
        var UI  = this;
        var opt = UI.options;
        return UI.ui_format_data(function(opt){
            var val = UI.arena.find("textarea").val();
            if(opt.trimData)
                val = $.trim(val);
            return val || null;
        });
    },
    //...............................................................
    setData : function(val, jso){
        var UI = this;
        this.ui_parse_data(val, function(s){
            if((_.isNumber(s) && isNaN(s)) || _.isUndefined(s) || _.isNull(s))
                s = "";
            UI.arena.find("textarea").val(s);
        });
    },
    //...............................................................
    resize : function(){
        var UI  = this;
        var opt = UI.options;
        var W   = UI.$pel.width();
        var H   = UI.$pel.height();

        // 指定宽度
        if(opt.width) {
            UI.arena.css("width", $z.dimension(opt.width, W));
        }

        if(opt.height){
            UI.arena.css("height", $z.dimension(opt.height, H));   
        }
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);