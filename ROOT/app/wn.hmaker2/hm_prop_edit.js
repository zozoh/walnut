(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'app/wn.hmaker2/support/hm__methods',
    'app/wn.hmaker2/hm_prop_edit_block',
    'app/wn.hmaker2/hm_prop_edit_com',
], function(ZUI, Wn, MenuUI, 
    HmMethods,
    EditBlockUI,
    EditComUI){
//==============================================
var html = `
<div class="ui-arena hm-prop-edit" ui-fitparent="yes">
    <div class="hm-prop-tabs">
        <ul class="hm-W">
            <li ptype="block"><%=hmaker.prop.tab_block%></li>
            <li ptype="com"><%=hmaker.prop.tab_com%></li>
        </ul>
    </div>
    <div class="hm-prop-body">
        <div class="hm-W">
            <div class="hm-prop-con" ptype="block" ui-gasket="block"></div>
            <div class="hm-prop-con" ptype="com"   ui-gasket="com"></div>
        </div>
    </div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_prop_edit", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);

        UI.listenBus("active:page",   UI.doActiveOther);
        UI.listenBus("active:folder", UI.doActiveOther);
        UI.listenBus("active:rs",     UI.doActiveOther);
        UI.listenBus("active:other",  UI.doActiveOther);
        UI.listenBus("active:com",    UI.doActiveCom);
        
        UI.listenBus("change:block",  UI.doChangeBlock);
        UI.listenBus("change:com",    UI.doChangeCom);
    },
    //...............................................................
    events : {
        'click .hm-prop-tabs li[ptype]' : function(e) {
            this.switchTab($(e.currentTarget).attr("ptype"));
        }
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 确保有一个标签被选中
        UI.switchTab();

        // 块元素的属性编辑器
        new EditBlockUI({
            parent : UI,
            gasketName : "block"
        }).render(function(){
            UI.defer_report("block");
        });

        // 控件的属性编辑器
        new EditComUI({
            parent : UI,
            gasketName : "com"
        }).render(function(){
            UI.defer_report("com");
        });

        // 返回延迟加载
        return ["block", "com"];
    },
    //...............................................................
    switchTab : function(ptype) {
        var UI = this;
        ptype = ptype || UI.local("hm_prop_edit_tab") || "block";

        // 记录状态
        UI.local("hm_prop_edit_tab", ptype);

        UI.arena.find('.hm-prop-tabs li').removeAttr("current")
            .filter('[ptype="'+ptype+'"]').attr("current", "yes");

        UI.arena.find('.hm-prop-con').removeAttr("current")
            .filter('[ptype="'+ptype+'"]').attr("current", "yes");

        UI.resize(true);
    },
    //...............................................................
    doActiveOther : function(){
        console.log("hm_prop_edit->doActiveOther:");
        // this.gasket.com.showBlank();
    },
    //...............................................................
    doActiveCom : function(uiCom) {
        console.log("hm_prop_edit->doActiveCom:", uiCom.uiName);
    },
    //...............................................................
    doChangeBlock : function(mode, uiCom, block) {
        if("panel" == mode)
            return;
        console.log("hm_prop_edit::doChangeBlock:", mode, uiCom.uiName);
        this.gasket.block.update(uiCom, block);
    },
    //...............................................................
    doChangeCom : function(mode, uiCom, com) {
        if("panel" == mode)
            return;
        console.log("hm_prop_edit::doChangeCom:", mode, uiCom.uiName);
        this.gasket.com.update(uiCom, com);
    },
    //...............................................................
    resize : function() {
        var UI  = this;
        var jCE = UI.arena.find('.hm-prop-com-ele');
        var W   = UI.arena.outerWidth();
        jCE.css({
            "width" : W,
            "left"  : jCE.attr("show") ? 0 : W
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);