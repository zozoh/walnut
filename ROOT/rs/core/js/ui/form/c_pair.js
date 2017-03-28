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
    },
    //...............................................................
    events : {
        "change .pairs input" : function(){
            this.__on_change();
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

        // 如果没内容显示空
        if(!obj || _.isEmpty(obj)){
            UI.arena.attr("show", "empty");
            return;
        }

        // 开始循环设置内容
        for(var key in obj) {
            var jTr = $(`<tr>
                <td class="cp-key"></td>
                <td class="cp-val"><input spellcheck="false"></td>
            </tr>`).appendTo(jTBody);
            jTr.attr("o-key",key).children(".cp-key").text(key);
            var val = UI._V(obj, key);
            jTr.find(".cp-val input").val(val).attr("placeholder", val);
        }
    },
    //...............................................................
    __draw_data : function(obj) {
        var UI  = this;
        var opt = UI.options;

        // 有内容，直接更新
        UI.arena.attr("show", "pairs");
        
        // 已经有模板了
        if(opt.objTemplate) {
            UI.arena.find(".pairs tr").each(function(){
                var jTr = $(this);
                var key = jTr.attr("o-key");
                var val = UI._V(obj, key) || "";
                if(!opt.mergeWith || val){
                    jTr.find("input").val(val);
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