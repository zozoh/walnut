define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    browser : function(){
        var UI = this;
        while(!UI.__browser__ && UI) {
            UI = UI.parent;
        }
        return UI;
    },
    chuteUI : function() {
        return this.browser().subUI("chute");
    },
    fire : function() {
        var args = Array.from(arguments);
        var uiBrowser = this.browser();
        // console.log("fire", args)
        uiBrowser.trigger.apply(uiBrowser, args);
    },
    invokeCallback : function(methodName, args, context){
        var UI   = this;
        var opt  = UI.browser().options;
        $z.invoke(opt, methodName, args, context || UI);
    },

}; // ~End methods
//====================================================================

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});
