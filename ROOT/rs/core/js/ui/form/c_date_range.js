(function($z){
$z.declare([
    'zui',
    'ui/form/support/range_methods',
    'jquery-plugin/zcal/zcal',
], function(ZUI, FormMethods){
//==============================================
var html = `<div class="ui-arena com-date-range com-range">
    <section>
        <dl class="rv-left">
            <dt><em>{{com.range.left}}</em><b>{{clear}}</b></dt>
            <dd></dd>
        </dl>
        <dl class="rv-right">
            <dt><em>{{com.range.right}}</em><b>{{clear}}</b></dt>
            <dd></dd>
        </dl>
    </section>
    <footer>
        <b m="left">{{com.range.involve}}</b>
        <span>{{com.range.empty}}</span>
        <b m="right">{{com.range.involve}}</b>
    </footer>
</div>`;
//===================================================================
return ZUI.def("ui.form_com_date_range", {
    //...............................................................
    dom  : html,
    css  : ["ui/form/theme/component-{{theme}}.css",
            "jquery-plugin/zcal/theme/zcal-{{theme}}.css"],
    i18n : "ui/form/i18n/{{lang}}.js",
    //...............................................................
    init : function(opt){
        FormMethods(this);
    },
    //...............................................................
    events : {
        // 取消日历高亮
        "click > .com-range > section > dl > dt b" : function(e){
            var jq = $(e.currentTarget).closest("dl").find(">dd");
            jq.zcal("blur");
        },
        // 开关左右区间
        "click > .com-range > footer b" : function(e){
            $z.toggleAttr(e.currentTarget, "on", "yes");
            this.__show_data(true);
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 设置主区域宽高
        UI.arena.css({
            "width"  : opt.width,
            "height" : opt.height,
        });

        // 日历的配置信息
        var calOpt = {
            current : new Date(),
            toggleBlur  : true,
            blockWidth  : "100%",
            blockHeight : "100%",
            swticher : {
                today : UI.msg("dt.today"), 
                prev  : '<i class="zmdi zmdi-chevron-left"></i>',
                next  : '<i class="zmdi zmdi-chevron-right"></i>', 
            },
            i18n : UI.msg("dt"),
            on_actived : function(){
                UI.__show_data(true);
            },
            on_blur : function(){
                UI.__show_data(true);
            }
        };

        // 创建两侧日历
        UI.arena.find(">section>dl.rv-left>dd").zcal(calOpt);
        UI.arena.find(">section>dl.rv-right>dd").zcal(_.extend({}, calOpt, {
            swticherAtRight : true
        }));

        // 显示
        UI.__show_data();
    },
    //...............................................................
    _get_data : function(){
        var UI = this;
        // 得到数据
        var l_on  = UI.arena.find('>footer>b[m="left"]').attr("on") ? true : false;
        var r_on  = UI.arena.find('>footer>b[m="right"]').attr("on") ? true : false;
        var l_val = UI.arena.find(">section>dl.rv-left>dd").zcal("actived");
        var r_val = UI.arena.find(">section>dl.rv-right>dd").zcal("actived");

        // 拼合字符串
        var str = null;
        if(l_val || r_val) {
            str = "";
            str += l_on ? "[" : "(";
            str += l_val ? l_val.format("yyyy-mm-dd") : "";
            str += ",";
            str += r_val ? r_val.format("yyyy-mm-dd") : "";
            str += r_on ? "]" : ")";
        }

        // 返回值
        return str;
    },
    //...............................................................
    _set_value : function(l_on, l_val, r_val, r_on){
        //console.log(l_on,l_val, r_val, r_on)
        this.arena.find('>footer>b[m="left"]').attr("on",  l_on ? "yes" : null);
        this.arena.find('>footer>b[m="right"]').attr("on", r_on ? "yes" : null);
        this.arena.find(">section>dl.rv-left>dd") .zcal("active", l_val);
        this.arena.find(">section>dl.rv-right>dd").zcal("active", r_val);
        this.__show_data();
    },
    //...............................................................
    __on_resize : function(){
        this.arena.find(">section>dl.rv-left>dd").zcal("resize");
        this.arena.find(">section>dl.rv-right>dd").zcal("resize");
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);