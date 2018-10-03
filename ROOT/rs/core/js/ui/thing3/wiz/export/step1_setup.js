(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form'
], function(ZUI, Wn, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-export-1-setup" ui-fitparent="yes" ui-gasket="form">
    
</div>
*/};
//==============================================
return ZUI.def("app.wn.the_1_setup", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "all",
            mergeData : false,
            on_change : function(){
                UI.syncFormFieldsStatus();
            },
            fields : [{
                    key : "exportType",
                    title : "i18n:th3.export.exportType",
                    type   : "string",
                    dft    : opt.exportType,
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
                    title : "i18n:th3.export.pageRange",
                    tip   : "i18n:th3.conf.export.pageRange_tip",
                    type  : "boolean",
                    dft   : opt.pageRange,
                    uiWidth : "auto",
                    uiType : "@toggle",
                }, {
                    key : "pageBegin",
                    title : "i18n:th3.export.pageBegin",
                    type  : "int",
                    dft   : opt.pageBegin,
                    uiWidth : 100,
                    uiConf : {
                        valueType : "int"
                    }
                }, {
                    key : "pageEnd",
                    title : "i18n:th3.export.pageEnd",
                    tip   : "i18n:th3.conf.export.pageEnd_tip",
                    type  : "int",
                    dft   : opt.pageEnd,
                    uiWidth : 100,
                    uiConf : {
                        valueType : "int"
                    }
                }, {
                    key : "audoDownload",
                    title : "i18n:th3.export.audoDownload",
                    tip   : "i18n:th3.conf.export.audoDownload_tip",
                    type  : "boolean",
                    dft   : opt.audoDownload,
                    uiWidth : "auto",
                    uiType : "@toggle",
                }],
        }).render(function(){
            UI.defer_report("form");
        });
        
        return ["form"];
    },
    //...............................................................
    syncFormFieldsStatus : function(){
        var UI = this;
        var data = UI.gasket.form.getData();
        if(data.pageRange) {
            UI.gasket.form.enableField();
        }
        else {
            UI.gasket.form.disableField("pageBegin", "pageEnd");
            UI.gasket.form.enableFieldNot("pageBegin", "pageEnd");
        }
    },
    //...............................................................
    isDataReady : function(){
        return true;
    },
    //...............................................................
    getData : function(){
        return {
            setup : this.gasket.form.getData()
        };
    },
    //...............................................................
    setData : function(data) {
        this.gasket.form.setData(data.setup || {});
        this.syncFormFieldsStatus();
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);