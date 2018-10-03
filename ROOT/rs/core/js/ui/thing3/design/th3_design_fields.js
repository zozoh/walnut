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
<div class="ui-arena th3-design-flds" ui-fitparent="yes">
    <div class="thd-menu" ui-gasket="menu"></div>
    <div class="thd-fld-list" ui-gasket="list"></div>
    <div class="thd-fld-conf" ui-gasket="fld"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.th3.thdesign_fields", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;
        //--------------------------------------- 字段操作菜单
        new MenuUI({
            parent : UI,
            gasketName : "menu",
            tipDirection : "up",
            setup : [{
                icon : '<i class="fa fa-plus"></i>',
                text : "i18n:th3.conf.addfld",
                handler : function(){
                    // 获取字段名
                    UI.prompt("i18n:th3.conf.addfld_tip", {
                        icon  : '<i class="zmdi zmdi-plus-circle"></i>',
                        check : function(str, callback) {
                            var fldName = $.trim(str);
                            if(fldName) {
                                // 字段名不能非法
                                if(!/^[0-9a-z_]+$/.test(fldName)) {
                                    return callback(UI.msg("th3.conf.e_fld_invalid"));
                                }
                                // 字段已存在
                                if(UI.gasket.list.has(fldName)){
                                    return callback(UI.msg("th3.conf.e_fld_exists"));
                                }
                            }
                            // 恢复成功的状态
                            callback();
                        },
                        ok : function(str){
                            var fldName = $.trim(str);
                            // 字段名不能为空
                            if(!fldName) {
                                UI.alert("th3.conf.e_fld_nkey", "warn");
                                return;
                            }

                            // 添加到列表
                            UI.gasket.list.add({key:fldName}, -1);
                            UI.gasket.list.setActived(fldName);

                            // 通知更新
                            UI.notifyChanged();
                        }
                    });
                }
            }, {
                icon : '<i class="fa fa-trash"></i>',
                text : "i18n:th3.conf.delfld",
                handler : function(){
                    var it = UI.gasket.list.remove();
                    if(it) {
                        UI.gasket.list.setActived(it);
                    }
                    // 通知更新
                    UI.notifyChanged();
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-up"></i>',
                tip  : "i18n:th3.conf.mv_up",
                handler : function(){
                    UI.gasket.list.moveUp();
                    // 通知更新
                    UI.notifyChanged();
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-down"></i>',
                tip  : "i18n:th3.conf.mv_down",
                handler : function(){
                    UI.gasket.list.moveDown();
                    // 通知更新
                    UI.notifyChanged();
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
                var html = UI.str("th3.conf.ficon." + fo.key
                              , UI.str("th3.conf.cicon." + fo.editAs
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
        return ["menu", "list"];
    },
    //...............................................................
    showBlank : function(callback){
        var UI = this;

        new DomUI({
            parent : UI,
            gasketName : "fld",
            dom : '<div class="thd-blank">'
                + ' <i class="zmdi zmdi-arrow-left"></i> '
                + '{{th3.conf.blank}}'
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

                    // 将更新的值，存储到 list 里面，根据这个来 getData
                    var jFo = UI.gasket.list.$item();
                    UI.gasket.list.update(newFo, jFo);
                    
                    // 通知父控件更新
                    UI.notifyChanged();
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
            title  : "i18n:th3.conf.key.key",
        }, {
            key    : "title",
            title  : "i18n:th3.conf.key.title",
            uiConf : {
                placeholder : "i18n:auto",
            }
        }, {
            key    : "type",
            title  : "i18n:th3.conf.key.type",
            editAs : "droplist",
            uiConf : {
                text : function(s) {
                    if(!s)
                        return "i18n:default";
                    return "i18n:th3.conf.t." + s + " : " + s;
                },
                value : function(s) {
                    return s;
                },
                items : [null, "string","int","float","boolean","datetime","time","object"]
            }
        }, {
            key    : "tip",
            title  : "i18n:th3.conf.key.tip",
            editAs : "text",
        }, {
            // TODO 如果支持默认值，那么创建的时候要增加默认值才行
            key    : "dft",
            title  : "i18n:th3.conf.key.dft",
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
            title  : "i18n:th3.conf.key.hide",
            type   : "boolean",
            editAs : "toggle"
        }, {
            key    : "editAs",
            title  : "i18n:th3.conf.key.editAs",
            editAs : "droplist",
            uiConf : {
                icon : function(s) {
                    if(s)
                        return UI.msg("th3.conf.cicon." + s);
                },
                text : function(s) {
                    if(!s)
                        return "i18n:default";
                    return "i18n:th3.conf.c." + s + " : " + s;
                },
                value : function(s) {
                    return s;
                },
                items : [null, "input", "text", "label", "color", "background",
                    "switch", "toggle", "droplist", "checklist", "radiolist",
                    "pair", "date_range", "number_range",
                    "opicker", "datepicker","combotable"]
            }
        }, {
            key : "uiConf",
            title  : "i18n:th3.conf.key.uiConf",
            type  : "object",
            editAs : "text",
            uiConf : {
                height: 100,
                placeholder : "i18n:th3.conf.key.uiConf_ph",
                parseData : function(obj){
                    return $z.toJson(obj);
                },
                formatData : function(json) {
                    return $z.fromJson(json);
                }
            }
        }, {
            key    : "uiWidth",
            title  : "i18n:th3.conf.key.uiWidth",
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
    getData : function(){
        // 得到字段
        var fields = this.gasket.list.getData();

        return {
            fields : fields
        };
    },
    //...............................................................
    setData : function(thConf) {
        var UI   = this;

        if(_.isString(thConf))
            thConf = $z.fromJson(thConf);

        // 确保有内容
        thConf = thConf || {};

        // 记录旧的配置
        UI.__old_conf = $z.toJson(thConf);

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
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);