define(function (require, exports, module) {
var Wn = require("wn/util");
// ....................................
var __format_thing_fld = function(fld) {
    // id
    if("id" == fld.key) {
        $z.setUndefined(fld, "title", "i18n:thing.key.id");
        $z.setUndefined(fld, "hide", true);
        $z.extend(fld, {
            type   : "string",
            editAs : "label"
        });
    }
    // th_nm
    else if("th_nm" == fld.key) {
        $z.setUndefined(fld, "title", "i18n:thing.key.th_nm");
        $z.extend(fld, {
            type : "string",
            editAs : "input",
            escapeHtml : false,
            display : function(o) {
                var html = "";
                if(o.thumb){
                    html += '<img src="/o/thumbnail/id:'+o.id+'?_t='+$z.timestamp()+'">';
                }else{
                    html += '<i class="fa fa-cube th_thumb"></i>';
                }
                html += $z.escapeText(o.th_nm);
                if(o.len > 0) {
                    html += '<span><i class="zmdi zmdi-format-align-left"></i><em>' + o.len + '</em></span>';
                }
                return html;
            }
        });
    }
    // th_enabled
    else if("th_enabled" == fld.key) {
        $z.setUndefined(fld, "title", "i18n:thing.key.th_enabled");
        $z.setUndefined(fld, "escapeHtml", false);
        $z.setUndefined(fld, "hide", false);
        $z.setUndefined(fld, "display", function(o){
            return o.th_enabled ? '<i class="fa fa-toggle-on"></i>'
                                : '<i class="fa fa-toggle-off"></i>';
        });
        _.extend(fld, {
            type   : "boolean",
            editAs : "toggle",
        });
    }
    // th_ow
    else if("th_ow" == fld.key) {
        $z.setUndefined(fld, "title", "i18n:thing.key.th_ow");
        $z.setUndefined(fld, "hide", true);
        $z.extend(fld, {
            type   : "string",
            editAs : "input"
        });
    }
    // lbls
    else if("lbls" == fld.key) {
        $z.setUndefined(fld, "title", "i18n:thing.key.lbls");
        $z.setUndefined(fld, "tip", "i18n:thing.key.lbls_tip");
        $z.extend(fld, {
            type : "object",
            virtual : false,
            editAs : "input",
            uiConf : {
                parseData : function(lbls){
                    if(_.isArray(lbls))
                        return lbls.join(", ");
                    return "";
                },
                formatData : function(str) {
                    if(str)
                        return str.split(/[,， \t]+/g);
                    return [];
                }
            },
            display : function(o) {
                var html = "";
                if(_.isArray(o.lbls)){
                    for(var lbl of o.lbls) {
                        html += '<span class="th-lbl">' + lbl + '</span>';
                    }
                }
                return html;
            }
        });
    }
    // thumb
    else if("thumb" == fld.key) {
        $z.setUndefined(fld, "title", "i18n:thing.key.thumb");
        $z.extend(fld, {
            hide : true,
            type : "string",
            beforeSetData : function(o){
                this.UI.setTarget($z.tmpl("id:{{th_set}}/data/{{id}}/thumb.jpg")(o));
            },
            editAs : "image",
            uiConf : {
                dataType : "idph"
            }
        });
    }
    // 日期
    else if(/^(lm|ct)$/.test(fld.key)) {
        $z.setUndefined(fld, "title", "i18n:thing.key." + fld.key);
        $z.setUndefined(fld, "hide", true);
        fld.type = "datetime";
        $z.extend(fld, {
            type   : "datetime",
            editAs : "label",
        });
    }
    // 递归
    else if(_.isArray(fld.fields)){
        for(var i=0; i<fld.fields.length; i++){
            var subFld = fld.fields[i];
            __format_theConf_field(subFld);
        }
    }
    // 最后返回
    return fld;
};
// ....................................
// 处理不同数据模式的初始化方法
var DATA_MODE = {
    // 标准的 thing
    "thing" : function(conf, opt) {
        var UI = this;
        // 读取指定配置文件
        var oThConf = Wn.fetch("id:"+UI.getHomeObjId()+"/thing.js", true);
        if(oThConf) {
            var json = Wn.read(oThConf);
            var thConf = $z.fromJson(json);
            _.extend(conf, thConf);
        }
        // ----------------- fields
        // 指定了字段
        if(_.isArray(opt.fields) && opt.fields.length > 0)
            conf.fields = [].concat(opt.fields);
        // 为了兼容旧模式，处理指定的字段
        var fields = [];
        for(var i=0; i<conf.fields.length; i++){
            var fld = conf.fields[i];
            // console.log(fld.key, !/^__/.test(fld.key))
            // 特殊字段无视就好
            if(!/^__/.test(fld.key)){
                fields.push(__format_thing_fld(fld));
            }
        }
        conf.fields = fields;

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
        conf.searchMenu = opt.searchMenu || conf.searchMenu || [{
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
        conf.meta = $z.fallbackUndefined(opt.meta, conf.meta, {
            update : function(th, key, callback) {
                var obj  = key ? $z.obj(key, th[key]) : th;
                var json = $z.toJson(obj);

                Wn.execf('thing {{th_set}} update {{id}} -fields', json, th, function(re){
                    var newTh = $z.fromJson(re);
                    $z.doCallback(callback, [newTh]);
                });
            }
        });
        // ----------------- detail
        conf.detail = $z.fallbackUndefined(opt.detail, conf.detail, {
            read : function(th, callback) {
                Wn.execf('thing {{th_set}} detail {{id}}', th, callback);
            },
            save : function(th, det, callback) {
                Wn.execf('thing {{th_set}} detail {{id}} -content', det.content||"", th, function(){
                    if(det.brief || det.tp) {
                        var cmdText = $z.tmpl('thing {{th_set}} detail {{id}}')(th);
                        if(det.tp)
                            cmdText += ' -tp ' + det.tp;
                        // 更新摘要和类型
                        if(det.brief) {
                            Wn.exec(cmdText + ' -brief', det.breif, callback);
                        }
                        // 仅仅更新类型
                        else {
                            Wn.exec(cmdText, callback);
                        }
                    }
                });
            }
        });
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
        //console.log("listen", uiBus)
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
        //console.log("fire", args)
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
            conf = {
                bus : bus,
                dataMode : opt.dataMode || "thing"
            };

            // 按照模式处理各个配置项
            DATA_MODE[conf.dataMode].call(UI, conf, opt);

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
