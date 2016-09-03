(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-text"><textarea spellcheck="false"></textarea></div>
*/};
//===================================================================
return ZUI.def("ui.form_com_text", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "change textarea" : function(){
            this.__on_change();
        }
    },
    //...............................................................
    __on_change : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI.parent;
        var v = UI.getData();
        $z.invoke(opt, "on_change", [v], context);
        UI.trigger("change", v);
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return UI.ui_format_data(function(opt){
            return UI.arena.find("textarea").val();
        });
    },
    //...............................................................
    setData : function(val, jso){
        var UI = this;
        this.ui_parse_data(val, function(s){
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