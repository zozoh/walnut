(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker/component/hmc',
    'ui/menu/menu'
], function(ZUI, Wn, HMC, MenuUI){
//==============================================
var html = function(){/*
<textarea></textarea>
*/};
//==============================================
return ZUI.def("app.wn.hmaker_com_text", {
    //...............................................................
    checkDom : function(){
        var UI = this;
        
        
        // 首先整理 DOM 结构
        var jW = UI.arena.children(".hmc-wrapper");
        if(jW.children().size() == 0){
            jW.html($z.getFuncBodyAsStr(html, true));
        }

        console.log(UI.el.innerHTML)

        // 根据属性生成 css

        // 为 css 创建 <style> 插入到文档头部

    },
    //...............................................................
    redraw : function(){
        var UI  = this;

        // 确保 DOM 结构合法
        UI.checkDom();
        
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);