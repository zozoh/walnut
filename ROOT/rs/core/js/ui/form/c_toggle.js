(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_c_methods',
], function(ZUI, FormCtrlMethods){
//==============================================
var html = '<div class="ui-arena com-toggle" toggle-on="no"><b></b></div>';
//===================================================================
return ZUI.def("ui.form_com_toggle", FormCtrlMethods({
    //...............................................................
    dom  : html,
    //...............................................................
    init : function(opt){
        // 为 true/false 设置值
        $z.setUndefined(opt, "values", [false, true]);
    },
    //...............................................................
    events : {
        "click .com-toggle" : function(e){
            $z.toggleAttr(this.arena, "toggle-on", "yes", "no");
            // 通知
            this.__on_change();
        }
    },
    //...............................................................
    _val : function(index) {
        var UI  = this;
        var opt = UI.options;
        if(opt.values && index >= 0 && index<opt.values.length) {
            return opt.values[index];
        }
        return undefined;
    },
    //...............................................................
    _get_data : function(){
        var index = this.arena.attr("toggle-on") == "yes" ? 1 : 0;
        return this._val(index);
    },
    //...............................................................
    _set_data : function(val){
        var UI = this;
        this.arena.attr({
            "animat-on" : null,
            "toggle-on" : val == this._val(1) ? "yes" : "no"
        });
        window.setTimeout(function(){
            UI.arena.attr("animat-on", "yes");
        }, 0);
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return UI._get_data();
        });
    },
    //...............................................................
    setData : function(val){
        var UI = this;
        UI.ui_parse_data(val, function(v){
            UI._set_data(v)
        });
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);