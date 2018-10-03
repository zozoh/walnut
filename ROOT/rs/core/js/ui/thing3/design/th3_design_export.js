(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
], function(ZUI, Wn, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena th3-design-export" ui-fitparent="yes" ui-gasket="form">
</div>
*/};
//==============================================
return ZUI.def("ui.th3.thdesign_export", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function() {
        var UI  = this;
        //--------------------------------------- 
        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "all",
            displayMode : "compact",
            fitparent : false,
            mergeData : false,
            on_change : function(key, val){
                //console.log(key, val)
                // 设置字段状态
                UI.syncFormFieldsStatus();
                // 通知一下同步保存按钮状态
                UI.notifyChanged();
            },
            fields : [{
                    key : "enabled",
                    title : "i18n:th3.conf.import.enabled",
                    type   : "boolean",
                    uiType : "@switch",
                }, {
                    key : "exportType",
                    title : "i18n:th3.conf.export.exportType",
                    type   : "string",
                    dft    : "csv",
                    uiType : "@droplist",
                    uiConf : {
                        items : [{
                                value : "csv",
                                text  : "i18n:th3.conf.export.etp_csv"
                            }, {
                                value : "xls", 
                                text  : "i18n:th3.conf.export.etp_xls"
                            }]
                    }
                }, {
                    key : "pageRange",
                    title : "i18n:th3.conf.export.pageRange",
                    tip   : "i18n:th3.conf.export.pageRange_tip",
                    type  : "boolean",
                    dft   : false,
                    uiWidth : "auto",
                    uiType : "@toggle",
                }, {
                    key : "pageBegin",
                    title : "i18n:th3.conf.export.pageBegin",
                    type  : "int",
                    dft   : 1,
                    uiWidth : 100,
                    uiConf : {
                        valueType : "int"
                    }
                }, {
                    key : "pageEnd",
                    title : "i18n:th3.conf.export.pageEnd",
                    tip   : "i18n:th3.conf.export.pageEnd_tip",
                    type  : "int",
                    dft   : -1,
                    uiWidth : 100,
                    uiConf : {
                        valueType : "int"
                    }
                }, {
                    key : "audoDownload",
                    title : "i18n:th3.conf.export.audoDownload",
                    tip   : "i18n:th3.conf.export.audoDownload_tip",
                    type  : "boolean",
                    dft   : false,
                    uiWidth : "auto",
                    uiType : "@toggle",
                }, {
                    key : "mapping",
                    title : "i18n:th3.conf.export.mapping",
                    tip   : "i18n:th3.conf.export.mapping_tip"
                }, {
                    key : "processTmpl",
                    title : "i18n:th3.conf.export.processTmpl",
                    tip   : "i18n:th3.conf.export.processTmpl_tip",
                    uiType : "@text",
                    uiConf : {
                        placeholder : UI.msg("th3.conf.export.processTmpl_placeholder"),
                        height: 60
                    }
                }],
        }).render(function(){
            UI.defer_report("form");
        });

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
    syncFormFieldsStatus : function(){
        var UI = this;
        var data = UI.gasket.form.getData();
        if(data.enabled) {
            if(data.pageRange) {
                UI.gasket.form.enableField();
            }
            else {
                UI.gasket.form.disableField("pageBegin", "pageEnd");
                UI.gasket.form.enableFieldNot("pageBegin", "pageEnd");
            }
        }
        // 禁止
        else {
            UI.gasket.form.disableFieldNot("enabled");
        }
    },
    //...............................................................
    getData : function() {
        var UI = this;
        var data = UI.gasket.form.getData();
        //console.log(setupObj)
        return {
            dataExport : data
        };
    },
    //...............................................................
    setData : function(thConf) {
        var UI = this;

        thConf = thConf || {};

        // 确保是对象
        if(_.isString(thConf))
            thConf = $z.fromJson(thConf);

        // 更新通用全局配置
        var data = thConf.dataExport || {};

        // 更新
        UI.gasket.form.setData(data);

        // 设置字段状态
        UI.syncFormFieldsStatus();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);