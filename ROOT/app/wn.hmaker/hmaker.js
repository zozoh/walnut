(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker/hmaker_nav',
    'app/wn.hmaker/hmaker_page',
    'ui/obrowser/vmd_thumbnail'
], function(ZUI, Wn, NavUI, PageUI, ThumbnailUI){
//==============================================
var html = function(){/*
<div class="ui-arena hmaker" ui-fitparent="yes" mode="inside">
    <div class="hmaker-nav">
        <div class="ue-bar0" ui-gasket="menu"></div>
        <div class="ue-list" ui-gasket="tree"></div>
    </div>
    <div class="hmaker-main" ui-gasket="main"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.hmaker/hmaker.css",
    i18n : "app/wn.hmaker/i18n/{{lang}}.js",
    //...............................................................
    events : {
        "click .ue-bar1" : function(e){
            var UI  = this;
            var jMa = UI.arena.find(".hmaker-main");
            jMa.toggleClass("shelf-hide");
            UI.local("shelfHide", jMa.hasClass("shelf-hide"));
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        var jNav  = UI.arena.children(".hmaker-nav");
        var jMain = UI.arena.children(".hmaker-main");
        var jDeta = UI.arena.children(".hmaker-deta");

        // 恢复持久的插入项隐藏设定
        if(UI.local("shelfHide")){
            jMain.addClass("shelf-hide");
        }

        // 指定了外部的大纲视图，自己内部的大纲视图就删掉吧
        if(opt.outline && opt.outline.size()>0) {
            UI.arena.attr("mode", "outside");
            jNav.attr("mode", "outside").appendTo(opt.outline.empty());
            UI.addElement(opt.outline);
            //console.log("outside outline:" + UI.$outline.html());
        }
        
        // 创建侧边栏 UI
        UI.uiNav = new NavUI({
            parent : UI,
            $el    : jNav
        }).render();
    },
    //...............................................................
    change_mainUI : function(o) {
        var UI  = this;

        // html 就打开页面编辑器
        if('html' == o.tp) {
            new PageUI({parent:UI, gasketName:"main"}).render(function(){
                this.update(o);
                UI.parent.updateMenuByObj(o, "hmaker", this);
            });
            UI.browser.setCurrentObjId(o.pid);
        }
        // 如果是目录，就显示缩略图界面
        else if('DIR' == o.race) {
            new ThumbnailUI({parent:UI, gasketName:"main"}).render(function(){
                this.browser = UI.browser;
                this.update(o, UI);
                UI.parent.updateMenuByObj(o, null, this);
            });
            UI.browser.setCurrentObjId(o.id);
        }
        // 不支持
        else {
            throw "change_mainUI fail : " + o.ph;
        }
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        //console.log(UI.uiNav);

        // 显示道航栏
        UI.uiNav.update(o);
    },
    //...............................................................
    getCurrentEditObj : function() {
        return $z.invoke(this.gasket.main, "getCurrentEditObj", []);
    },
    //...............................................................
    getCurrentTextContent : function() {
        return $z.invoke(this.gasket.main, "getCurrentTextContent", []);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);