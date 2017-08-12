(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods',
    'ui/otiles/otiles'
], function(ZUI, Wn, HmMethods, OTilesUI){
//==============================================
var html = `
<div class="ui-arena hm-folder" ui-fitparent="yes">
    <header></header>
    <section ui-gasket="list"></section>
</div>
`;
//==============================================
return ZUI.def("app.wn.hmaker_folder", {
    dom : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);

        UI.listenBus("reload:folder", UI.refresh);
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 显示对象浏览器
        new OTilesUI({
            parent : UI,
            gasketName : "list",
            renameable : true,
            on_actived : function(o){
                UI.fire("active:file", o);
            },
            on_blur : function(jItems, nextObj, nextItem){
                UI.fire("blur:file", nextObj);
            }
        }).render(function(){
            UI.defer_report("list");
        });

        return ["list"];
    },
    //...............................................................
    update : function(o) {
        var UI = this;

        // 记录数据 
        UI.oFolderId = o.id;

        // 通知其他部分激活某个对象的属性
        UI.fire("active:folder", o);

        // 更新显示对象 
        UI.refresh();
    },
    //...............................................................
    refresh : function(){
        var UI = this;
        
        // 得到数据 
        var o = Wn.getById(UI.oFolderId);

        if(!o)
            return;

        // 更新显示对象 
        UI.showLoading();
        Wn.getChildren(o, null, function(children){
            UI.hideLoading();

            if(!UI.hmaker())
                return;

            // 显示对象路径
            var aph = UI.getRelativePath(o);
            UI.arena.children("header").empty()
                .append($(UI.getObjIcon(o)))
                .append($('<a target="_blank" href="/a/open/browser?ph=id:'+o.id+'">' + aph + '</a>'));
            //console.log("haha", children)
            // 更新列表
            UI.gasket.list.setData(children);

            // 如果没有激活的项目了，相当于 blur
            if(!UI.gasket.list.hasActived())
                UI.fire("blur:file");
        }, true);
    },
    //...............................................................
    getCurrentEditObj : function(){
        return Wn.getById(this.oFolderId);
    },
    //...............................................................
    getChecked : function() {
        return this.gasket.list.getChecked();
    },
    //...............................................................
    getActions : function(){
        return ["@::refresh",
                "@::hmaker/hm_delete",
                "::hmaker/hm_create", 
                "~",
                "@::hmaker/pub_site",
                "~",
                "@::hmaker/hm_site_conf",
                "~",
                "::zui_debug",
                "::open_console"];
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);