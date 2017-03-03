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
        HmMethods(this);
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
                //console.log(com)
                UI.uiCom.saveData("panel", com);

                UI.__sync_form_status(com);
            },
            autoLineHeight : true,
            fields : [{
                title  : "i18n:hmaker.com.image.tt_image",
                fields : UI.IMG_FIELDS()
            // }, {
            //     title  : "i18n:hmaker.com.image.tt_text",
            //     fields : UI.TXT_FIELDS()
            }]
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
            title  : "i18n:hmaker.prop.img_src",
            type   : "string",
            dft    : null,
            uiType : "ui/picker/opicker",
            uiConf : {
                base : oHome,
                lastObjKey : "hmaker_pick_image",
                mustInBase : true,
                setup : {
                    defaultByCurrent : false,
                    multi : false,
                    filter    : function(o) {
                        if('DIR' == o.race)
                            return true;
                        return /^image/.test(o.mime);
                    }
                },
                parseData : function(str){
                    //console.log("parseData", str);
                    if(!str)
                        return null;
                    // 指定了 ID
                    var m = /id:([\w\d]+)/.exec(str);
                    if(m)
                        return Wn.getById(m[1]);

                    // 指定了相对站点的路径
                    if(/^\//.test(str)){
                        var oHome = UI.getHomeObj();
                        return Wn.fetch(Wn.appendPath(oHome.ph, str));
                    }

                    // 默认指定了相对页面的路径
                    var oPage = UI.pageUI().getCurrentEditObj();
                    var pph = oPage.ph;
                    var pos = pph.lastIndexOf("/");
                    var aph = Wn.appendPath(pph.substring(0,pos), str);
                    return Wn.fetch(aph);
                },
                formatData : function(o){
                    if(!o)
                        return null;
                    var oHome = UI.getHomeObj();
                    //console.log("formatData:", o)
                    return "/" + Wn.getRelativePath(oHome, o);
                }
            }
        }, {
            key    : "href",
            title  : "i18n:hmaker.prop.href",
            type   : "string",
            uiWidth : "all",
            uiType  : "app/wn.hmaker2/support/c_edit_link",
        }, {
            key    : "newtab",
            title  : "i18n:hmaker.com.image.newtab",
            type   : "boolean",
            editAs : "toggle",
        }, {
            key    : "objectFit",
            title  : "i18n:hmaker.prop.objectFit",
            type   : "string",
            dft    : "fill",
            editAs : "link",
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
            key    : "text",
            title  : "i18n:hmaker.com.image.text",
            tip    : "i18n:hmaker.com.image.text_tip",
            type   : "string",
            dft    : null,
            emptyAsNull : true,
            editAs : "text",
            uiConf : {height:100}
        }];
    },
    //...............................................................
    TXT_FIELDS : function(){
        return [{
            key    : "textPos",
            title  : "i18n:hmaker.com.image.text_pos",
            type   : "string",
            dft    : "bottom",
            editAs : "switch",
            uiConf : {
                items : [{
                    text  : "i18n:hmaker.com.image.text_pos_N",
                    value : "top"
                }, {
                    text  : "i18n:hmaker.com.image.text_pos_S",
                    value : "bottom"
                }, {
                    text  : "i18n:hmaker.com.image.text_pos_P",
                    value : "center"
                }]
            }
        }, {
            key    : "text",
            title  : "i18n:hmaker.com.image.text",
            tip    : "i18n:hmaker.com.image.text_tip",
            type   : "string",
            dft    : null,
            emptyAsNull : true,
            editAs : "text",
            uiConf : {height:100}
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
        this.gasket.form.setData(com);
        this.__sync_form_status(com);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);