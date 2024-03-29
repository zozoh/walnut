(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/menu/menu',
    'ui/tree/tree',
], function(ZUI, Wn, HmMethods, MenuUI, TreeUI){
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
    init : function() {
        var UI = HmMethods(this);

        UI.listenBus("update:obj", UI.updateNode);
        UI.listenBus("reload:folder", UI.reloadNode);
        UI.listenBus("items:disable_by_sensors", UI.disableBySensors);
        UI.listenBus("items:disable_leafs", UI.disableLeafs);
        UI.listenBus("items:enable", UI.enableAll);
    },
    //...............................................................
    getDropSensors : function(conf, ing) {
        var UI = this;
        var oHome = UI.getHomeObj();

        // 不是拖拽顶级节点，把站点根也放进去
        conf = conf || {};
        if(ing.data && ing.data.id != oHome.id && ing.data.pid != oHome.id) {
            conf.$root = UI.$el;
            conf.oRoot = oHome;
        }

        return UI.uiTree.getDropSensors(conf);
    },
    //...............................................................
    update : function(o, callback, args) {
        var UI = this;
        var oHome  = UI.getHomeObj();
        var homeId = oHome.id;

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
                Wn.exec('obj -match \'pid:"'+o.id+'"\' -sort "nm:1" -json -l', function(re){
                    var list = $z.fromJson(re);
                    callback(list);
                })
            },
            filter : function(o) {
                if(/^[.]/.test(o.nm))
                    return null;
                return o;
            },
            ancestor : function(id, callback) {
                Wn.exec('obj id:'+id+' -an nodes -anuntil \'id:"'+homeId+'"\'', function(re){
                    var list = $z.fromJson(re);
                    callback(list);
                })
            },
            idKey : "id",
            nmKey : "nm",
            icon  : function(o){
                return UI.getObjIcon(o);
            },
            escapeHtml : false,
            text  : function(o){
                var html = '<a href="/a/open/';
                html += window.wn_browser_appName;
                html += '?ph=id:'+homeId;
                html += '#hmaker2::' + o.id;
                html += '">';
                html += UI.getObjText(o);
                html += '</a>';
                if(o.title)
                    html += '<em>' + o.title + '</em>';
                return html;
            },
            events : {
                'click a[href]' : function(e) {
                    e.preventDefault();
                }
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
                if(!o)
                    return;
                //console.log("nav actived", o, this);
                // 如果当前就正在编辑这个页，啥也表做了
                // if(UI.local("last_open_obj_id") == o.id)
                //     return;

                // 记录一下上次激活的 ID
                UI.local("last_open_obj_id", o.id);

                // 修改地址栏
                UI.hmaker().browser().pushHistory(oHome, "hmaker2::"+o.id);

                // 激活
                UI.fire("active:rs", o);
            },
            // on_blur : function() {
            //     UI.local("last_open_obj_id", null);
            //     console.log("blur");
            //     UI.fire("rs:blur");
            // },
            on_click_actived_text : function(o, jText, jNode){
                UI.do_rename(o, jText, jNode);
            },
            // 启用拖拽
            drag : {
                moreDragSensors : function() {
                    var ing    = this;
                    var hmaker = UI.hmaker();
                    // 得到当前操作的节点
                    var ao    = UI.uiTree.getNodeData(ing.$target);
                    var anMap = $z.obj(ao.id, ao);

                    // 调用主界面
                    var list = $z.invoke(hmaker.gasket.main, "getDropSensors", [{
                        ignoreLeaf        : true,
                        ignoreChecked     : false,
                        ignoreAncestorMap : anMap,
                    }, ing]);
                    return list;
                },
                on_ready : function() {
                    var ing = this;
                    UI.fire("items:disable_by_sensors", ing.sensors);
                    UI.fire("items:disable_leafs");
                },
                on_end : function(oTa) {
                    // 恢复节点
                    UI.fire("items:enable");

                    // 嗯，要开始移动了
                    var objs = this.data;
                    UI.moveTo(oTa, objs, function(){
                        UI.fire("reload:folder");
                    });
                }
            }
        }).render(function(){
            var lastOpenId = args || UI.local("last_open_obj_id");
            if(lastOpenId)
                this.setActived(lastOpenId);
            //this.setActived(2);
            $z.doCallback(callback, [], UI);
        });
        
    },
    //...............................................................
    do_rename : function(o, jText, jNode) {
        var UI = this;
        jNode.attr("edit-text-on", "yes");

        // 准备文本
        var text = o.nm;
        if(o.title) {
            text += ": " + o.title;
        }

        // 开启编辑模式
        $z.editIt(jText.parent(), {
            text : text,
            copyStyle : false,
            after : function(newval, oldval) {
                jNode.removeAttr("edit-text-on");
                // 去掉空白
                newval = $.trim(newval);
                // 如果有效就执行改名看看
                if(newval && newval!=oldval){
                    // 不支持特殊字符
                    if(/['"\\\\\/$%*]/.test(newval)) {
                        alert(UI.msg("e.fnm.invalid"));
                        return;
                    }

                    // 分析一下
                    var m = /^([^:]+):(.*)$/.exec(newval);
                    var nm, tt;
                    if(m) {
                        nm = $.trim(m[1]);
                        tt = $.trim(m[2]);
                    }
                    // 没指定 title
                    else {
                        nm = newval;
                        tt = "";
                    }


                    // 改名咯
                    Wn.exec("mv -oqT id:"+o.id + " 'id:"+o.pid+"/"+nm+"';", function(re){
                        // 错误
                        if(/^e[.]/.test(re)){
                            alert(UI.msg(re));
                        }
                        // 真的改名
                        else{
                            // 修改标题
                            var cmdText = 'obj id:'+o.id+' -u \'{title:"'+tt+'"}\' -o';
                            Wn.exec(cmdText, function(re){
                                // 错误
                                if(/^e[.]/.test(re)){
                                    alert(UI.msg(re));
                                }
                                var obj = $z.fromJson(re);
                                console.log(obj);
                                Wn.saveToCache(obj);
                                //jText.text(obj.nm);
                                UI.fire("update:obj", obj);
                            });
                        }
                    });
                }
            }
        });
    },
    //...............................................................
    remove : function(oid) {
        var UI = this;

        // 移除
        var jN2 = UI.uiTree.removeNode(oid);
        //console.log("jN2", jN2)

        // 高亮下一个节点
        UI.uiTree.setActived(jN2);
    },
    //...............................................................
    getActived : function(){
        return this.uiTree.getActived();
    },
    getActivedId : function(){
        return this.uiTree.getActivedId();
    },
    //...............................................................
    setActived : function(nd, quiet, callback){
        this.uiTree.setActived(nd, quiet, callback);
    },
    //...............................................................
    disableBySensors : function(sens) {
        for(var i=0; i<sens.length; i++) {
            var sen = sens[i];
            if(sen.visible && sen.disabled && "tree"==sen.drag_sen_type) {
                this.uiTree.disableNode(sen.$ele);
            }
        }
    },
    //...............................................................
    disableLeafs : function() {
        this.uiTree.disableNode(function(o, jNode){
            return 'leaf' == jNode.attr("ndtp");
        });
    },
    //...............................................................
    enableAll : function() {
        this.uiTree.enableNode();
    },
    //...............................................................
    updateNode : function(o) {
        this.uiTree.updateNode(o.id, o, true);
    },
    //...............................................................
    openNode : function(nd, callback, forceReload){
        this.uiTree.openNode(nd, callback, forceReload);
    },
    //...............................................................
    closeNode : function(nd){
        this.uiTree.closeNode(nd);
    },
    //...............................................................
    reloadNode : function(o, callback){
        var UI = this;
        var homeId = UI.getHomeObjId();
        var jNode  = o ? UI.uiTree.findNode(o) : null;

        //console.log("hahah")

        // 如果有节点
        if(jNode && jNode.length > 0) {
            UI.uiTree.reload(jNode, callback);
        }
        // 否则全刷新
        else {
            UI.refresh(callback);
        }
    },
    //...............................................................
    refresh : function(callback){
        var UI = this;
        UI.uiTree.showLoading();
        UI.uiTree.reload(function(){
            UI.uiTree.hideLoading();
            $z.doCallback(callback,[],UI);
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);