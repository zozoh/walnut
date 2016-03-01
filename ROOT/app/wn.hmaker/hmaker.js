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
    <div class="hmaker-deta"><div class="hmaker-deta-wrapper">
        <div class="ue-com-title"></div>
        <div class="ue-com-prop"></div>
    </div></div>
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
        },
        "click .ue-ssize i" : function(e){
            var jq = $(e.currentTarget);
            // 高亮
            jq.parent().children().removeClass("highlight");
            jq.addClass("highlight");
            // 设置
            this.updateScreenSize(jq.attr("val"));
        },
        "change .ue-ssize input" : function(e){
            var UI  = this;
            var jq  = $(e.currentTarget);
            var val = jq.val();
            // 没值，用默认的
            if(!val){
                jq.val(jq.attr("oldv") || "");
                return;
            }
            // 确保是数字 
            if(!/^\d+$/.test(jq.val())){
                alert(UI.msg("hmaker.e_nonb"));
                jq.val(jq.attr("oldv") || "");
                return;
            }
            // 设置吧
            var jIx = UI.arena.find(".ue-ssize input[name=x]");
            var jIy = UI.arena.find(".ue-ssize input[name=y]");
            var val = (jIx.val()*1) + "x" + (jIy.val()*1);
            this.updateScreenSize(val);
        }
    },
    //...............................................................
    updateScreenSize : function(val){
        var UI  = this;
        var jIx = UI.arena.find(".ue-ssize input[name=x]");
        var jIy = UI.arena.find(".ue-ssize input[name=y]");
        var jSt = UI.arena.find(".ue-stage");
        var jSc = jSt.children(".ue-screen");
        // 限制宽高
        if(val) {
            jSt.attr("mode", "mobile");
            var m = /^(\d+)x(\d+)$/.exec(val);
            var w = m[1] * 1;
            var h = m[2] * 1;
            jSc.css({
                width  : w,
                height : h
            });
            jIx.val(w).attr("oldv",w); jIy.val(h).attr("oldv",h);
        }
        // 全屏模式
        else {
            jSt.attr("mode", "pc");
            jSc.css({
                width  : "",
                height : ""
            });
            jIx.val("").removeAttr("oldv"); jIy.val("").removeAttr("oldv");
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
            // 已经打开页面编辑器了，那么就更新就好了
            // if(UI.gasket.main && PageUI.uiName == UI.gasket.main.uiName ){
            //     UI.gasket.main.update(o);
            // }
            // // 重新建立页面编辑器
            // else{
                new PageUI({parent:UI, gasketName:"main"}).render(function(){
                    this.update(o);
                    UI.parent.updateMenuByObj(o, "hmaker");
                });
            //}
        }
        // 如果是目录，就显示缩略图界面
        else if('DIR' == o.race) {
            new ThumbnailUI({parent:UI, gasketName:"main"}).render(function(){
                this.update(o, UI);
                UI.parent.updateMenuByObj(o);
            });
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
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);