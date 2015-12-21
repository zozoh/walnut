(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/shelf/shelf',
    'ui/obrowser/obrowser_sky',
    'ui/obrowser/obrowser_main'
], function(ZUI, Wn, ShelfUI){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser ui-oicon-16" 
     ui-fitparent="true"
     ui-gasket="shelf">
</div>
*/};
//==============================================
return ZUI.def("ui.obrowser", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : ["theme/obrowser.css","ui/oicons.css"],
    i18n : "ui/obrowser/i18n/{{lang}}.js",
    //..............................................
    init : function(options){
        var UI = this;

        $z.setUndefined(options, "checkable", true);
        
        // 绑定 UI 间的监听关系
        UI.on("browser:change", function(o){
            UI.subUI("shelf/chute").update(UI, o);
            UI.subUI("shelf/sky").update(UI, o);
            UI.subUI("shelf/main").update(UI, o);
        });
        UI.on("change:viewmode", function(){
            var o = UI.getCurrentObj();
            UI.subUI("shelf/main").update(UI, o);
        });
        UI.on("menu:viewmode", function(vm){
            this.setViewMode(vm);            
        });
    },
    //..............................................
    canOpen : function(o){
        var UI = this;
        if(_.isFunction(UI.options.canOpen)){
            return UI.options.canOpen.call(UI, o);
        }
        return "DIR" == o.race;
    },
    //..............................................
    redraw : function(){
        var UI = this;

        // 初始化界面
        (new ShelfUI({
            parent : this,
            gasketName : "shelf",
            display : {
                sky : 40,
                footer : 32
            },
            sky : {
                uiType : "ui/obrowser/obrowser_sky",
                uiConf : {
                    className : "obrowser-block",
                    fitparent : true
                }
            },
            chute : {
                uiType : "ui/obrowser/obrowser_chute",
                uiConf : {
                    className : "obrowser-block",
                    fitparent : true
                }
            },
            main : {
                uiType : "ui/obrowser/obrowser_main",
                uiConf : {
                    className : "obrowser-block",
                    fitparent : true
                }
            },
            footer : {
                uiType : "ui/dom",
                uiConf : {
                    className : "obrowser-block obrowser-footer",
                    fitparent : true,
                    dom : "<b>I am footer</b>"
                }
            },

        })).render(function(){
            // 回报延迟加载
            UI.defer_report(0, "browser-shelf");
        });

        // 返回延迟加载
        return ["browser-shelf"];
    },
    //..............................................
    // 将一个 actions 的字符串数组展开成为真的动作命令
    // actions  - 的格式类似 ["@:r:new", ":w:tree", "::search", "~", "::properties"]
    // forceTop - true 相当于所有项目都有 @
    // 返回一个新的展开过的数组
    extend_actions : function(actions, forceTop){
        var UI = this;
        // 分析菜单项，生成各个命令的读取路径
        // actions 的格式类似 ["@:r:new", ":w:tree", "::search", "~", "::properties"]
        // @ 开头的项表示固定显示
        var ac_phs     = [];
        var menu_setup = [];
        var menu_items = [];
        actions.forEach(function(str, index){
            // 分隔符，特殊处理
            if(str == "~"){
                menu_items.push({type:'separator'});
                if(forceTop){
                    menu_setup.push(index);
                }
                return;
            }
            var ss = str.split(":");
            // 生成命令的读取路径
            menu_items.push(ac_phs.length);
            ac_phs.push("~/.ui/actions/"+ss[2]+".js");
            // 记录固定显示的项目下标
            if(forceTop || ss[0]=="@")
                menu_setup.push(index);
        });
        // 逐次得到菜单的动作命令
        var alist = UI.batchRead(ac_phs);
        for(var i=0; i<menu_items.length; i++){
            var index = menu_items[i];
            if(_.isNumber(index)){
                var mi = eval('(' + alist[index] + ')');
                if(mi.type=="group" || _.isArray(mi.items)){
                    mi._items_array = mi.items;
                    mi.items = function(jq, mi, callback){
                        var items = this.extend_actions(mi._items_array, true);
                        callback(items);
                    };
                }
                mi.context = UI;
                menu_items[i] = mi;
            }
        }
        // 将有固定显示的项目移动到顶级
        for(var i=0; i<menu_setup.length; i++){
            var index = menu_setup[i];
            menu_setup[i] = menu_items[index];
            menu_items[index] = null;
        }

        // 消除被移除的项目
        menu_items = _.without(menu_items, null);

        if(menu_items.length>0){
            // 和折叠按钮有分隔符
            if(menu_setup.length>0){
                menu_setup.push({type:"separator"});
            }

            // 最后创建一个固定扳手按钮，以便展示菜单
            // ? 折叠按钮用 <i class="fa fa-ellipsis-v"></i> 如何 ?
            // ? 折叠按钮用 <i class="fa fa-bars"></i> 如何 ?
            menu_setup.push({
                type  : 'group',
                icon  : '<i class="fa fa-ellipsis-v"></i>',
                items : menu_items
            });
        }

        // 返回配置好的菜单命令
        return menu_setup;
    },
    //..............................................
    setData : function(obj){
        var UI = this;
        // 没值
        if(!obj){
            // 如果是记录最后一次
            if(UI.options.lastObjId){
                var lastId = UI.local(UI.options.lastObjId);
                if(lastId){
                    UI.setData("id:"+lastId);
                    return;
                }
            }
            // 看看有没有当前对象
            var c_oid = UI.getCurrentObjId();
            if(c_oid){
                UI.setData("id:"+c_oid);
            }
            // 默认采用主目录
            else{
                UI.setData("~");
            }
            return;
        }

        // 字符串
        if(_.isString(obj)){
            var o;
            if(/^id:\w{6.}$/.test(obj)){
                var oid = obj.substring(0,3);
                o = UI.getById(oid);
            }else{
                o = UI.fetch(obj);
            }
            UI.setData(o ? o : "~");
            return;
        }

        // 保存到缓冲
        UI.saveToCache(obj);

        // 持久记录最后一次位置
        if(UI.options.lastObjId){
            UI.local(UI.options.lastObjId, obj.id);
        }

        // 临时记录当前的对象
        UI.setCurrentObjId(obj.id);

        // 调整尺寸
        UI.resize();

        // 触发事件
        UI.trigger("browser:change", obj);
        $z.invoke(UI.options, "on_change", [obj], UI);
        
    },
    //..............................................
    refresh : function(){
        var oid = this.getCurrentObjId();
        this.cleanCache("oid:"+oid);
        this.setData("id:"+oid);
    },
    //..............................................
    showUploader: function(options){
        var ta =  this.getCurrentObj();
        Wn.uploadPanel(_.extend({
            target : ta
        }, options));
    },
    //..............................................
    getViewMode : function(){
        return this.local("viewmode") || "thumbnail";
    },
    setViewMode : function(mode){
        // 检查 mode 是否合法
        if(_.isString(mode)
           && /^table|thumbnail|slider|scroller|icons|columns$/.test(mode)
           && mode != this.getViewMode()){
            this.local("viewmode", mode);
            this.trigger("change:viewmode", mode);
        }
    },
    //..............................................
    getCurrentObjId : function(){
        return this.$el.attr("current-oid");
    },
    setCurrentObjId : function(oid){
        if(!oid)
            this.$el.removeAttr("current-oid");
        else
            this.$el.attr("current-oid", oid);
    },
    getCurrentObj : function(){
        var oid = this.getCurrentObjId();
        return this.getById(oid);
    },
    getPath : function(){
        return this.subUI("shelf.sky").getPath();
    },
    getPathObj : function(){
        return this.subUI("shelf.sky").getData();
    },
    //..............................................
    getActived : function(){
        return this.subUI("shelf.main").getActived();
    },
    getChecked : function(){
        return this.subUI("shelf.main").getChecked();
    },
    //..............................................
    getChildren : function(o){
        return Wn.getChildren(o);
    },
    //..............................................
    getAncestors : function(o, includeSelf){
        return Wn.getAncestors(o, includeSelf);
    },
    //..............................................
    getAncestorPath : function(o, includeSelf){
        return Wn.getAncestorPath(o, includeSelf);
    },
    //..............................................
    batchRead : function(objList, callback){
        return Wn.batchRead(objList, callback, this);
    },
    //..............................................
    read : function(o, callback){
        return Wn.read(o, callback, this);
    },
    //..............................................
    get : function(o, quiet){
        if(_.isUndefined(o) || $z.isjQuery(o) || _.isElement(o)){
            return this.subUI("shelf.main").getData(o);
        }
        return Wn.get(o, quiet);
    },
    //..............................................
    getById : function(oid, quiet) {
        return Wn.getById(oid, quiet);
    },
    //..............................................
    fetch : function(ph, quiet){
        return Wn.fetch(ph, quiet);
    },
    //..............................................
    saveToCache : function(o){
        Wn.saveToCache(o);
    },
    //..............................................
    cleanCache : function(key){
        Wn.cleanCache(key);
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);