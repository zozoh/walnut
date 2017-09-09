(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing/support/th_methods',
    'ui/search/search',
], function(ZUI, Wn, ThMethods, SearchUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-manager" ui-fitparent="true"
    ui-gasket="main"></div>
*/};
//==============================================
return ZUI.def("ui.th_search", {
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
        var conf = UI.getConf().search || {};

        // 开始分析设置配置项
        conf.fields = opt.fields;

        // 加载搜索器
        new SearchUI(_.extend({}, conf, {
            parent : UI,
            gasketName : "main",
        })).render(function(){
            UI.defer_report("main");
        });

        // 返回延迟加载
        return ["main"];
    },
    //..............................................
    update : function(o) {
        
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);