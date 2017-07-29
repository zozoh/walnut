(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
    'ui/zcron/support/zcr_methods',
    'ui/zcron/zcron',
    'ui/tabs/tabs',
    'ui/form/c_input',
    'ui/form/c_array',
], function(ZUI, FormMethods, ZCronMethods, ZCron, TabsUI, InputUI, ArrayUI){
//==============================================
var html = function(){/*
<div class="ui-arena zcron">
    <header class="zcron-dr">
        <em>日期范围</em>
        <div ui-gasket="drange"></div>
    </header>
    <header class="zcron-month">
        <em>选择月份</em>
        <div ui-gasket="month"></div>
    </header>
    <section class="zcron-date"  ui-gasket="date"></section>
    <section class="zcron-time"  ui-gasket="time"></section>
    <section class="zcron-expr"><input spellcheck="false"></section>
    <footer  class="zcron-text"><div></div></footer>
</div>
*/};
//==============================================
return ZUI.def("ui.zcron", {
    __zcron_ui__ : true,
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : ["ui/zcron/theme/zcron-{{theme}}.css",
            "ui/form/theme/component-{{theme}}.css"],
    i18n : ["ui/zcron/i18n/{{lang}}.js", "ui/form/i18n/{{lang}}.js"],
    //...............................................................
    init : function(opt){
        FormMethods(this);
        ZCronMethods(this);
    },
    //...............................................................
    events : {
        'change .zcron-expr input' : function(e) {
            var UI = this;
            var jInput = $(e.currentTarget);
            var cron = $.trim(jInput.val());
            UI._set_data(cron);
        }
    },
    //...............................................................
    redraw : function(){
        var UI = this;
        //...................................................
        // 日期范围
        new InputUI({
            parent : UI,
            gasketName : "drange",
            assist : {
                icon   : '<i class="zmdi zmdi-calendar"></i>',
                uiType : 'ui/form/c_date_range',
            },
            // 监听更新
            do_ui_listen : {
                "$parent data:change" : function(ozc){
                    this.setData(ozc.rgDate ? ozc.rgDate.toString() : "");
                }
            },
            // 通知更新
            on_change : function(val) {
                var ozc = UI._get_data();
                ozc.setPartExtDate(val ? "D"+val
                                       : null );
                UI._set_data(ozc);
            }
        }).render(function(){
            UI.defer_report("drange");
        });
        //...................................................
        // 月份范围
        new ArrayUI({
            parent : UI,
            gasketName : "month",
            items : [1,2,3,4,5,6,7,8,9,10,11,12],
            text  : function(v) {
                return UI.msg("zcron.exp.month.dict")[v-1];
            },
            // 监听更新
            do_ui_listen : {
                "$parent data:change" : function(ozc){
                    UI.setCronToArrayUI(UI.gasket.month, ozc, "matchMonth");
                }
            },
            // 通知更新
            on_change : function(v){
                UI.notifyStdDatePartChange();
            }
        }).render(function(){
            UI.defer_report("month");
        });
        //...................................................
        // 周/日期视图
        new TabsUI({
            parent : UI,
            gasketName : "date",
            defaultKey : "weekly",
            setup : {
                "weekly" : {
                    text : "周",
                    uiType : 'ui/zcron/support/zcr_weekly',
                },
                "monthly" : {
                    text : "日期",
                    uiType : 'ui/zcron/support/zcr_monthly',
                }
            },
            on_changeUI : function(key, subUI) {
                var ozc = UI._get_data();
                if(ozc)
                    subUI.update(ozc);
            }
        }).render(function(){
            UI.defer_report("date");
        });
        //...................................................
        // 时间视图
        new TabsUI({
            parent : UI,
            gasketName : "time",
            defaultKey : "tmrg",
            setup : {
                "hms" : {
                    text : "小时/分钟",
                    uiType : 'ui/zcron/support/zcr_hms',
                },
                "tmrg" : {
                    text : "时间范围",
                    uiType : 'ui/zcron/support/zcr_tmrg',
                },
                "tps" : {
                    text : "时间点",
                    uiType : 'ui/zcron/support/zcr_tps',
                }
            },
            on_changeUI : function(key, subUI) {
                var ozc = UI._get_data();
                if(ozc)
                    subUI.update(ozc);
            }
        }).render(function(){
            UI.defer_report("time");
        });
        //...................................................
        // 返回延迟加载
        return ["drange", "month", "date", "time"];
    },
    //...............................................................
    __update_expr : function(ozc) {
        this.arena.find(".zcron-expr input").val(ozc.toString());
    },
    //...............................................................
    __update_explain : function(ozc) {
        var UI = this;
        var text = ozc.toText(UI.msg("zcron.exp"));
        UI.arena.find(".zcron-text > div").text(text);
    },
    //...............................................................
    setPart : function(index, str) {
        var UI  = this;
        console.log("setPart", index, str);
        var ozc = UI._get_data();
        ozc.__set_part(index, str);
        console.log("ozc", ozc);
        UI._set_data(ozc);
        return this;
    },
    //...............................................................
    // 根据控件状态，或者标准日期部分（日 月 周）的描述字符串
    getPartOfStdDate : function() {
        var UI = this;
        var vs = ["*","*","?"];
        // 获得日
        if(UI.gasket.date.isCurrent("monthly")){
            vs[0] = UI.subUI("date/main").getData();
        }
        
        // 获得月
        vs[1] = UI.getStrFromArrayUI(UI.gasket.month, "1-12", "*");

        // 获得周
        if(UI.gasket.date.isCurrent("weekly")){
            vs[2] = UI.subUI("date/main").getData();
        }
        //console.log(vs);

        // 最后返回
        return vs.join(" ");
    },
    //...............................................................
    notifyStdDatePartChange : function(){
        var str = this.getPartOfStdDate();
        this.setPart(2, str);
    },
    //...............................................................
    _set_data : function(cron){
        var UI = this;
        var jT = UI.arena.find(".zcron-text > div");

        // 解析
        var ozc;

        try{
            ozc = ZCron(cron || "0 0 0 * * ?");
        }
        // 格式出错了
        catch(E){
            jT.attr("invalid", "yes").text("表达式格式错误：" + E);
            return;
        }
        console.log("_set_data:", ozc.toString());

        // 恢复显示模式
        jT.removeAttr("invalid");

        // 更新表达式
        this.__update_expr(ozc);

        // 更新解释
        this.__update_explain(ozc);

        // 更新子控件
        UI.trigger("data:change", ozc);
    },
    //...............................................................
    _get_data : function(){
        var cron = $.trim(this.arena.find(".zcron-expr input").val());
        if(!cron)
            return null;
        return ZCron(cron);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);