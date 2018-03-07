(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = `
<div class="ui-arena hmc-video-prop" ui-fitparent="yes" ui-gasket="form">
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_video_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        var UI = HmMethods(this);
        UI.listenBus("change:com_video", UI.update);
    },
    //...............................................................
    redraw : function() {
        var UI  = this;
        var oHome = UI.getHomeObj();

        // 通用样式设定
        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "all",
            // 根据 src 的改变，读取其他元数据
            on_change : function(key, val) {
                // 如果 src 更改，则修改相应的字段
                if("src" == key) {
                    //console.log(val)
                    // 有的话，读取一下元数据
                    if(val) {
                        var oVideo = Wn.fetch(Wn.appendPath(oHome.ph, val));
                        this.update({
                            "naturalWidth"  : oVideo.width,
                            "naturalHeight" : oVideo.height,
                            "duration"      : oVideo.duration,
                        });
                    }
                    // 否则清空
                    else {
                        this.update({
                            "naturalWidth"  : null,
                            "naturalHeight" : null,
                            "duration"      : null,
                        });
                    }
                }
                // 确保保存
                UI.uiCom.saveData("panel", this.getData());
            },
            autoLineHeight : true,
            fields : [{
                key    : "src",
                title  : "i18n:hmaker.com.video.src",
                type   : "string",
                dft    : null,
                uiType : "ui/picker/opicker",
                uiConf : UI.getObjPickerEditConf("hmaker_pick_image", /^video/)
            }, {
                key    : "controls",
                title  : "i18n:hmaker.com.video.controls",
                type   : "string",
                dft    : null,
                editAs : "switch",
                uiConf : {
                    singleKeepOne : false,
                    items : [{
                        text: 'i18n:hmaker.com.video.ctrl_dft', value:"dft"
                    }]
                }
            }, {
                key    : "autoplay",
                title  : "i18n:hmaker.com.video.autoplay",
                type   : "boolean",
                editAs : "toggle",
            }, {
                key    : "muted",
                title  : "i18n:hmaker.com.video.muted",
                type   : "boolean",
                editAs : "toggle",
            }, {
                key    : "poster",
                title  : "i18n:hmaker.com.video.poster",
                type   : "string",
                dft    : null,
                uiType : "ui/picker/opicker",
                uiConf : UI.getObjPickerEditConf("hmaker_pick_image", /^image/)
            }, {
                key    : "naturalWidth",
                title  : "i18n:hmaker.com.video.naturalWidth",
                type   : "int",
                editAs : "label",
            }, {
                key    : "naturalHeight",
                title  : "i18n:hmaker.com.video.naturalHeight",
                type   : "int",
                editAs : "label",
            }, {
                key    : "duration",
                title  : "i18n:hmaker.com.video.duration",
                type   : "float",
                editAs : "label",
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        // 返回延迟加载
        return ["form"];
    },
    //...............................................................
    update : function(com) {
        // 重新获取一下，因为控件的 paint 可能会改 com 的值
        // console.log(com)
        // com = this.uiCom.getData();
        // console.log(com)
        // 更新
        this.gasket.form.setData(com);
        //this.__sync_form_status(com);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);