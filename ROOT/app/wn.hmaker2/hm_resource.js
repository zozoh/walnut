(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/menu/menu',
    'ui/tree/tree',
], function(ZUI, Wn, HmPanelMethods, MenuUI, TreeUI){
//==============================================
var html = function(){/*
<div class="ui-arena hm-panel hm-resource" ui-fitparent="yes">
    <header>
        <ul class="hm-W">
            <li class="hmpn-tt"><i class="zmdi zmdi-collection-item"></i> {{hmaker.res.title}}</li>
            <li class="hmpn-opt" ui-gasket="opt"></li>
            <li class="hmpn-pin"><i class="fa fa-thumb-tack"></i></li>
        </ul>
    </header>
    <section ui-gasket="body"></section>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_resource", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    reloadPage : function(aid){
        var UI = HmPanelMethods(this);
        
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
        var re = Wn.exec("hmaker id:"+UI.getHomeObjId()+" newpage '"+UI.msg("hmaker.nav.new_page")+"'")
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
    update : function(o, callback) {
        var UI = this;

        var _list = function(o, callback) {
            Wn.exec('obj -match \'pid:"'+o.id+'"\' -sort "race:1,nm:1" -json -l',
            function(re){
                var list = $z.fromJson(re);
                callback(list);
            })
        };

        // 生成 TreeUI
        UI.uiTree = new TreeUI({
            parent     : UI,
            gasketName : "body",
            tops : function(callback){
                var rootObj = UI.getHomeObj();
                _list(rootObj, callback);
            },
            children : function(o, callback){
                //Wn.getChildren(o, null, callback);
                Wn.exec('obj -match \'pid:"'+o.id+'"\' -sort "race:1" -json -l', function(re){
                    var list = $z.fromJson(re);
                    callback(list);
                })
            },
            idKey : "id",
            nmKey : "nm",
            icon  : function(o){
                return UI.getObjIcon(o);
            },
            text  : function(o){
                return o.nm;
            },
            isLeaf : function(o){
                // 特殊的目录: lib
                if(o.pid == UI.getHomeObjId()) {
                    if('lib' == o.nm)
                        return true;
                }
                return 'DIR' != o.race;
            },
            openWhenActived : false,
            data : function(jNode, o){
                if(!o)
                    return Wn.getById(jNode.attr("oid"));
            },
            on_actived : function(o, jNode){
                //console.log("nav actived", o, this);
                // 记录一下上次激活的 ID
                UI.local("last_open_obj_id", o.id);

                // 激活
                UI.fire("active:rs", o);
            },
            // on_blur : function() {
            //     UI.local("last_open_obj_id", null);
            //     console.log("blur");
            //     UI.fire("rs:blur");
            // },
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
                                    //jText.text(obj.nm);
                                    UI.updateNode(obj.id, obj);
                                }
                            });
                        }
                    }
                });
            }
        }).render(function(){
            var lastOpenId = UI.local("last_open_obj_id");
            if(lastOpenId)
                this.setActived(lastOpenId);
            //this.setActived(2);
            $z.doCallback(callback, [], UI);
        });
        
    },
    //...............................................................
    remove : function(oid) {
        var UI = this;

        // 移除
        var jN2 = UI.uiTree.removeNode(oid);

        // 高亮下一个节点
        UI.uiTree.setActived(jN2);
    },
    //...............................................................
    setActived : function(arg){
        this.uiTree.setActived(arg);
    },
    //...............................................................
    refresh : function(callback){
        var UI = this;
        UI.uiTree.reload(function(){
            callback.apply(UI);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);