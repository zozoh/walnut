(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_c_methods',
], function(ZUI, ComMethods){
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
        ComMethods(this);
    },
    //...............................................................
    events : {
        "change .pairs input" : function(){
            this.__on_change();
        }
    },
    //...............................................................
    __draw_data : function(obj) {
        var UI  = this;
        var opt = UI.options;

        // 如果没内容显示空
        if(!obj || _.isEmpty(obj)){
            UI.arena.attr("show", "empty");
            return;
        }
        // 有内容，直接更新
        UI.arena.attr("show", "pairs");
        var jTBody = UI.arena.find(".pairs tbody").empty();

        // 开始循环设置内容
        for(var key in obj) {
            var jTr = $('<tr><td class="cp-key"></td><td class="cp-val"><input spellcheck="false"></td></tr>').appendTo(jTBody);
            jTr.children(".cp-key").text(key);
            jTr.find(".cp-val input").val(obj[key]);
        }
    },
    //...............................................................
    getData : function() {
        var UI = this;
        var re = {};

        if(!UI.arena.attr("empty-obj")){
            UI.arena.find(".pairs tr").each(function(){
                var key = $('.cp-key', this).text();
                var val = $('.cp-val input', this).val();
                re[key] = val;
            });
        }

        return re;
    },
    //...............................................................
    setData : function(obj) {
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