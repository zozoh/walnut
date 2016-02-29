(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/table/table'
], function(ZUI, Wn, TableUI){
//==============================================
var html = function(){/*
<div class="ui-arena obrowser-vmd-table" ui-gasket="table" ui-fitparent="yes"></div>
*/};
//==============================================
return ZUI.def("ui.obrowser_vmd_table", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //..............................................
    events : {
        "dblclick .wnobj" : function(e){
            var UI = this;
            var o  = UI.getData(e.currentTarget);
            UI.browser.setData("id:"+o.id);
        }
    },
    //..............................................
    redraw : function(){
        var UI = this;
        UI.uiTable = (new TableUI({
            parent : UI,
            fitParent : true,
            gasketName : "table",
            checkable : UI.browser.options.checkable,
            multi : UI.browser.options.multi,
            layout : {
                sizeHint : '*'
            },
            // 标记一下标准属性
            on_draw_row : function(jRow, o){
                jRow.addClass("wnobj");
                if(/^[.].+/.test(o.nm)){
                    jRow.addClass("wnobj-hide");
                }
            },
            // 捕捉事件
            on_checked : function(objs){
                UI.browser.trigger("browser:info", UI.msg("obrowser.selectNobj", {n:objs.length}))
            },
            on_blur : function(){
                UI.browser.trigger("browser:info", "");
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
    update : function(o){
        var UI = this;

        UI.uiTable.showLoading();

        // 得到当前所有的子节点
        Wn.getChildren(o, null, function(objs){
            // 更新数据
            UI.uiTable.setData(objs);

            // 最后重新计算一下尺寸
            UI.resize();
        });
    },
    //..............................................
    getData : function(arg){
        return this.uiTable.getData(arg);
    },
    //..............................................
    isActived : function(ele){
        return this.uiTable.isActived(ele);
    },
    //..............................................
    getActived : function(){
        return this.uiTable.getActived();
    },
    //..............................................
    getChecked : function(){
        return this.uiTable.getChecked();
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);