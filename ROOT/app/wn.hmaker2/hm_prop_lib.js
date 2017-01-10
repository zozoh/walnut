(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
], function(ZUI, Wn, HmMethods){
//==============================================
var html = `
<div class="ui-arena hm-prop-lib" ui-fitparent="yes">
    <div class="hpl-info">
        <header>
            <h5>{{hmaker.lib.title}}</h5>
            <div><%=hmaker.lib.icon%></div>
        </header>
        <section hm-inner-html="app/wn.hmaker2/i18n/{{lang}}/help_lib.html"></section>
    </div>
    <div class="hpl-item">
        <header>
            <h5>{{hmaker.lib.item}}</h5>
            <div><%=hmaker.lib.icon_item%></div>
        </header>
    </div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_prop_lib", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);

        UI.listenBus("active:libItem", UI.showLibItem);
        UI.listenBus("active:lib",     UI.showHelp);
        UI.listenBus("blur:libItem",   UI.showHelp);
    },
    //...............................................................
    redraw : function() {
        var UI = this;
    },
    //...............................................................
    showLibItem : function(o) {
        var UI = this;
        //console.log("showLibItem", o);

        // 显示对应区块
        var jDiv = UI.arena.children('div').removeAttr("show")
            .filter(".hpl-item").attr("show","yes");
        
        // 显示组件信息
        jDiv.find(">header>h5").text(o.nm);
    },
    //...............................................................
    showHelp : function() {
        var UI = this;
        console.log("showHelp");

        // 显示对应区块
        UI.arena.children('div').removeAttr("show")
            .filter(".hpl-info").attr("show","yes");
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);