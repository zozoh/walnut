(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods',
    'ui/list/list',
    'ui/o_edit_text/o_edit_text',
    'ui/support/dom'
], function(ZUI, Wn, HmMethods, ListUI, EditTextUI, DomUI){
//==============================================
var html = `
<div class="ui-arena hm-lib" ui-fitparent="yes">
    <header>
        <span class="lib"><%=hmaker.lib.icon%><b>{{hmaker.lib.title}}</b></span>
        <span class="item"></span>
    </header>
    <nav ui-gasket="list"></nav>
    <section ui-gasket="code"></section>
</div>
`;
//==============================================
return ZUI.def("app.wn.hmaker_lib", {
    dom : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 显示库列表
        new ListUI({
            parent : UI,
            gasketName : "list",
            escapeHtml : false,
            icon : UI.msg("hmaker.lib.icon_item"),
            text : function(o) {
                return '<b>' + o.nm + '</b>';
            },
            on_actived : function(o){
                UI._show_libItem(o);
                UI.arena.find(">header>.item").html(
                    UI.msg("hmaker.lib.icon_item") + '<b>' + o.nm + '</b>'
                );
                UI.fire("active:libItem", o);
            },
            on_blur : function(jItems, nextObj, nextItem){
                if(!nextObj){
                    UI._show_blank();
                    UI.arena.find(">header>.item").empty();
                    UI.fire("blur:libItem", nextObj);
                }
            }
        }).render(function(){
            UI.defer_report("list");
        });

        // 显示空白
        UI._show_blank();

        // 返回以便延迟加载
        return ["list"];
    },
    //...............................................................
    _show_libItem : function(o) {
        var UI = this;
        // 加载代码编辑器
        if("ui.o_edit_text" != UI.gasket.code.uiName) {
            new EditTextUI({
                parent : UI,
                gasketName : "code",
                showMeta : false,
            }).render(function(){
                this.update(o);
            });
        }
        // 直接更新代码编辑器
        else {
            UI.gasket.code.update(o);
        }
    },
    //...............................................................
    _show_blank : function(){
        var UI = this;
        // 显示代码编辑器
        new DomUI({
            parent : UI,
            gasketName : "code",
            dom : `<div class="blank-tip">
                <i class="fa fa-hand-o-left"></i>
                <em>{{hmaker.lib.blank}}</em>
            </div>`
        }).render();
    },
    //...............................................................
    update : function(o) {
        var UI = this;

        // 通知其他部分激活某个对象的属性
        UI.fire("active:lib");

        // 更新显示对象 
        UI.refresh();
    },
    //...............................................................
    refresh : function(){
        var UI = this;
        
        // 准备命令
        var cmdText = 'hmaker lib id:'+UI.getHomeObjId()+' -list obj';
        //console.log(cmdText);

        // 更新显示对象 
        UI.showLoading();
        Wn.exec(cmdText, function(re){
            UI.hideLoading();

            var list = $z.fromJson(re);
            //console.log(list)
            UI.gasket.list.setData(list);
        });
    },
    //...............................................................
    getCurrentEditObj : function(){
        return $z.invoke(this.gasket.code, "getCurrentEditObj", []);
    },
    //...............................................................
    getCurrentTextContent : function(){
        return $z.invoke(this.gasket.code, "getCurrentTextContent", []);
    },
    //...............................................................
    getActions : function(){
        return ["@::refresh",
                "@::hmaker/hm_delete",
                "@::save_text", 
                "~",
                "::hmaker/hm_site_new",
                "::hmaker/hm_site_dup",
                "::hmaker/hm_site_del",
                "~",
                "::hmaker/pub_site",
                "~",
                "::hmaker/hm_site_conf",
                "~",
                "::zui_debug",
                "::open_console"];
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);