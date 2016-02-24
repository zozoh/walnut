(function($z){
$z.declare([
    'zui'
], function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="hour" class="tmln-hour">
    </div>
</div>
<div class="ui-arena tmln" ui-fitparent="yes">
    <div class="tmlnW"></div>
    <div class="tmln-ruler"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.timeline", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/timeline/timeline.css",
    init : function(options){
        var UI  = this;
        var opt = options;
    },
    //..............................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;
        var jW  = UI.arena.find(".tmlnW");
        var jRu = UI.arena.find(".tmln-ruler");

        // 输出 24 个小时的时间槽和标尺
        for(var i=0;i<24;i++){
            var sec = i * 3600;
            var key = (i>9?"":"0")+i+":00";
            // 时间槽
            $('<div class="tmln-hour">')
                .attr("sec", sec)
                .attr("key", key)
                .appendTo(jW);
            // 标尺
            $('<div class="tmln-rui">')
                .attr("sec", sec)
                .attr("key", key)
                .text(key)
                .appendTo(jRu);
        }


    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);