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
        var UI  = this;
        var opt = UI.options;

        // 清除值
        if(!o) {
            UI.$el.removeData("@OBJ");
            UI.arena.find('textarea').val('');
        }
        // 显示内容
        else {
            UI.$el.data("@OBJ", o);
            Wn.read(o, function(content){
                UI.arena.find("textarea").val(content);
            }, !opt.useCache);
        }
    },
    //...............................................................
    getCurrentEditObj : function(){
        return this.$el.data("@OBJ");
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