(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-input"><input></div>
*/};
//===================================================================
return ZUI.def("ui.form_com_input", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return UI.arena.find("input").val();
        });
    },
    //...............................................................
    setData : function(val, jso){
        var UI = this;
        this.ui_parse_data(jso.toStr(), function(s){
            UI.arena.find("input").val(s);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);