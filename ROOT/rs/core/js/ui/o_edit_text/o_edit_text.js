(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena otext" ui-fitparent="yes">
<textarea spellcheck="false"></textarea>
</div>
*/};
//==============================================
return ZUI.def("ui.o_edit_text", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/o_edit_text/o_edit_text.css",
    //...............................................................
    update : function(o) {
        var UI = this;
        Wn.read(o, function(content){
            UI.arena.find("textarea").val(content);
        });
    },
    //...............................................................
    getCurrentTextContent : function(){
        return this.arena.find("textarea").val();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);