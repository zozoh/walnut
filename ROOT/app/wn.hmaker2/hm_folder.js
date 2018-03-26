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
        var resUI = UI.resourceUI();

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
            },
            on_open : function(o){
                UI.resourceUI().setActived(o.id);
            },
            drag : {
                sensors : function() {
                    return resUI.getDropSensors({
                        checkeds : this.getCheckedMap(),
                    });
                },
                on_begin : function(){
                    var checkedIds = this.getCheckedIds();
                    //console.log(checkedIds)
                    // 选中节点全部灰掉
                    resUI.disableNode(checkedIds, true);
                    
                    // 叶子节点也全部灰掉和共享库
                    resUI.disableNode(function(o, jNode){
                        return 'leaf' == jNode.attr("ndtp");
                    });
                },
                on_end : function(oTa) {
                    resUI.enableNode();

                    console.log("oTa", oTa);
                    // 嗯，要开始移动了
                    var objs = this.getChecked();
                    UI.moveTo(oTa, objs, function(){
                        UI.gasket.list.remove(objs);
                    });
                }
            }
        }).render(function(){
            UI.defer_report("list");
        });

        return ["list"];
    },
    //...............................................................
    getDropSensors : function(conf) {
        var UI = this;
        return UI.gasket.list.getDropSensors(_.extend({
            $root : UI.arena.find(">header"),
            oRoot : Wn.getById(UI.oFolderId),
        },conf));
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
        Wn.exec('obj -match \'pid:"'+o.id+'"\' -sort "race:1,nm:1" -json -l', function(re){
            var children = $z.fromJson(re);
            
            UI.hideLoading();

            if(!UI.hmaker())
                return;

            // 显示对象路径
            var appName = window.wn_browser_appName || "wn.hmaker2";
            var aph = UI.getRelativePath(o);
            UI.arena.children("header").empty()
                .append($(UI.getObjIcon(o, true)))
                .append($('<span>').text(aph));
                // .append($('<a target="_blank" href="/a/open/'
                //             + appName + '?ph=id:'+o.id+'">' + aph + '</a>'));
            //console.log("haha", children)
            // 更新列表
            UI.gasket.list.setData(children);

            // 如果没有激活的项目了，相当于 blur
            if(!UI.gasket.list.hasActived())
                UI.fire("blur:file");
        });
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
        return ["@::hmaker/hm_refresh",
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