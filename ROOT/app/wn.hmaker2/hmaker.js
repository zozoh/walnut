(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/hm_ui_methods',
    'app/wn.hmaker2/hm_panel_methods',
    'app/wn.hmaker2/hm_resource',
    'app/wn.hmaker2/hm_page',
    'app/wn.hmaker2/hm_page_prop',
    'app/wn.hmaker2/hm_folder',
    'app/wn.hmaker2/hm_unknown',
], function(ZUI, Wn, FormUI, SetupHmUI, SetupPanelUI, 
    HmResourceUI, 
    HmPageUI, HmPagePropUI,
    HmFolderUI, HmUnknownUI){
//==============================================
var html = function(){/*
<div class="ui-arena hmaker" ui-fitparent="yes">
    <div class="hm-con-main" ui-gasket="main"></div>
    <div class="hm-con-resource" ui-gasket="resource"></div>
    <div class="hm-con-prop" ui-gasket="prop"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker2", {
    __hmaker__ : "1.0",
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.hmaker2/hmaker.css",
    i18n : "app/wn.hmaker2/i18n/{{lang}}.js",
    //...............................................................
    init : function() {
        var UI = this;
        
        UI.listenSelf("rs:actived", function(o){
            UI.changeMain(o);
        });
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        
        SetupPanelUI(new HmResourceUI({
            parent : UI,
            gasketName : "resource"
        })).render(function(){
            UI.defer_report("resource");
        });

        return ["resource"];
    },
    //...............................................................
    update : function(o) {
        this.gasket.resource.update(o);
    },
    //...............................................................
    changeMain : function(o) {
        var UI = this;

        var MainUI, PropUI;

        // 如果是文件夹，那么显示 FolderUI
        if('DIR' == o.race) {
            MainUI = HmFolderUI;
        }
        // 如果是网页，显示 PageUI
        else if(/^text\/html$/.test(o.mime)){
            MainUI = HmPageUI;
            PropUI = HmPagePropUI;
        }
        // 其他的显示错误的 UI
        else {
            MainUI = HmUnknownUI;
            PropUI = HmPagePropUI;
        }

        // 首先加载属性
        SetupPanelUI(new PropUI({
            parent : UI,
            gasketName : "prop"
        })).render(function(){
            SetupHmUI(new MainUI({
                parent : UI,
                gasketName : "main"
            })).render(function(){
                this.update(o);
            });
        });
    },
    //...............................................................
    getCurrentEditObj : function() {
        return $z.invoke(this.gasket.main, "getCurrentEditObj", []);
    },
    //...............................................................
    getCurrentTextContent : function() {
        return $z.invoke(this.gasket.main, "getCurrentTextContent", []);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);