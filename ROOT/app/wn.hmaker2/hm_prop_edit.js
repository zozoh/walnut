(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'app/wn.hmaker2/hm__methods',
    'app/wn.hmaker2/hm_prop_edit_block'
], function(ZUI, Wn, MenuUI, 
    HmMethods,
    EditBlockUI){
//==============================================
var html = function(){/*
<div class="ui-arena hm-prop-edit" ui-fitparent="yes">
    <div class="hm-prop-tabs">
        <ul class="hm-W">
            <li ptype="block"><%=hmaker.prop.tab_block%></li>
            <li ptype="area"><%=hmaker.prop.tab_area%></li>
            <li ptype="com"><%=hmaker.prop.tab_com%></li>
        </ul>
    </div>
    <div class="hm-prop-body"><div class="hm-W">
        <div class="hm-prop-con" ptype="block" ui-gasket="block"></div>
        <div class="hm-prop-con" ptype="area"  ui-gasket="area">现在还没啥可设置的，无视我吧</div>
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
        UI.listenBus("change:block",  UI.updateBlock);
        UI.listenBus("change:com",    UI.changeCom);
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

        // 返回延迟加载
        return ["block"];
    },
    //...............................................................
    switchTab : function(ptype) {
        var UI = this;
        ptype = ptype || "block";

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
        UI.updateBlock(prop);
    },
    //...............................................................
    updateBlock : function(prop) {
        this.gasket.block.update(prop);
    },
    //...............................................................
    changeCom : function(com) {
        console.log("edit> change:com", com);
    },
    //...............................................................
    drawCom : function(uiDef, callback) {
        var UI = this;
        // 先销毁
        if(UI.gasket.body)
            UI.gasket.body.destroy();

        // 没定义，就直接回调了
        if(!uiDef) {
            $z.doCallback(callback, [], UI);
        }
        // 设置
        else {
            seajs.use(uiDef.uiType, function(PropUI){
                new PropUI(_.extend({}, uiDef.uiConf||{}, {
                    parent : UI,
                    gasketName : "com"
                })).render(function(){
                    $z.doCallback(callback, [this], UI);
                });
            });
        }        
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);