(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/tabs/tabs',
    'ui/form/c_icon',
    'ui/form/c_name',
    'ui/pop/pop',
    'ui/form/form',
    'ui/menu/menu',
    'ui/list/list',
    'ui/support/dom',
], function(ZUI, Wn, TabsUI, CIconUI, CNameUI, POP, FormUI, MenuUI, ListUI, DomUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-design" ui-fitparent="yes">
    <div class="thd-info">
        <div class="thd-icon" ui-gasket="icon"></div>
        <div class="thd-name" ui-gasket="name"></div>
        <div class="thd-id"></div>
        <div class="thd-vc"><a>{{thing.conf.viewsource}}</a></div>
        <div class="thd-btns" mode="loaded">
            <a>{{thing.conf.cancel}}</a>
            <b><i class="fa fa-save"></i> {{thing.conf.saveflds}}</b>
            <em><i class="fa fa-cog fa-spin"></i> {{thing.conf.saving}}</em>
        </div>
    </div>
    <div class="thd-tabs" ui-gasket="tabs"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.thdesign", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //...............................................................
    init : function() {
        this.listenSelf("change:data", this.on_sub_changed);
    },
    //...............................................................
    events : {
        // 保存修改
        "click .thd-btns b" : function(e){
            var UI = this;
            var opt = UI.options;
            var jB = $(e.currentTarget);
            var json = UI.getDataJson();
            var tsId = UI.__oTS.id;
            var tsPh = UI.__oTS.ph;

            // 生成命令
            var cmdText = 'json -n > "' + tsPh + '/thing.js"; obj id:'+tsId;

            // 执行命令
            jB.parent().attr("mode", "ing");
            Wn.exec(cmdText, json, function(re){
                jB.parent().attr("mode", "loaded");
                //console.log("re:", re);
                // 得到新的对象，并存入缓存
                var oTS2 = $z.fromJson(re);
                Wn.saveToCache(oTS2);

                // 记入内存
                UI.__OLD_TH_CONF_JSON = json;

                // 标记修改
                UI.__is_changed = true;

                // 调用回调
                $z.invoke(opt, "on_change", [oTS2], UI);
            });
        },
        // 放弃修改
        "click .thd-btns a" : function(e){
            var UI = this;
            var jBtns = UI.arena.find(".thd-btns");
            
            // 恢复
            UI.update(null, UI.__OLD_TH_CONF_JSON);

            // 修改按钮状态
            jBtns.attr("mode", "loaded");

            // 闪一下表示放弃
            $z.blinkIt(UI.arena.find('.thd-tabs'));
        },
        // 查看 thing.js
        "click .thd-vc a" : function() {
            var json = this.getDataJson();
            POP.openViewTextPanel(json);
        }
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
        new TabsUI({
            parent : UI,
            gasketName : "tabs",
            defaultKey : UI.local('tabsKey') || "general",
            setup : {
                "general" : {
                    text : "i18n:thing.conf.tab_general",
                    uiType : "ui/thing/th_design_general",
                },
                "fields" : {
                    text : "i18n:thing.conf.tab_fields",
                    uiType : "ui/thing/th_design_fields",
                },
                "import" : {
                    text : "i18n:thing.conf.tab_import",
                    uiType : "ui/thing/th_design_import",
                },
                "export" : {
                    text : "i18n:thing.conf.tab_export",
                    uiType : "ui/thing/th_design_export",
                },
                "commands" : {
                    text : "i18n:thing.conf.tab_commands",
                    uiType : "ui/thing/th_design_commands",
                }
            },
            on_changeUI : function(key, theUI) {
                //console.log("key", key)
                // 本地记录当前标签状态
                UI.local('tabsKey', key);

                // 为 UI 设置通知函数
                theUI.notifyChanged = function(){
                    UI.trigger("change:data", this.getData());
                };

                // 设置数据
                theUI.setData(UI.getData());
            }
        }).render(function(){
            UI.defer_report("tabs");
        });

        // 返回延迟加载
        return ["icon", "name", "tabs"];
    },
    
    //...............................................................
    update : function(oThSet, thConf) {
        var UI   = this;
        oThSet   = oThSet || UI.__oTS;
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

        // 确保是对象
        if(_.isString(thConf)) {
            thConf = $z.fromJson(thConf);
            $z.setUndefined(thConf, "searchMenuFltWidthHint", null);
        }

        // 记录
        UI.__TH_CONF = thConf;
        UI.__OLD_TH_CONF_JSON = $z.toJson(thConf,null, '  ');

        // 更新
        UI.gasket.tabs.getCurrentUI().setData(thConf);

        // 同步按钮状态
        //UI.__check_btn_status();
    },
    //...............................................................
    on_sub_changed : function(data) {
        var UI = this;
        var jBtns = UI.arena.find(".thd-btns");
        //console.log("maybe data changed!", data);
        // 先和自己融合
        _.extend(UI.__TH_CONF, data);

        // 看看 json 变没
        var json = $z.toJson(UI.__TH_CONF,null, '  ');

        // 变了
        if(UI.isNeedSave()) {
            jBtns.attr("mode", "changed");
            $z.blinkIt(jBtns);
        }
        // 木有变
        else {
            jBtns.attr("mode", "loaded");
        }
    },
    //...............................................................
    // 判断是否修改了
    isChanged : function(){
        return this.__is_changed ? true : false;
    },
    //...............................................................
    isNeedSave : function(){
        var json = this.getDataJson();
        return json != this.__OLD_TH_CONF_JSON;
    },
    //...............................................................
    getDataJson : function(){
        return $z.toJson(this.__TH_CONF,null, '  ');
    },
    //...............................................................
    getData : function(){
        return this.__TH_CONF;
    },
    //...............................................................
    setData : function(oThSet) {
        this.update(oThSet);
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jInfo = UI.arena.find(">.thd-info");
        var jTabs = UI.arena.find(">.thd-tabs");

        var H = UI.arena.height();
        jTabs.css("height", H - jInfo.outerHeight(true));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);