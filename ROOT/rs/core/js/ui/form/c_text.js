(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena com-text"><textarea></textarea></div>
*/};
//===================================================================
return ZUI.def("ui.form_com_text", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    resize : function(){
        var UI  = this;
        var opt = UI.options;
        var W   = UI.$pel.width();
        var H   = UI.$pel.height();

        // 指定宽度
        if(opt.width) {
            UI.arena.css("width", $z.dimension(opt.width, W));
        }

        if(opt.height){
            UI.arena.css("height", $z.dimension(opt.height, H));   
        }
        

    },
    //...............................................................
    getData : function(){
        return this.arena.find("textarea").val();
    },
    //...............................................................
    setData : function(val, jso){
        this.arena.find("textarea").val(jso.toStr());
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);