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
<div class="ui-arena thconf" ui-fitparent="yes">
    <div class="thc-info">
        <div class="thc-icon" ui-gasket="icon"></div>
        <div class="thc-name" ui-gasket="name"></div>
        <div class="thc-id"></div>
        <div class="thc-btns" mode="loaded">
            <a>{{thing.conf.cancel}}</a>
            <b><i class="fa fa-save"></i> {{thing.conf.saveflds}}</b>
            <em><i class="fa fa-cog fa-spin"></i> {{thing.conf.saving}}</em>
        </div>
    </div>
    <div class="thc-flds">
        <div class="thc-menu" ui-gasket="menu"></div>
        <div class="thc-fld-list" ui-gasket="list"></div>
        <div class="thc-fld-conf" ui-gasket="fld"></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thconf", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "app/wn.thing/theme/thing-{{theme}}.css",
    i18n : "app/wn.thing/i18n/{{lang}}.js",
    //...............................................................
    events : {
        // 保存修改
        "click .thc-btns b" : function(e){
            var UI = this;
            var jB = $(e.currentTarget);
            var json = UI.getThConfJson();

            // 生成命令
            var cmdText = 'json > "' + UI.__oTS.ph + '/thing.js"';

            // 执行命令
            jB.parent().attr("mode", "ing");
            Wn.exec(cmdText, json, function(re){
                jB.parent().attr("mode", "loaded");
                //console.log("re:", re);
                // 记入缓存
                UI.__old_conf = json;
            });
        },
        // 放弃修改
        "click .thc-btns a" : function(e){
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

                    // 成功后调用回调
                    $z.invoke(opt, "on_change", [oTS2], UI);
                });
            }
        }).render(function(){
            UI.defer_report("icon");
        });

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

                    // 成功后调用回调
                    $z.invoke(opt, "on_change", [oTS2], UI);
                });
            }
        }).render(function(){
            UI.defer_report("name");
        });

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
                
                // 得到字段显示名
                var keyName = UI.str("thing.conf.fnm." + fo.key, fo.key);
                
                // 内置专有字段，需要显示一下自己的字段真实名
                if(/^(id|th_nm|th_ow|lbls|th_site|th_pub)$/.test(fo.key)) {
                    keyName += " (" + fo.key + ")";
                }

                // 字段名
                html += '<b>' + keyName + '</b>';

                // 自定义了显示名
                if(fo.title) {
                    html += '<em>' + UI.text(fo.title) + '</em>';
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
        return ["icon", "name", "menu", "list"];
    },
    //...............................................................
    showBlank : function(callback){
        var UI = this;

        new DomUI({
            parent : UI,
            gasketName : "fld",
            dom : '<div class="thc-blank">'
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
        if(/^(id|th_nm|th_ow|lbls|th_site|th_pub|__brief_and_content__)$/.test(fo.key)) {
            dis_flds = ["key","type","multi","editAs","uiConf"];
        }
        // 附件和媒体
        else if("__media__" == fo.key || "__attachment__" == fo.key) {
            dis_flds = ["key","type","editAs","uiConf"];
        }
        // 缩略图
        else if("thumb" == fo.key) {
            dis_flds = ["key","type","multi","editAs","uiConf", "hide"];
        }
        // 默认为自定义字段
        else {
            dis_flds = ["multi"];
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
                arenaClass : "thc-fld-form",
                uiWidth : "all",
                hideDisabled : true,
                fields : UI.__gen_fields(),
                on_change : function(key, val) {
                    var newFo = this.getData();
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
        this.gasket.fld.enableField()
                .disableField.apply(this.gasket.fld, dis_flds)
                    .setData(fo);
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
            key    : "multi",
            title  : "i18n:thing.conf.key.multi",
            type   : "boolean",
            editAs : "toggle"
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
            key    : "hide",
            title  : "i18n:thing.conf.key.hide",
            type   : "boolean",
            editAs : "toggle"
        }]
    },
    //...............................................................
    update : function(oThSet) {
        var UI   = this;
        var opt  = UI.options;
        UI.__oTS = oThSet;

        // 更新一下 ThingSet 设置
        UI.gasket.icon.setData(oThSet.icon);
        UI.gasket.name.setData(oThSet.nm);
        UI.arena.find(".thc-info .thc-id").text(oThSet.id);

        // 读取配置文件
        var oThConf = Wn.fetch(oThSet.ph + "/thing.js", true);
        if(!oThConf){
            UI.alert("thing.conf.e_no_thingjs");
            return;
        }

        // 读取并解析
        var json = Wn.read(oThConf);
        UI.__updat_thConf_json(json);

        // 同步按钮状态
        UI.__check_btn_status();
    },
    //...............................................................
    __updat_thConf_json : function(json) {
        var UI = this;
        var thConf = $z.fromJson(json);

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
    },
    //...............................................................
    __check_btn_status : function(){
        var UI = this;
        var jBtns = UI.arena.find(".thc-btns");
        var json = UI.getThConfJson();
        if(json == UI.__old_conf) {
            jBtns.attr("mode", "loaded");
        }else{
            jBtns.attr("mode", "changed");
            $z.blinkIt(jBtns);
        }
    },
    //...............................................................
    isChanged : function(){
        return this.arena.find(".thc-btns").attr("mode") == "changed";
    },
    //...............................................................
    getThConfJson : function(){
        var fields = this.gasket.list.getData();
        return $z.toJson({fields:fields});
    },
    //...............................................................
    setData : function(oThSet) {
        this.update(oThSet);
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jInfo = UI.arena.find(">.thc-info");
        var jFlds = UI.arena.find(">.thc-flds");

        var H = UI.arena.height();
        jFlds.css("height", H - jInfo.outerHeight(true));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);