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

        UI.listenBus("reload:folder", UI.reloadNode);
    },
    //...............................................................
    getDropSensors : function(conf) {
        return this.uiTree.getDropSensors(conf);
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
                Wn.exec('obj -match \'pid:"'+o.id+'"\' -sort "race:1" -json -l', function(re){
                    var list = $z.fromJson(re);
                    callback(list);
                })
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
                            // 去掉非法字符
                            newval = newval.replace(/['"&*#^`?<>\/\\]/,"");

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
                                        Wn.saveToCache(obj);
                                        //jText.text(obj.nm);
                                        UI.updateNode(obj.id, obj, true);
                                    });
                                }
                            });
                        }
                    }
                });
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
    remove : function(oid) {
        var UI = this;

        // 移除
        var jN2 = UI.uiTree.removeNode(oid);
        console.log("jN2", jN2)

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
    enableNode : function(nd, deeply) {
        this.uiTree.enableNode(nd, deeply);
    },
    //...............................................................
    disableNode : function(nd, deeply) {
        this.uiTree.disableNode(nd, deeply);
    },
    //...............................................................
    updateNode : function(o) {
        this.uiTree.updateNode(o.id, o, true);
    },
    //...............................................................
    openNode : function(nd, callback){
        this.uiTree.openNode(nd, callback);
    },
    //...............................................................
    reloadNode : function(o, callback){
        var UI = this;
        var homeId = UI.getHomeObjId();
        // 如果有节点
        if(o && homeId != o.id) {
            UI.uiTree.reload(o.id, callback);
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