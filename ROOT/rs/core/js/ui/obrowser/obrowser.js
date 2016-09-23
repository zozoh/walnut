(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/obrowser/support/browser__methods',
    'ui/obrowser/obrowser_sky',
    'ui/obrowser/obrowser_chute',
    'ui/obrowser/obrowser_main',
    'ui/obrowser/obrowser_footer'
], function(ZUI, Wn, BrowserMethods, SkyUI, ChuteUI, MainUI, FooterUI){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser ui-oicon-16" ui-fitparent="true">
    <div class="obw-sky" ui-gasket="sky"></div>
    <div class="obw-con"><div class="obw-con-wrapper">
        <div class="obw-chute" ui-gasket="chute"></div>
        <div class="obw-main"  ui-gasket="main"></div>
    </div></div>
    <div class="obw-footer" ui-gasket="footer"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.obrowser", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : ["theme/ui/obrowser/obrowser.css",
            "theme/ui/support/thumbnail.css", 
            "theme/ui/support/oicons.css"],
    i18n : "ui/obrowser/i18n/{{lang}}.js",
    //..............................................
    __browser__ : true,
    //..............................................
    __eval_canOpen : function(options){
        var co = options.canOpen;
        // 默认只要是目录就能打开
        if(!co) {
            options.canOpen = function(o){
                return 'DIR' == o.race;
            }
        }
        // 字符串
        else if(_.isString(co)){
            var regex = new RegExp(co);
            options.canOpen = function(o){
                return 'DIR' == o.race && regex.test(Wn.objTypeName(o));
            };
        }
        // 正则表达式
        else if(_.isRegExp(co)){
            options.canOpen = function(o){
                return 'DIR' == o.race && co.test(Wn.objTypeName(o));
            };
        }
        // 本身就是函数
        else if(_.isFunction(co)){
            // 嗯，那就啥也不做了
        }
        // 错误的定义
        else{
            alert("invalid 'canOpen' defination: " + co);
            throw "invalid 'canOpen' defination: " + co;
        }
    },
    //..............................................
    init : function(options){
        var UI  = BrowserMethods(this);
        var opt = options;

        // 设置默认的 sidebar 定义
        $z.setUndefined(opt, "sidebar", {});
        if(opt.sidebar) {
            // 兼容以前的 sidebar : true 格式的选项
            if(_.isBoolean(opt.sidebar)) {
                opt.sidebar = {};
            }
            // 用户直接给了一个 uiConf
            if(!opt.sidebar.uiType && !opt.sidebar.uiConf && !_.isEmpty(opt.sidebar)) {
                opt.sidebar = {
                    uiType : 'ui/obrowser/obrowser_chute_sidebar',
                    uiConf : opt.sidebar
                }
            }
            // 补充不足
            else {
                opt.sidebar = $z.extend({
                    uiType : 'ui/obrowser/obrowser_chute_sidebar',
                    uiConf : {
                        path : "~/.ui/sidebar.html"
                    }
                }, opt.sidebar);
            }
        }

        // 其他默认设置
        $z.setUndefined(opt, "checkable", false);
        $z.setUndefined(opt, "multi", true);
        $z.setUndefined(opt, "editable", false);
        $z.setUndefined(opt, "skybar", true);
        $z.setUndefined(opt, "footbar", true);

        // 控制打开 
        UI.__eval_canOpen(opt);
        
        // 默认菜单条
        $z.setUndefined(opt, "appSetup", {
            actions : ["@::viewmode"]
        });
        
        // 绑定 UI 间的监听关系
        // UI.on("browser:change", function(o, theEditor){
        //     UI.changeCurrentObj(o, theEditor);
        // });
        // UI.on("menu:viewmode", function(vm){
        //     this.setViewMode(vm);
        // });
        // UI.on("change:hidden-obj-visibility", function(){
        //     var o = UI.getCurrentObj();
        //     UI.subUI("main").update(UI, o);
        // });

        // 绑定历史记录
        if(opt.history){
            window.onpopstate =  function(e){
                if(e.state)
                    UI.setData(e.state);
            };
        }
    },
    //..............................................
    _update_history : function(o, theEditor){
        // 当前的路径
        var nwSt = {
            opath : "?ph=id:" + encodeURIComponent(o.id),
            hash  : theEditor ? "#" + theEditor : ""
        };

        // 当前的路径
        var cuSt = {
            opath : location.search,
            hash  : location.hash
        };

        //console.log($z.toJson(nwSt), $z.toJson(cuSt))

        // 那么 URL 应该是
        var url = nwSt.opath + nwSt.hash
        //console.log("url", url)

        // 如果编辑的是同一个对象，则可以复用之前的编辑器
        if(nwSt.opath == cuSt.opath){
            if(!nwSt.hash && cuSt.hash){
                theEditor = cuSt.hash.substring(1);
                url += cuSt.hash;
            }
        }
        
        // 路径不相等，推入历史
        if(nwSt.opath != cuSt.opath){
            //console.log("push!!!")
            history.pushState(o, o.nm + " : wn.browser", url);
        }
        // 编辑器不相等，更换当前
        else if(nwSt.hash != cuSt.hash){
            //console.log("replace!!!")
            history.replaceState(o, o.nm + " : wn.browser", url);
        }

        // 返回编辑器
        return theEditor;

    },
    //..............................................
    changeCurrentObj : function(o, theEditor, callback){
        var UI  = this;
        var opt = UI.options;

        //console.log("changeCurrentObj", theEditor);

        // 支持没有 theEditor 的写法
        if(_.isFunction(theEditor)){
            callback  = theEditor;
            theEditor = undefined;
        }

        // 是否可以打开，不能打开的话，打开父目录即可
        // 如果是需要打开主目录，则一定可以
        if(!Wn.isHome(o) && !opt.canOpen(o)){
            var oP = UI.getById(o.pid);
            var oC = UI.getCurrentObj();
            // 只有不同才必要切换
            if(!oC || oP.id != oC.id){
                UI.changeCurrentObj(oP);
                // console.log("do change", oP.nm, oC.nm)
            }
            return;
        }

        // 更新历史记录，从历史记录可以恢复编辑器
        if(opt.history){
            var oldEditor = UI._update_history(o, theEditor);
            theEditor = theEditor || oldEditor;
        }
        //console.log("the editor", theEditor, location);

        // 临时记录当前的对象
        UI.setCurrentObjId(o.id);

        // 动态读取对象对应
        if("auto" == opt.appSetup){
            var UI = this;

            Wn.loadAppSetup(o, {
                context : UI,
                editor  : theEditor
            }, function(o, asetup){
                UI.__call_subUI_update(o, asetup, callback);
            });  
        }
        // 采用默认的策略，只有普通文件夹才能打开
        // 否则相当于打开对象的父目录，同时菜单项只有有限的几个
        else{
            var asetup;
            // 给定 appSetup 的获取逻辑
            if(_.isFunction(opt.appSetup)){
                asetup = opt.appSetup.call(UI, o);
            }
            // 给定一个静态的 appSetup
            else if(_.isObject(opt.appSetup)){
                asetup = $z.clone(opt.appSetup);
            }
            // 否则给个默认的
            else{
                asetup = {
                    actions : ["@::viewmode"]
                };
            }
            Wn.extendAppSetup(asetup);
            // 调用个个子 UI 的更新
            UI.__call_subUI_update(o, asetup, callback);
        }
    },
    //..............................................
    updateMenuByObj : function(o, theEditor, menuContext){
        var UI = this;
        Wn.loadAppSetup(o, {
            context : UI,
            editor  : theEditor
        }, function(o, asetup){
            UI.subUI("sky").updateMenu(asetup.menu, menuContext);
        });  
    },
    //..............................................
    updateMenu : function(menuSetup, menuContext){
        var UI = this;
        if(UI.gasket.sky){
            UI.gasket.sky.updateMenu(menuSetup, menuContext);
        }
    },
    //..............................................
    __call_subUI_update : function(o, asetup, callback){
        var UI  = this;
        var opt = UI.options;

        if(UI.subUI("sky"))
            UI.subUI("sky").update(o, asetup);

        if(UI.subUI("chute"))
            UI.subUI("chute").update(o, asetup);

        // 当前主区域绘制完成后，需要调用回调
        UI.subUI("main").update(o, asetup, callback);
        
        // 持久记录最后一次位置
        if(opt.lastObjId){
            UI.local(UI.options.lastObjId, o.id);
        }
    },
    //..............................................
    extend_actions : function(actions, forceTop, isInEditor){
        return Wn.extendActions(actions, forceTop, isInEditor);
    },
    //..............................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 初始化显示隐藏开关 
        UI.arena.attr("hidden-obj-visibility", UI.getHiddenObjVisibility());

        // 显示各个 UI
        var uiTypes = [];

        //......................................
        // Sky
        if(opt.skybar) {
            uiTypes.push("sky");
            new SkyUI({
                parent : UI,
                gasketName : "sky"
            }).render(function(){
                UI.defer_report("sky");
            });
        }
        // 否则移除
        else {
            UI.arena.find(".obw-sky").remove();
            UI.arena.find(".obw-con").css("top",0);
        }
        //......................................
        // Chute
        if(opt.sidebar){
            uiTypes.push("chute");
            new ChuteUI({
                parent : UI,
                gasketName : "chute",
                setup : opt.sidebar
            }).render(function(){
                UI.defer_report("chute");
            });
        }
        // 否则移除
        else {
            UI.arena.find(".obw-chute").remove();
            UI.arena.find(".obw-main").css("left",0);
        }
        //......................................
        // Main
        uiTypes.push("main");
        new MainUI({
            parent : UI,
            gasketName : "main"
        }).render(function(){
            UI.defer_report("main");
        });
        //......................................
        // Footer
        if(opt.footbar){
            uiTypes.push("footer");
            new FooterUI({
                parent : UI,
                gasketName : "footer"
            }).render(function(){
                UI.defer_report("footer");
            });
        }
        // 否则移除
        else {
            UI.arena.find(".obw-footer").remove();
            UI.arena.find(".obw-con").css("bottom",0);
        }

        // 返回以便延迟加载
        return uiTypes;
    },
    //..............................................
    setData : function(obj, theEditor, callback){
        var UI = this;

        // 支持没有 theEditor 的写法
        if(_.isFunction(theEditor)){
            callback  = theEditor;
            theEditor = undefined;
        }

        // 没值
        if(!obj){
            // 如果是记录最后一次
            if(UI.options.lastObjId){
                var lastId = UI.local(UI.options.lastObjId);
                if(lastId && Wn.getById(lastId, true)){
                    UI.setData("id:"+lastId, theEditor, callback);
                    return;
                }
            }
            // 看看有没有当前对象
            var c_oid = UI.getCurrentObjId();
            if(c_oid){
                UI.setData("id:"+c_oid, theEditor, callback);
            }
            // 默认采用主目录
            else{
                UI.setData("~", theEditor, callback);
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
            UI.setData(o ? o : "~", theEditor, callback);
            return;
        }

        // 保存到缓冲
        UI.saveToCache(obj);

        // 临时记录当前的对象
        //UI.setCurrentObjId(obj.id);

        // 调整尺寸
        //UI.resize();

        // 改变当前对象
        UI.changeCurrentObj(obj, theEditor, callback);

        // 触发事件
        UI.trigger("browser:change", obj, theEditor);
        $z.invoke(UI.options, "on_change", [obj, theEditor], UI);
        
    },
    //..............................................
    refresh : function(callback){
        var oid = this.getCurrentObjId();
        this.cleanCache("oid:"+oid);
        this.setData("id:"+oid, callback);
    },
    //..............................................
    showUploader: function(options){
        var ta =  this.getCurrentObj();
        Wn.uploadPanel(_.extend({
            target : ta
        }, options));
    },
    //..............................................
    getCurrentEditObj : function(){
        var theUI = this.subUI("main/view")
        if(!theUI)
            return undefined;
        return $z.invoke(theUI, "getCurrentEditObj");
    },
    getCurrentTextContent : function(){
        var theUI = this.subUI("main/view")
        if(!theUI)
            return undefined;
        return $z.invoke(theUI, "getCurrentTextContent");
    },
    getCurrentJsonContent : function(){
        var theUI = this.subUI("main/view")
        if(!theUI)
            return undefined;
        return $z.invoke(theUI, "getCurrentJsonContent");
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
        return this.subUI("sky").getPath();
    },
    getPathObj : function(){
        return this.subUI("sky").getData();
    },
    //..............................................
    getActived : function(){
        return this.subUI("main").getActived();
    },
    setActived : function(arg){
        this.subUI("main").setActived(arg);
        return this;
    },
    getChecked : function(){
        return this.subUI("main").getChecked();
    },
    //..............................................
    // 修改激活项目的名称
    rename : function(){
        this.subUI("main").rename();
    },
    //.............................................. 
    getChildren : function(o, filter, callback){
        return Wn.getChildren(o, filter||this.options.filter, callback);
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
    write : function(o, content, callback, context){
        return Wn.write(o, content, callback, context);
    },
    //..............................................
    get : function(o, quiet){
        if(_.isUndefined(o) || $z.isjQuery(o) || _.isElement(o)){
            return this.subUI("main").getData(o);
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