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
    },
    off : function(){
        $(this.root).css("fontSize", "");
    },
    resize : function(){
        //console.log("I am skin.resize");
        do_change_root_font_size(this, 640, 100, 80);
    }
};
//=======================================================================
});

