(function($z){
$z.declare(['zui','ui/otable/otable'], function(ZUI, TableUI){
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
    update : function(o, UIBrowser){
        var UI = this;

        // 得到当前所有的子节点
        var list = UIBrowser.getChildren(o);

        // 渲染表格
        (new TableUI({
            parent : UI,
            fitParent : true,
            gasketName : "table",
            data : list,
            checkable : true,
            nameTitle : "i18n:obrowser.title.nm",
            icon : function(o){
                var iconHtml;
                // 自定义方法
                if(_.isFunction(UIBrowser.options.icon)){
                    iconHtml = UIBrowser.options.icon.call(UIBrowser, o);
                }
                // 绘制默认图标
                return iconHtml || '<i class="oicon" otp="'+(o.tp||'folder')+'"></i>';
            },
            layout : {
                sizeHint : '*'
            },
            columns : [ {
                key : "lm",
                title : "i18n:obrowser.title.lm",
                type : "datetime",
                display : function(o, fld){
                    return $z.parseDate(o.lm).toLocaleString();
                }
            }, {
                key : "len",
                title : "i18n:obrowser.title.len",
                display : function(o, fld){
                    if(o.race == 'DIR')
                        return  "--";
                    return $z.sizeText(o.len);
                }
            }, {
                key : "tp",
                title : "i18n:obrowser.title.tp",
                display : function(o, fld){
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
            // 标记一下标准的属性
            this.arena.find(".otable-row").each(function(){
                var jRow = $(this).addClass("wnobj");
                jRow.find(".otable-row-nm [tp=text]").addClass("wnobj-nm");
                if(/^[.].+/.test(jRow.attr("onm"))){
                    jRow.addClass("wnobj-hide");
                }
            });
        });

        // 最后重新计算一下尺寸
        UI.resize();
    },
    //..............................................
    getData : function(arg){
        return this.subUI("table").getData(arg);
    },
    //..............................................
    isActived : function(ele){
        return $(ele).closest(".otable-row").hasClass("otable-row-actived");
    },
    //..............................................
    getActived : function(){
        var UI = this;
        var jq = UI.arena.find(".otable-row-actived");
        if(jq.size()==0)
            return null;
        return UI.browser.getById(jq.attr("oid"));
    },
    //..............................................
    getChecked : function(){
        var UI = this;
        var re = [];
        this.arena.find(".otable-row-checked").each(function(){
            re.push(UI.browser.getById($(this).attr("oid")));
        });
        return re;
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);