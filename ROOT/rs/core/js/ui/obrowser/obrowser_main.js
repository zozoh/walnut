(function($z){
$z.declare([
    'zui',
    'ui/obrowser/vmd_table',
    'ui/obrowser/vmd_thumbnail ',
    'ui/obrowser/vmd_icons',
    'ui/obrowser/vmd_scroller',
    'ui/obrowser/vmd_columns',
    'ui/obrowser/vmd_slider'
], function(ZUI, MenuUI){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser-main" ui-gasket="view" ui-fitparent="true"></div>
*/};
//==============================================
return ZUI.def("ui.obrowser_main", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    init : function(){
        var UI = this;

        // 记录通用变量
        UI.browser = UI.parent;

        // 监听事件
        UI.on("menu:viewmode", function(vm){
            UI.browser.setViewMode(vm);
        });
        UI.on("menu:showhide", function(isShow){
            UI.browser.setHiddenObjVisibility(isShow ? "show" : "hidden");
        });
    },
    //..............................................
    events : {
        "contextmenu .wnobj" : function(e){
            console.log(e.target)
        }
    },
    //..............................................
    update : function(UIBrowser, o, asetup, callback){
        var UI = this;
        var subView = UI.subUI("view");

        // 准备加载子 UI
        var uiType, uiConf;

        //console.log(asetup)
        // 准备记录 menuContext
        var menuContext;

        // 如果有编辑器，就用编辑器处理
        if(asetup && asetup.currentEditor){
            var ed = asetup.currentEditor;
            // 准备通知消息
            var msg = ed.icon || "";
            msg += " " + UI.text(ed.text) || ed.key;
            msg += ": " + o.nm;
            UIBrowser.trigger("browser:info", msg);

            // 创建编辑器
            uiType = ed.uiType;
            uiConf = $z.extend({},ed.uiConf, {
                editor : ed
            });
            // 支持外部 outline
            if(UIBrowser.subUI("chute")){
                if(ed.outline)
                    uiConf.outline = UIBrowser.subUI("chute").showOutline();
                else
                    UIBrowser.subUI("chute").removeOutline();
            }

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
            var uiChute = UIBrowser.subUI("chute");
            if(uiChute)
                uiChute.removeOutline();
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
            UIBrowser.trigger("browser:info", o.ph);
        }
        // 实在不知道怎么处理了
        else{
            throw "obrowser.warn.fail_open";
        }

        // 为其设置一些帮助属性
        uiConf.on_init = function(){
            this.browser = UIBrowser;
        };

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
            new TheUI(uiConf).render(function(){
                // 绘制菜单
                if(asetup)
                    UIBrowser.updateMenu(asetup.menu, menuContext || this);

                // 更新编辑器内容
                this.update(o, callback);
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
        $z.invoke(this.subUI("view"),"rename");
    },
    //..............................................
    isActived : function(){
        return this.subUI("view").getActived();
    },
    //..............................................
    getActived : function(){
        return this.subUI("view").getActived();
    },
    //..............................................
    setActived : function(arg){
        this.subUI("view").setActived(arg);
    },
    //..............................................
    getChecked : function(){
        return this.subUI("view").getChecked();
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);