(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing/support/th_methods',
    'ui/thing/th_search',
    'ui/thing/th_obj',
], function(ZUI, Wn, DomUI, ThMethods, ThSearchUI, ThObjUI){
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
        $z.invoke(this.gasket.search, "_fill_context", [uiSet]);
        $z.invoke(this.gasket.obj   , "_fill_context", [uiSet]);
    },
    //..............................................
    update : function(oDir, callback) {
        var UI  = this;
        var opt = UI.options;

        // console.log(opt)

        // TODO 这里根据 oDir 来决定动态的配置信息

        // 准备延迟加载项
        UI.defer(["search", "blank"], function(){
            UI.gasket.search.update(oDir, function(){
                $z.doCallback(callback, [oDir], UI);
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

        // 默认显示空白
        UI.showBlank(function(){
            UI.defer_report("blank");
        });
    },
    //..............................................
    showObj : function(o, callback) {
        var UI = this;

        // 如果已经是对象加载器了，直接更新
        if(UI.gasket.obj.uiName == "ui.th_obj") {
            UI.gasket.obj.update(o, function(){
                $z.doCallback(callback, [o], UI);
            });
        }
        // 加载对象编辑器
        else {
            new ThObjUI({
                parent : UI,
                gasketName : "obj",
                bus : UI,
            }).render(function(){
                this.update(o, function(){
                    $z.doCallback(callback, [o], UI);
                });
            });
        }
    },
    //..............................................
    showBlank : function(callback) {
        var UI = this;

        // 替换掉索引项
        new DomUI({
            parent : UI,
            gasketName : "obj",
            dom : `<div class="th-obj-blank">
                <i class="fa fa-hand-o-left"></i>
                {{thing.blank}}
            </div>`
        }).render(function(){
            $z.doCallback(callback, [], UI);
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);