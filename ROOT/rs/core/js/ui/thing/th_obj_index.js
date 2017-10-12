(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing/support/th_methods',
    'ui/thing/th_obj_index_meta',
    'ui/thing/th_obj_index_detail',
], function(ZUI, Wn, DomUI, ThMethods, ThObjMetaUI, ThObjDetailUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj-index" ui-fitparent="true">
    <header><div>
        <div class="toi-tabs"></div>
        <div class="toi-menu" ui-gasket="menu"></div>
    </div></header>
    <section ui-gasket="main"></section>
</div>
*/};
//==============================================
return ZUI.def("ui.th_obj_index", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        ThMethods(this);
    },
    //..............................................
    events : {
        'click .toi-tabs li[m]' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            if('meta' == jq.attr("m")) {
                UI.showMeta();
            } else {
                UI.showDetail();
            }
        }
    },
    //..............................................
    redraw : function(){
        var UI   = this;
        var conf = UI.getBusConf();
        var jTabs = UI.arena.find(">header .toi-tabs");
        
        console.log("draw index", conf);

        // 都有
        if(conf.detail && conf.meta) {
            jTabs.show().html(UI.compactHTML(`
                <ul>
                <li m="meta">{{thing.meta}}</li>
                <li m="detail">{{thing.detail.title}}</li>
                </ul>
            `));
            UI.__show_main(function(){
                UI.defer_report("main");
            });
        }
        // 仅有内容
        else if(conf.detail) {
            jTabs.hide();
            UI.showDetail(function(){
                UI.defer_report("main");
            });
        }
        // 那么就仅有元数据咯
        else if(conf.meta) {
            jTabs.hide();
            UI.showMeta(function(){
                UI.defer_report("main");
            });
        }
        // 总得有点啥吧
        else {
            throw "not setup meta or detail!";
        }

        // 返回延迟加载
        return ["main"];
    },
    //..............................................
    _fill_context : function(uiSet) {
        uiSet.index = this;
    },
    //..............................................
    update : function(o, callback) {
        // 记录当前对象
        this.__OBJ = o;
        this.gasket.main.update(o, callback);
        // TODO 同时也要更新对象菜单吧
    },
    //..............................................
    __show_main : function(callback){
        var UI = this;
        // 显示主界面界面
        if("detail" == UI.local("current_tab")){
            UI.showDetail(callback);
        }
        // 默认显示元数据界面
        else {
            UI.showMeta(callback);
        }
    },
    //..............................................
    showMeta : function(callback) {
        var UI  = this;
        var bus = UI.bus();
        var jTabs = UI.arena.find(">header .toi-tabs");

        new ThObjMetaUI({
            parent : UI,
            gasketName : "main",
            bus : bus,
        }).render(function(){
            UI.local("current_tab", "meta");
            jTabs.find('li').removeAttr("current")
                .filter('[m="meta"]').attr("current", "yes");
            if(UI.__OBJ) {
                this.update(UI.__OBJ, callback);
            } else {
                $z.doCallback(callback, [this], UI);
            }
        });
    },
    //..............................................
    showDetail : function(callback) {
        var UI  = this;
        var bus = UI.bus();
        var jTabs = UI.arena.find(">header .toi-tabs");

        new ThObjDetailUI({
            parent : UI,
            gasketName : "main",
            bus : bus,
        }).render(function(){
            UI.local("current_tab", "detail");
            jTabs.find('li').removeAttr("current")
                .filter('[m="detail"]').attr("current", "yes");
            if(UI.__OBJ) {
                this.update(UI.__OBJ, callback);
            } else {
                $z.doCallback(callback, [this], UI);
            }
        });
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);