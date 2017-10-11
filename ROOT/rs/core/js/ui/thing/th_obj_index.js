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
    <header>
        <div class="toi-tabs"></div>
        <div class="toi-menu" ui-gasket="menu"></div>
    </header>
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
    redraw : function(){
        var UI   = this;
        var conf = UI.getBusConf();
        var jTabs = UI.arena.find(">header>.toi-tabs");
        
        console.log("draw index", conf);

        // 准备对象分类信息
        if(conf.detail) {
            jTabs.show().html(UI.compactHTML(`
                <ul>
                <li m="meta">{{thing.meta}}</li>
                <li m="detail">{{thing.detail}}</li>
                </ul>
            `));
        }
        // 那么就仅有元数据咯
        else {
            jTabs.hide();
        }

        // TODO: 准备对象菜单

        // 创建元数据界面

        // 创建详情界面
    },
    //..............................................
    _fill_context : function(uiSet) {
        uiSet.index = this;
    },
    //..............................................
    update : function(o, callback) {
        var UI  = this;
        var bus = UI.bus();
        //console.log("update index", o);
        
        
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);