define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    browser : function(){
        var UI = this;
        while(UI && !UI.__browser__) {
            UI = UI.parent;
        }
        return UI;
    },
    opt : function(){
        return this.browser().options;
    },
    chuteUI : function() {
        return this.browser().subUI("chute");
    },
    mainUI : function() {
        return this.browser().subUI("main");
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
    //..............................................
    getViewMode : function(){
        return this.browser().local("viewmode") || "thumbnail";
    },
    setViewMode : function(mode){
        var UI = this;
        if(_.isString(mode)
           && /^(table|thumbnail|slider|scroller|icons|columns)$/.test(mode)
           && mode != UI.getViewMode()){

            // 本地存储
            UI.browser().local("viewmode", mode);
            
            // 更新显示
            var o = UI.browser().getCurrentObj();
            UI.mainUI().update(o);
        }
    },
    //..............................................
    getHiddenObjVisibility : function(){
        return this.browser().local("hidden-obj-visibility") || "hidden";
    },
    setHiddenObjVisibility : function(vho){
        var UI = this;
        if(_.isString(vho)
           && /^(show|hidden)$/.test(vho)){
            UI.browser().local("hidden-obj-visibility", vho);
            var o = UI.browser().getCurrentObj();
            UI.mainUI().update(o);
            UI.browser().arena.attr("hidden-obj-visibility", vho);
        }
    },
    //..............................................
}; // ~End methods
//====================================================================

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});
