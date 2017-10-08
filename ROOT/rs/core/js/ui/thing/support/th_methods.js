define(function (require, exports, module) {
var Wn = require("wn/util");
// ....................................
// 处理不同数据模式的初始化方法
var DATA_MODE = {
    // 标准的 thing
    "thing" : function(conf, opt) {
        // ----------------- actions
        conf.actions = _.extend({
            // thing 的默认查询方法
            query : function(params, callback) {
                var UI = this;
                $z.setUndefined(params, "skip",  0);
                $z.setUndefined(params, "limit", 50);
                var cmdText = $z.tmpl("thing {{pid}} query -skip {{skip}}"
                                    + " -limit {{limit}}"
                                    + " -json -pager")(params);
                // 增加排序项
                if(params.sort) {
                    cmdText += " -sort '" + params.sort + "'";
                }
                // 因为条件可能比较复杂，作为命令的输入妥当一点
                Wn.exec(cmdText, params.match || "{}", function(re) {
                    UI.doActionCallback(re, callback);
                });
            }
        }, opt.actions);
        // ----------------- searchMenu
        conf.searchMenu = conf.searchMenu || [{
            icon : '<i class="zmdi zmdi-refresh"></i>',
            text : "i18n:refresh",
            asyncIcon : '<i class="zmdi zmdi-refresh zmdi-hc-spin"></i>',
            asyncText : "i18n:loading",
            asyncHandler : function(jq, mi, callback) {
                this.uis("search").refresh(callback);
            }
        }];
        // ----------------- objMenu
        // ----------------- search
        // ----------------- meta
        // ----------------- detail
        // ----------------- media
        // ----------------- attachment
        // ----------------- folders
        // ----------------- busEvents
    },
    // 普通数据对象
    "obj" : function(conf, opt) {

    }
};
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
    getHomeObjId : function() {
        return this.bus().__HOME_OBJ.id;
    },
    getHomeObj : function() {
        return this.bus().__HOME_OBJ;
    },
    setHomeObj : function(obj) {
        this.bus().__HOME_OBJ = obj;
    },
    //....................................................
    // 获取一个 UI 集合，以便方便访问整个控件簇
    uis : function(key){
        var uiSet = {
            "ME" : this
        };
        this.bus()._fill_context(uiSet);
        return key ? uiSet[key] : uiSet;
    },
    //....................................................
    invokeUI : function(uiName, methodName, args, context) {
        $z.invoke(this.uis(uiName), methodName, args||[], context);
    },
    //....................................................
    getBusConf : function(keys) {
        return $z.pick(this.initBusConf(), keys);
        // return $z.pick(this.options, 
        //         ["bus",
        //          "dataMode","actions",
        //          "searchMenu","objMenu",
        //          "fields","meta","detail",
        //          "media","attachment"]);
    },
    //....................................................
    initBusConf : function() {
        var bus  = this.bus();
        var opt  = bus.options;
        var conf = bus.__CONF;

        // 首次的话，初始化配置信息
        if(!conf){
            conf = {
                bus : bus,
                dataMode : opt.dataMode || "thing"
            };
            // 复制字段
            conf.fields = [].concat(opt.fields);

            // 按照模式处理各个配置项
            DATA_MODE[conf.dataMode](conf, opt);

            // 标识处理完成
            bus.__CONF = conf;
        }

        // 返回配置对象
        return conf;
    },
    //....................................................
    // 处理命令的通用回调
    doActionCallback : function(re, callback) {
        var UI = this;
        if(!re || /^e./.test(re)){
            UI.alert(re || "empty", "warn");
            return;
        }
        try {
            var reo = $z.fromJson(re);
            $z.doCallback(callback, [reo], UI);
        }
        // 出错了，还是要控制一下
        catch(E) {
            UI.alert(E, "warn");
        }
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
