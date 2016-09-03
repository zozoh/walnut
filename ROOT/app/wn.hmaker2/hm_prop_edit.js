(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'app/wn.hmaker2/hm__methods',
    'app/wn.hmaker2/hm_prop_edit_block',
    'app/wn.hmaker2/hm_prop_edit_com',
    'app/wn.hmaker2/hm_prop_edit_ele',
], function(ZUI, Wn, MenuUI, 
    HmMethods,
    EditBlockUI,
    EditComUI,
    EditEleUI){
//==============================================
var html = function(){/*
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
        <div class="hm-prop-com-ele"><div class="hm-W" ui-gasket="ele"></div></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hm_prop_edit", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = HmMethods(this);

        
        UI.listenBus("change:block",    UI.changeBlock);
        UI.listenBus("change:com",      UI.changeCom);
        UI.listenBus("change:com:ele",  UI.changeComEle);

        UI.listenBus("show:com:ele",  UI.showComEle);

        UI.listenBus("hide:com",      UI.hideCom);
        UI.listenBus("hide:com:ele",  UI.hideComEle);

        UI.listenBus("active:block",  UI.activeBlock);
        UI.listenBus("active:page",   UI.hideComEle);
        UI.listenBus("active:folder", UI.hideComEle);
        UI.listenBus("active:rs",     UI.hideComEle);
        UI.listenBus("active:other",  UI.hideComEle);
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

        // 控件内元素扩展编辑面板
        new EditEleUI({
            parent : UI,
            gasketName : "ele"
        }).render(function(){
            UI.defer_report("ele");
        });

        // 返回延迟加载
        return ["block", "com", "ele"];
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
    activeBlock : function(jBlock) {
        var UI = this;
        var uiPage = UI.pageUI();

        // 得到 Block 的属性
        var prop = uiPage.getBlockProp(jBlock);

        // 更新
        UI.changeBlock(prop, true);
    },
    //...............................................................
    changeBlock : function(prop, full) {
        if(prop && prop.__prop_ignore_update)
            return;
        this.gasket.block.update(prop, full);
    },
    //...............................................................
    changeCom : function(com) {
        //console.log("edit> change:com", com);
        // 直接无视吧
        if(com && com.__prop_ignore_update)
            return;
        this.gasket.com.update(com);
    },
    //...............................................................
    changeComEle : function(ele) {
        //console.log("edit> change:ele", ele);
        this.gasket.ele.update(ele);
    },
    //...............................................................
    drawCom : function(uiDef, callback) {
        this.gasket.com.draw(uiDef, callback);
    },
    //...............................................................
    hideCom : function() {
        this.gasket.com.showBlank();
    },
    //...............................................................
    drawComEle : function(uiDef, callback) {
        this.gasket.ele.draw(uiDef, callback);
    },
    //...............................................................
    showComEle : function() {
        this.arena.find('.hm-prop-com-ele').attr("show","yes");
        this.resize(true);
    },
    //...............................................................
    hideComEle : function() {
        this.arena.find('.hm-prop-com-ele').removeAttr("show");
        this.resize(true);
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