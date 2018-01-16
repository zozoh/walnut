(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = '<div class="ui-arena hmc-searcher-prop" ui-fitparent="yes" ui-gasket="form"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_searcher_prop", HmMethods({
    dom  : html,
    //...............................................................
    redraw : function() {
        var UI  = this;

        // 通用样式设定
        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "all",
            on_update : function(com) {
                UI.uiCom.saveData("panel", com);
            },
            uiWidth : "all",
            fields : [{
                title  : "i18n:hmaker.com.searcher.display",
                fields : [{
                        key    : "placeholder",
                        title  : 'i18n:hmaker.com.searcher.placeholder',
                        type   : "string",
                        dft    : "",
                    }, {
                        key    : "defaultValue",
                        title  : 'i18n:hmaker.com.searcher.defaultValue',
                        type   : "string",
                        dft    : "",
                    }, {
                        key    : "btnText",
                        title  : 'i18n:hmaker.com.searcher.btnText',
                        type   : "string",
                        dft    : "",
                    }, {
                        key    : "trimSpace",
                        title  : 'i18n:hmaker.com.searcher.trimSpace',
                        type   : "boolean",
                        dft    : true,
                        uiType : "@toggle"
                    }, {
                        key    : "maxLen",
                        title  : 'i18n:hmaker.com.searcher.maxLen',
                        tip    : 'i18n:hmaker.com.searcher.maxLen_tip',
                        type   : "int",
                        dft    : 0,
                    }]
            }, {
                title  : "i18n:hmaker.com.searcher.post",
                fields : [{
                        key    : "postAction",
                        title  : 'i18n:hmaker.com.searcher.postAction',
                        type   : "string",
                        uiType  : "app/wn.hmaker2/support/c_edit_link",
                    }, {
                        key    : "postTarget",
                        title  : 'i18n:hmaker.com.searcher.postTarget',
                        dft    : "_blank",
                        uiType : "@switch",
                        uiConf : {
                            items : [{
                                text  : "i18n:hmaker.com.searcher.postTarget_blank",
                                value : "_blank",
                            }, {
                                text  : "i18n:hmaker.com.searcher.postTarget_self",
                                value : "self",
                            }]
                        }
                    }, {
                        key    : "postParamName",
                        title  : 'i18n:hmaker.com.searcher.postTarget',
                        dft    : "k",
                    }]
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
    update : function(com) {
        this.gasket.form.setData(com);
    },
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);