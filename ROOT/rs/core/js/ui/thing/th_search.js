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
    },
    //..............................................
    update : function(o, callback) {
        var UI  = this;
        var opt = UI.options;

        // 提出子控件需要的配置信息
        var conf = UI.getBusConf("bus,search,fields,searchMenu,actions");

        // 加载搜索器
        new SearchUI(_.extend(conf, {
            parent : UI,
            gasketName : "main",
        })).render(function(){
            $z.doCallback(callback, [], UI.bus());
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);