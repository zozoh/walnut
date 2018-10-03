(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_icon',
    'ui/form/c_name',
    'ui/form/form',
    'ui/tabs/tabs'
], function(ZUI, Wn, CIconUI, CNameUI, FormUI, TabsUI){
//==============================================
var html = function(){/*
<div class="ui-arena th3-design-general" ui-fitparent="yes">
    <section class="tdg-form" ui-gasket="form"></section>
    <section class="tdg-more" ui-gasket="more"></section>
</div>
*/};
//==============================================
return ZUI.def("ui.th3.thdesign_general", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function() {
        var UI  = this;       
        //--------------------------------------- 显示相关设置：form
        new FormUI({
            parent : UI,
            gasketName : "form",
            mergeData : false,
            uiWidth : "all",
            displayMode : "compact",
            fitparent : false,
            on_change : function(){
                UI.notifyChanged();
            },
            fields : [{
                key : "searchMenuFltWidthHint",
                title : "i18n:th3.conf.general.k_smfwh",
                type : "string",
                dft : "",
                uiWidth : 100,
                uiType : "@input",
                uiConf : {
                    placeholder : "50%"
                }
            }, {
                key : "layout",
                title : "i18n:th3.conf.general.k_layout",
                uiType : "@input",
                uiConf : {
                    placeholder : 'ui/thing3/layout/col3_md_ma.xml',
                    assist : {
                        icon : '<i class="zmdi zmdi-more"></i>',
                        autoOpen : false,
                        uiType : "ui/form/c_list",
                        uiConf : {
                            textAsValue : true,
                            items : [
                                "ui/thing3/layout/col3_md_ma.xml",
                                "ui/thing3/layout/col2_m.xml",
                            ]
                        }
                    }
                }
            }]
        }).render(function(){
            UI.defer_report("form");
        });
        //--------------------------------------- 一组高级设置
        new TabsUI({
            parent : UI,
            gasketName : "more",
            defaultKey : "uniqueKeys",
            mode : "left",
            on_changeUI : function(key, subUI, prevUI){
                // 记录一下 UI 的改动
                subUI.listenSelf('change:value', function(val){
                    //console.log(key, v);
                    UI.__set_more_val(key, val);
                });
                // 设置上对应的 val
                if(UI.__MORE) {
                    var v = UI.__get_more_val(key);
                    subUI.setData(v);
                }
            },
            setup : {
                "uniqueKeys" : {
                    text : "i18n:th3.conf.general.ukeys",
                    uiType : "ui/form/c_text",
                    uiConf : {
                        placeholder: 'i18n:th3.conf.general.ukeys_tip',
                        height : "100%",
                        parseData : function(uks) {
                            //console.log("parseDate")
                            var ss = [];
                            if(_.isArray(uks) && uks.length > 0) {
                                for(var i=0;i<uks.length;i++) {
                                    var uk = uks[i];
                                    if(_.isArray(uk.name) && uk.name.length > 0) {
                                        if(uk.required)
                                            ss.push("*" + uk.name.join(","));
                                        else
                                            ss.push(uk.name.join(","));
                                    }
                                }
                            }
                            return ss.join("\n");
                        },
                        formatData : function(str) {
                            //console.log("formatData")
                            if(!str)
                                return null;
                            var uks = [];
                            var ss = $z.splitIgnoreBlank(str, "\n");
                            for(var i=0; i<ss.length; i++) {
                                var uk = {};
                                var s =ss[i];
                                if(/^[*]/.test(s)) {
                                    uk.required = true;
                                    s = s.substring(1);
                                }
                                uk.name = $z.splitIgnoreBlank(s, ",");
                                if(uk.name.length > 0)
                                    uks.push(uk);
                            }
                            return uks.length > 0 ? uks : null;
                        }
                    }
                }, // ~ "uniqueKeys"
                "lnKeys" : {
                    text : "链接键设置",
                    uiType : "ui/form/c_text",
                    uiConf : {
                        height : "100%",
                        asJson : true,
                        placeholder : "JSON格式的配置信息"
                    }
                },
                "searchFilter" : {
                    text : "过滤设置",
                    uiType : "ui/form/c_text",
                    uiConf : {
                        height : "100%",
                        asJson : true,
                        placeholder : "JSON格式的配置信息"
                    }
                },
                "searchSorter" : {
                    text : "排序设置",
                    uiType : "ui/form/c_text",
                    uiConf : {
                        height : "100%",
                        asJson : true,
                        placeholder : "JSON格式的配置信息"
                    }
                },
                "searchPager" : {
                    text : "翻页设置",
                    uiType : "ui/form/c_text",
                    uiConf : {
                        height : "100%",
                        asJson : true,
                        placeholder : "JSON格式的配置信息"
                    }
                }
            }
        }).render(function(){
            UI.defer_report("more");
        });

        // 返回延迟加载
        return ["form", "more"];
    },
    //...............................................................
    __get_more_val : function(key) {
        var UI = this;
        var more = UI.__MORE;

        // 读取，默认为 null
        return more[key] || null;
    },
    //...............................................................
    __set_more_val : function(key, val) {
        var UI = this;
        var more = UI.__MORE;

        more[key] = val;

        // 通知改动
        UI.notifyChanged();
    }, 
    //...............................................................
    getData : function() {
        var UI = this;
        
        var formData = UI.gasket.form.getData();

        return _.extend({}, formData, UI.__MORE);
    },
    //...............................................................
    setData : function(thConf) {
        var UI = this;

        thConf = thConf || {};

        // 记录一下 more value
        UI.__MORE = $z.pick(thConf, /^(uniqueKeys|lnKeys|search(.+))$/);
        console.log("haha", UI.__MORE)

        // 确保是对象
        if(_.isString(thConf))
            thConf = $z.fromJson(thConf);

        // 设置表单
        UI.gasket.form.setData(thConf);

        // 设置more
        var key = UI.gasket.more.getCurrentKey();
        var val = UI.__get_more_val(key);
        UI.gasket.more.getCurrentUI().setData(val);
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jForm = UI.arena.find('>.tdg-form');
        var jMore = UI.arena.find('>.tdg-more');
        var H = UI.arena.height();
        jMore.css('height', H - jForm.outerHeight(true));
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);