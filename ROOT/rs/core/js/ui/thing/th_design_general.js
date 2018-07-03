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
return ZUI.def("app.wn.thdesign_general", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        
    },
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
                        height : 140,
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
                        height : 140,
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
                        height : 140,
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