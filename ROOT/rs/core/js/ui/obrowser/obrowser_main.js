(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/obrowser/support/browser__methods',
    'ui/obrowser/vmd_table',
    'ui/obrowser/vmd_thumbnail',
], function(ZUI, Wn, BrowserMethods){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser-main" ui-gasket="view" ui-fitparent="true"></div>
*/};
//==============================================
return ZUI.def("ui.obrowser_main", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    init : function(){
        var UI = BrowserMethods(this);

        // 监听事件
        UI.on("menu:viewmode", function(vm){
            UI.browser().setViewMode(vm);
        });
        UI.on("menu:showhide", function(isShow){
            UI.browser().setHiddenObjVisibility(isShow ? "show" : "hidden");
        });
    },
    //..............................................
    events : {
        "contextmenu .wnobj" : function(e){
            console.log(e.target)
        }
    },
    //..............................................
    update : function(o, asetup, callback){
        var UI = this;
        var subView = UI.subUI("view");
        var UIBrowser = UI.browser();

        // 准备加载子 UI
        var uiType, uiConf;

        //console.log(asetup)
        // 准备记录 menuContext
        var menuContext;

        // 如果有编辑器，就用编辑器处理
        if(asetup && asetup.currentEditor){
            var ed = asetup.currentEditor;

            //console.log(ed);

            // 准备通知消息
            var msg = ed.icon || "";
            msg += " " + UI.text(ed.text) || ed.key;
            UIBrowser.trigger("browser:info", msg);

            // 创建编辑器
            uiType = ed.uiType;
            uiConf = $z.extend({},ed.uiConf, {
                editor : ed
            });
            // 支持外部 outline
            // if(UIBrowser.subUI("chute")){
            //     if(ed.outline)
            //         uiConf.outline = UIBrowser.subUI("chute").showOutline();
            //     else
            //         UIBrowser.subUI("chute").removeOutline();
            // }

            // 支持外部脚注
            if(ed.footer && UIBrowser.subUI("footer"))
                uiConf.footer = UIBrowser.subUI("footer").arena;

            // 编辑器附着在在哪里呢？
            if(subView && subView.editorGasketName){
                uiConf.parent = subView;
                uiConf.gasketName = subView.editorGasketName;
            }
            // 直接附着在父上
            else{
                uiConf.parent = UI;
                uiConf.gasketName = "view";    
            }
        }
        // 没有编辑器，那么 DIR 还能处理
        else if('DIR' == o.race){
            // 去掉 outline
            var uiChute = UI.chuteUI();
            // 得到显示模式
            var vmd = UIBrowser.getViewMode();
            uiType = "ui/obrowser/vmd_" + vmd;
            uiConf = {
                parent : UI,
                gasketName : "view"
            };

            // 菜单的上下文一定是 obrowser_main
            menuContext = UI;

            // 发出通知
            var msg = Wn.objIconHtml(o) + " <b>" + Wn.objDisplayPath(UI, o.ph, 2) + '</b>';
            UIBrowser.trigger("browser:info", msg);
        }
        // 实在不知道怎么处理了
        else{
            throw "obrowser.warn.fail_open";
        }

        // 没必要改变视图类型，直接更新就好，如果是这种情况，那么肯定不是打开编辑器喔
        // if(subView && UI.$el.attr("ui-type") == uiType){
        //     subView.update(o, callback);
        //     subView.trigger("browser:show", o);
        //     UI.resize();
        // }
        // 渲染新的 UI
        //else {
        UI.$el.attr("ui-type", uiType);
        seajs.use(uiType, function(TheUI){
            // 确保子 UI 有 browser 方法可以获取到 browser 实例
            if(_.isFunction(uiConf.on_init)){
                uiConf.__on_init = uiConf.on_init();
            }
            uiConf.on_init = function(opt) {
                this.browser = UI.browser;

                var opt = this.options;
                if(opt.__on_init) {
                    opt.__on_init.call(this, [opt]);
                }
            }

            // 创建实例
            new TheUI(uiConf).render(function(){
                // 绘制菜单
                if(asetup)
                    UIBrowser.updateMenu(asetup.menu, menuContext || this);

                // 更新编辑器内容
                if(_.isFunction(this.update)){
                    this.update(o, function(){
                        $z.doCallback(callback, [], UIBrowser);
                        UI.resize();
                    }, asetup ? asetup.args : null);
                }
                this.parent.trigger("browser:show", o);
                UI.resize();
            });
        });
        //}
    },
    //..............................................
    updateMenuByObj : function(o, theEditor, menuContext){
        this.parent.updateMenuByObj(o, theEditor, menuContext);
    },
    //..............................................
    getData : function(arg){
        return this.subUI("view").getData(arg);
    },
    //..............................................
    // 修改激活项目的名称
    rename : function(){
        var UI  = this;
        var opt = UI.opt();
        if(opt.renameable)
            $z.invoke(UI.subUI("view"),"rename");
    },
    //..............................................
    isActived : function(){
        return this.getActived() ? true : false;
    },
    //..............................................
    getActived : function(){
        return $z.invoke(this.subUI("view"), "getActived");
    },
    //..............................................
    setActived : function(arg){
        return $z.invoke(this.subUI("view"), "setActived", [arg]);
    },
    //..............................................
    getChecked : function(){
        return $z.invoke(this.subUI("view"), "getChecked");
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);