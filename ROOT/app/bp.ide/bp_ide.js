(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/bp.ide/bp_ide_nav',
    'app/bp.ide/bp_ide_footer',
    'app/bp.ide/bp_page'
], function(ZUI, Wn, NavUI, FooterUI, PageUI){
//==============================================
var html = function(){/*
<div class="ui-arena bp" ui-fitparent="yes">
    <div class="bp-con-nav"     ui-gasket="nav"></div>
    <div class="bp-con-main"    ui-gasket="main"></div>
    <div class="bp-con-footer"  ui-gasket="footer"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.bp_ide", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/bp.ide/bp_ide.css",
    i18n : "app/bp.ide/i18n/{{lang}}.js",
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 如果指定了外部 outline
        if($z.isjQuery(opt.outline)){
            UI.arena.find(".bp-con-main").css("left",0);
            UI.arena.find(".bp-con-nav").appendTo(opt.outline.empty());
        }

        // 如果指定了外部 footer
        if($z.isjQuery(opt.footer)){
            UI.arena.find(".bp-con-nav, .bp-con-main").css("bottom",0);
            UI.arena.find(".bp-con-footer").appendTo(opt.footer.empty());
        }

        // 添加子 UI
        UI.uiNav = new NavUI({
            parent : UI,
            gasketName : "nav"
        }).render();

        UI.uiFooter = new FooterUI({
            parent : UI,
            gasketName : "footer"
        }).render();
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        //console.log("I am screen update:", o);

        // 更新 nav
        UI.uiNav.update(o);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);