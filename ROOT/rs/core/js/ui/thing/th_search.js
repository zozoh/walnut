(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing/support/th_methods',
    'ui/search/search',
], function(ZUI, Wn, ThMethods, SearchUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-search" ui-fitparent="true"
    ui-gasket="main"></div>
*/};
//==============================================
return ZUI.def("ui.th_search", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        var UI = ThMethods(this);

        UI.listenBus("change:meta", UI.setObj);
    },
    //..............................................
    _fill_context : function(uiSet) {
        uiSet.search = this;
    },
    //..............................................
    update : function(o, callback) {
        var UI  = this;
        var opt = UI.options;
        var bus = UI.bus();

        // 保存 HomeObj
        bus.setHomeObj(o);

        // 提出子控件需要的配置信息
        var conf = UI.getBusConf();
        //console.log("search", conf)

        // 加载搜索器
        new SearchUI(_.extend(conf, {
            parent : UI,
            gasketName : "main",
            menu : conf.searchMenu,
            menuContext : bus,
            data : function(params, callback){
                $z.invoke(conf.actions, "query", [_.extend(params, {
                    pid : bus.getHomeObjId()
                }), callback], bus);
            },
            filter : conf.searchFilter,
            list   : _.extend({}, conf.searchList, {
                fields : conf.fields,
                on_actived : function(th, jRow, prevObj) {
                    console.log("on_actived");
                    $z.invoke(bus, "showObj", [th]);
                },
                on_blur : function(objs, jRows, nextObj) {
                    if(!nextObj){
                        console.log("on_blur");
                        $z.invoke(bus, "showBlank");
                    }
                }
            }),
            pager : conf.searchPager,
        })).render(function(){
            this.refresh(function(){
                this.uiList.setActived(0);
                var args = Array.from(arguments);
                $z.doCallback(callback, args, UI.bus());
            });
        });
    },
    //..............................................
    setObj : function(obj) {
        console.log("i am setObj")
        this.gasket.main.uiList.update(obj);
    },
    //..............................................
    refresh : function(callback) {
        this.gasket.main.refresh(callback);
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);