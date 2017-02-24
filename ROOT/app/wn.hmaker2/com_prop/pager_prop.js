(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = '<div class="ui-arena hmc-pager-prop" ui-fitparent="yes" ui-gasket="form"></div>';
//==============================================
return ZUI.def("app.wn.hm_com_pager_prop", HmMethods({
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
                UI.__sync_form_status();
            },
            uiWidth : "all",
            fields : [{
                key    : "pagerType",
                title  : 'i18n:hmaker.com.pager.pagerType',
                type   : "string",
                dft    : "button",
                editAs : "switch",
                uiConf : {
                    items : [
                        {text:"i18n:hmaker.com.pager.type_button",value:"button"},
                        {text:"i18n:hmaker.com.pager.type_jumper",value:"jumper"},
                    ]
                }
            }, {
                key    : "freeJump",
                title  : 'i18n:hmaker.com.pager.free_jump',
                type   : "boolean",
                dft    : true,
                editAs : "toggle",
            }, {
                key    : "maxBarNb",
                title  : 'i18n:hmaker.com.pager.max_bar_nb',
                type   : "int",
                dft    : 9,
            }, {
                key    : "dftPageSize",
                title  : 'i18n:hmaker.com.pager.dft_pgsz',
                tip  : 'i18n:hmaker.com.pager.dft_pgsz_tip',
                type   : "input",
                dft    : 50,
            }, {
                title  : "i18n:hmaker.com.pager.t_button",
                fields : [{
                    key    : "showFirstLast",
                    title  : 'i18n:hmaker.com.pager.show_first_last',
                    type   : "boolean",
                    dft    : true,
                    editAs : "toggle",
                }, {
                    key    : "btnFirst",
                    title  : 'i18n:hmaker.com.pager.btn_first',
                    type   : "string",
                    dft    : "|<<",
                    uiConf : {placeholder : "|<<"}
                }, {
                    key    : "btnPrev",
                    title  : 'i18n:hmaker.com.pager.btn_prev',
                    type   : "string",
                    dft    : "<",
                    uiConf : {placeholder : "<"}
                }, {
                    key    : "btnNext",
                    title  : 'i18n:hmaker.com.pager.btn_next',
                    type   : "string",
                    dft    : ">",
                    uiConf : {placeholder : ">"}
                }, {
                    key    : "btnLast",
                    title  : 'i18n:hmaker.com.pager.btn_last',
                    type   : "string",
                    dft    : ">>|",
                    uiConf : {placeholder : ">>|"}
                }]
            }, {
                title  : "i18n:hmaker.com.pager.t_brief",
                fields : [{
                    key    : "showBrief",
                    title  : 'i18n:hmaker.com.pager.show_brief',
                    type   : "boolean",
                    dft    : true,
                    editAs : "toggle",
                }, {
                    key    : "briefText",
                    title  : 'i18n:hmaker.com.pager.brief_text',
                    tip    : 'i18n:hmaker.com.pager.brief_text_tip',
                    type   : "string",
                    dft    : UI.msg("hmaker.com.pager.brief_dft"),
                    editAs : "text",
                    uiConf : {
                        placeholder : UI.msg("hmaker.com.pager.brief_dft"),
                        height      : 80,

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
    __sync_form_status : function(com) {
        com = com || this.uiCom.getData();

        // 分页类型
        if("jumper" == com.pagerType) {
            this.gasket.form.disableField("maxBarNb");
            this.gasket.form.enableField("freeJump");
        } else {
            this.gasket.form.disableField("freeJump");
            this.gasket.form.enableField("maxBarNb");
        }

        // 信息文字
        if(com.showBrief) {
            this.gasket.form.enableField("briefBext");
        } else {
            this.gasket.form.disableField("briefBext");
        }

    },
    //...............................................................
    update : function(com) {
        this.gasket.form.setData(com);
        this.__sync_form_status(com);
    },
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);