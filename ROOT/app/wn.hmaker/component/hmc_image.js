(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker/component/hmc',
    'ui/menu/menu'
], function(ZUI, Wn, HMC, MenuUI){
//==============================================
return ZUI.def("app.wn.hmaker_com_image", {
    //...............................................................
    checkDom : function(){
        var UI = this;
        var jM = UI.arena.find(".hmc-main");
        var jImg = jM.children("img");
        if(jImg.size() == 0){
            jImg = $('<img>').appendTo(jM);
        }
        if(!jImg.prop("src")){
            var oBlank = Wn.fetchBy("%wn.hmaker: obj $APP_HOME/component/hmc_image_blank.jpg");
            console.log(oBlank)
            jImg.prop("src", "/o/read/id:" + encodeURIComponent(oBlank.id));
        }
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