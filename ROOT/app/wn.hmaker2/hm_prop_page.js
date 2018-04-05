(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/tabs/tabs',
    'app/wn.hmaker2/support/hm__methods_panel',
], function(ZUI, Wn, TabsUI, HmMethods){
//==============================================
var html = '<div class="ui-arena hm-prop-page" ui-fitparent="yes" ui-gasket="tabs"></div>';
//==============================================
return ZUI.def("app.wn.hm_prop_page", {
    dom  : html,
    //...............................................................
    init : function() {
        HmMethods(this);
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 得到最后一次的 UI Key
        var lastKey = UI.local("hm_prop_page_last_tab") || "meta";

        // 页面设置
        new TabsUI({
            parent : UI,
            gasketName : "tabs",
            on_changeUI : function(key, subUI){
                UI.local("hm_prop_page_last_tab", key);
                $z.invoke(subUI, "refresh");
            },
            defaultKey : lastKey,
            setup : {
                "meta" : {
                    text : 'i18n:hmaker.page.setup',
                    uiType : 'app/wn.hmaker2/hm_prop_page_meta',
                    uiConf : {}
                },
                "skin" : {
                    text : 'i18n:hmaker.page.skin',
                    uiType : 'app/wn.hmaker2/hm_prop_page_skin',
                    uiConf : {}
                }
            }
        }).render(function(){
            UI.defer_report("tabs");
        });

        // 返回延迟加载
        return ["tabs"];
    },
    //...............................................................
    refresh : function(){
        // var UI = this;
        // if(UI.gasket.tabs) {
        //     var tabMainUI = UI.gasket.tabs.getCurrentUI();
        //     $z.invoke(tabMainUI, "refresh");
        // }
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);