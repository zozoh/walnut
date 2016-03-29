(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form'
], function(ZUI, Wn, FormUI){
//==============================================
var html = function(){/*
<div class="ui-arena opreview" ui-fitparent="yes" show-info="yes">
    <div class="opreview-main"></div>
    <div class="opreview-info" ui-gasket="info"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.o_view_obj", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/o_view_obj/o_view_obj.css",
    i18n : "ui/o_view_obj/i18n/{{lang}}.js",
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
    },
    //...............................................................
    events : {
        "dblclick .opreview-main[mode=pic] img" : function(e){
            e.stopPropagation();
            var jImg = $(e.currentTarget);
            jImg.toggleClass("autofit");
        },
        "dragstart .opreview-main[mode=pic] img" : function(e){
            e.preventDefault();
        },
        "click .form-title" : function() {
            var UI = this;
            UI.arena.attr("show-info", "no");
        },
        "dblclick .opreview-main" : function() {
            var UI = this;
            UI.arena.attr("show-info", "yes");
        }
    },
    //...............................................................
    __show_info : function(o, fields) {
        var UI = this;
        new FormUI({
            parent : UI,
            gasketName : "info",
            title : '<i class="oicon" otp="'+o.tp+'"></i>' + o.nm,
            fields : (fields||[]).concat(UI.my_fields)
        }).render(function(){
            this.setData(o);
        });
    },
    //...............................................................
    getCurrentEditObj : function(){
        return this.gasket.info.getData();
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        var jM = UI.arena.find(".opreview-main").empty();
        
        // 不能预览文件夹
        if(o.race == "DIR"){
            throw "can.not.preview.DIR";
        }

        // 文本内容
        if(/text|javascript|json/.test(o.mime)){
            jM.attr("mode", "text");
            var jPre = $('<pre>').appendTo(jM);
            Wn.read(o, function(content){
                jPre.text(content);
            });
            // 显示信息
            UI.__show_info(o);
        }
        // 可以预览的图像
        else if(/\/(jpeg|png|gif)/.test(o.mime)){
            jM.attr("mode", "pic");
            var jImg = $("<img>").appendTo(jM);
            jImg.prop("src", "/o/read/id:"+o.id).one("load", function(){
                jImg.attr({
                    "old-width"  : this.width,
                    "old-height" : this.height
                });
                var W  = jM.width();
                var H  = jM.height();
                if(this.width > W || this.height > H){
                    jImg.addClass("autofit");
                }else{
                    jImg.removeClass("autofit");
                }
            });
            // 显示信息
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
            }]);
        }
        // 其他的对象
        else{
            jM.attr("mode","others");
            jM.html(UI.msg("opreview.noway"));
            // 显示信息
            UI.__show_info(o);
        }
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);