(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
], function(ZUI, FormMethods){
//==============================================
var html = `<div class="ui-arena com-pair">
    <section class="pairs">
        <table><tbody></tbody></table>
    </section>
    <section class="empty">{{com.pair.empty}}</section>
</div>`;
//==============================================
return ZUI.def("ui.form_com_pair", {
    dom  : html,
    //...............................................................
    init : function(opt){
        FormMethods(this);

        $z.setUndefined(opt, "trimSpace", true);
        $z.setUndefined(opt, "mergeWith", false);
        $z.setUndefined(opt, "templateAsDefault", true);
    },
    //...............................................................
    events : {
        "change .pairs input" : function(){
            this.__on_change();
        },
        "dblclick input[dft-val]" : function(e){
            var jInput = $(e.currentTarget);
            var val = $.trim(jInput.val());
            if(!val && jInput.attr("dft-val")){
                jInput.val(jInput.attr("dft-val"));
                this.__on_change();
            }
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 清空内容
        var jTBody = UI.arena.find(".pairs tbody").empty();

        // 如果已经指明对象模板
        if(opt.objTemplate) {
            UI.__redraw_table(opt.objTemplate);
        }
    },
    //...............................................................
    _V : function(obj, key) {
        if(!obj)
            return undefined;
        var re = obj[key];
        if(this.options.trimSpace)
            return $.trim(re);
        return re;
    },
    //...............................................................
    __redraw_table : function(obj){
        var UI = this;
        var opt = UI.options;

        // 清空内容
        var jTBody = UI.arena.find(".pairs tbody").empty();

        // 开始循环设置内容
        var isEmpty = true;
        if(obj) {
            for(var key in obj) {
                isEmpty = false;
                var jTr = $(`<tr>
                    <td class="cp-key"></td>
                    <td class="cp-val"><input spellcheck="false"></td>
                </tr>`).appendTo(jTBody);
                jTr.attr("o-key",key).children(".cp-key").text(key);
                var val = UI._V(obj, key);
                var jInput = jTr.find(".cp-val input")
                                .val(val)
                                    .attr("dft-val", val);
                if(opt.templateAsDefault)
                    jInput.attr("placeholder", val);
            }
        }

        // 标识空对象
        UI.arena.attr("show", isEmpty ? "empty" : "pairs");
    },
    //...............................................................
    __draw_data : function(obj) {
        var UI  = this;
        var opt = UI.options;
        
        // 已经有模板了
        if(opt.objTemplate) {
            UI.arena.find(".pairs tr").each(function(){
                var jTr = $(this);
                var key = jTr.attr("o-key");
                var val = UI._V(obj, key);
                if(_.isString(val)){
                    jTr.find("input").val(val);
                }
                else if(!opt.mergeWith){
                    jTr.find("input").val("");
                }
            });
        }
        // 重新绘制
        else {
            UI.__redraw_table(obj);
        }
    },
    //...............................................................
    setObjTemplate : function(ot, cleanData) {
        var UI   = this;
        var opt  = UI.options;
        var data = UI._get_data();

        opt.objTemplate = ot;

        // 重绘模板
        if(opt.objTemplate) {
            UI.__redraw_table(opt.objTemplate);
        }
        // 重绘数据
        UI.__draw_data(cleanData ? {} : data);
    },
    //...............................................................
    _get_data : function() {
        var UI = this;
        var opt = UI.options;

        var re = {};
        if(UI.arena.attr("show") != "empty"){
            UI.arena.find(".pairs tr").each(function(){
                var jTr = $(this);
                var jInput = jTr.find('input');
                var key = jTr.attr("o-key");
                var val = jInput.val() || jInput.attr("placeholder");
                if(opt.trimSpace)
                    val = $.trim(val);
                re[key] = val;
            });
        }

        return re;
    },
    //...............................................................
    _set_data : function(obj) {
        //console.log("pair._set_data:", obj)
        // 如果是字符串，可能是 JSON
        if(_.isString(obj))
            obj = $z.fromJson(obj);

        // 那么开始绘制
        this.__draw_data(obj);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);