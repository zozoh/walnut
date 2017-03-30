(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form'
], function(ZUI, Wn, FormUI){
//==============================================
var html = '<div class="ui-arena opreview" ui-fitparent="yes" ui-gasket="form"></div>';
//==============================================
return ZUI.def("ui.o_view_meta", {
    dom  : html,
    css  : "ui/o_view_obj/theme/o_view_obj-{{theme}}.css",
    i18n : "ui/o_view_obj/i18n/{{lang}}.js",
    //...............................................................
    __show_info : function(o, fields, callback) {
        var UI  = this;
        var opt = UI.options;
        new FormUI({
            parent : UI,
            gasketName : "form",
            title : opt.hideTitle ? null : '<i class="oicon" otp="'+o.tp+'"></i>' + o.nm,
            fields : (fields||[]).concat(UI.my_fields)
        }).render(function(){
            this.setData(o);
            $z.doCallback(callback, [o], UI);
        });
    },
    //...............................................................
    getCurrentEditObj : function(){
        return this.gasket.form.getData();
    },
    //...............................................................
    update : function(o, callback) {
        var UI = this;
        UI.$el.attr("oid", o.id);
        UI.refresh(callback);
    },
    //...............................................................
    refresh : function(callback) {
        var UI = this;

        // 得到对象
        var oid = UI.$el.attr("oid");
        var o = Wn.getById(oid);

        // console.log("I am refresh");
        
        // 图像
        if(/^image\//.test(o.mime)){
            UI.__show_info(o, [{
                key   : "width",
                title : "i18n:obj.width",
                type  : "int",
                dft : -1,
                editAs : "label"
            },{
                key   : "height",
                title : "i18n:obj.height",
                type  : "int",
                dft : -1,
                editAs : "label"
            }], callback);
        }
        // 可预览的视频
        else if(/^video\./.test(o.mime) && o.video_preview){
            UI.__show_info(o, [{
                key   : "width",
                title : "i18n:obj.width",
                type  : "int",
                dft : -1,
                editAs : "label"
            },{
                key   : "height",
                title : "i18n:obj.height",
                type  : "int",
                dft : -1,
                editAs : "label"
            }], callback);
        }
        // 其他的对象
        else{
            UI.__show_info(o, null, callback);
        }
    },
    //...............................................................
    init : function(){
        var UI = this;

        UI.my_fields = [{
            key   : "id",
            title : "i18n:obj.id",
            type  : "string",
            editAs : "label"
        }, {
            key   : "ph",
            title : "i18n:obj.ph",
            type  : "string",
            editAs : "label",
            uiConf : {
                escapeHtml : false,
                parseData : function(ph) {
                    return Wn.objDisplayPath(UI, ph, -3);
                }
            }
        }, {
            key   : "lm",
            title : "i18n:obj.lm",
            type  : "datetime",
            editAs : "label"
        }, {
            key   : "len",
            title : "i18n:obj.len",
            type  : "int",
            dft : 0,
            editAs : "label",
            uiConf : {
                escapeHtml : false,
                parseData : function(len) {
                    return '<b>' + $z.sizeText(len) + '</b> <em>(' + len + ')</em>';
                }
            }
        }, {
            key   : "race",
            title : "i18n:obj.race",
            type  : "string",
            editAs : "label"
        }, {
            key   : "tp",
            title : "i18n:obj.tp",
            type  : "string",
            editAs : "label"
        }, {
            key   : "mime",
            title : "i18n:obj.mime",
            type  : "string",
            editAs : "label"
        }, {
            key   : "md",
            title : "i18n:obj.md",
            type  : "string",
            editAs : "label"
        }, {
            key   : "c",
            title : "i18n:obj.c",
            type  : "string",
            editAs : "label"
        }, {
            key   : "m",
            title : "i18n:obj.m",
            type  : "string",
            editAs : "label"
        }, {
            key   : "g",
            title : "i18n:obj.g",
            type  : "string",
            editAs : "label"
        }, {
            key   : "ct",
            title : "i18n:obj.ct",
            type  : "datetime",
            editAs : "label"
        }];
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);