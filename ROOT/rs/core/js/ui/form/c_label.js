(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-label"></div>
*/};
//===================================================================
return ZUI.def("ui.form_com_label", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    getData : function(){
        return this.$el.data("@VAL");
    },
    //...............................................................
    setData : function(val, fld, ftype){
        this.$el.data("@VAL", val);
        this.arena.text(ftype.toText(fld, val));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);