define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    //....................................................
    // 得到当前界面用来做消息通知对象
    bus : function(){
        return this.__layout_bus;
    },
    //....................................................
    // 得到自己所在区域的 key
    getMyAreaKey : function() {
        var $ar = this.$el.closest('[wl-area]');
        return $ar.attr('wl-key');
    },
    //....................................................
    // 监听消息
    // - ignoreLvl : "self" 无视自己发出的消息, "area" 无视本区域发出消息
    listenBus : function(event, handler, ignoreLvl){
        var cid   = this.cid;
        var uiBus = this.bus();
        //console.log("listen", uiBus, event)
        // 不是监听函数，木有意义
        if(!_.isFunction(handler))
            return;
        this.listenUI(uiBus, event, function(eo){
            //console.log("handle", event)
            // 无视自己
            if('self' == ignoreLvl && this.cid == eo.UI.cid)
                return;
            // 无视本区域
            var myAreaKey = this.getMyAreaKey();
            if('area' == ignoreLvl && myAreaKey == eo.key)
                return; 
            // 调用回调
            handler.apply(this, [eo]);
        });
    },
    //....................................................
    // 发送消息
    fireBus : function(eventType, data) {
        var bus = this.bus();
        var $ar = this.$el.closest('[wl-area]');
        //console.log("fire", event, args)
        bus.fire(eventType, $ar, this, data);
    },
    //....................................................
    invokeUI : function(uiName, methodName, args, context) {
        var bus = this.bus();
        var subUI = bus.subUI(uiName);
        $z.invoke(subUI, methodName, args||[], context);
    }
    //....................................................
}; // ~End methods

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});
