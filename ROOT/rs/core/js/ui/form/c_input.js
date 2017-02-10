(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-input"><input spellcheck="false"><span class="unit">?</span></div>
*/};
//===================================================================
return ZUI.def("ui.form_com_input", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "change input" : function(){
            this.__on_change();
        },
        "keydown textarea" : function(e){
            if(13 == e.which && (e.metaKey || e.ctrlKey)) {
                this.__on_change();
            }
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
    redraw : function(){
        var UI     = this;
        var opt    = UI.options;
        var jUnit  = UI.arena.find(".unit");
        var jInput = UI.arena.find("input");

        // 声明了单位，显示一下
        if(opt.unit) {
            jUnit.text(UI.text(opt.unit));
        }
        // 木有单位，移除
        else{
            jUnit.remove();
        }

        // 占位符显示
        if(opt.placeholder) {
            jInput.attr("placeholder", opt.placeholder);
        }

        // ComboBox 列表
        if(_.isArray(opt.list) && opt.list.length > 0 ) {
            var comboId = "combo_id_ui_" + UI.cid;
            var html = '<datalist id="' + comboId + '">';
            for(var v of opt.list){
                html += '<option value="'+v+'">';
            }
            html += '</datalist>';
            $(html).appendTo(UI.arena);
            
            jInput.attr("list", comboId);
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
            if((_.isNumber(s) && isNaN(s)) || _.isUndefined(s) || _.isNull(s))
                s = "";
            UI.arena.find("input").val(s);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);