(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
], function(ZUI, Wn, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-design-import" ui-fitparent="yes" ui-gasket="form">
</div>
*/};
//==============================================
return ZUI.def("app.wn.thdesign_import", {
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
                console.log(key, val)
                // 设置字段状态
                UI.syncFormFieldsStatus();
                // 通知一下同步保存按钮状态
                UI.notifyChanged();
            },
            fields : [{
                    key : "enabled",
                    title : "i18n:thing.conf.dimport.enabled",
                    type   : "boolean",
                    uiType : "@switch",
                }, {
                    key : "accept",
                    title : "i18n:thing.conf.dimport.accept",
                    tip   : "i18n:thing.conf.dimport.accept_tip",
                    uiConf : {
                        placeholder : UI.msg("thing.conf.dimport.accept_placeholder")
                    }
                },{
                    key : "uniqueKey",
                    title : "i18n:thing.conf.dimport.unikey",
                    tip   : "i18n:thing.conf.dimport.unikey_tip",
                }, {
                    key : "mapping",
                    title : "i18n:thing.conf.dimport.mapping",
                    tip   : "i18n:thing.conf.dimport.mapping_tip",
                }, {
                    key : "fixedForm",
                    title : "i18n:thing.conf.dimport.fixedForm",
                    tip   : "i18n:thing.conf.dimport.fixedForm_tip",
                }, {
                    key : "afterCommand",
                    title : "i18n:thing.conf.dimport.afterCommand",
                    tip   : "i18n:thing.conf.dimport.afterCommand_tip",
                    uiType : "@text",
                    uiConf : {
                        height: 100
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
            UI.gasket.form.enableField();
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
            dataImport : data
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
        var data = thConf.dataImport || {};

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