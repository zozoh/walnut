(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'app/wn.hmaker2/hm_ui_methods',
    'app/wn.hmaker2/hm_page_prop_block'
], function(ZUI, Wn, MenuUI, 
    SetupHmUI,
    HmPagePropBlockUI){
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
    <section>
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
            <div class="hm-prop-con" ptype="com"   ui-gasket="com">I am com</div>
        </div></div>
    </section>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_page_prop", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function() {
        var UI = this;
        UI.listenParent("block:actived", UI.activeBlock);
        UI.listenParent("block:change",  UI.updateBlock);
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
        SetupHmUI(new HmPagePropBlockUI({
            parent : UI,
            gasketName : "block"
        })).render(function(){
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
    updateCom : function(uiDef) {
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
                new PropUI(_.extend({}, uiConf||{}, {
                    parent : UI,
                    gasketName : "body"
                })).render(function(){
                    // 回调
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