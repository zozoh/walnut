define(function (require, exports, module) {
//=======================================================================
function do_change_root_font_size(context, designWidth, rem2px){
    var size = (context.win.innerWidth / designWidth) * rem2px;
    context.root.style.fontSize = (size < rem2px ? size : rem2px) + 'px' ;
}
//=======================================================================
module.exports = {
    on : function(){
        //console.log("I am skin.on");
        do_change_root_font_size(this, 640, 100);
    },
    off : function(){
        $(this.root).css("fontSize", "");
    },
    resize : function(){
        //console.log("I am skin.resize");
        do_change_root_font_size(this, 640, 100);
    }
};
//=======================================================================
});

