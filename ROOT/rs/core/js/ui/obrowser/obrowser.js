(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/shelf/shelf',
    'ui/obrowser/obrowser_sky',
    'ui/obrowser/obrowser_main',
    'ui/obrowser/obrowser_footer'
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
    css  : ["theme/ui/obrowser/obrowser.css",
            "theme/ui/obrowser/thumbnail.css", 
            "theme/ui/support/oicons.css"],
    i18n : "ui/obrowser/i18n/{{lang}}.js",
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
        var UI = this;

        $z.setUndefined(options, "sidebar", true);
        $z.setUndefined(options, "checkable", true);
        $z.setUndefined(options, "editable", false);
        // 控制打开 
        UI.__eval_canOpen(options);
        
        // 默认菜单条
        $z.setUndefined(options, "appSetup", {
            actions : ["@::viewmode"]
        });
        
        // 绑定 UI 间的监听关系
        UI.on("browser:change", function(o, theEditor){
            UI.changeCurrentObj(o, theEditor);
        });
        UI.on("change:viewmode", function(){
            var o = UI.getCurrentObj();
            UI.subUI("shelf/main").update(UI, o);
        });
        UI.on("menu:viewmode", function(vm){
            this.setViewMode(vm);
        });
        UI.on("change:hidden-obj-visibility", function(){
            var o = UI.getCurrentObj();
            UI.subUI("shelf/main").update(UI, o);
        });
        UI.on("menu:showhide", function(isShow){
            this.setHiddenObjVisibility(isShow ? "show" : "hidden");
        });

        // 绑定历史记录
        if(options.history){
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
    changeCurrentObj : function(o, theEditor){
        var UI  = this;
        var opt = UI.options;

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
                UI.__call_subUI_update(o, asetup);
            });  
        }
        // 采用默认的策略，只有普通文件夹才能打开
        // 否则相当于打开对象的父目录，同时菜单项只有有限的几个
        else{
            var asetup;
            if(_.isFunction(opt.appSetup)){
                asetup = opt.appSetup.call(UI, o);
            }
            else if(_.isObject(opt.appSetup)){
                asetup = $z.clone(opt.appSetup);
            }
            else{
                throw "Unsupport appSetup: " + opt.appSetup;
            }
            Wn.extendAppSetup(asetup);
            // 调用个个子 UI 的更新
            UI.__call_subUI_update(o, asetup);
        }
        
    },
    //..............................................
    updateMenuByObj : function(o, theEditor){
        var UI = this;
        Wn.loadAppSetup(o, {
            context : UI,
            editor  : theEditor
        }, function(o, asetup){
            UI.subUI("shelf/sky").updateMenu(UI, o, asetup);
        });  
    },
    //..............................................
    __call_subUI_update : function(o, asetup){
        var UI = this;
        //try{
        var uiChute = UI.subUI("shelf/chute");
        if(uiChute)
            uiChute.update(UI, o, asetup);
        UI.subUI("shelf/main").update(UI, o, asetup);
        UI.subUI("shelf/sky").update(UI, o, asetup);
        // 持久记录最后一次位置
        if(UI.options.lastObjId){
            UI.local(UI.options.lastObjId, o.id);
        }
        // }
        // catch(eKey){
        //     alert(UI.msg(eKey));
        //     throw eKey;
        // }
    },
    //..............................................
    extend_actions : function(actions, forceTop, isInEditor){
        return Wn.extendActions(actions, forceTop, isInEditor);
    },
    //..............................................
    redraw : function(){
        var UI = this;

        // 初始化显示隐藏开关 
        UI.arena.attr("hidden-obj-visibility", UI.getHiddenObjVisibility());

        // 准备配置
        var conf = {
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
            main : {
                uiType : "ui/obrowser/obrowser_main",
                uiConf : {
                    className : "obrowser-block",
                    fitparent : true
                }
            },
            footer : {
                uiType : "ui/obrowser/obrowser_footer",
                uiConf : {
                    className : "obrowser-block",
                    fitparent : true
                }
            },

        };

        // 是否显示侧边栏
        if(UI.options.sidebar){
            conf.display.chute = 180;
            conf.chute = {
                uiType : "ui/obrowser/obrowser_chute",
                uiConf : {
                    className : "obrowser-block",
                    fitparent : true
                }
            };
        }

        // 初始化界面
        (new ShelfUI(conf)).render(function(){
            // 回报延迟加载
            UI.defer_report(0, "browser-shelf");
        });

        // 返回延迟加载
        return ["browser-shelf"];
    },
    //..............................................
    setData : function(obj, theEditor){
        var UI = this;
        // 没值
        if(!obj){
            // 如果是记录最后一次
            if(UI.options.lastObjId){
                var lastId = UI.local(UI.options.lastObjId);
                if(lastId){
                    UI.setData("id:"+lastId, theEditor);
                    return;
                }
            }
            // 看看有没有当前对象
            var c_oid = UI.getCurrentObjId();
            if(c_oid){
                UI.setData("id:"+c_oid, theEditor);
            }
            // 默认采用主目录
            else{
                UI.setData("~", theEditor);
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
            UI.setData(o ? o : "~", theEditor);
            return;
        }

        // 保存到缓冲
        UI.saveToCache(obj);

        // 临时记录当前的对象
        //UI.setCurrentObjId(obj.id);

        // 调整尺寸
        //UI.resize();

        // 触发事件
        UI.trigger("browser:change", obj, theEditor);
        $z.invoke(UI.options, "on_change", [obj, theEditor], UI);
        
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
        if(_.isString(mode)
           && /^(table|thumbnail|slider|scroller|icons|columns)$/.test(mode)
           && mode != this.getViewMode()){
            this.local("viewmode", mode);
            this.trigger("change:viewmode", mode);
        }
    },
    //..............................................
    getHiddenObjVisibility : function(){
        return this.local("hidden-obj-visibility") || "hidden";
    },
    setHiddenObjVisibility : function(vho){
        if(_.isString(vho)
           && /^(show|hidden)$/.test(vho)){
            this.local("hidden-obj-visibility", vho);
            this.trigger("change:hidden-obj-visibility", vho);
            this.arena.attr("hidden-obj-visibility", vho);
        }
    },
    //..............................................
    getCurrentEditObj : function(){
        var theUI = this.subUI("shelf/main/view")
        if(!theUI)
            return undefined;
        return $z.invoke(theUI, "getCurrentEditObj");
    },
    getCurrentTextContent : function(){
        var theUI = this.subUI("shelf/main/view")
        if(!theUI)
            return undefined;
        return $z.invoke(theUI, "getCurrentTextContent");
    },
    getCurrentJsonContent : function(){
        var theUI = this.subUI("shelf/main/view")
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
    getChildren : function(o, filter){
        return Wn.getChildren(o, filter||this.options.filter);
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