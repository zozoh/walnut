(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = `
<div class="ui-arena hmc-text-prop" ui-fitparent="yes" ui-gasket="form">
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_text_prop", HmMethods({
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
            autoLineHeight : true,
            uiWidth : "all",
            fields : [{
                key   : "textAlign",
                title : 'i18n:hmaker.com.text.textAlign',
                type  : "string",
                dft    : "",
                editAs : "switch",
                uiConf : {
                    items : [{
                        icon : '<i class="fa fa-align-left">',
                        val  : 'left',
                    }, {
                        icon : '<i class="fa fa-align-center">',
                        val  : 'center',
                    }, {
                        icon : '<i class="fa fa-align-right">',
                        val  : 'right',
                    }]
                }
            }, {
                key   : "color",
                title : 'i18n:hmaker.com.text.color',
                type   : "string",
                dft    : "",
                editAs : "color",
            }, {
                key   : "lineHeight",
                title : 'i18n:hmaker.com.text.lineHeight',
                type  : "string",
                dft    : "",
                editAs : "input",
            }, {
                key   : "letterSpacing",
                title : 'i18n:hmaker.com.text.letterSpacing',
                type  : "string",
                dft    : "",
                editAs : "input",
            }, {
                key   : "fontSize",
                title : 'i18n:hmaker.com.text.fontSize',
                type  : "string",
                dft    : "",
                editAs : "input",
            }, {
                key   : "textShadow",
                title : 'i18n:hmaker.com.text.textShadow',
                type  : "string",
                dft    : "",
                editAs : "input",
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