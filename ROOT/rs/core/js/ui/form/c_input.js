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
        return this.arena.find("input").val();
    },
    //...............................................................
    setData : function(val, fld, ftype){
        this.arena.find("input").val(ftype.toStr(fld, val));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);