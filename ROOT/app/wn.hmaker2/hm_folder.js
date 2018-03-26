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
                    var checkeds = this.getCheckedMap();
                    // 首先增加一个 oHome
                    var oHome = UI.getHomeObj();
                    var list1 = [{
                        name : "drag",
                        rect : 1,
                        text : "HOME",
                        $ele : resUI.$el,
                        data : oHome,
                    }];

                    // 看下面的文件夹
                    var list2 = resUI.getDropSensors({
                        rect : -.5,
                        ignoreChecked : false,
                        ignoreLeaf    : false,
                    });

                    // 循环检查下面的 sensor 是不是要 disabled
                    for(var x=0; x<list2.length; x++) {
                        var sen   = list2[x];
                        var o     = sen.data;
                        var jNode = sen.$ele.closest(".tree-node");

                        // 高亮节点或者叶子节点要搞一下
                        if('leaf' == jNode.attr("ndtp") || jNode.hasClass("tree-node-actived")) {
                            sen.disabled = true;
                            continue;
                        }

                        // 自己不能在选中的 ID 里
                        if(checkeds[o.id]) {
                            sen.disabled = true;
                            continue;
                        }
                            
                        // 找到自己所有的祖先，也都不能在选中的 ID 里
                        var jPs = jNode.parents(".tree-node");
                        for(var i=0; i<jPs.length; i++) {
                            var pid = jPs.eq(i).attr("oid");
                            if(checkeds[pid]) {
                                sen.disabled = true;
                                break;
                            }
                        }
                    }
                    //console.log(list2);
                    
                    // 嗯，就这样，返回吧
                    return [].concat(list1, list2);
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

                    //console.log("oTa", oTa);
                    // 嗯，要开始移动了
                    if(oTa) {
                        // 准备命令吧
                        var cmds = [];
                        var objs = this.getChecked();
                        for(var i=0; i<objs.length; i++) {
                            var obj = objs[i];
                            cmds.push('mv id:' + obj.id + ' id:' + oTa.id);
                            cmds.push('echo "%[' + (i+1) + '/' + objs.length
                                        +'] move ' + obj.nm + ' => ' + oTa.nm + '"');
                        }
                        cmds.push('echo "%[-1/0] ' + objs.length + ' objs done!"');
                        // 执行命令
                        var cmdText = cmds.join(";\n");
                        //console.log(cmdText)
                        Wn.processPanel(cmdText, function(){
                            //console.log("-------------All Done");
                            this.close();

                            // 直接删除
                            UI.gasket.list.remove(objs);
                            
                            // 刷新资源面板（会导致 folder 界面也跟着刷新）
                            var aid = resUI.getActivedId() || oTa.id;
                            resUI.refresh(function(){
                                resUI.setActived(aid, true, function(){
                                    //console.log("openNode",oTa.id, oTa.nm)
                                    resUI.openNode(oTa.id, function(list){
                                        //console.log("done", list)
                                    });
                                });
                            });
                        });
                    }
                }
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
            var appName = window.wn_browser_appName || "wn.hmaker2";
            var aph = UI.getRelativePath(o);
            UI.arena.children("header").empty()
                .append($(UI.getObjIcon(o)))
                .append($('<span>').text(aph));
                // .append($('<a target="_blank" href="/a/open/'
                //             + appName + '?ph=id:'+o.id+'">' + aph + '</a>'));
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