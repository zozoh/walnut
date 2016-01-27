(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'ui/tree/tree'
], function(ZUI, Wn, MenuUI, TreeUI){
//==============================================
var html = function(){/*
<div class="ui-arena bp-nav" ui-fitparent="yes">
    <div class="bp-nav-abar" ui-gasket="menu"></div>
    <div class="bp-nav-tree" ui-gasket="tree"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.bp_ide_nav", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 添加菜单
        UI.uiMenu = new MenuUI({
            parent : UI,
            gasketName : "menu",
            setup : [{
                icon  : '<i class="fa fa-ellipsis-v"></i>',
                iconAtRight : true,
                text  : 'i18n:bp.nav.actions',
                items : [{
                    icon  : '<i class="fa fa-file-o"></i>',
                    text : 'i18n:bp.nav.new_page',
                    handler : function(){
                        UI.createPage();
                    }
                }, {
                    icon  : '<i class="fa fa-refresh"></i>',
                    text : 'i18n:bp.nav.reload_page',
                    handler : function(){
                        UI.reloadPage();
                    }
                }, {
                    text : 'i18n:bp.nav.del_page',
                    handler : function(){
                        UI.deletePage();
                    }
                }]
            }]
        }).render();
    },
    //...............................................................
    reloadPage : function(){
        var UI = this;
        var Te = UI.uiTree;
        var jNdPage = Te.$topByName("page");
        var oPage = Te.getNodeData(jNdPage);
        var aid = Te.getActivedId();

        // 清除子
        Wn.saveToCache(oPage, true);

        // 重新加载 
        Te.reload(jNdPage, function(){
            Te.active(aid);
        });
    },
    //...............................................................
    deletePage : function(){
        var UI = this;
        var Te = UI.uiTree;
        var jNode = Te.getActivedNode();
        if(jNode.size() == 0){
            alert(UI.msg("bp.nav.e_del_none"));
            return;
        }
        if(Te.isTop(jNode)){
            alert(UI.msg("bp.nav.e_del_top"));
            return;   
        }
        // 得到节点数据
        var oPage = Te.getNodeData(jNode);
        // 准备删除后高亮的节点 ID
        var nextId;
        // 后面还有
        if(jNode.next().size() > 0){
            nextId = Te.getNodeId(jNode.next());
        }
        // 前面还有
        else if(jNode.prev().size() > 0){
            nextId = Te.getNodeId(jNode.prev());   
        }
        // 都木有了 ...
        else{
            nextId = Te.getNodeId(Te.$topByName("page"));
        }

        // 执行删除
        var re  = Wn.exec("rm id:"+oPage.id);
        // 执行错误 
        if(/^e.cmd/.test(re)){
            alert(re);
            return;
        }
        Te.removeNode(jNode);

        // 高亮下一个节点
        Te.active(nextId);
    },
    //...............................................................
    createPage : function() {
        var UI = this;
        var Te = UI.uiTree;
        // 找到页面节点
        var jNdPage = Te.$topByName("page");
        var oPage = Te.getNodeData(jNdPage);
        
        // 执行创建
        var re = Wn.exec("bp id:"+UI.getHomeId()+" newpage '"+UI.msg("bp.nav.new_page")+"'")
        // 执行错误 
        if(/^e.cmd/.test(re)){
            alert(re);
            return;
        }
        var oNP = $z.fromJson(re);


        // 在回调中刷新当前节点
        Wn.saveToCache(oPage, true);

        // 刷新完毕，高亮最新节点
        Te.reload(jNdPage, function(){
            Te.openNode(jNdPage);
            Te.active(oNP.id);
        });
    },
    //...............................................................
    getHomeId : function(){
        return this.$el.attr("root-wnobj-id");
    },
    //...............................................................
    update : function(o) {
        var UI = this;

        // 记录根节点
        UI.$el.attr("root-wnobj-id", o.id);

        // 生成 TreeUI
        UI.uiTree = new TreeUI({
            parent     : UI,
            gasketName : "tree",
            tops       : function(callback){
                var rootId  = UI.$el.attr("root-wnobj-id");
                var rootObj = Wn.getById(rootId);
                // 按这个排序
                var nb = ['site.json', 'theme', 'page'];
                Wn.getChildren(rootObj, null, function(list){
                    (list||[]).sort(function(o0, o1){
                        var n0 = nb.indexOf(o0.nm);
                        var n1 = nb.indexOf(o1.nm);
                        return n0-n1;
                    });
                    callback(list);
                });
            },
            children : function(o, callback){
                Wn.getChildren(o, null, callback);
            },
            idKey : "id",
            nmKey : "nm",
            icon : function(o){
                var rootId  = UI.$el.attr("root-wnobj-id");
                // 顶级节点有定制的icon
                if(o.pid == rootId) {
                    // 主题
                    if("theme" == o.nm){
                        return '<i class="fa fa-cubes"></i>';
                    }
                    // 页面
                    else if("page" == o.nm){
                        return '<i class="fa fa-sitemap"></i>';
                    }
                    // 配置
                    else if("site.json" == o.nm){
                        return '<i class="fa fa-gear"></i>';
                    }
                }
                return  '<i class="fa fa-file-code-o"></i>';
            },
            text : function(o){
                var rootId  = UI.$el.attr("root-wnobj-id");
                // 顶级节点有定制的名称
                if(o.pid == rootId) {
                    var key = "bp.o_" + o.nm.replace(".", "_");
                    return UI.msg(key);
                }
                // 其他就是名称
                return o.nm;
            },
            isLeaf : function(o){
                var rootId  = UI.$el.attr("root-wnobj-id");
                // 父节点的 theme 不能再打开了
                if(o.nm == 'theme' && o.pid == rootId){
                    return true;
                }
                // 其他随意 
                return 'DIR' != o.race;
            },
            openWhenActived : true,
            on_actived : function(o, jNode){
                console.log("nav actived", o, this);
            },
            don_contextmenu : function(o, jNode) {
                return [{
                    text : "AAAAA",
                    handler : function(){
                        console.log(this)
                    }
                },{
                    text : "BBBBBB",
                    handler : function(){
                        alert("BBBBBB");
                    }
                }, {
                    text : "XXXXX",
                    items : [{
                        text : "BBBBBB",
                        handler : function(){
                            alert("BBBBBB");
                        }
                    }, {
                        text : "CCCCCCC",
                        handler : function(){
                            alert("CCCCCCC");
                        }
                    }]
                },{
                    text : "BBBBBB",
                    handler : function(){
                        alert("BBBBBB");
                    }
                }, {
                    text : "CCCCCCC",
                    handler : function(){
                        alert("CCCCCCC");
                    }
                }];
            }
        }).render();
        
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var jT = UI.arena.find(".bp-nav-tree");
        var jB = UI.arena.find(".bp-nav-abar");
        jT.css("top", jB.outerHeight(true));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);