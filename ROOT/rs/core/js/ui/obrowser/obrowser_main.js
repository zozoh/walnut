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
<div class="ui-arena obrowser-main" ui-gasket="view">main again</div>
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
    update : function(UIBrowser, o){
        var UI = this;
        UI.arena.empty();

        // 得到显示模式
        var vmd = UIBrowser.getViewMode();

        // 加载对应的控件
        if(UI.$el.attr("current-viewmode") != vmd){
            seajs.use("ui/obrowser/vmd_"+vmd, function(TheUI){
                (new TheUI({
                    parent : UI,
                    gasketName : "view"
                })).render(function(){
                    this.browser = UIBrowser;
                    this.update(UIBrowser, o);
                });
            });        
        }
        // 没必要切换控件，更新控件的内容就好
        else{
            UI.subUI("view").update(UIBrowser, o);
        }
        

        // 最后重新计算一下尺寸
        UI.resize();
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