(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'app/wn.hmaker2/support/hm__methods_panel',
    'app/wn.hmaker2/hm_prop_folder',
    'app/wn.hmaker2/hm_prop_lib',
    'app/wn.hmaker2/hm_prop_page',
    'app/wn.hmaker2/hm_prop_edit',
    'app/wn.hmaker2/hm_prop_other',
], function(ZUI, Wn, MenuUI, 
    HmPanelMethods,
    PropFolderUI,
    PropLibUI,
    PropPageUI,
    PropEditUI,
    PropOtherUI){
//==============================================
var html = function(){/*
<div class="ui-arena hm-panel hm-prop" ui-fitparent="yes">
    <header>
        <ul class="hm-W">
            <li class="hmpn-tt"><i class="zmdi zmdi-settings"></i> {{hmaker.prop.title}}</li>
            <li class="hmpn-opt" ui-gasket="opt"></li>
            <li class="hmpn-pin"><i class="fa fa-thumb-tack"></i></li>
        </ul>
    </header>
    <section class="hm-prop-form" ui-gasket="form"></section>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_prop", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "app/wn.hmaker2/theme/hmaker_prop-{{theme}}.css",
    //...............................................................
    init : function() {
        var UI = HmPanelMethods(this);
        
        
        UI.listenBus("active:folder", UI.onActiveFolder);
        UI.listenBus("active:lib",    UI.onActiveLib);
        UI.listenBus("active:page",   UI.onActivePage);
        UI.listenBus("active:com",    UI.onActiveCom);
        UI.listenBus("active:other",  UI.onActiveOther);
    },
    //...............................................................
    onActiveFolder : function(o){
        var UI = this;
        new PropFolderUI({
            parent : UI,
            gasketName : "form"
        }).render(function(){
            this.oFolder = o;
            this.gasket.upload.setTarget(o);
            this.do_active_file(this.oFolder);
        });
    },
    //...............................................................
    onActiveLib : function(){
        var UI = this;
        new PropLibUI({
            parent : UI,
            gasketName : "form"
        }).render(function(){
            this.showHelp();
        });
    },
    //...............................................................
    onActiveCom : function(uiCom) {
        var UI = this;
        if(!UI.gasket.form || 'app.wn.hm_prop_edit' != UI.gasket.form.uiName) {
            new PropEditUI({
                parent : UI,
                gasketName : "form"
            }).render(function(){
                this.doActiveCom(uiCom);
            });
        }
        // 否则直接来一下
        else {
            UI.gasket.form.doActiveCom(uiCom);
        }
    },
    //...............................................................
    onActivePage : function(){
        var UI = this;
        // if(UI.gasket.form)
        //     console.log("onActivePage", UI.gasket.form.uiName)
        // 已经是 PageUI 了
        if(UI.gasket.form && 'app.wn.hm_prop_page' == UI.gasket.form.uiName) {
            UI.gasket.form.refresh();
        }
        // 不是的话，搞一个新的
        else {
            new PropPageUI({
                parent : UI,
                gasketName : "form"
            }).render(function(){
                this.refresh();
            });
        }
    },
    //...............................................................
    onActiveOther : function(o){
        var UI = this;
        new PropOtherUI({
            parent : UI,
            gasketName : "form"
        }).render(function(){
            this.gasket.meta.update(o);
        });
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);