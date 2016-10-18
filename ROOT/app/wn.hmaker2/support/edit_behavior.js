(function($z){
$z.declare([
    'zui',
], function(ZUI){
//==============================================
var html = `
<div class="ui-arena edit-behavior">
    <div class="ebe-tsa-tip"></section>
    <div class="ebe-tsa-bars"></section>
    <div class="ebe-tsa-areas"></section>
</div>`;
//==============================================
return ZUI.def("ui.edit_behavior", {
    dom  : html,
    css  : 'theme/app/wn.hmaker2/support/edit_behavior.css',
    //...............................................................
    events : {
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        
    },
    //...............................................................
    getData : function() {
        var UI = this;
        
        return UI.arena.text();
    },
    //...............................................................
    /*
    接受格式如 be:[behavior]:args 格式的字符串
     - be: 为前缀，表示行为字符串
     - [behavior] 为行为模式
     - [args] 不同的行为需要的参数也不一样

    现在支持的行为模式
    * toggleShowArea:COM2:B2
    */
    setData : function(str) {
        var UI  = this;
        var opt = UI.options;

        UI.arena.text(str);
        console.log(opt.uiPage)
        
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);