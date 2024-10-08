(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = `
<div class="ui-arena hmc-image-prop" ui-fitparent="yes" ui-gasket="form">
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_image_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        var UI = HmMethods(this);
        UI.listenBus("change:com_img", UI.update);
    },
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
                UI.__sync_form_status(com);
            },
            autoLineHeight : true,
            fields : UI.IMG_FIELDS()
        }).render(function(){
            UI.defer_report("form");
        });

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
    IMG_FIELDS : function(){
        var UI = this;
        var oHome = this.getHomeObj();
        return [{
            key    : "src",
            title  : "i18n:hmaker.com.image.src",
            type   : "string",
            dft    : null,
            uiType : "ui/picker/opicker",
            uiConf : UI.getObjPickerEditConf("hmaker_pick_image", /^image/)
        }, {
            key    : "href",
            title  : "i18n:hmaker.prop.href",
            type   : "string",
            uiWidth : "all",
            uiType  : "app/wn.hmaker2/support/c_edit_link",
        }, {
            key    : "alt",
            title  : "i18n:hmaker.com.image.alt",
            type   : "string",
            dft    : null,
            emptyAsNull : true,
            uiWidth : "all",
            editAs : "input",
            uiConf : {placeholder:"i18n:hmaker.com.image.alt_tip"}
        }, {
            key    : "newtab",
            title  : "i18n:hmaker.com.image.newtab",
            type   : "boolean",
            editAs : "toggle",
        }, {
            key    : "naturalWidth",
            title  : "i18n:hmaker.com.image.naturalWidth",
            type   : "int",
            editAs : "label",
        }, {
            key    : "naturalHeight",
            title  : "i18n:hmaker.com.image.naturalHeight",
            type   : "int",
            editAs : "label",
        }, {
            key    : "objectFit",
            title  : "i18n:hmaker.prop.objectFit",
            type   : "string",
            dft    : "fill",
            editAs : "switch", 
            uiConf : {
                items : [{
                    text : 'i18n:hmaker.prop.objectFit_fill',
                    val  : 'fill',
                }, {
                    text : 'i18n:hmaker.prop.objectFit_contain',
                    val  : 'contain',
                }, {
                    text : 'i18n:hmaker.prop.objectFit_cover',
                    val  : 'cover',
                }]
            }
        }, {
            key    : "textPos",
            title  : "i18n:hmaker.com.image.tpos",
            type   : "string",
            dft    : null,
            editAs : "switch", 
            uiConf : {
                singleKeepOne : false,
                items : [{
                    text : 'i18n:hmaker.com.image.tpos_top',
                    val  : 'top',
                }, {
                    text : 'i18n:hmaker.com.image.tpos_center',
                    val  : 'center',
                }, {
                    text : 'i18n:hmaker.com.image.tpos_bottom',
                    val  : 'bottom',
                }]
            }
        }, {
            key    : "hoverShow",
            title  : "i18n:hmaker.com.image.hovershow",
            type   : "boolean",
            dft    : false,
            editAs : "toggle", 
        }, {
            key    : "text",
            title  : "i18n:hmaker.com.image.text",
            tip    : "i18n:hmaker.com.image.text_tip",
            type   : "string",
            dft    : null,
            emptyAsNull : true,
            uiWidth : "all",
            editAs : "text",
            uiConf : {height:200}
        }];
    },
    //...............................................................
    __sync_form_status : function(com) {
        // 开启新窗口选项
        if(!_.isUndefined(com.href)){
            if(com.href) {
                this.gasket.form.enableField("newtab");
            }
            // 关闭新窗口选项
            else{
                this.gasket.form.disableField("newtab");
            }
        }
    },
    //...............................................................
    update : function(com) {
        // 重新获取一下，因为控件的 paint 可能会改 com 的值
        // console.log(com)
        // com = this.uiCom.getData();
        // console.log(com)
        // 更新
        this.gasket.form.setData(com);
        this.__sync_form_status(com);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);