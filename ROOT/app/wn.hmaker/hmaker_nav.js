(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'ui/tree/tree'
], function(ZUI, Wn, MenuUI, TreeUI){
//==============================================
return ZUI.def("app.wn.hmaker_nav", {
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 添加菜单
        UI.uiMenu = new MenuUI({
            parent : UI,
            gasketName : "menu",
            setup : [{
                icon  : '<i class="fa fa-plus"></i>',
                handler : function(){
                    UI.createPage();
                }
            }, {
                icon  : '<i class="fa fa-refresh"></i>',
                handler : function(){
                    UI.reloadPage();
                }
            }, {
                icon  : '<i class="fa fa-trash"></i>',
                handler : function(){
                    UI.deletePage();
                }
            }]
        }).render();
    },
    //...............................................................
    reloadPage : function(aid){
        var UI = this;
        aid = aid || UI.uiTree.getActivedId();

        // 清除缓存
        Wn.cleanCache("oid:" + UI.getHomeId());

        // 重新加载 
        UI.uiTree.showLoading();
        UI.uiTree.reload(function(){
            if(aid)
                UI.uiTree.setActived(aid);
        });
    },
    //...............................................................
    deletePage : function(){
        var UI = this;
        var Te = UI.uiTree;
        var jNode = UI.uiTree.getActivedNode();
        if(jNode.size() == 0){
            alert(UI.msg("hmaker.nav.e_del_none"));
            return;
        }
        
        // 得到节点数据
        var oPage = Te.getNodeData(jNode);

        // 准备删除后高亮的节点 ID
        var nextId;
        // 后面还有
        if(jNode.next().size() > 0){
            nextId = UI.uiTree.getNodeId(jNode.next());
        }
        // 前面还有
        else if(jNode.prev().size() > 0){
            nextId = UI.uiTree.getNodeId(jNode.prev());   
        }
        // 都木有了 ...
        else{
            nextId = null;
        }

        // 执行删除
        var re  = Wn.exec("rm id:"+oPage.id);
        // 执行错误 
        if(/^e.cmd/.test(re)){
            alert(re);
            return;
        }
        UI.uiTree.removeNode(jNode);

        // 高亮下一个节点
        if(nextId)
            UI.uiTree.setActived(nextId);
    },
    //...............................................................
    createPage : function() {
        var UI = this;
        
        // 执行创建
        var re = Wn.exec("hmaker id:"+UI.getHomeId()+" newpage '"+UI.msg("hmaker.nav.new_page")+"'")
        // 执行错误 
        if(/^e.cmd/.test(re)){
            alert(re);
            return;
        }
        // 解析
        var oNP = $z.fromJson(re);

        // 添加节点并高亮它
        UI.uiTree.addNode(oNP).setActived(oNP.id);
    },
    //...............................................................
    getHomeId : function(){
        return this.$el.attr("root-wnobj-id");
    },
    getHomeObj : function(){
        return Wn.getById(this.getHomeId());
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
                Wn.getChildren(rootObj, null, callback);
            },
            children : function(o, callback){
                Wn.getChildren(o, null, callback);
            },
            idKey : "id",
            nmKey : "nm",
            icon : function(o){
                if('DIR' == o.race){
                    return  '<i class="fa fa-folder-o"></i>';
                }
                return  '<i class="fa fa-file-code-o"></i>';
            },
            text : function(o){
                return o.nm;
            },
            isLeaf : function(o){
                return 'DIR' != o.race;
            },
            openWhenActived : false,
            data : function(jNode, o){
                if(!o)
                    return Wn.getById(jNode.attr("oid"));
            },
            on_actived : function(o, jNode){
                //console.log("nav actived", o, this);
                UI.parent.change_mainUI(o);
            },
            on_click_actived_text : function(o, jText, jNode){
                var UI = this;
                $z.editIt(jText.parent(), {
                    text : o.nm,
                    after : function(newval, oldval) {
                        // 去掉空白
                        newval = $.trim(newval);
                        // 如果有效就执行改名看看
                        if(newval && newval!=oldval){
                            // 去掉非法字符
                            newval = newval.replace()
                            // 改名咯
                            Wn.exec("mv -oqT id:"+o.id + " 'id:"+o.pid+"/"+newval+"';", function(re){
                                // 错误
                                if(/^e[.]/.test(re)){
                                    alert(UI.msg(re));
                                }
                                // 真的改名
                                else{
                                    var obj = $z.fromJson(re);
                                    Wn.saveToCache(obj);
                                    jText.text(obj.nm);
                                }
                            });
                        }
                    }
                });
            }
        }).render(function(){
            this.setActived(2);
        });
        
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