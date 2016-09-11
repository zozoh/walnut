(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'app/wn.hmaker2/hm__methods',
    'app/wn.hmaker2/hm__methods_panel',
    'app/wn.hmaker2/hm_resource',
    'app/wn.hmaker2/hm_page',
    'app/wn.hmaker2/hm_prop',
    'app/wn.hmaker2/hm_folder',
    'app/wn.hmaker2/hm_other',
], function(ZUI, Wn, FormUI, 
    HmMethods, HmPanelMethods, 
    HmResourceUI, 
    HmPageUI, 
    HmPropUI,
    HmFolderUI, 
    HmOtherUI){
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
        var UI = HmMethods(this);
        
        UI.listenSelf("active:rs", function(o){
            UI.changeMain(o);
        });
    },
    //...............................................................
    redraw : function(){
        var UI  = this;

        UI.showLoading();
        
        // 资源面板
        HmPanelMethods(new HmResourceUI({
            parent : UI,
            gasketName : "resource"
        })).render(function(){
            UI.defer_report("resource");
        });

        // 属性面板
        HmPanelMethods(new HmPropUI({
            parent : UI,
            gasketName : "prop"
        })).render(function(){
            UI.defer_report("prop");
        });

        // 返回延迟加载
        return ["resource", "prop"];
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        UI.__home_id = o.id;
        UI.gasket.resource.update(o, function(){
            UI.hideLoading();
        });
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
        }
        // 其他的显示错误的 UI
        else {
            MainUI = HmOtherUI;
        }

        // 加载主界面
        HmMethods(new MainUI({
            parent : UI,
            gasketName : "main"
        })).render(function(){
            // 更新菜单
            var actions = $z.invoke(this, "getActions") || [];
            var menuSetup = Wn.extendActions(actions, false, true);
            UI.browser.updateMenu(menuSetup, this);

            // 更新主界面
            this.update(o);
        });
    },
    //...............................................................
    openCreatePanel : function() {
        var UI   = this;
        var oHome = UI.getHomeObj();

        // 显示新建文件对象面板
        Wn.createPanel(oHome, function(newObj){
            UI.resourceUI().refresh(function(){
                this.setActived(newObj.id);
            });
        }, [{
            race : "FILE",
            tp   : "html",
            text : "i18n:hmaker.html",
            tip  : "i18n:hmaker.html_tip",
        }, {
            race : "DIR",
            tp   : "folder",
            text : "i18n:hmaker.folder",
            tip  : "i18n:hmaker.folder_tip",
        }]);
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