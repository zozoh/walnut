(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods',
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
            on_blur : function(){
                UI.fire("blur:file");
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

        // 显示对象路径
        var aph = UI.getRelativePath(o);
        UI.arena.children("header").empty()
            .append($(UI.getObjIcon(o)))
            .append($('<a target="_blank" href="/a/open/browser?ph=id:'+o.id+'">' + aph + '</a>'));

        // 更新显示对象 
        Wn.getChildren(o, null, function(children){
            //console.log("haha", children)
            UI.gasket.list.setData(children);
        }, true);
    }
});
//===================================================================
});
})(window.NutzUtil);