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
<div class="ui-arena obrowser-main" ui-gasket="view"></div>
*/};
//==============================================
return ZUI.def("ui.obrowser_main", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    events : {
        "contextmenu .wnobj" : function(e){
            console.log(e.target)
        }
    },
    //..............................................
    update : function(UIBrowser, o, asetup){
        var UI = this;
        var subView = UI.subUI("view");

        // 准备加载子 UI
        var uiType, uiConf;

        // 如果有编辑器，就用编辑器处理
        if(asetup && asetup.currentEditor){
            var ed = asetup.currentEditor;
            uiType = ed.uiType;
            uiConf = $z.extend({},ed.uiConf, {
                editor : ed
            });
            // 支持外部 outline
            if(ed.outline)
                uiConf.outline = UIBrowser.subUI("shelf/chute").showOutline();
            else
                UIBrowser.subUI("shelf/chute").removeOutline();

            // 支持外部脚注
            if(ed.footer)
                uiConf.footer = UIBrowser.subUI("shelf/footer").arena;

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
            UIBrowser.subUI("shelf/chute").removeOutline();
            // 得到显示模式
            var vmd = UIBrowser.getViewMode();
            uiType = "ui/obrowser/vmd_" + vmd;
            uiConf = {
                parent : UI,
                gasketName : "view"
            };
        }
        // 实在不知道怎么处理了
        else{
            throw "obrowser.warn.fail_open";
        }

        // 没必要改变视图类型，直接更新就好，如果是这种情况，那么肯定不是打开编辑器喔
        if(subView && UI.$el.attr("ui-type") == uiType){
            subView.update(o, UIBrowser);
            subView.trigger("browser:show", o);
            UI.resize();
        }
        // 渲染新的 UI
        else {
            UI.$el.attr("ui-type", uiType);
            seajs.use(uiType, function(TheUI){
                (new TheUI(uiConf)).render(function(){
                    this.browser = UIBrowser;
                    this.update(o, UIBrowser);
                    this.parent.trigger("browser:show", o);
                    UI.resize();
                });
            });
        }
    },
    //..............................................
    getData : function(arg){
        return this.subUI("view").getData(arg);
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
    getChecked : function(){
        return this.subUI("view").getChecked();
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);