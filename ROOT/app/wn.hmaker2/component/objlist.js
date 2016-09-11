(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="noapi" class="warn">
        <i class="zmdi zmdi-alert-polygon"></i> {{hmaker.com.objlist.noapi}}
    </div>
    <div code-id="notemplate" class="warn">
        <i class="zmdi zmdi-alert-polygon"></i> {{hmaker.com.objlist.notemplate}}
    </div>
    <div code-id="filter" class="hmol-filter">
        <div class="flt-kwd">
            <div class="flt-kwd-input"><input></div>
            <div class="flt-kwd-btn"><b>Search</b></div>
        </div>
        <div class="flt-fld-list"></div>
        <div class="flt-fld-more"></div>
    </div>
    <div code-id="sorter" class="hmol-sorter">
        <ul></ul>
    </div>
    <div code-id="items" class="hmol-items">
        <div class="hmol-items-loader">点击测试数据加载</div>
    </div>
    <div code-id="pager_normal" class="pager pager-normal">
        普通翻页条
    </div>
    <div code-id="pager_jump" class="pager pager-jump">
        <!--ul>
            <li a="first"><a></a>
            <li a="prev"><a></a>
            <li a="current"><a></a>
            <li a="next"><a></a>
            <li a="last"><a></a>
        </ul-->
        带跳转的翻页条
    </div>
</div>
<div class="hmc-objlist ui-arena hm-del-save"></div>
`
//==============================================
return ZUI.def("app.wn.hm_com_objlist", {
    dom  : html,
    //...............................................................
    init : function(){
        var UI = HmComMethods(this);
    },
    //...............................................................
    events : {
        
    },
    //...............................................................
    setupProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/objlist_prop.js',
            uiConf : {}
        };
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 检查显示模式
        if(!UI.__check_mode(com)){
            return ;
        }

        // 清空
        UI.arena.empty();

        // 绘制
        UI.__paint_filter(com);
        UI.__paint_sorter(com);
        UI.__paint_item(com);
        UI.__paint_pager(com);

    },
    //...............................................................
    __paint_filter : function(com) {
        var UI  = this;
        var flt = com.filter;
        if(!flt)
            return;

        var jFilter = UI.ccode("filter").appendTo(UI.arena);

        // 关键字
        if(!flt.keyword || flt.keyword.length == 0) {
            jFilter.find(".flt-kwd").remove();
        }

        // 过滤字段
        var jFlds = jFilter.find(".flt-fld-list");
        var jMore = jFilter.find(".flt-fld-more");
        if(flt.fields.length > 0) {
            var showCount = 0;
            for(var fld of flt.fields) {
                // 创建字段
                var jF = $('<div class="flt-fld">').attr({
                    "key"   : fld.key,
                    "show"  : fld.show ? "yes" : null
                }).appendTo(jFlds);

                // 计数
                showCount += fld.show ? 1 : 0;

                // 文字描述
                $('<div class="flt-fld-text">').text(fld.text).appendTo(jF);

                // 可选值列表
                var jUl = $('<ul>').appendTo(jF);
                for(var cnd of fld.list) {
                    $('<li>').text(cnd.text).attr("value", cnd.value).appendTo(jUl);
                }

                // 可多选
                if(fld.multi)
                    $('<div class="flt-fld-multi">多选</div>').appendTo(jF);
            }

            // 如果还有字段需要隐藏
            if(showCount == flt.fields.length) {
                jMore.remove();
            }
        }
        // 没有过滤字段
        else {
            jFlds.remove();
            jMore.remove();
        }

     },
     //...............................................................
    __paint_sorter : function(com) {
        var UI = this;

        if(com.sorter && com.sorter.fields.length > 0) {
            var jSorter = UI.ccode("sorter").appendTo(UI.arena);
            var jUl = jSorter.find("ul");

            for(var fld of com.sorter.fields) {
                $('<li>').text(fld.text).attr({
                    "key"   : fld.key,
                    "order" : fld.order || 1,
                    "toggleable" : fld.toggleable ? "yes" : null,
                    "more" : fld.more ? $z.toJson(fld.more) : null
                }).appendTo(jUl);
            }
        }
     },
     //...............................................................
    __paint_pager : function(com) {
        if(com.pager)
            this.ccode("pager_" + (com.pager.style || "normal")).appendTo(this.arena);
    },
     //...............................................................
    __paint_item : function(com) {
        var UI = this;
        UI.ccode("items").appendTo(UI.arena);
    },
    //...............................................................
    __check_mode : function(com) {
        var UI = this;
        UI.arena.empty();

        // 确保有数据接口
        if(!com.api) {
            UI.ccode("noapi").appendTo(UI.arena);
            return false;
        }

        // 确保有显示模板
        if(!com.template) {
            UI.ccode("notemplate").appendTo(UI.arena);
            return false;
        }

        // 通过检查
        return true;
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);