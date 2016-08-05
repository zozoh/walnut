(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'app/wn.hmaker2/hm__methods',
    'app/wn.hmaker2/hm_prop_edit_block',
    'app/wn.hmaker2/hm_prop_edit_com',
], function(ZUI, Wn, MenuUI, 
    HmMethods,
    EditBlockUI,
    EditComUI){
//==============================================
var html = function(){/*
<div class="ui-arena hm-prop-edit" ui-fitparent="yes">
    <div class="hm-prop-tabs">
        <ul class="hm-W">
            <li ptype="block"><%=hmaker.prop.tab_block%></li>
            <li ptype="com"><%=hmaker.prop.tab_com%></li>
        </ul>
    </div>
    <div class="hm-prop-body"><div class="hm-W">
        <div class="hm-prop-con" ptype="block" ui-gasket="block"></div>
        <div class="hm-prop-con" ptype="com"   ui-gasket="com"></div>
    </div></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_prop_edit", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = HmMethods(this);

        UI.listenBus("active:block",  UI.activeBlock);
        UI.listenBus("change:block",  UI.changeBlock);
        UI.listenBus("change:com",    UI.changeCom);
        UI.listenBus("hide:com",      UI.hideCom);
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
        ptype = ptype || "com";

        UI.arena.find('.hm-prop-tabs li').removeAttr("current")
            .filter('[ptype="'+ptype+'"]').attr("current", "yes");

        UI.arena.find('.hm-prop-con').removeAttr("current")
            .filter('[ptype="'+ptype+'"]').attr("current", "yes");

        UI.resize(true);
    },
    //...............................................................
    activeBlock : function(jBlock) {
        var UI = this;
        var uiPage = UI.pageUI();

        // 得到 Block 的属性
        var prop = uiPage.getBlockProp(jBlock, true);

        // 更新
        UI.changeBlock(prop);
    },
    //...............................................................
    changeBlock : function(prop) {
        this.gasket.block.update(prop);
    },
    //...............................................................
    changeCom : function(com) {
        console.log("edit> change:com", com);
        this.gasket.com.update(com);
    },
    //...............................................................
    drawCom : function(uiDef, callback) {
        this.gasket.com.drawCom(uiDef, callback);
    },
    //...............................................................
    hideCom : function() {
        this.gasket.com.showBlank();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);