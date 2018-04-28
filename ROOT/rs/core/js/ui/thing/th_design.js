(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_icon',
    'ui/form/c_name',
    'ui/form/form',
    'ui/menu/menu',
    'ui/list/list',
    'ui/support/dom',
], function(ZUI, Wn, CIconUI, CNameUI, FormUI, MenuUI, ListUI, DomUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-design" ui-fitparent="yes">
    <div class="thd-info">
        <div class="thd-icon" ui-gasket="icon"></div>
        <div class="thd-name" ui-gasket="name"></div>
        <div class="thd-id"></div>
        <div class="thd-btns" mode="loaded">
            <a>{{thing.conf.cancel}}</a>
            <b><i class="fa fa-save"></i> {{thing.conf.saveflds}}</b>
            <em><i class="fa fa-cog fa-spin"></i> {{thing.conf.saving}}</em>
        </div>
    </div>
    <div class="thd-setup" ui-gasket="setup"></div>
    <div class="thd-flds">
        <div class="thd-menu" ui-gasket="menu"></div>
        <div class="thd-fld-list" ui-gasket="list"></div>
        <div class="thd-fld-conf" ui-gasket="fld"></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thdesign", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //...............................................................
    events : {
        // 保存修改
        "click .thd-btns b" : function(e){
            var UI = this;
            var opt = UI.options;
            var jB = $(e.currentTarget);
            var json = UI.getThConfJson();
            var tsId = UI.__oTS.id;
            var tsPh = UI.__oTS.ph;

            // 生成命令
            var cmdText = 'json > "' + tsPh + '/thing.js"; obj id:'+tsId;

            // 执行命令
            jB.parent().attr("mode", "ing");
            Wn.exec(cmdText, json, function(re){
                jB.parent().attr("mode", "loaded");
                //console.log("re:", re);
                // 得到新的对象，并存入缓存
                var oTS2 = $z.fromJson(re);
                Wn.saveToCache(oTS2);

                // 记入内存
                UI.__old_conf = json;
                // 标记修改
                UI.__is_changed = true;

                // 调用回调
                $z.invoke(opt, "on_change", [oTS2], UI);
            });
        },
        // 放弃修改
        "click .thd-btns a" : function(e){
            var UI = this;
            var jA = $(e.currentTarget);
            
            // 生成命令
            var it = UI.gasket.list.getActivedId();
            UI.__updat_thConf_json(UI.__old_conf);
            UI.gasket.list.setActived(it);

            // 修改按钮状态
            jA.parent().attr("mode", "loaded");
        },
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;
        //--------------------------------------- 集合图标
        new CIconUI({
            parent : UI,
            gasketName : "icon",
            dftIcon : '<i class="fa fa-cubes"></i>',
            balloon : 'up:thing.conf.icon_modify',
            on_change : function(icon) {
                var iconHtml = icon.replace(/"/g, "\\\\\"");
                var cmdText = 'obj id:' + UI.__oTS.id + ' -u \'icon:"'+iconHtml+'"\' -o';
                //console.log(cmdText)
                Wn.exec(cmdText, function(re) {
                    // 处理错误 
                    if(/^e./.test(re)) {
                        UI.alert(re, "warn");
                        return;
                    }

                    // 得到新的对象，并存入缓存
                    var oTS2 = $z.fromJson(re);
                    Wn.saveToCache(oTS2);

                    // 标记修改
                    UI.__is_changed = true;
                    //console.log(UI.__is_changed)

                    // 成功后调用回调
                    $z.invoke(opt, "on_change", [oTS2], UI);
                });
            }
        }).render(function(){
            UI.defer_report("icon");
        });
        //--------------------------------------- 集合名称
        new CNameUI({
            parent : UI,
            gasketName : "name",
            on_change : function(nm) {
                var str = nm.replace(/[ \t"'$]/g, '');
                var cmdText = 'obj id:' + UI.__oTS.id + ' -u \'nm:"'+nm+'"\' -o';
                Wn.exec(cmdText, function(re) {
                    // 处理错误 
                    if(/^e./.test(re)) {
                        UI.alert(re, "warn");
                        return;
                    }

                    // 得到新的对象，并存入缓存
                    var oTS2 = $z.fromJson(re);
                    Wn.saveToCache(oTS2);

                    // 标记修改
                    UI.__is_changed = true;

                    // 成功后调用回调
                    $z.invoke(opt, "on_change", [oTS2], UI);
                });
            }
        }).render(function(){
            UI.defer_report("name");
        });
        //--------------------------------------- 集合通用设置
        new FormUI({
            parent : UI,
            gasketName : "setup",
            fitparent : false,
            uiWidth : "all",
            on_change : function(){
                // 同步按钮状态
                UI.__check_btn_status();
            },
            fields : [{
                title : "数据集通用设定",
                cols : 5,
                fields : [{
                    key : "searchMenuFltWidthHint",
                    title : "菜单收缩",
                    type : "string",
                    dft : "",
                    editAs : "input",
                    uiConf : {
                        placeholder : "50%"
                    }
                }, {
                    key : "thIndex",
                    title : "显示索引",
                    type : "string",
                    dft : "all",
                    span : 2,
                    editAs : "droplist",
                    uiConf : {
                        items : [
                            {value : "all",    text : "显示属性和内容"},
                            {value : "meta",   text : "仅显示属性"},
                            {value : "detail", text : "仅显示内容"},
                            {value : "hide",   text : "全部隐藏"},
                        ]
                    }
                }, {
                    key : "thData",
                    title : "显示数据",
                    type : "string",
                    dft : "all",
                    span : 2,
                    editAs : "droplist",
                    uiConf : {
                        items : [
                            {value : "all",        text : "显示多媒体和附件"},
                            {value : "media",      text : "仅显示多媒体"},
                            {value : "attachment", text : "仅显示附件"},
                            {value : "hide",   text : "全部隐藏"},
                        ]
                    }
                }]
            }],
        }).render(function(){
            UI.defer_report("setup");
        });
        //--------------------------------------- 字段操作菜单
        new MenuUI({
            parent : UI,
            gasketName : "menu",
            tipDirection : "up",
            setup : [{
                icon : '<i class="fa fa-plus"></i>',
                text : "i18n:thing.conf.addfld",
                handler : function(){
                    // 获取字段名
                    UI.prompt("thing.conf.addfld_tip", {
                        icon  : '<i class="zmdi zmdi-plus-circle"></i>',
                        check : function(str, callback) {
                            var fldName = $.trim(str);
                            if(fldName) {
                                // 字段名不能非法
                                if(!/^[0-9a-z_]+$/.test(fldName)) {
                                    return callback(UI.msg("thing.conf.e_fld_invalid"));
                                }
                                // 字段已存在
                                if(UI.gasket.list.has(fldName)){
                                    return callback(UI.msg("thing.conf.e_fld_exists"));
                                }
                            }
                            // 恢复成功的状态
                            callback();
                        },
                        ok : function(str){
                            var fldName = $.trim(str);
                            // 字段名不能为空
                            if(!fldName) {
                                UI.alert("thing.conf.e_fld_nkey", "warn");
                                return;
                            }

                            // 添加到列表
                            UI.gasket.list.add({key:fldName}, -1);
                            UI.gasket.list.setActived(fldName);

                            // 同步按钮状态
                            UI.__check_btn_status();
                        }
                    });
                }
            }, {
                icon : '<i class="fa fa-trash"></i>',
                text : "i18n:thing.conf.delfld",
                handler : function(){
                    var it = UI.gasket.list.remove();
                    if(it) {
                        UI.gasket.list.setActived(it);
                    }
                    // 同步按钮状态
                    UI.__check_btn_status();
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-up"></i>',
                tip  : "i18n:thing.conf.mv_up",
                handler : function(){
                    UI.gasket.list.moveUp();
                    // 同步按钮状态
                    UI.__check_btn_status();
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-down"></i>',
                tip  : "i18n:thing.conf.mv_down",
                handler : function(){
                    UI.gasket.list.moveDown();
                    // 同步按钮状态
                    UI.__check_btn_status();
                }
            }]
        }).render(function(){
            UI.defer_report("menu");
        });
        //--------------------------------------- 字段列表
        new ListUI({
            parent : UI,
            gasketName : "list",
            escapeHtml : false,
            idKey : "key",
            nmKey : "title",
            display : function(fo) {
                var html = UI.str("thing.conf.ficon." + fo.key
                              , UI.str("thing.conf.cicon." + fo.editAs
                                    , '<i class="zmdi zmdi-minus"></i>'));
                
                // 字段名
                html += '<b>' + fo.key + '</b>';
                
                // 指定了字段的标题
                if(fo.title) {
                    html += '<em>:' + UI.text(fo.title) + '</em>';
                }
                // 看看有没有默认描述
                else {
                    var keyTitle = UI.str("thing.key." + fo.key, "");
                    if(keyTitle) {
                        html += '<em>&gt;' + keyTitle + '</em>';
                    }
                }
                
                // 返回
                return html;
            },
            on_actived : function(fo) {
                UI.showField(fo);
            },
            on_blur : function(jItems, nextObj){
                if(!nextObj) {
                    UI.showBlank();
                }
            }
        }).render(function(){
            UI.defer_report("list");
        });

        // 返回延迟加载
        return ["icon", "name", "setup", "menu", "list"];
    },
    //...............................................................
    showBlank : function(callback){
        var UI = this;

        new DomUI({
            parent : UI,
            gasketName : "fld",
            dom : '<div class="thd-blank">'
                + ' <i class="zmdi zmdi-arrow-left"></i> '
                + '{{thing.conf.blank}}'
                + '</div>'
        }).render(function(){
            $z.doCallback(callback, [], UI);
        });
    },
    //...............................................................
    showField : function(fo) {
        var UI = this;

        // 得到字段状态信息
        var dis_flds;
        // 内置专有字段，有了固定的设置
        if(/^(id|th_ow|lbls|th_site|th_enabled|lm|ct)$/.test(fo.key)) {
            dis_flds = ["key","type","dft","editAs","uiConf"];
        }
        // 缩略图和名称
        else if(/^(thumb|th_nm)$/.test(fo.key)) {
            dis_flds = ["key","type","dft","editAs","uiConf", "hide"];
        }
        // 默认为自定义字段
        else {
            dis_flds = [];
        }

        // 没必要重绘 form
        if(UI.gasket.fld && "ui.dom" != UI.gasket.fld.uiName) {
            UI.__fld_form_set_data(fo, dis_flds);
        }
        // 显示 form
        else {
            new FormUI({
                parent : UI,
                gasketName : "fld",
                arenaClass : "thd-fld-form",
                uiWidth : "all",
                hideDisabled : true,
                fields : UI.__gen_fields(),
                on_change : function(key, val) {
                    var newFo = this.getData();

                    // 确保字段被更新
                    newFo[key] = val;
                    //console.log(newFo)

                    // 根据字段类型，格式化默认值
                    // if(!_.isUndefined(newFo.dft)){
                    //     // NULL
                    //     if("null" == newFo.dft) {
                    //         newFo.dft = null;
                    //     }
                    //     // 其他转换一下
                    //     else if(newFo.type){
                    //         newFo.dft = $z.strToJsObj(newFo.dft, newFo.type);
                    //     }
                    // }

                    // 转换一下 uiWidth
                    if(newFo.uiWidth && /^[0-9]+$/.test(newFo.uiWidth)){
                        newFo.uiWidth = parseInt(newFo.uiWidth);
                    }

                    var jFo = UI.gasket.list.$item();
                    UI.gasket.list.update(newFo, jFo);
                    UI.__check_btn_status();
                }
            }).render(function(){
                UI.__fld_form_set_data(fo, dis_flds);
            });
        }
    },
    //...............................................................
    __fld_form_set_data : function(fo, dis_flds) {
        // 全部恢复
        this.gasket.fld.enableField();
        // 禁止指定字段
        if(dis_flds && dis_flds.length > 0)
            this.gasket.fld.disableField(dis_flds);
        // 设置数据
        this.gasket.fld.setData(fo);
    },
    //...............................................................
    __gen_fields : function() {
        var UI = this;

        return [{
            key    : "key",
            title  : "i18n:thing.conf.key.key",
        }, {
            key    : "title",
            title  : "i18n:thing.conf.key.title",
            uiConf : {
                placeholder : "i18n:auto",
            }
        }, {
            key    : "type",
            title  : "i18n:thing.conf.key.type",
            editAs : "droplist",
            uiConf : {
                text : function(s) {
                    if(!s)
                        return "i18n:default";
                    return "i18n:thing.conf.t." + s + " : " + s;
                },
                value : function(s) {
                    return s;
                },
                items : [null, "string","int","float","boolean","datetime","time","object"]
            }
        }, {
            key    : "tip",
            title  : "i18n:thing.conf.key.tip",
            editAs : "text",
        }, {
            // TODO 如果支持默认值，那么创建的时候要增加默认值才行
            key    : "dft",
            title  : "i18n:thing.conf.key.dft",
            type   : "string",
            editAs : "input",
            uiConf : {
                formatData : function(val) {
                    //console.log("parse", val)
                    if("null" == val)
                        return null;
                    if("undefined" == val)
                        return undefined;
                    return val;
                },
                parseData : function(val) {
                    //console.log("format", val, this);
                    if(_.isNull(val))
                        return "null";
                    if(_.isUndefined(val))
                        return "undefined";
                    return val;                    
                },
            }
        }, {
            key    : "hide",
            title  : "i18n:thing.conf.key.hide",
            type   : "boolean",
            editAs : "toggle"
        }, {
            key    : "editAs",
            title  : "i18n:thing.conf.key.editAs",
            editAs : "droplist",
            uiConf : {
                icon : function(s) {
                    if(s)
                        return UI.msg("thing.conf.cicon." + s);
                },
                text : function(s) {
                    if(!s)
                        return "i18n:default";
                    return "i18n:thing.conf.c." + s + " : " + s;
                },
                value : function(s) {
                    return s;
                },
                items : [null, "input", "text", "label", "color", "background",
                    "switch", "toggle", "droplist", "checklist", "radiolist",
                    "pair", "date_range", "number_range",
                    "opicker", "datepicker"]
            }
        }, {
            key : "uiConf",
            title  : "i18n:thing.conf.key.uiConf",
            type  : "object",
            editAs : "text",
            uiConf : {
                height: 100,
                placeholder : "i18n:thing.conf.key.uiConf_ph",
                parseData : function(obj){
                    return $z.toJson(obj);
                },
                formatData : function(json) {
                    return $z.fromJson(json);
                }
            }
        }, {
            key    : "uiWidth",
            title  : "i18n:thing.conf.key.uiWidth",
            type   : "string",
            editAs : "input",
            uiConf : {
                assist : {
                    icon : '<i class="zmdi zmdi-more"></i>',
                    uiType : "ui/form/c_list",
                    uiConf : {
                        drawOnSetData : false,
                        textAsValue : true,
                        items : ["all","auto"]
                    }
                }
            }
        }]
    },
    //...............................................................
    update : function(oThSet, thConf) {
        var UI   = this;
        var opt  = UI.options;
        UI.__oTS = oThSet;

        //console.log(oThSet, thConf)

        // 更新一下 ThingSet 设置
        UI.gasket.icon.setData(oThSet.icon);
        UI.gasket.name.setData(oThSet.nm);
        UI.arena.find(".thd-info .thd-id").text(oThSet.id);

        // 读取配置文件
        if(!thConf) {
            var oThConf = Wn.fetch(oThSet.ph + "/thing.js", true);
            if(!oThConf){
                UI.alert("thing.conf.e_no_thingjs");
                return;
            }

            // 读取并解析
            thConf = Wn.read(oThConf, true);
        }

        // 更新
        UI.__updat_thConf_json(thConf);

        // 同步按钮状态
        UI.__check_btn_status();
    },
    //...............................................................
    // 判断是否修改了
    isChanged : function(){
        return this.__is_changed ? true : false;
    },
    //...............................................................
    isNeedSave : function(){
        var json = this.getThConfJson();
        return json != this.__old_conf;
    },
    //...............................................................
    __updat_thConf_json : function(thConf) {
        var UI = this;
        if(_.isString(thConf))
            thConf = $z.fromJson(thConf);

        // 确保有内容
        thConf = thConf || {};

        // 记录旧的配置
        UI.__old_conf = $z.toJson(thConf);

        // 更新通用全局配置
        var setupObj = {
            searchMenuFltWidthHint : thConf.searchMenuFltWidthHint,
        };
        // thIndex
        if(thConf.meta && thConf.detail 
            || (_.isUndefined(thConf.meta) && _.isUndefined(thConf.detail))) {
            setupObj.thIndex = "all";
        } else if(thConf.meta) {
            setupObj.thIndex = "meta";
        } else if(thConf.detail){
            setupObj.thIndex = "detail";
        } else {
            setupObj.thIndex = "hide";
        }
        // thData
        if(thConf.media && thConf.attachment 
            || (_.isUndefined(thConf.media) && _.isUndefined(thConf.attachment))) {
            setupObj.thData = "all";
        } else if(thConf.media) {
            setupObj.thData = "media";
        } else if(thConf.attachment){
            setupObj.thData = "attachment";
        } else {
            setupObj.thData = "hide";
        }
        //console.log(setupObj)
        // 设置
        UI.gasket.setup.setData(setupObj);

        // 找到字段列表
        var fields = thConf.fields || [];
        UI.gasket.list.setData(fields);

        // 显示空
        if(fields.length == 0) {
            UI.showBlank();
        }
        // 显示字段
        else {
            UI.gasket.list.setActived(0);
        }
    },
    //...............................................................
    __check_btn_status : function(){
        var UI = this;
        var jBtns = UI.arena.find(".thd-btns");
        if(UI.isNeedSave()) {
            jBtns.attr("mode", "changed");
            $z.blinkIt(jBtns);
        }else{
            jBtns.attr("mode", "loaded");
        }
    },
    //...............................................................
    isFieldsChanged : function(){
        return this.arena.find(".thd-btns").attr("mode") == "changed";
    },
    //...............................................................
    getThConfJson : function(){
        // 得到字段
        var fields = this.gasket.list.getData();

        // 得到全局设置
        var setupObj = this.gasket.setup.getData();

        // 得到对象
        var thConf = {
            meta       : /^(all|meta)$/.test(setupObj.thIndex),
            detail     : /^(all|detail)$/.test(setupObj.thIndex),
            media      : /^(all|media)$/.test(setupObj.thData),
            attachment : /^(all|attachment)$/.test(setupObj.thData),
            fields: fields,
        };
        console.log(thConf)
        if(setupObj.searchMenuFltWidthHint)
            thConf.searchMenuFltWidthHint = setupObj.searchMenuFltWidthHint;

        // 转换为 JSON 字符串
        return $z.toJson(thConf);
    },
    //...............................................................
    setData : function(oThSet) {
        this.update(oThSet);
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jInfo  = UI.arena.find(">.thd-info");
        var jSetup = UI.arena.find(">.thd-setup");
        var jFlds  = UI.arena.find(">.thd-flds");

        var H = UI.arena.height();
        jFlds.css("height", H - jSetup.outerHeight(true) - jInfo.outerHeight(true));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);