(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/obrowser/support/browser__methods_list',
    'ui/table/table'
], function(ZUI, Wn, BrowserMethods, TableUI){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser-vmd-table" ui-gasket="list" ui-fitparent="yes"></div>
*/};
//==============================================
return ZUI.def("ui.obrowser_vmd_table", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    init : function(){
        BrowserMethods(this);
    },
    //..............................................
    redraw : function(){
        var UI  = this;
        var opt = UI.opt();

        UI.uiTable = (new TableUI({
            parent : UI,
            fitParent : true,
            gasketName : "list",
            renameable : opt.renameable,
            multi      : opt.multi,
            checkable  : opt.checkable,
            layout : {
                sizeHint : '*'
            },
            // 标记一下标准属性
            on_draw_item : function(jRow, o){
                jRow.addClass("wnobj");
                if(/^[.].+/.test(o.nm)){
                    jRow.addClass("wnobj-hide");
                }
            },
            // 捕捉事件
            on_checked : function(objs){
                UI.__do_notify();
            },
            on_actived : function(objs){
                UI.__do_notify();
            },
            on_blur : function(){
                UI.__do_notify();
            },
            on_open : function(o) {
                UI.browser().setData("id:"+o.id);
            },
            fields : [ {
                key   : "nm",
                title : "i18n:obrowser.title.nm",
                type  : "string",
                escapeHtml : false,
                display : function(o){
                    var html = Wn.objIconHtml(o);
                    html += '<span class="wnobj-nm">'+Wn.objDisplayName(UI, o)+'</span>';
                    return html;
                }
            }, {
                key   : "lm",
                title : "i18n:obrowser.title.lm",
                type  : "datetime",
                display : function(o){
                    return $z.parseDate(o.lm).toLocaleString();
                }
            }, {
                key   : "len",
                title : "i18n:obrowser.title.len",
                type  : "int",
                display : function(o){
                    if(o.race == 'DIR')
                        return  "--";
                    return $z.sizeText(o.len);
                }
            }, {
                key   : "tp",
                title : "i18n:obrowser.title.tp",
                display : function(o){
                    //console.log(o.id,o.nm,o.tp, o.mime, o.race)
                    var re;
                    // 有没有指定的类型
                    if(o.tp){
                        re = UI.str("ftype." + o.tp);
                        if(re) return re;
                    }
                    // 依靠 MIME
                    if("FILE" == o.race && o.mime){
                        var mimeKey = o.mime.replace(/[\/-]/,"_");
                        re = UI.str("fmime."+mimeKey);
                        //console.log(re)
                        if(re) return re;
                    }
                    // 根据 race
                    return UI.str("frace."+o.race, "Unknown");
                    
                }
            } ]
        })).render(function(){
            UI.defer_report("table");
        });
        return ["table"];
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);