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

        UI.listenBus("update:obj", UI.updatePathBar);
        UI.listenBus("reload:folder", UI.refresh);
        UI.listenBus("items:disable_by_sensors", UI.disableBySensors);
        UI.listenBus("items:disable_leafs", UI.disableLeafs);
        UI.listenBus("items:enable", UI.enableAll);
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
            on_actived : function(obj){
                UI.fire("active:file", obj);
            },
            on_blur : function(jItems, nextObj, nextItem){
                UI.fire("blur:file", nextObj);
            },
            on_open : function(obj){
                UI.resourceUI().setActived(obj.id);
            },
            on_rename : function(obj) {
                UI.fire("update:obj", obj);
            },
            drag : {
                moreDragSensors : function() {
                    var anMap = UI.gasket.list.getCheckedMap();
                    // 调用资源库获取可拖拽目标
                    var list = resUI.getDropSensors({
                        ignoreLeaf        : true,
                        ignoreChecked     : true,
                        ignoreAncestorMap : anMap,
                    }, this);
                    return list;
                },
                on_ready : function(){
                    var ing = this;
                    UI.fire("items:disable_by_sensors", ing.sensors);
                    UI.fire("items:disable_leafs");
                },
                on_end : function(oTa) {
                    // 恢复节点
                    UI.fire("items:enable");

                    //console.log("oTa", oTa);
                    // 嗯，要开始移动了
                    var objs = UI.gasket.list.getChecked();
                    UI.moveTo(oTa, objs, function(){
                        // 移除自己被移动的节点
                        UI.gasket.list.remove(objs);

                        // 准备恢复的 ID
                        var aid = resUI.getActivedId();

                        // 整站刷新
                        if(oTa.id == UI.getHomeObjId()){
                            resUI.refresh(function(){
                                if(aid)
                                    resUI.setActived(aid);
                            });
                        }
                        // 首先重新刷一下当前节点
                        else if(aid) {
                            resUI.reloadNode(aid, function(){
                                resUI.openNode(oTa.id, true);
                            });
                        }
                        // 直接打开目标
                        else {
                            resUI.openNode(oTa.id, true);
                        }
                    });
                }
            }
        }).render(function(){
            UI.defer_report("list");
        });

        return ["list"];
    },
    //...............................................................
    getDropSensors : function(conf, ing) {
        var UI = this;

        // 如果不是拖拽当前节点，那么把当前文件夹也放进去
        conf = conf || {};
        if(ing.data && ing.data.id != UI.oFolderId && ing.data.pid != UI.oFolderId) {
            conf.$root = UI.arena.find(">header");
            conf.oRoot = Wn.getById(UI.oFolderId);
        }

        return UI.gasket.list.getDropSensors(conf);
    },
    //...............................................................
    disableBySensors : function(sens) {
        var oHome = this.getHomeObj();
        for(var i=0; i<sens.length; i++) {
            var sen = sens[i];
            if(!sen.visible)
                continue;
            // var so  = sen.data;
            // var rph = Wn.getRelativePath(oHome, so);
            //console.log(i, sen.drag_sen_type, rph, sen)
            if(sen.disabled && "folder"==sen.drag_sen_type) {
                //console.log(" -- disableItem", i);
                this.gasket.list.disableItem(sen.$ele);
            }
        }
    },
    //...............................................................
    disableLeafs : function() {
        this.gasket.list.disableItem(function(o){
            return 'FILE' == o.race;
        });
    },
    //...............................................................
    enableAll : function() {
        this.gasket.list.enableItem();
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
        var obj = Wn.getById(UI.oFolderId);

        if(!obj)
            return;

        // 更新显示对象 
        UI.showLoading();
        Wn.exec('obj -match \'pid:"'+obj.id+'"\' -sort "race:1,nm:1" -json -l', function(re){
            var children = $z.fromJson(re);
            
            UI.hideLoading();

            if(!UI.hmaker())
                return;

            // 显示对象路径
            UI.updatePathBar(obj);
            
            //console.log("haha", children)
            // 更新列表
            UI.gasket.list.setData(children);

            // 如果没有激活的项目了，相当于 blur
            if(!UI.gasket.list.hasActived())
                UI.fire("blur:file");
        });
    },
    //...............................................................
    updatePathBar : function(obj) {
        var UI = this;
        var jH = UI.arena.children("header").empty();
        if(!obj) {
            jH.text("empty!");
        }
        // 显示对象路径
        else {
            //var appName = window.wn_browser_appName || "wn.hmaker2";
            var aph = UI.getRelativePath(obj);
            jH.append($(UI.getObjIcon(obj, true)))
              .append($('<span>').text(aph));
                // .append($('<a target="_blank" href="/a/open/'
                //             + appName + '?ph=id:'+obj.id+'">' + aph + '</a>'));
        }
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