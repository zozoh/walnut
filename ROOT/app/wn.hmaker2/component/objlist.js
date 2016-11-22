(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com_dynamic'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="noapi" class="dds-warn">
        <i class="zmdi zmdi-alert-polygon"></i> {{hmaker.dds.noapi}}
    </div>
    <div code-id="notemplate" class="dds-warn">
        <i class="zmdi zmdi-alert-polygon"></i> {{hmaker.dds.notemplate}}
    </div>
    <div code-id="api.empty"   class="api-empty">{{hmaker.dds.api_empty}}</div>
    <div code-id="api.loading" class="api-loading">
        <i class="zmdi zmdi-rotate-right zmdi-hc-spin"></i> {{hmaker.dds.api_loading}}
    </div>
    <div code-id="api.lackParams" class="api-lack-params">
        <i class="zmdi zmdi-alert-circle-o"></i> {{hmaker.dds.api_lack_params}}
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
<div class="hmc-objlist ui-arena hm-del-save hmc-dds">
    <section class="hmc-dds-msg"></section>
    <div class="hmc-objlist-filter">
        <div class="flt-kwd">
            <div class="flt-kwd-input"><input></div>
            <div class="flt-kwd-btn"><b>Search</b></div>
        </div>
        <div class="flt-fld-list"></div>
        <div class="flt-fld-more"></div>
    </div>
    <div class="hmc-objlist-sorter">
        <ul></ul>
    </div>
    <div class="hmc-objlist-list hmc-dynamic-con"></div>
    <div class="hmc-objlist-pager"></div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_objlist", {
    dom     : html,
    keepDom : false,
    //...............................................................
    init : function(){
        var UI = HmComMethods(this);
    },
    //...............................................................
    events : {
        "click .dynamic-reload" : function(){
            var UI    = this;
            var jList = UI.arena.children(".hmc-objlist-list");
            UI.__reload_data(null, jList);
        }
    },
    //...............................................................
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/objlist_prop',
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

        var jList = UI.arena.find(">.hmc-objlist-list").show();

        // 绘制
        UI.__paint_filter(com);
        UI.__paint_sorter(com);
        UI.__paint_data(com, jList);
        UI.__paint_pager(com);

    },
    //...............................................................
    __draw_data : function(list, com, dynamicKeys) {
        var UI = this;
        var jW = UI.$el.find(".hm-com-W")
        com = com || UI.getData();
        var jList = UI.arena.children(".hmc-objlist-list").empty();

        // 绘制动态参数键列表
        UI.__draw_dynamic_keys(jW);

        // 绘制重新加载按钮
        UI.__draw_dynamic_reload(jW);

        // 动态参数，但是缺少默认值，那么就没有足够的数据绘制了，显示一个信息吧
        if(UI.isDynamicButLackParams()) {
            UI.ccode("api.lackParams").appendTo(jList);
            return;
        }

        // 如果木有数据，就显示空
        if(!_.isArray(list) || list.length == 0) {
            UI.ccode("api.empty").appendTo(jList);
            return;
        }

        // console.log(list)

        // 加载模板
        var tmplInfo = UI.evalTemplate(com.template);

        // 得到皮肤选择器
        var skinSelector = UI.getSkinForTemplate(com.template);

        // 准备绘制模板参数
        var tmplOptions = _.extend({}, com.options, {
            API : UI.getHttpApiUrl()
        });

        // 逐个绘制
        for(var obj of list) {
            var ele  = document.createElement(tmplInfo.tagName || 'DIV');
            var jDiv = $(ele).appendTo(jList)[tmplInfo.name](obj, tmplOptions);
            if(skinSelector)
                jDiv.addClass(skinSelector);
        }

    },
    //...............................................................
    __paint_filter : function(com) {
        var UI  = this;
        var flt = com.filter;
        var jFilter = UI.arena.find(">.hmc-objlist-filter");

        // 隐藏过滤器
        if(!flt){
            jFilter.hide();
            return;
        }
        
        // 显示过滤器
        jFilter.show();

        // 关键字
        if(!flt.keyword || flt.keyword.length == 0) {
            jFilter.find(".flt-kwd").hide();
        }else{
            jFilter.find(".flt-kwd").show();
        }

        // 过滤字段
        var jFlds = jFilter.find(".flt-fld-list").empty();
        var jMore = jFilter.find(".flt-fld-more").empty();
        if(flt.fields.length > 0) {
            jFlds.show();
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
                jMore.hide();
            }else{
                jMore.show();
            }
        }
        // 没有过滤字段
        else {
            jFlds.hide();
            jMore.hide();
        }

     },
     //...............................................................
    __paint_sorter : function(com) {
        var UI = this;
        var jSorter = UI.arena.find(">.hmc-objlist-sorter");

        // 隐藏排序
        if(!com.sorter || com.sorter.fields.length == 0){
            jSorter.hide();
            return;
        }
        
        // 显示排序
        jSorter.show();

        // 准备循环绘制
        var jUl = jSorter.find("ul").empty();
        for(var fld of com.sorter.fields) {
            $('<li>').text(fld.text).attr({
                "key"   : fld.key,
                "order" : fld.order || 1,
                "toggleable" : fld.toggleable ? "yes" : null,
                "more" : fld.more ? $z.toJson(fld.more) : null
            }).appendTo(jUl);
        }
     },
     //...............................................................
    __paint_pager : function(com) {
        var UI = this;
        var jPager = UI.arena.find(">.hmc-objlist-pager").empty();

        // 隐藏翻页器
        if(!com.pager){
            jPager.hide();
            return;
        }
        
        // 显示翻页器
        jPager.show();
        UI.ccode("pager_" + (com.pager.style || "normal")).appendTo(jPager);
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);