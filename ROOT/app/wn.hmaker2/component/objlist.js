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
    <div code-id="list.empty"   class="hmol-list-empty">{{hmaker.com.objlist.items_empty}}</div>
    <div code-id="api.loading" class="hmol-list-loading">
        <i class="zmdi zmdi-rotate-right zmdi-hc-spin"></i> {{hmaker.com.objlist.items_reloading}}
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
<div class="hmc-objlist ui-arena hm-del-save">
    <section class="hmol-msg"></section>
    <div class="hmol-filter">
        <div class="flt-kwd">
            <div class="flt-kwd-input"><input></div>
            <div class="flt-kwd-btn"><b>Search</b></div>
        </div>
        <div class="flt-fld-list"></div>
        <div class="flt-fld-more"></div>
    </div>
    <div class="hmol-sorter">
        <ul></ul>
    </div>
    <div class="hmol-list"></div>
    <div class="hmol-pager"></div>
</div>
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
        "dblclick .hmol-list" : function(){
            this.__reload_items();
        }
    },
    //...............................................................
    __draw_items : function(list, com) {
        var UI  = this;
        com = com || UI.getData();
        var jList = UI.arena.children(".hmol-list").empty();

        // 如果木有数据，就显示空
        if(!_.isArray(list) || list.length == 0) {
            UI.ccode("list.empty").appendTo(jList);
            return;
        }

        // 读取 JS 文件内容(必须是个jQuery插件)，并生成逻辑
        var aphJS  = UI.getTemplateObjPath(com.template, "js");
        var jsContent = Wn.read(aphJS);
        eval(jsContent);

        // 逐个绘制
        // TODO 加入皮肤的支持
        for(var obj of list) {
            $('<div>').addClass(com.template).appendTo(jList)[com.template](obj, com.mapping);
        }

    },
    //...............................................................
    __reload_items : function(com){
        var UI = this;
        var jList = UI.arena.find(">.hmol-list").show();
        com = com || UI.getData();

        // 记录一下接口的特征，以防止重复加载
        var api_finger = $z.toJson(_.pick(com, "api", "params"));

        // 得到 api 的相关信息
        var apiUrl = UI.getHttpApiUrl(com.api);
        var params = com.params || {};
        // 显示正在加载
        UI.ccode("api.loading").appendTo(jList.empty());
        // 向服务器请求
        $.post(apiUrl, params, function(re){
            // 请求成功后记录接口特征
            UI.__api_finger = api_finger;

            // api 返回错误
            if(/^e[.]/.test(re)){
                $('<div class="api-error">').text(re).appendTo(jList.empty());
                return;
            }

            // 试图解析数据
            try {
                // 记录数据
                UI.__list = $z.fromJson(re);

                // 重绘项目
                UI.__draw_items(UI.__list, com);
            }
            // 接口调用错误
            catch (errMsg) {
                $('<div class="api-error">').text(errMsg).appendTo(jList.empty());
            }
        });
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

        // 绘制
        UI.__paint_filter(com);
        UI.__paint_sorter(com);
        UI.__paint_item(com);
        UI.__paint_pager(com);

    },
    //...............................................................
    __paint_item : function(com) {
        var UI = this;
        var jList = UI.arena.find(">.hmol-list").show();

        // 记录一下接口的特征，以防止重复加载
        var api_finger = $z.toJson(_.pick(com, "api", "params"));

        // 采用旧数据
        if(UI.__list && api_finger == UI.__api_finger) {
            UI.__draw_items(UI.__list, com);
        }
        // 重新加载
        else {
            UI.__reload_items(com);
        }
    },
    //...............................................................
    __paint_filter : function(com) {
        var UI  = this;
        var flt = com.filter;
        var jFilter = UI.arena.find(">.hmol-filter");

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
        var jSorter = UI.arena.find(">.hmol-sorter");

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
        var jPager = UI.arena.find(">.hmol-pager").empty();

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
    __check_mode : function(com) {
        var UI = this;
        var jMsg = UI.arena.find(">section.hmol-msg").empty();

        // 确保有数据接口
        if(!com.api) {
            UI.ccode("noapi").appendTo(jMsg.show());
            UI.arena.find(">div").hide();
            return false;
        }

        // 确保有显示模板
        if(!com.template) {
            UI.ccode("notemplate").appendTo(jMsg.show());
            UI.arena.find(">div").hide();
            return false;
        }

        // 通过检查
        jMsg.hide();
        return true;
        
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);