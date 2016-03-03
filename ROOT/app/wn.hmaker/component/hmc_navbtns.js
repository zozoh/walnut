(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker/component/hmc',
    'ui/menu/menu'
], function(ZUI, Wn, HMC, MenuUI){
//==============================================
var html = function(){/*
<div class="navbtns"></div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_com_navbtns", {
    //...............................................................
    checkDom : function(){
        var UI = this;
        var jW = UI.arena.find(".hmc-wrapper");
        
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 确保 DOM 结构合法
        UI.checkDom();

        // 标题
        opt.$title.html(opt.titleHtml);
        
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);