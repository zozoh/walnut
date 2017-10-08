(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing/support/th_methods',
    'ui/thing/th_search',
    'ui/thing/th_obj',
], function(ZUI, Wn, ThMethods, ThSearchUI, ThObjUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-manager" ui-fitparent="true">
    <div class="th-search-con" ui-gasket="search"></div>
    <div class="th-obj-con" ui-gasket="obj"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.th_manager", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        ThMethods(this);
    },
    //..............................................
    _fill_context : function(uiSet) {
        uiSet.manager = this;
        this.gasket.search._fill_context(uiSet);
        this.gasket.obj._fill_context(uiSet);
    },
    //..............................................
    update : function(o, callback) {
        var UI  = this;
        var opt = UI.options;

        console.log(opt)

        // TODO 这里根据 o 来决定动态的配置信息

        // 准备延迟加载项
        UI.defer(["search","obj"], function(){
            UI.gasket.search.update(o, function(){
                $z.doCallback(callback, [o], UI);
            });
        });

        // 加载搜索器
        new ThSearchUI({
            parent : UI,
            gasketName : "search",
            bus : UI,
        }).render(function(){
            UI.defer_report("search");
        });

        // 加载对象编辑器
        new ThObjUI({
            parent : UI,
            gasketName : "obj",
            bus : UI,
        }).render(function(){
            UI.defer_report("obj");
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);