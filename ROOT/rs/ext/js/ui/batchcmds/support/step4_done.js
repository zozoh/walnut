(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/pop/pop'
], function(ZUI, Wn, POP){
//==============================================
var html = function(){/*
<div class="ui-arena bc-step4-done" ui-fitparent="yes">
    <header>
        <i class="zmdi zmdi-check-circle"></i>
    </header>
    <section>执行完毕</section>
    <footer></footer>
</div>
*/};
//==============================================
return ZUI.def("ui.ext.bc_step4_done", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "click footer a" : function() {
            var UI = this;
            var logs = UI.parent.getData().importLog || [];
            var logStr = logs.join("\n");
            POP.openViewTextPanel({
                width  : "90%",
                height : "90%",
                data   : logStr
            }, UI);
        }
    },
    //...............................................................
    setData : function(data) {
        var UI = this;
        //console.log(data)
        if(data.importLog && data.importLog.length > 0) {
            $('<a>').text('查看日志')
                .appendTo(UI.arena.find('footer'));
        }
    }
});
//===================================================================
});
})(window.NutzUtil);