(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
    'ui/form/c_droplist',
], function(ZUI, Wn, HmMethods, FormUI, DroplistUI){
//==============================================
var html = `
<div class="ui-arena hmc-searcher-prop" ui-fitparent="yes" >
    <section class="hmcs-form" ui-gasket="form"></section>
    <h4>{{hmaker.com.searcher.tip_by}}</h4>
    <section class="hmcs-tip-api" ui-gasket="tip_api"></section>
    <section class="hmcs-tip-opt" ui-gasket="tip_opt"></section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_searcher_prop", HmMethods({
    dom  : html,
    //...............................................................
    redraw : function() {
        var UI  = this;

        // 得到 API 的主目录
        var oApiHome = Wn.fetch("~/.regapi/api");

        // 通用样式设定
        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "all",
            fitparent : false,
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
                                value : "_self",
                            }]
                        }
                    }, {
                        key    : "postParamName",
                        title  : 'i18n:hmaker.com.searcher.postParamName',
                        dft    : "k",
                    }]
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        // 显示提示列表
        new DroplistUI({
            parent : UI,
            gasketName : "tip_api",
            emptyItem : {},
            items : function(){
                return UI.getHttpApiList(function(oApi){
                    return HmRT.isMatchDataType(oApi.api_return, "StringArray");
                })
            },
            itemData : function(o) {
                var ph = "/" + Wn.getRelativePath(oApiHome, o);
                return {
                    icon  : '<i class="fa fa-plug"></i>',
                    text  : o.title,
                    value : ph,
                    tip   : ph,
                };
            },
            on_change : function(v){
                UI.uiCom.saveData(null, {tipApi:v, tipParams:{}}, true);
            }
        }).render(function(){
            UI.defer_report("tip_api");
        });

        // 返回延迟加载
        return ["form", "tip_api"];
    },
    //...............................................................
    update : function(com) {
        var UI = this;
        UI.gasket.form.setData(com);
        UI.gasket.tip_api.setData(com.tipApi);

        // 得到 api 对象
        var oApi = com.tipApi ? Wn.fetch("~/.regapi/api" + com.tipApi)
                              : null;

        // 如果有了 api，那么显示一下 api 的表单
        if(oApi) {
            // 更新就好
            if(UI.gasket.tip_opt) {
                UI.gasket.tip_opt.setData(com.tipParams || {});
            }
            // 生成新的表单
            else {
                var fields = UI._eval_form_fields_by_dsetting(oApi.params);
                new FormUI({
                    parent     : UI,
                    gasketName : "tip_opt",
                    mergeData  : false,
                    fitparent  : false,
                    uiWidth :"all",
                    fields     : fields,
                    on_update  : function(){
                        var data = this.getData();
                        UI.uiCom.saveData("panel", $z.obj("tipParams", data), true);
                    }
                }).render(function(){
                    this.setData(com.tipParams || {});
                });
            }
        }
        // 否则，注销表单
        else if(UI.gasket.tip_opt) {
            UI.gasket.tip_opt.destroy();
        }
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jForm = UI.arena.find(">.hmcs-form");
        var jH4   = UI.arena.find(">h4");
        var jTipApi = UI.arena.find(">.hmcs-tip-api");
        var jTipOpt = UI.arena.find(">.hmcs-tip-opt");
        var H = UI.arena.height();
        var h0 = jForm.outerHeight(true);
        var h1 = jH4.outerHeight(true);
        var h2 = jTipApi.outerHeight(true);
        jTipOpt.css("height", H - h0 - h1 - h2);
    }
    //...............................................................
}));
//===================================================================
});
})(window.NutzUtil);