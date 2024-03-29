define(function (require, exports, module) {
var POP = require('ui/pop/pop');
//...............................................................
// 方法表
module.exports = {
// ....................................
__format_field : function(UI, fld) {
    // id
    if("id" == fld.key) {
        $z.setUndefined(fld, "title", "i18n:th3.key.id");
        $z.setUndefined(fld, "hide", true);
        $z.extend(fld, {
            type   : "string",
            editAs : "label"
        });
    }
    // th_nm
    else if("th_nm" == fld.key) {
        $z.setUndefined(fld, "title", "i18n:th3.key.th_nm");
        $z.extend(fld, {
            type : "string",
            editAs : "input",
            escapeHtml : false,
            display : function(o) {
                //console.log(o.__force_update, o.th_nm)
                var html = "";
                // 自定义了缩略图
                if(o.thumb){
                    var src = "/o/thumbnail/id:"+o.id;
                    if(o.__force_update)
                        src += "?_t="+Date.now();
                    html += '<img src="' + src + '">';
                }
                // 那么采用默认的
                else{
                    // 试图找到自己的 thing_set
                    var uiThSearch = this.parent ? this.parent.parent : null;
                    var oHome = null;
                    if(uiThSearch){
                        oHome = uiThSearch.getHomeObj();
                    }
                    // 采用 oHome 的 icon
                    if(oHome && oHome.icon) {
                        html += oHome.icon;
                        //html += '<i class="fa fa-cube th_thumb"></i>';
                    }
                    // 一个默认图标
                    else {
                        html += '<i class="fa fa-cube"></i>';
                    }
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
        $z.setUndefined(fld, "title", "i18n:th3.key.th_enabled");
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
        $z.setUndefined(fld, "title", "i18n:th3.key.th_ow");
        $z.setUndefined(fld, "hide", true);
        $z.extend(fld, {
            type   : "string",
            editAs : "input"
        });
    }
    // lbls
    else if("lbls" == fld.key) {
        $z.setUndefined(fld, "title", "i18n:th3.key.lbls");
        $z.setUndefined(fld, "tip", "i18n:th3.key.lbls_tip");
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
        $z.setUndefined(fld, "title", "i18n:th3.key.thumb");
        var uiConf = fld.uiConf || {};
        $z.extend(fld, {
            hide : true,
            type : "string",
            tip  : "i18n:th3.keytip.thumb",
            beforeSetData : function(o){
                this.UI.setTarget($z.tmpl("id:{{th_set}}/data/{{id}}/thumb.jpg")(o));
            },
            editAs : "image",
            uiConf : {
                dataType : "idph",
                height : uiConf.height || 180,
                readonly : uiConf.readonly || false,
                remove : function(obj, callback) {
                    if(obj)
                        Wn.exec('rm id:'+obj.id, callback);
                }
            }
        });
        //console.log(fld)
    }
    // 日期
    else if(/^(lm|ct)$/.test(fld.key)) {
        $z.setUndefined(fld, "title", "i18n:th3.key." + fld.key);
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
            this.__format_field(UI, subFld);
        }
    }

    // 如果没有声明字段的标题，看看是否需要自动为其声明
    if(!fld.title) {
        var keyTitle = UI.str("th3.key." + fld.key, "");
        if(keyTitle){
            fld.title = keyTitle;
        }
    }

    // 最后返回
    return fld;
},
//...............................................................
__normalize_action_menu : function(items, conf) {
    // 防守
    if(!_.isArray(items) || items.length <= 0)
        return;
    conf = conf || {};
    // 准备快速映射对象
    var quick_map = {
        "@create" : {
            icon : '<i class="zmdi zmdi-flare"></i>',
            text : "i18n:th3.create",
            fireEvent : "show:create"
        },
        "@refresh" : {
            icon : '<i class="zmdi zmdi-refresh"></i>',
            tip  : "i18n:th3.refresh_tip",
            asyncIcon : '<i class="zmdi zmdi-refresh zmdi-hc-spin"></i>',
            asyncFireEvent : "list:refresh"
        }, 
        "@remove" : {
            icon : '<i class="fa fa-trash"></i>',
            tip  : "i18n:th3.rm_tip",
            fireEvent : "list:remove"
        },
        "@cleanup" : {
            icon : '<i class="fa fa-eraser"></i>',
            text : "i18n:th3.cleanup",
            fireEvent : "do:cleanup"
        },
        "@restore" : {
            icon : '<i class="zmdi zmdi-window-minimize"></i>',
            text : "i18n:th3.restore",
            fireEvent : "do:restore"
        },
        // @import 还需要配置启用开关 conf.dataImport.enabled 
        // 否则会被无视
        "@import" : {
            text : "i18n:th3.import.tt", 
            fireEvent : "do:import"
        },
        // @export 还需要配置启用开关 conf.dataExport.enabled
        // 否则会被无视
        "@export" : {
            text : "i18n:th3.export.tt", 
            fireEvent : "do:export"
        },
        "@config" : {
            icon : '<i class="fa fa-gears"></i>',
            tip : "i18n:th3.conf_setup",
            fireEvent : 'open:config'
        },
        "|" : {type:"separator"}
    };

    // 迭代
    for(var i=0; i<items.length; i++) {
        var mi = items[i];

        // 字符串的话，用快速映射
        if(mi && _.isString(mi)) {
            // @import 还需要配置启用开关 conf.dataImport.enabled
            if("@import" == mi) {
                mi = conf.dataImport && conf.dataImport.enabled
                            ? quick_map[mi] : null;
            }
            // @export 还需要配置启用开关 conf.dataExport.enabled
            else if("@export" == mi) {
                mi = conf.dataExport && conf.dataExport.enabled
                            ? quick_map[mi] : null;
            }
            // 其他直接取
            else {
                mi = quick_map[mi] || null;
            }
            // 设置回去
            items[i] = mi;
        }

        if(!mi)
            continue;

        // 组的话·递归
        if(_.isArray(mi.items)) {
            this.__normalize_action_menu(mi.items, conf);
            continue;
        }
        // 开始展开吧
        //------------------------------------------
        // <i..>::i18n:xxx::->doSomething  # 调用函数
        if(mi.handlerName) {
            mi.handler = function(jq, mi) {
                var thM  = this.thMain();
                thM.invokeExtCommand({
                    method   : mi.handlerName,
                    jBtn     : jq,
                    menuItem : mi
                });
            }
        }
        // <i..>::i18n:xxx::~>doSomething  # 调用异步函数
        else if(mi.asyncHandlerName) {
            mi.asyncHandler = function(jq, mi, callback) {
                var thM  = this.thMain();
                thM.invokeExtCommand({
                    method   : mi.asyncHandlerName,
                    jBtn     : jq,
                    menuItem : mi,
                    callback : callback
                });
            }
        }
        // <i..>::i18n:xxx::-@do:create    # 触发消息
        else if(mi.fireEvent) {
            mi.handler = function(jq, mi) {
                this.fireBus(mi.fireEvent);
            }
        }
        // <i..>::i18n:xxx::~@do:create    # 触发异步消息
        else if(mi.asyncFireEvent) {
            mi.asyncHandler = function(jq, mi, callback) {
                this.fireBus(mi.asyncFireEvent, [callback]);
            }
        }
    }
},
//...............................................................
__merge_extendCommand : function(UI, conf) {
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
                    UI["__ext_"+key] = func;
                }
            }
        }
    }
},
//...............................................................
evalConf : function(UI, conf, opt, home) {
    // 设置默认值
    $z.setUndefined(conf, "thumbSize", "256x256");
    conf.searchMenuFltWidthHint = opt.searchMenuFltWidthHint 
                                    || conf.searchMenuFltWidthHint
                                    || "50%";
    conf.searchList    = opt.searchList    || conf.searchList;
    conf.searchSorter  = opt.searchSorter  || conf.searchSorter;
    conf.searchPager   = opt.searchPager   || conf.searchPager;
    conf.topMenu       = opt.topMenu       || conf.topMenu;
    conf.extendCommand = opt.extendCommand || conf.extendCommand;
    conf.eventRouter   = opt.eventRouter   || conf.eventRouter;
    // 合并扩展方法
    this.__merge_extendCommand(UI, conf);

    // 指定了字段
    if(_.isArray(opt.fields) && opt.fields.length > 0)
        conf.fields = [].concat(opt.fields);
    
    // 格式化所有字段
    var fields = [];
    for(var i=0; i<conf.fields.length; i++){
        var fld = conf.fields[i];
        // console.log(fld.key, !/^__/.test(fld.key))
        // 特殊字段无视就好
        if(!/^__/.test(fld.key)){
            fields.push(this.__format_field(UI, fld));
        }
    }
    conf.fields = fields;

    // ----------------- searchFilter
    conf.searchFilter = opt.searchFilter || conf.searchFilter || {
        tabsPosition : "drop",
        tabsKeepChecked : true,
        tabs : [{
                icon : home.icon  || '<i class="fas fa-database"></i>',
                text : home.title || home.nm,
                value   : {th_live : 1},
                checked : true
            }, {
                icon : '<i class="fas fa-trash"></i>',
                text : "i18n:th3.conf.filter.recycle",
                color      : "#C44",
                background : "#F88",
                value   : {th_live : -1},
            }]
    };
    // 设置默认
    $z.setUndefined(conf.searchFilter, "keyField", ["th_nm"]);

    // ----------------- searchMenu
    conf.searchMenu = opt.searchMenu 
                        || conf.searchMenu 
                        || ["@create","@refresh","@remove", {
                            icon  : '<i class="zmdi zmdi-more-vert"></i>',
                            items : ["@import","@export",
                                    "|","@cleanup", "@restore",
                                    "|","@config"]
                        }];
    // ----------------- 自定义搜索菜单项
    if(conf.extendCommand) {
        // 对于搜索列表的自定义菜单
        if(_.isArray(conf.extendCommand.search) && conf.extendCommand.search.length>0) {
            // 寻找到 More 的项目
            var miMore = null;
            for(var i=0; i<conf.searchMenu.length; i++) {
                var mi = conf.searchMenu[i];
                if(_.isArray(mi.items)) {
                    miMore = mi;
                    break;
                }
            } 
            // 确保有 More 项目
            if(!miMore) {
                miMore = {
                    icon  : '<i class="zmdi zmdi-more-vert"></i>',
                    items : []
                };
                conf.searchMenu.push(miMore);
            }
            // 加入扩展项
            miMore.items.unshift({type:"separator"});
            for(var i=conf.extendCommand.search.length-1; i>=0; i--) {
                var cmd = conf.extendCommand.search[i];
                miMore.items.unshift(cmd);
                //conf.searchMenu.push(btn);
            }
        }
    }

    // 格式化菜单项目
    this.__normalize_action_menu(conf.searchMenu, conf);
    this.__normalize_action_menu(conf.topMenu, conf);

    // 调用自定义函数
    var on_init_conf = opt.on_init_conf || conf.on_init_conf;
    if(on_init_conf) {
        var func_nm = '__ext_' + on_init_conf;
        $z.invoke(UI, func_nm, [conf,opt,home]);
    }

},
//...............................................................
}; // ~End methods
//===============================================================
});