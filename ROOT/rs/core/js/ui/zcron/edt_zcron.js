(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
    'ui/zcron/zcron',
    'ui/form/c_input',
    'ui/tabs/tabs',
], function(ZUI, FormMethods, ZCron, InputUI, TabsUI){
//==============================================
var html = function(){/*
<div class="ui-arena zcron">
    <header>
        <em>生效日期范围</em>
        <div class="zcron-dr" ui-gasket="drange"></div>
        <a>清除</a>
    </header>
    <section class="zcron-date" ui-gasket="date"></section>
    <section class="zcron-time" ui-gasket="time"></section>
    <section class="zcron-expr"><input spellcheck="false"></section>
    <footer  class="zcron-text"><div></div></footer>
</div>
*/};
//==============================================
return ZUI.def("ui.zcron", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : ["ui/zcron/theme/zcron-{{theme}}.css",
            "ui/form/theme/component-{{theme}}.css"],
    i18n : ["ui/zcron/i18n/{{lang}}.js", "ui/form/i18n/{{lang}}.js"],
    //...............................................................
    init : function(opt){
        FormMethods(this);
    },
    //...............................................................
    events : {
        
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 日期范围
        new InputUI({
            parent : UI,
            gasketName : "drange",
            assist : {
                icon   : '<i class="zmdi zmdi-calendar"></i>',
                uiType : 'ui/form/c_date_range',
            }
        }).render(function(){
            UI.defer_report("drange");
        });

        // 日期视图
        new TabsUI({
            parent : UI,
            gasketName : "date",
            ddefaultKey : "monthly",
            setup : {
                "weekly" : {
                    text : "周",
                    uiType : 'ui/zcron/support/zcr_weekly',
                    uiConf : {}
                },
                "monthly" : {
                    text : "月",
                    uiType : 'ui/form/c_array',
                    uiConf : {
                        items : function(){
                            var re = [];
                            for(var i=1; i<=31; i++)
                                re.push(i);
                            return re;
                        },
                        groupSize : 7,
                        on_change : function(v){
                            console.log(v);
                        }
                    }
                }
            }
        }).render(function(){
            UI.defer_report("date");
        });

        // 时间视图
        new TabsUI({
            parent : UI,
            gasketName : "time",
            setup : {
                "hms" : {
                    text : "时分秒",
                    uiType : 'ui/support/dom',
                    uiConf : {
                        html : "I am HMS"
                    }
                },
                "repeat" : {
                    text : "重复",
                    uiType : 'ui/support/dom',
                    uiConf : {
                        html : "I am Repeat"
                    }
                },
                "pick" : {
                    text : "挑选时间点",
                    uiType : 'ui/support/dom',
                    uiConf : {
                        html : "I am pick"
                    }
                }
            }
        }).render(function(){
            UI.defer_report("time");
        });

        // 返回延迟加载
        return ["drange", "date", "time"];
    },
    //...............................................................
    getCron : function(){

    },
    //...............................................................
    _set_data : function(){

    },
    //...............................................................
    _get_data : function(){

    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);