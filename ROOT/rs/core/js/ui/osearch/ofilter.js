(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent>
<div class="ofilter-keyword">
    <input placeholder="{{osearch.filter.tip}}">
</div>
</div>
*/};
//==============================================
return ZUI.def("ui.ofilter", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    init : function(options){
    },
    //..............................................
    events : {
    },
    //..............................................
    redraw : function(callback){
    },
    //..............................................
    resize : function(){
    },
    //..............................................
    setData : function(o){
        var UI = this;
        UI.$el.attr("pid", o.id);
        return this;
    },
    //..............................................
    getData : function(){
        var UI = this;
        var q = {
            pid : UI.$el.attr("pid")
        };
        return {
            condition : $z.toJson(q)
        }
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);