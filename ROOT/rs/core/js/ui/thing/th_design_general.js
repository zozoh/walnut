(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/c_icon',
    'ui/form/c_name',
    'ui/form/form'
], function(ZUI, Wn, CIconUI, CNameUI, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-design-general" ui-fitparent="yes" ui-gasket="form">
</div>
*/};
//==============================================
return ZUI.def("ui.thing.thdesign_general", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function() {
        var UI  = this;       
        //--------------------------------------- 集合通用设置
        new FormUI({
            parent : UI,
            gasketName : "form",
            mergeData : false,
            uiWidth : "all",
            displayMode : "compact",
            on_change : function(){
                UI.notifyChanged();
            },
            fields : [{
                title : "i18n:thing.conf.general.t_display",
                fields : [{
                    key : "searchMenuFltWidthHint",
                    title : "i18n:thing.conf.general.k_smfwh",
                    type : "string",
                    dft : "",
                    uiType : "@input",
                    uiConf : {
                        placeholder : "50%"
                    }
                }, {
                    key : "thIndex",
                    title : "i18n:thing.conf.general.k_thIndex",
                    type : "object",
                    dft : [],
                    uiType : "@switch",
                    uiConf : {
                        multi : true,
                        items : [{
                                value : "meta",
                                text : "i18n:thing.conf.general.k_thIndex_m"
                            }, {
                                value : "detail",
                                text : "i18n:thing.conf.general.k_thIndex_d"
                            }]
                    }
                }, {
                    key : "thData",
                    title : "i18n:thing.conf.general.k_thData",
                    type : "object",
                    dft : [],
                    uiType : "@switch",
                    uiConf : {
                        multi : true,
                        items : [{
                                value : "media",
                                text  : "i18n:thing.data.media"
                            }, {
                                value : "attachment", 
                                text : "i18n:thing.data.attachment"
                            }]
                    }
                }, {
                    key : "thumbSize",
                    title : "i18n:thing.conf.general.thumbsz",
                    tip : "i18n:thing.conf.general.thumbsz_tip",
                    type : "object",
                    dft : [],
                    uiWidth : "auto",
                    uiType : "@input",
                }]
            }, {
                title : "i18n:thing.conf.general.ukeys",
                fields : [{
                    key : "uniqueKeys",
                    tip : 'i18n:thing.conf.general.ukeys_tip',
                    type : "object",
                    dft : null,
                    uiType : "@text",
                    uiConf : {
                        height : 60,
                        parseData : function(uks) {
                            console.log("parseDate")
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
                            console.log("formatData")
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
                }]
            }, {
                title : "i18n:thing.conf.general.t_query",
                fields : [{
                    key : "searchFilter",
                    title : "过滤设置",
                    type  : "object",
                    dft   : null,
                    uiType : "@text",
                    uiConf : {
                        height : 120,
                        asJson : true,
                        placeholder : "JSON格式的配置信息"
                    }
                }, {
                    key : "searchSorter",
                    title : "排序设置",
                    type  : "object",
                    dft   : null,
                    uiType : "@text",
                    uiConf : {
                        height : 130,
                        asJson : true,
                        placeholder : "JSON格式的配置信息"
                    }
                }, {
                    key : "searchPager",
                    title : "翻页设置",
                    type  : "object",
                    dft   : null,
                    uiType : "@text",
                    uiConf : {
                        height : 100,
                        asJson : true,
                        placeholder : "JSON格式的配置信息"
                    }
                }]
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
    getData : function() {
        var UI = this;
        var setupObj = UI.gasket.form.getData();
        //console.log(setupObj)
        return _.extend({}, setupObj, {
            meta       : setupObj.thIndex.indexOf("meta")>=0,
            detail     : setupObj.thIndex.indexOf("detail")>=0,
            media      : setupObj.thData.indexOf("media")>=0,
            attachment : setupObj.thData.indexOf("attachment")>=0
        });
    },
    //...............................................................
    setData : function(thConf) {
        var UI = this;

        thConf = thConf || {};

        // 确保是对象
        if(_.isString(thConf))
            thConf = $z.fromJson(thConf);

        // 更新通用全局配置
        var setupObj = _.extend({}, thConf, {
            thIndex : [],
            thData  : []
        });
        // thIndex
        if(thConf.meta)
            setupObj.thIndex.push("meta");
        if(thConf.detail)
            setupObj.thIndex.push("detail");
        
        // thData
        if(thConf.media)
            setupObj.thData.push("media");
        if(thConf.attachment)
            setupObj.thData.push("attachment");

        // 更新
        UI.gasket.form.setData(setupObj);
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);