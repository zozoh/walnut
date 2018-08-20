define(function (require, exports, module) {
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
                height : 180,
                remove : function(obj, callback) {
                    if(obj)
                        Wn.exec('rm id:'+obj.id, callback);
                }
            }
        });
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
},
//...............................................................
// 格式化扩展命令的函数
__format_extend_command : function(cmd) {
    var btn = _.extend({
        icon : null,
        text : 'SomeCommand'
    }, cmd);
    // 如果是调用方法
    if(btn.handlerName) {
        // 修改方法名前缀，因为初始化时，会将这个函数加入前缀增加到 bus 上
        btn.handlerName = "__ext_" + btn.handlerName;
        btn.handler = function(jq, mi) {
            var bus  = this.bus();
            var data = $z.invoke(this, "getData");
            $z.invoke(bus, mi.handlerName, [data], {
                UI  : this,
                jBtn : jq,
                menuItem : mi,
                bus : bus,
                POP : POP,
                Wn  : Wn
            });
        }
        return btn;
    }
    // 指定命令的话，应该是异步调用
    else if(cmd.cmdText) {
        // TODO 这个看以后后没有必要实现
    }
    // 不是一个合法的结构的话，就无视吧
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
    conf.objMenu       = opt.objMenu       || conf.objMenu;
    conf.extendCommand = opt.extendCommand || conf.extendComma
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
    conf.searchMenu = opt.searchMenu || conf.searchMenu;
    // 设置默认菜单
    if(!_.isArray(conf.searchMenu)) {
        conf.searchMenu = [{
                // 命令: 创建
                icon : '<i class="zmdi zmdi-flare"></i>',
                text : "i18n:th3.create",
                handler : function() {
                    this.fireBus("do:create");
                }
            }, {
                // 命令: 刷新
                icon : '<i class="zmdi zmdi-refresh"></i>',
                tip  : "i18n:th3.refresh_tip",
                asyncIcon : '<i class="zmdi zmdi-refresh zmdi-hc-spin"></i>',
                asyncHandler : function(jq, mi, callback) {
                    //this.uis("search").refresh(callback, true);
                    this.fireBus("list:refresh", [callback, true]);
                }
            }, {
                // 命令: 删除
                icon : '<i class="fa fa-trash"></i>',
                tip  : "i18n:th3.rm_tip",
                handler : function() {
                    // this.uis("search").removeChecked(null, function(o){
                    //     return o.th_live >= 0 ? o : null;
                    // });
                    this.fireBus('list:remove');
                }
            }];
    }
    // 准备更多项目
    var miMore = {
        icon  : '<i class="zmdi zmdi-more-vert"></i>',
        items : []
    };
    // 更多菜单项:导入
    if(conf.dataImport && conf.dataImport.enabled) {
        //conf.searchMenu.push({
        miMore.items.push({
            text : "导入数据..",
            handler : function(jBtn, mi){
                this.fireBus("do:import");
            }
        });
    }
    // 更多菜单项目：导出
    if(conf.dataExport && conf.dataExport.enabled) {
        //conf.searchMenu.push({
        miMore.items.push({
            text : "导出数据..",
            handler : function(jBtn, mi){
                this.fireBus("do:export");
            }
        });
    }
    // 更多用户自定义的菜单项
    if(conf.extendCommand) {
        // 对于搜索列表的自定义菜单
        if(_.isArray(conf.extendCommand.search) && conf.extendCommand.search.length>0) {
            miMore.items.push({type:"separator"});
            for(var i=0; i<conf.extendCommand.search.length; i++) {
                var cmd = conf.extendCommand.search[i];
                var btn = this.__format_extend_command(cmd);
                // 计入
                miMore.items.push(btn);
                //conf.searchMenu.push(btn);
            }
        }
        // 对于 meta 对象的自定义菜单
        if(_.isArray(conf.extendCommand.obj) && conf.extendCommand.obj.length > 0) {
            if(!_.isArray(conf.objMenu)){
                conf.objMenu = [];
            }
            for(var i=0; i<conf.extendCommand.obj.length; i++) {
                var cmd = conf.extendCommand.obj[i];
                var btn = this.__format_extend_command(cmd);
                // 计入
                conf.objMenu.push(btn);
            }
        }
    }
    // 更多菜单项:清空回收站
    miMore.items.push({type:"separator"});
    miMore.items.push({
        icon : '<i class="fa fa-eraser"></i>',
        text : "i18n:th3.clean_do",
        handler : function() {
            this.fireBus("do:cleanup");
        }
    });
    // 更多菜单项:从回收站恢复
    miMore.items.push({
        icon : '<i class="zmdi zmdi-window-minimize"></i>',
        text : "i18n:th3.clean_restore",
        handler : function() {
            this.fireBus("do:restore");
        }
    });
},
//...............................................................
}; // ~End methods
//===============================================================
});