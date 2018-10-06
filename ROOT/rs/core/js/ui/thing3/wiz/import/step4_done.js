(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/pop/pop'
], function(ZUI, Wn, POP){
//==============================================
var html = function(){/*
<div class="ui-arena th3-wiz-done" ui-fitparent="yes">
    <header>
        <i class="zmdi zmdi-check-circle"></i>
    </header>
    <section>{{th3.import.done}}</section>
    <footer></footer>
</div>
*/};
//==============================================
return ZUI.def("app.wn.th3_i_4_done", {
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
            $('<a>').text(UI.msg("th3.import.viewlog"))
                .appendTo(UI.arena.find('footer'));
        }
    }
});
//===================================================================
});
})(window.NutzUtil);