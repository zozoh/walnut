(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
], function(ZUI, FormMethods){
//==============================================
var html = function(){/*
<div class="ui-arena com-name">
    <span></span>
    <a>{{modify}}</a>
</div>
*/};
//===================================================================
return ZUI.def("ui.form_com_name", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(opt){
        FormMethods(this);
    },
    //...............................................................
    events : {
        "click a" : function(){
            var UI = this;
            UI.prompt("modify", {
                placeholder : UI._get_data(),
                ok : function(str){
                    UI._set_data(str);
                    UI.__on_change();
                }
            });
        }
    },
    //...............................................................
    _get_data : function(){
        return this.arena.find("span").text();
    },
    //...............................................................
    _set_data : function(val, jso){
        this.arena.find("span").text(val);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);