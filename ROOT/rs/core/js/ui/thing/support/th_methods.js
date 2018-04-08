define(function (require, exports, module) {
var Wn = require("wn/util");
// ....................................
var __format_thing_fld = function(UI, fld) {
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
            tip  : "i18n:thing.keytip.thumb",
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

    // 如果没有声明字段的标题，看看是否需要自动为其声明
    if(!fld.title) {
        var keyTitle = UI.str("thing.key." + fld.key, "");
        if(keyTitle){
            fld.title = keyTitle;
        }
    }

    // 最后返回
    return fld;
};
// ....................................
// 处理不同数据模式的初始化方法
var DATA_MODE = {
    ///////////////////////////////////
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
                fields.push(__format_thing_fld(UI, fld));
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
                var cmdText = $z.tmpl("thing {{pid}} query '<%=match%>' -skip {{skip}}"
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
            },
            // thing 的默认创建方法
            create : function(objName, callback){
                var UI = this;
                var oHome = UI.getHomeObj();
                var cmdText = "thing {{id}} create '{{nm}}'";
                Wn.execf(cmdText, {
                    id : UI.getHomeObjId(),
                    nm : objName.replace(/'/g, ""),
                }, function(re) {
                    UI.doActionCallback(re, callback);
                });
            },
            // thing 的默认移除方法
            remove : function(objList, callback) {
                var UI = this;
                if(_.isArray(objList) && objList.length > 0) {
                    var cmdText = "thing " + UI.getHomeObjId() + " delete -l";
                    for(var i=0; i<objList.length; i++) {
                        var obj = objList[i];
                        cmdText += " " + obj.id;
                    }
                    Wn.exec(cmdText, function(re) {
                        UI.doActionCallback(re, callback);
                    });
                }
                // 否则直接调用回调
                else {
                    $z.doCallback(callback, [[]]);
                }
            }
        }, opt.actions);
        // ----------------- searchMenu
        conf.searchMenu = opt.searchMenu || conf.searchMenu || [{
            // 命令: 创建
            icon : '<i class="zmdi zmdi-flare"></i>',
            text : "i18n:thing.create",
            handler : function() {
                this.uis("search").createObj();
            }
        }, {
            // 命令: 刷新
            icon : '<i class="zmdi zmdi-refresh"></i>',
            tip  : "i18n:thing.refresh_tip",
            asyncIcon : '<i class="zmdi zmdi-refresh zmdi-hc-spin"></i>',
            asyncHandler : function(jq, mi, callback) {
                this.uis("search").refresh(callback, true);
            }
        }, {
            // 命令: 删除
            icon : '<i class="fa fa-trash"></i>',
            tip  : "i18n:thing.rm_tip",
            handler : function() {
                this.uis("search").removeChecked(null, function(o){
                    return o.th_live >= 0 ? o : null;
                });
            }
        }, {
            icon  : '<i class="zmdi zmdi-more-vert"></i>',
            items : [{
                // 命令: 清空回收站
                icon : '<i class="fa fa-eraser"></i>',
                text : "i18n:thing.clean_do",
                handler : function() {
                    var UI = this;
                    UI.confirm("thing.clean_confirm", {
                        icon : "warn",
                        ok : function(){
                            var cmdText = "thing " + UI.getHomeObjId() + " clean";
                            Wn.logpanel(cmdText, function(){
                                $z.invoke(UI.bus(), "showBlank");
                                UI.uis("search").refresh(true);
                            });
                        }
                    });
                }
            }, {
                // 命令: 显示回收站
                icon : '<i class="fa fa-trash-o"></i>',
                text : "i18n:thing.clean_show",
                handler : function() {
                    this.uis("search").setKeyword("th_live=-1");
                }
            }, {
                // 命令: 从回收站中恢复
                icon : '<i class="zmdi zmdi-window-minimize"></i>',
                text : "i18n:thing.clean_restore",
                handler : function() {
                    var UI  = this;

                    // 得到选中的对象们
                    var list = UI.uis("search").getChecked();
                    // 判断 th_live == -1 的对象
                    var checkedObjs = [];
                    for(var i=0; i<list.length; i++) {
                        var obj = list[i];
                        if(obj.th_live < 0)
                            checkedObjs.push(obj);
                    }

                    // 没有对象，显示警告
                    if(checkedObjs.length == 0){
                        UI.alert("thing.err.restore_none", "warn");
                        return;
                    }

                    // 组装命令
                    var cmdText = "thing " + UI.getHomeObjId() + " restore -l";
                    for(var i=0; i<checkedObjs.length; i++) {
                        var obj = checkedObjs[i];
                        cmdText += " " + obj.id;
                    }

                    // 执行命令后清空对象显示，并刷新列表
                    Wn.exec(cmdText, function(re) {
                        $z.invoke(UI.bus(), "showBlank");
                        UI.uis("search").refresh(true);
                    });
                }
            }, {
                // 命令: 弹出配置界面
                icon : '<i class="fa fa-gears"></i>',
                text : "i18n:thing.conf_setup",
                handler : function() {
                    this.fire("setup");
                }
            }]
        }];
        // ----------------- meta
        conf.meta = $z.fallback([undefined, true], [opt.meta, conf.meta], {
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
        conf.detail = $z.fallback([undefined, true], [opt.detail, conf.detail], {
            read : function(th, callback) {
                Wn.execf('thing {{th_set}} detail {{id}}', th, callback);
            },
            save : function(th, det, callback) {
                //console.log(det);
                Wn.execf('thing {{th_set}} detail {{id}} -content', det.content||"", th, function(){
                    if(!_.isUndefined(det.brief) || det.tp) {
                        var cmdText = $z.tmpl('thing {{th_set}} detail {{id}}')(th);
                        if(det.tp)
                            cmdText += ' -tp ' + det.tp;
                        // 更新摘要和类型
                        if(!_.isUndefined(det.brief)) {
                            Wn.exec(cmdText + ' -brief', det.brief||"null", callback);
                        }
                        // 仅仅更新类型
                        else {
                            Wn.exec(cmdText, callback);
                        }
                    }
                    // 否则直接回调了
                    else {
                        $z.doCallback(callback);
                    }
                });
            }
        });
        //------------------------- 定义 media/attachment 的设置方法
        var __setup_files = function(conf, mode) {
            conf[mode] = $z.fallback([undefined, true], [opt[mode], conf[mode]], {
                list : function(th, callback) {
                    Wn.execf('thing {{th_set}} '+mode+' {{id}}', th, function(re){
                        var list = $z.fromJson(re);
                        $z.doCallback(callback, [list]);
                    });
                },
                remove : function(th, list, callback) {
                    var cmdText = 'thing {{th_set}} '+mode+' {{id}} -del';
                    for(var i=0; i<list.length; i++) {
                        var oM = list[i];
                        cmdText += ' "' + oM.nm + '"'; 
                    }
                    Wn.execf(cmdText, th, function(re){
                        var list = $z.fromJson(re);
                        $z.doCallback(callback, [list]);
                    });
                },
                upload : function(setup) {
                    var th = setup.obj;
                    var ph = "id:"+th.th_set+"/data/"+th.id+"/"+mode+"/";
                    var url = "/o/upload/<%=ph%>";
                    url += "?nm=<%=file.name%>";
                    url += "&sz=<%=file.size%>";
                    url += "&mime=<%=file.type%>";
                    if(!setup.overwrite)
                        url += '&dupp=${major}(${nb})${suffix}';
                    $z.uploadFile({
                        file : setup.file,
                        url  : url,
                        ph   : "id:"+th.th_set+"/data/"+th.id+"/"+mode+"/",
                        evalReturn : "ajax",
                        progress : function(e){
                            $z.invoke(setup, "progress", [e.loaded/e.total]);
                        },
                        done : function(newObj){
                            Wn.execf('thing {{th_set}} '+mode+' {{id}} -ufc',th,function(){
                                setup.done.apply(this, [newObj]);
                            });
                        },
                        fail : setup.fail,
                    });
                }
            });
            $z.setUndefined(conf[mode], "multi", true);
            $z.setUndefined(conf[mode], "overwrite", true);
        };
        // ----------------- media
        __setup_files(conf, "media");
        $z.setUndefined(conf.media, "filter", function(f){
            return true;
        });
        // ----------------- attachment
        __setup_files(conf, "attachment");
    },
    ///////////////////////////////////
    // 普通数据对象
    "obj" : function(conf, opt) {
        var UI = this;
        // ----------------- fields
        // 指定了字段
        if(_.isArray(opt.fields) && opt.fields.length > 0){
            conf.fields = [].concat(opt.fields);
        }
        // 设定默认字段
        else {
            conf.fields = [{
                key : "nm",
                title : "Name",
            }];
        }

        // ----------------- actions
        conf.actions = _.extend({
            // 默认查询方法
            query : function(params, callback) {
                //console.log(params)
                var UI = this;
                $z.setUndefined(params, "skip",  0);
                $z.setUndefined(params, "limit", 50);
                $z.setUndefined(params, "match", "{}");
                $z.setUndefined(params, "sort", "{}");
                var cmdText = $z.tmpl("obj id:" + UI.getHomeObjId()
                                    + " -match '<%=match%>' -sort '<%=sort%>' -skip {{skip}}"
                                    + " -limit {{limit}}"
                                    + " -json -l -pager")(params);
                // 执行命令
                Wn.exec(cmdText, function(re) {
                    UI.doActionCallback(re, callback);
                });
            },
            // 默认创建方法
            create : function(objName, callback){
                var UI = this;
                var cmdText = "obj id:{{id}} -new \"nm:'{{nm}}', race:'FILE'\" -o";
                Wn.execf(cmdText, {
                    id : UI.getHomeObjId(),
                    nm : objName.replace(/['"]/g, ""),
                }, function(re) {
                    UI.doActionCallback(re, callback);
                });
            },
            // 默认移除方法
            remove : function(objList, callback) {
                var UI = this;
                //console.log("remove", objList)
                if(_.isArray(objList) && objList.length > 0) {
                    var cmdText = "" ;
                    for(var i=0; i<objList.length; i++) {
                        var obj = objList[i];
                        cmdText += "rm id:" + obj.id + ";";
                    }
                    Wn.exec(cmdText, callback);
                }
                // 否则直接调用回调
                else {
                    $z.doCallback(callback);
                }
            }
        }, opt.actions);
        // ----------------- searchMenu
        conf.searchMenu = opt.searchMenu || conf.searchMenu || [{
            // 命令: 创建
            icon : '<i class="zmdi zmdi-flare"></i>',
            text : "i18n:thing.create",
            handler : function() {
                this.uis("search").createObj();
            }
        }, {
            // 命令: 刷新
            icon : '<i class="zmdi zmdi-refresh"></i>',
            tip  : "i18n:thing.refresh_tip",
            asyncIcon : '<i class="zmdi zmdi-refresh zmdi-hc-spin"></i>',
            asyncHandler : function(jq, mi, callback) {
                this.uis("search").refresh(callback, true);
            }
        }, {
            // 命令: 删除
            icon : '<i class="fa fa-trash"></i>',
            tip  : "i18n:del",
            handler : function() {
                this.uis("search").removeChecked();
            }
        }];
        // ----------------- meta
        conf.meta = $z.fallback([undefined, true], [opt.meta, conf.meta], {
            update : function(obj, key, callback) {
                var map  = key ? $z.obj(key, obj[key]) : obj;
                var json = $z.toJson(map);

                Wn.execf('obj id:{{id}} -u \'<%=json%>\'', {
                    id   : obj.id,
                    json : json
                }, function(re){
                    var newObj = $z.fromJson(re);
                    $z.doCallback(callback, [newObj]);
                });
            }
        });
        // ----------------- detail
        conf.detail = $z.fallback([undefined, true], [opt.detail, conf.detail], {
            read : function(obj, callback) {
                Wn.execf('cat id:{{id}}', obj, callback);
            },
            save : function(obj, det, callback) {
                //console.log(det);
                Wn.execf('str > id:{{id}}', det.content||"", obj, function(){
                    if(det.brief || det.tp) {
                        // 生成 json
                        var map = $z.pick(det, ["tp", "brief"]);
                        var json = ($z.toJson(map) || "").replace(/'/g, "");
                        // 执行更新
                        Wn.execf('obj id:{{id}} -u \'<%=json%>\'', {
                            id : obj.id,
                            json : json
                        }, callback);
                    }
                    // 否则直接回调了
                    else {
                        $z.doCallback(callback);
                    }
                });
            }
        });
        
    }
    ///////////////////////////////////
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
                dataMode : opt.dataMode || "thing"
            };

            // 处理一下通用的配置信息
            conf.searchMenuFltWidthHint = opt.searchMenuFltWidthHint 
                                          || conf.searchMenuFltWidthHint
                                          || "50%";
            conf.searchFilter = opt.searchFilter || conf.searchFilter || {};
            conf.searchList   = opt.searchList   || conf.searchList;
            conf.searchSorter = opt.searchSorter || conf.searchSorter;
            conf.searchPager  = opt.searchPager  || conf.searchPager;
            conf.objMenu = opt.objMenu || conf.objMenu;

            // 按照模式处理各个配置项
            DATA_MODE[conf.dataMode].call(UI, conf, opt);

            // 设置默认
            $z.setUndefined(conf.searchFilter, "keyField", ["th_nm"]);

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
        //console.log("after", re)
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
