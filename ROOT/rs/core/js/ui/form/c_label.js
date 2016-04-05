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
    init : function(opt){
        $z.setUndefined(opt, "escapeHtml", true);
    },
    //...............................................................
    getData : function(){
        var UI = this;
        return this.ui_format_data(function(opt){
            return UI.$el.data("@VAL") || "";
        });
    },
    //...............................................................
    setData : function(val, jso){
        var UI  = this;
        var opt = UI.options;

        // 记录数据
        this.$el.data("@VAL", val);
        val = _.isNull(val) || _.isUndefined(val) ? "" : val;

        // 设置显示 
        this.ui_parse_data(val, function(html){
            if(_.isDate(html)){
                html = html.format("yyyy-mm-dd HH:MM:ss");
            }
            if(opt.escapeHtml){
                this.arena.text(html);
            }else {
                this.arena.html(html);
            }
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);