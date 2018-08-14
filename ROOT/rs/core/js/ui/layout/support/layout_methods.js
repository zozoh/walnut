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
        //console.log("listen", uiBus, event)
        this.listenUI(uiBus, event, function(be){
            //console.log("handle", event)
            if(listenSelf || be.cid != cid) {
                handler.apply(this, be.args);
            }
        });
    },
    // 发送消息
    fire : function(event, args) {
        var cid  = this.cid;
        var uiBus = this.bus();
        //console.log("fire", event, args)
        uiBus.trigger(event, {
            cid  : cid,
            args : args,
        });
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
    invokeConfCallback : function(fldName, methodName, args){
        var UI   = this;
        var bus  = this.bus();
        var conf = this.getBusConf();
        $z.invoke(conf[fldName], methodName, args, bus);
    },
    //....................................................
    getBusConf : function(keys) {
        return $z.pick(this.initBusConf(), keys);
    },
    //....................................................
    initBusConf : function() {
        var UI   = this;
        var bus  = this.bus();
        var opt  = bus.options;
        var conf = bus.__CONF;

        // 首次的话，初始化配置信息
        if(!conf){
            //console.log("init")
            conf = {
                bus : bus,
                dataMode : opt.dataMode || "thing",
                thumbSize : opt.thumbSize || "256x256"
            };

            // 处理一下通用的配置信息
            conf.searchMenuFltWidthHint = opt.searchMenuFltWidthHint 
                                          || conf.searchMenuFltWidthHint
                                          || "50%";
            conf.searchList   = opt.searchList   || conf.searchList;
            conf.searchSorter = opt.searchSorter || conf.searchSorter;
            conf.searchPager  = opt.searchPager  || conf.searchPager;
            conf.objMenu = opt.objMenu || conf.objMenu;
            conf.extendCommand = opt.extendCommand || conf.extendCommand;

            // 按照模式处理各个配置项
            DATA_MODE[conf.dataMode].call(UI, conf, opt);

            // 如果声明了扩展方法，将其合并到 bus 里
            if(conf.extendCommand && _.isArray(conf.extendCommand.actions)) {
                for(var i=0; i<conf.extendCommand.actions.length; i++) {
                    var aph = conf.extendCommand.actions[i];
                    //console.log(aph);
                    // 读取一下这个函数集合
                    var restr = Wn.exec('cat "' + aph + '"');
                    var funcSet = eval('(' + restr + ')');
                    for(var key in funcSet) {
                        var func = funcSet[key];
                        // 是函数的话就合并到 bus 里
                        // 这样菜单里就能直接调用到了
                        if(_.isFunction(func)) {
                            bus["__ext_"+key] = func;
                        }
                    }
                }
            }

            // 标识处理完成
            bus.__CONF = conf;
        }

        // 返回配置对象
        return conf;
    },
    //....................................................
    // 处理命令的通用回调
    doActionCallback : function(re, ok, fail) {
        var UI = this;
        //console.log("after", re)
        if(!re || /^e./.test(re)){
            UI.alert(re || "empty", "warn");
            $z.doCallback(fail, [re], UI);
            return;
        }
        try {
            var reo = $z.fromJson(re);
            $z.doCallback(ok, [reo], UI);
        }
        // 出错了，还是要控制一下
        catch(E) {
            UI.alert(E, "warn");
            console.warn(E);
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
