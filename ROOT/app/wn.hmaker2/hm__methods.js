define(function (require, exports, module) {
var methods = {
    hmaker : function(){
        var UI = this;
        while(!UI.__hmaker__ && UI) {
            UI = UI.parent;
        }
        return UI;
    },
    // 得到站点主目录
    getHomeObj : function() {
        var uiHMaker = this.hmaker();
        return Wn.getById(uiHMaker.__home_id);
    },
    // 监听消息
    listenBus : function(event, handler){
        var uiHMaker = this.hmaker();
        this.listenUI(uiHMaker, event, handler);
    },
    // 发送消息
    fire : function() {
        var args = Array.from(arguments);
        var uiHMaker = this.hmaker();
        //console.log("fire", args)
        uiHMaker.trigger.apply(uiHMaker, args);
    },
    // 得到 HmPageUI，如果不是，则抛错
    pageUI : function(strict) {
        var UI = this;
        var uiHMaker = this.hmaker();
        var re = uiHMaker.gasket.main;
        // 严格模式
        if(strict){
            if(!re){
                throw 'PageUI Not Loadded!';
            }
            if(re.uiName != "app.wn.hmaker_page"){
                throw 'Not A PageUI: ' + re.uiName;
            }
        }
        if(re && re.uiName != "app.wn.hmaker_page")
            return null;
        return re;
    },
    // 将 rect 按照 posBy 转换成 posVal 字符串
    transRectToPosVal : function(rect, posBy) {
        var re = _.mapObject($z.rectObj(rect, posBy), function(val){
            return Math.round(val) + "px";
        });
        return _.values(re).join(",");
    }

}; // ~End methods
//====================================================================

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});