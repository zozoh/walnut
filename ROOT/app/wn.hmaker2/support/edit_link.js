(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/tabs/tabs',
], function(ZUI, Wn, TabsUI){
//==============================================
var html = `
<div class="ui-arena edit-link" ui-fitparent="yes" ui-gasket="main"></div>`;
//==============================================
return ZUI.def("ui.edit_link", {
    dom  : html,
    css  : 'app/wn.hmaker2/support/theme/hmaker_support-{{theme}}.css',
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 建立主界面
        new TabsUI({
            parent : UI,
            gasketName : "main",
            //defaultKey : "asAction",
            setup : {
                "asHref" : {
                    text : "超链接",
                    uiType : 'app/wn.hmaker2/support/edit_link_href',
                    uiConf : opt
                },
                "asAction" : {
                    text : "动作库",
                    uiType : 'app/wn.hmaker2/support/edit_link_action',
                    uiConf : opt
                },
            }
        }).render(function(){
            UI.defer_report("main");
        });

        // 返回延迟加载
        return ["main"];
    },
    //...............................................................
    getData : function() {
        return this.gasket.main.getCurrentUI().getData();
    },
    //...............................................................
    setData : function(href) {
        this.gasket.main.getCurrentUI().setData(href);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);