(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/bp.ide/bp_ide_stage',
    'app/bp.ide/bp_ide_prop',
], function(ZUI, Wn, NavUI, ToolbarUI, StageUI){
//==============================================
var html = function(){/*
<div class="ui-arena bp-main" ui-fitparent="yes">
    <div class="bp-con-add" ui-gasket="insert"></div>
    <div class="bp-con-c0"><div class="bp-c0">
        <div class="bp-con-toolbar" ui-gasket="toolbar"></div>
        <div class="bp-con-stage"   ui-gasket="stage"></div>
    </div></div>
    <div class="bp-con-prop" ui-gasket="prop"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.bp_page", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 如果指定了外部 outline
        if($z.isjQuery(opt.outline)){
            UI.arena.find(".bp-con-c0").css("left",0);
            UI.arena.find(".bp-con-nav").appendTo(opt.outline.empty());
        }

        // 如果指定了外部 footer
        if($z.isjQuery(opt.footer)){
            UI.arena.find(".bp-con-stage").css("bottom",0);
            UI.arena.find(".bp-con-footer").appendTo(opt.footer.empty());
        }

        // 添加子 UI
        UI.uiNav = new NavUI({
            parent : UI,
            gasketName : "nav"
        }).render();

        UI.uiToolbar = new ToolbarUI({
            parent : UI,
            gasketName : "toolbar"
        }).render();

        UI.uiStage = new StageUI({
            parent : UI,
            gasketName : "stage"
        }).render();

        UI.uiFooter = new FooterUI({
            parent : UI,
            gasketName : "footer"
        }).render();

        UI.uiProp = new PropUI({
            parent : UI,
            gasketName : "prop"
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