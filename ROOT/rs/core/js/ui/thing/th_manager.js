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
    //..............................................
    init : function(opt){
        ThMethods(this);
        this.__init_conf(opt);
    },
    //..............................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 提出子控件需要的配置信息
        var conf = UI.getConf();

        // 加载搜索器
        new ThSearchUI(_.extend({}, conf, {
            parent : UI,
            gasketName : "search",
            bus : UI,
        })).render(function(){
            UI.defer_report("search");
        });

        // 加载对象编辑器
        new ThObjUI(_.extend({}, conf, {
            parent : UI,
            gasketName : "obj",
            bus : UI,
        })).render(function(){
            UI.defer_report("obj");
        });

        // 返回延迟加载
        return ["search", "obj"];
    },
    //..............................................
    update : function(o) {
        this.gasket.search.update(o);
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);