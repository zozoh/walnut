define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    //....................................................
    // 得到当前界面用来做消息通知对象
    bus : function(){
        return this.options.bus || this;
    },
    // 监听消息
    listenBus : function(event, handler, listenSelf){
        var cid   = this.cid;
        var uiBus = this.bus();
        this.listenUI(uiBus, event, function(be){
            if(listenSelf || be.cid != cid) {
                handler.apply(this, be.args);
            }
        });
    },
    // 发送消息
    fire : function() {
        var cid  = this.cid;
        var args = Array.from(arguments);
        var uiBus = this.bus();
        uiBus.trigger.apply(uiBus, [{
            cid  : cid,
            args : args,
        }]);
    },
    //....................................................
    // 每个 UI 都要调用的初始化配置的方法
    __init_conf : function(opt) {

    },
    //....................................................
    getConf : function() {
        return $z.pick(this.options, 
                ["bus",
                 "dataMode","actions",
                 "searchMenu","objMenu",
                 "fields","meta","detail",
                 "media","attachment"]);
    },
    //....................................................
}; // ~End methods

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});
