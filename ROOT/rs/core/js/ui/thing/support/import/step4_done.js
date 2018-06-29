(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/pop/pop'
], function(ZUI, Wn, POP){
//==============================================
var html = function(){/*
<div class="ui-arena th-import-4-done" ui-fitparent="yes">
    <header>
        <i class="zmdi zmdi-check-circle"></i>
    </header>
    <section>{{thing.import.in_done}}</section>
    <footer></footer>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thi_4_done", {
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
            $('<a>').text(UI.msg("thing.import.viewlog"))
                .appendTo(UI.arena.find('footer'));
        }
    }
});
//===================================================================
});
})(window.NutzUtil);