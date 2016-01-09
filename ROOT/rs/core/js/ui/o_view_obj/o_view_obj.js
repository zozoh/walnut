(function($z){
$z.declare([
    'zui',
    'wn/util'
], function(ZUI, Wn){
//==============================================
var html = function(){/*
<div class="ui-arena opreview" ui-fitparent="yes">
    <div class="opreview-wrapper"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.o_view_obj", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/o_view_obj/o_view_obj.css",
    i18n : "ui/o_view_obj/i18n/{{lang}}.js",
    //...............................................................
    events : {
        "dblclick .opreview-wrapper[mode=pic] img" : function(e){
            var jImg = $(e.currentTarget);
            jImg.toggleClass("autofit");
        }
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        var jWrapper = UI.arena.find(".opreview-wrapper").empty();
        
        // 不能预览文件夹
        if(o.race == "DIR"){
            throw "can.not.preview.DIR";
        }

        // 文本内容
        if(/text|javascript|json/.test(o.mime)){
            jWrapper.attr("mode", "text");
            var jPre = $('<pre>').appendTo(jWrapper);
            Wn.read(o, function(content){
                jPre.text(content);
            });
        }
        // 可以预览的图像
        else if(/\/(jpeg|png|gif)/.test(o.mime)){
            jWrapper.attr("mode", "pic");
            var jImg = $("<img>").appendTo(jWrapper);
            jImg.prop("src", "/o/read/id:"+o.id).on("load", function(){
                jImg.attr({
                    "old-width"  : this.width,
                    "old-height" : this.height
                });
                UI.resize();
            });
        }
        // 其他的对象
        else{
            jWrapper.attr("mode","others");
            jWrapper.html(UI.msg("opreview.noway"));
        }
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var jImg = UI.arena.find("img");
        var oW = jImg.attr("old-width") * 1;
        var oH = jImg.attr("old-height") * 1;
        if(oW > 0){
            var jWrapper = UI.arena.find(".opreview-wrapper");
            var W  = jWrapper.width() - 100;
            var H  = jWrapper.height() - 100;
            if(oW > W || oH > H){
                jImg.addClass("autofit");
            }else{
                jImg.removeClass("autofit");
            }
        }
    },
    //...............................................................
    depose : function(){
        this.arena.find("img").unbind();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);