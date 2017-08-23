/**
为可变尺寸元素，提供一些帮助方法
*/
define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    //...............................................................
    // 在给定对象（可能是一组）前面增加一个助手对象，带八个柄以便改变对象大小
    _append_resize_assist : function(eles) {
        var UI = this;

        return $(eles).each(function(){
            $(`<div class="ui-resize-assist">
                <div class="uirz-ai rsz-hdl1" m="N"></div>
                <div class="uirz-ai rsz-hdl1" m="W"></div>
                <div class="uirz-ai rsz-hdl1" m="E"></div>
                <div class="uirz-ai rsz-hdl1" m="S"></div>
                <div class="uirz-ai rsz-hdl2" m="NW"></div>
                <div class="uirz-ai rsz-hdl2" m="NE"></div>
                <div class="uirz-ai rsz-hdl2" m="SW"></div>
                <div class="uirz-ai rsz-hdl2" m="SE"></div>
            </div>`).prependTo(this);
        }); 
    },
    //...............................................................
}; // ~End methods
//====================================================================

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});
