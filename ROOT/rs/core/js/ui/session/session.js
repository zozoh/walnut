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
        $z.evalData(UI.options.data, null, function(se){
            UI.arena.find("b").text(se.me);
        }, UI);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);