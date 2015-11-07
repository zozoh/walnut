(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="yes">
    {{walcome}} <b></b> | <a href="/u/do/logout">{{exit}}</a>
</div>
*/};
//=======================================================================
return ZUI.def("ui.session", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/session/session.css",
    //...............................................................
    redraw : function(){
        var UI = this;
        var se = UI.app.session;
        UI.arena.find("b").text(se.me);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);