(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-input"><input><span class="unit">?</span></div>
*/};
//===================================================================
return ZUI.def("ui.form_com_input", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "change input" : function(){
            this.__on_change();
        }
    },
    //...............................................................
    __on_change : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        var v = UI.getData();
        $z.invoke(opt, "on_change", [v], context);
        UI.trigger("change", v);
    },
    //...............................................................
    redraw : function(){
        var UI    = this;
        var opt   = UI.options;
        var jUnit = UI.arena.find(".unit");
        // 声明了单位，显示一下
        if(opt.unit) {
            jUnit.text(opt.unit);
        }
        // 木有单位，移除
        else{
            jUnit.remove();
        }
    },
    //...............................................................
    resize : function(){
        var UI    = this;
        var opt   = UI.options;
        var jUnit = this.arena.find(".unit");
        if(opt.unit) {
            UI.arena.find(".unit").css({
                "line-height" : (jUnit.outerHeight() -1) + "px"
            });
            UI.arena.find("input").css({
                "padding-right" : jUnit.outerWidth()
            })
        }
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return $.trim(UI.arena.find("input").val());
        });
    },
    //...............................................................
    setData : function(val, jso){
        var UI = this;
        this.ui_parse_data(val, function(s){
            var str = jso.parse(s).toStr();
            UI.arena.find("input").val($.trim(str));
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);