define(function (require, exports, module) {
//=======================================================================
function do_change_root_font_size(context, designWidth, maxNb, minNb){
    var size = (context.win.innerWidth / designWidth) * maxNb;
    //console.log(size, Math.max(size, minNb), Math.min(Math.max(size, minNb), maxNb))
    var px = Math.min(Math.max(size, minNb), maxNb);
    context.root.style.fontSize = px + 'px' ;
}
//=======================================================================
module.exports = {
    on : function(){
        //console.log("I am skin.on");
        do_change_root_font_size(this, 640, 100, 80);

        // 绑定事件
        $(".skin-image-mtlogo").click(function(e){
            e.stopPropagation();
            $z.toggleAttr(".skin-navmenu-0", "show");
        });
        $(this.doc.body).click(function(){
            $(".skin-navmenu-0").removeAttr("show");
        });
    },
    off : function(){
        $(this.root).css("fontSize", "");
    },
    resize : function(){
        //console.log("I am skin.resize");
        do_change_root_font_size(this, 640, 100, 70);
    }
};
//=======================================================================
});

