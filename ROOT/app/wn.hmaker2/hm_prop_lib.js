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
            <h5><b>{{hmaker.lib.item}}</b><a>{{hmaker.lib.rename}}</a></h5>
            <div><%=hmaker.lib.icon_item%></div>
            <h6>{{hmaker.lib.pages}}</h6>
        </header>
        <section></section>
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
    events : {
        "click .hpl-item > header > h5 > a" : function(){
            this.fire("rename:libItem");
        }
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
        var jPart = UI.arena.children('div').removeAttr("show")
            .filter(".hpl-item").attr("show","yes");
        
        // 显示组件信息
        jPart.find(">header>h5>b").text(o.nm);

        // 查询组件被使用的情况
        var oHome = UI.getHomeObj();
        var jList = jPart.find(">section").empty();
        Wn.execf("hmaker lib id:{{homeId}} -pages '{{libName}}' -l -e 'id|nm|ph|tp'", {
            homeId  : oHome.id,
            libName : o.nm
        }, function(re){
            var list = $z.fromJson(re);
            // 没有被引用
            if(list.length == 0) {
                jList.text(UI.msg("hmaker.lib.pages_none"));
            }
            // 列出被引用的项目
            else {
                var jUl = $('<ul>').appendTo(jList);
                for(var i=0; i<list.length; i++) {
                    var oRefer = list[i];
                    var jLi    = $('<li>').appendTo(jUl);
                    $('<em>').text(i+1).appendTo(jLi);
                    $(UI.getObjIcon(oRefer)).appendTo(jLi);
                    var rph = $z.getRelativePath(oHome.ph, oRefer.ph);
                    $('<b>').text(rph).appendTo(jLi);
                }
            }
        })
    },
    //...............................................................
    showHelp : function() {
        var UI = this;
        //console.log("showHelp");

        // 显示对应区块
        UI.arena.children('div').removeAttr("show")
            .filter(".hpl-info").attr("show","yes");
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);