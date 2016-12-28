(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'app/wn.hmaker2/support/hm__methods_panel',
    'app/wn.hmaker2/hm_prop_edit_block',
    'app/wn.hmaker2/hm_prop_edit_com',
], function(ZUI, Wn, MenuUI, 
    HmMethods,
    EditBlockUI,
    EditComUI){
//==============================================
var html = `
<div class="ui-arena hm-prop-edit" ui-fitparent="yes">
    <div class="hm-prop-head">
        <div class="hm-com-info"></div>
    </div>
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
        // 切换标签
        'click .hm-prop-tabs li[ptype]' : function(e) {
            this.switchTab($(e.currentTarget).attr("ptype"));
        },
        // 修改 comID
        "click .hm-prop-head .hm-com-info em" : function(e){
            //alert($(e.currentTarget).text())
            var UI = this;
            $z.editIt(e.currentTarget, function(newval, oldval, jEle){
                var comNewId = $.trim(newval);
                if(comNewId != oldval) {
                    //console.log("change com ID", comNewId);
                    // 修改接口
                    if(UI.uiCom.setComId(comNewId)){
                        // 通知更新
                        UI.uiCom.notifyActived();
                        // 修改显示
                        jEle.text(comNewId);
                    }
                }
            });
        },
        // 显示皮肤选择器
        "click .hm-skin-box" : function(e) {
            e.stopPropagation();
            var UI   = this;
            var jBox = $(e.currentTarget);

            // 得到可用皮肤列表
            var ctype = UI.uiCom.getComType();
            var skinList = UI.getSkinListForCom(ctype);

            UI.showSkinList(jBox, skinList, function(skin){
                UI.uiCom.setComSkin(skin);
            });
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
        //console.log("hm_prop_edit->doActiveOther:");
        // this.gasket.com.showBlank();
    },
    //...............................................................
    doActiveCom : function(uiCom) {
        var UI = this;

        // 保存实例
        UI.uiCom = uiCom;

        // 得到组件信息
        var comId = uiCom.getComId();
        var ctype = uiCom.getComType();

        // 准备显示的 HTML
        var html = '<span>' + UI.msg("hmaker.com."+ctype+".icon") + '</span>';
        html += '<b>' + UI.msg("hmaker.com."+ctype+".name") + '</b>';
        html += '<em>' + comId + '</em>';

        // 设置标题
        UI.arena.find('>.hm-prop-head>.hm-com-info').html(html);
    },
    //...............................................................
    doChangeBlock : function(mode, uiCom, block) {
        if("panel" == mode)
            return;
        //console.log("hm_prop_edit::doChangeBlock:", mode, uiCom.uiName);
        this.gasket.block.update(uiCom, block);
    },
    //...............................................................
    doChangeCom : function(mode, uiCom, com) {
        if("panel" == mode)
            return;
        //console.log("hm_prop_edit::doChangeCom:", mode, uiCom.uiName);
        // 执行更新
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