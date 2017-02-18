(function($z){
$z.declare([
    'zui',
    'ui/form/support/range_methods',
], function(ZUI, FormMethods){
//==============================================
var html = `<div class="ui-arena com-number-range com-range">
    <section>
        <dl class="rv-left">
            <dt><em>{{com.range.left}}</em><b>{{clear}}</b></dt>
            <dd><input placeholder="{{com.range.number}}"></dd>
        </dl>
        <dl class="rv-right">
            <dt><em>{{com.range.right}}</em><b>{{clear}}</b></dt>
            <dd><input placeholder="{{com.range.number}}"></dd>
        </dl>
    </section>
    <footer>
        <b m="left">{{com.range.involve}}</b>
        <span>{{com.range.empty}}</span>
        <b m="right">{{com.range.involve}}</b>
    </footer>
</div>`;
//===================================================================
return ZUI.def("ui.form_com_number_range", {
    //...............................................................
    dom  : html,
    css  : "theme/ui/form/component.css",
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt){
        FormMethods(this);
    },
    //...............................................................
    events : {
        // 删除输入框内容
        "click > .com-range > section > dl > dt b" : function(e){
            $(e.currentTarget).closest("dl").find(">dd input").val("");
            this.__show_data();
        },
        // 开关左右区间
        "click > .com-range > footer b" : function(e){
            $z.toggleAttr(e.currentTarget, "on", "yes");
            this.__show_data();
        },
        // 修改输入框
        "change > .com-range > section > dl > dd input" : function(e){
            var jq = $(e.currentTarget);
            var v  = jq.val() * 1;
            if(isNaN(v)){
                jq.val("");
            }
            this.__show_data();
        }
    },
    //...............................................................
    _get_data : function(){
        var UI = this;
        // 得到数据
        var l_on  = UI.arena.find('>footer>b[m="left"]').attr("on") ? true : false;
        var r_on  = UI.arena.find('>footer>b[m="right"]').attr("on") ? true : false;
        var l_val = $.trim(UI.arena.find(">section>dl.rv-left input").val());
        var r_val = $.trim(UI.arena.find(">section>dl.rv-right input").val());

        // 拼合字符串
        var str = null;
        if(l_val || r_val) {
            str = "";
            str += l_on ? "[" : "(";
            str += l_val ? l_val * 1 : "";
            str += ",";
            str += r_val ? r_val * 1 : "";
            str += r_on ? "]" : ")";
        }

        // 返回值
        return str;
    },
    //...............................................................
    _set_value : function(l_on, l_val, r_val, r_on){
        //console.log(l_on, l_val, r_val, r_on)
        this.arena.find('>footer>b[m="left"]').attr("on",  l_on ? "yes" : null);
        this.arena.find('>footer>b[m="right"]').attr("on", r_on ? "yes" : null);
        this.arena.find(">section>dl.rv-left input").val(this._V(l_val));
        this.arena.find(">section>dl.rv-right input").val(this._V(r_val));
        this.__show_data();
    },
    //...............................................................
    _V : function(s) {
        s = $.trim(s);
        if(s){
            return s * 1 || "";
        }
        return "";
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);