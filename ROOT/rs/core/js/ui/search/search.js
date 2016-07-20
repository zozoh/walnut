(function($z){
$z.declare([
    'zui', 
    'ui/mask/mask',
    'ui/menu/menu'
], function(ZUI, MaskUI, MenuUI){
//==============================================
// 快捷菜单项
var quick_menus = {
    "refresh" : {
        key   : "srh_refresh",
        text  : "i18n:refresh",
        handler : function(){
            this.refresh();
        }
    },
    "delete" : {
        key   : "srh_delete",
        text  : "i18n:delete",
        handler : function(){
            var UI   = this;
            var objs = UI.uiList.getChecked();
            if(!objs || objs.length == 0){
                alert(UI.msg("srh.e.nochecked"));
                return;
            }
            // 警告
            if(!confirm(UI.msg("delwarn"))){
                return;
            }
            // 开始执行 ...
            var opt  = UI.options;
            var tmpl = $z.tmpl(opt.edtCmdTmpl["delete"]);
            var str = "";
            objs.forEach(function(obj){
                str += tmpl(obj) + ";\n";
            });
            UI.exec(str, function(){
                var jN2 = null;
                for(var i=0; i<objs.length; i++){
                    jN2 = UI.uiList.remove(objs[i].id);
                }
                if(jN2){
                    UI.uiList.setActived(jN2);
                }
            });
        }
    },
    // 只有表格的时候才能生效
    "create" : {
        key   : "srh_new",
        text  : "i18n:new",
        handler : function(){
            var UI   = this;
            var opt  = UI.options;
            _pop_form_mask(UI, "i18n:new",{},opt.edtCmdTmpl["create"],function(newObj){
                UI.uiList.add(newObj).setActived(UI.uiList.getObjId(newObj));
                UI.uiList.resize();
            });
        }
    },
    // 只有表格的时候才能生效
    "edit" : {
        key   : "srh_edit",
        text  : "i18n:edit",
        handler : function(){
            var UI   = this;
            var obj  = UI.uiList.getActived();
            if(!obj){
                alert(UI.msg("srh.e.noactived"));
                return;
            }
            // 开始执行 ...
            var opt  = UI.options;
            _pop_form_mask(UI, "i18n:edit",obj,opt.edtCmdTmpl["edit"],function(newObj){
                UI.uiList.update(newObj);
            });
        }
    }
};
//...............................................................
function _pop_form_mask(UI, title, obj, cmdTmpl, callback){
    var opt = UI.options;
    new MaskUI(_.extend({}, opt.maskConf, {
        dom   : UI.ccode("formmask").html(),
        i18n  : UI._msg_map,
        exec  : UI.exec,
        dom_events:{
            "click .srh-qform-ok" : function(e){
                var jq = $(this);
                if(jq.attr("ui-ing")){
                    return;
                }
                var uiMask   = ZUI(this);
                var formData = uiMask.body.getData();
                // 如果数据不符合规范，form 控件会返回空的
                if(formData){
                    var json   = $z.toJson(formData).replace("'","\\'");
                    var cmdText = $z.tmpl(cmdTmpl)(_.extend({}, obj, {json:json}));
                    //console.log(cmdText);
                    UI.exec(cmdText, function(re){
                        var newObj = $z.fromJson(re);
                        callback(newObj);
                        uiMask.close();
                    });
                }
            },
            "click .srh-qform-cancel" : function(e){
                var uiMask = ZUI(this);
                uiMask.close();
            }
        },
        setup : {
            uiType : "ui/form/form",
            uiConf : _.extend({
                uiWidth : "all"
            }, opt.formConf, {
                title  : title, 
                fields : opt.list.uiConf.fields
            })
        }
    })).render(function(){
        this.body.setData(obj);
    });
}
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <div code-id="formmask">
        <div class="ui-arena srh-mask-form" ui-fitparent="yes">
            <div class="ui-mask-bg"></div>
            <div class="ui-mask-main"><div class="srh-mask">
                <div class="srh-qform" ui-gasket="main"></div>
                <div class="srh-qbtns">
                    <b class="srh-qform-ok"><i class="ing fa fa-spinner fa-spin"></i>{{ok}}</b>
                    <b class="srh-qform-cancel">{{cancel}}</b>
                </div>
            </div></div>
            <div class="ui-mask-closer"><i class="fa fa-close"></i></div>
        </div>
    </div>
</div>
<div class="ui-arena srh" ui-fitparent="true">
    <div class="srh-sky">
        <div class="srh-actions" ui-gasket="menu"></div>
        <div class="srh-filter" ui-gasket="filter"></div>
    </div>
    <div class="srh-list"  ui-gasket="list"></div>
    <div class="srh-pager" ui-gasket="pager"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.srh", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/ui/search/search.css",
    i18n : "ui/search/i18n/{{lang}}.js",
    init : function(options){
        var UI  = this;
        var opt = options;
        //...........................................
        // 必须制定获取数据的方法
        if(!opt.data){
            // 如果设置了执行器，默认用可执行命令来执行
            if(UI.exec)
                opt.data = "obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort '<%=sort%>'";
            else
                throw "srh require data or exec field!!!";
        }
        //...........................................
        // 检查每个 UI
        var __fmt_subUIs = function(opt, key, dftType){
            var ud = opt[key];
            // 字符串表示类型
            if(_.isString(ud)) {
                opt[key] = {uiType:ud, uiConf:{}};
            }
            // 对象的话 ..
            else if(_.isObject(ud)){
                // 标准声明的
                if(ud.uiType) {
                    ud.uiConf = ud.conf || {};
                }
                // 那么就当做是配置信息
                else{
                    opt[key] = {uiType:dftType, uiConf:ud};
                }
            }
            // 采用默认
            else{
                opt[key] = {uiType:dftType, uiConf:{}};
            }
        }
        __fmt_subUIs(opt, "filter", 'ui/search/filter');
        __fmt_subUIs(opt, "list",   'ui/table/table');
        __fmt_subUIs(opt, "pager",  'ui/search/pager');
        
        //...........................................
        // 检查菜单, string 表示的为快捷菜单项
        if(opt.menu){
            for(var i=0; i<opt.menu.length; i++){
                var mi = opt.menu[i];
                // 快捷菜单
                if(_.isString(mi)){
                    opt.menu[i] = quick_menus[mi];
                }
            }
        }
        //...........................................
        // 默认查询上下文
        if(!_.isFunction(opt.queryContext)) {
            opt.__static_query_context = opt.queryContext || {};
            opt.queryContext = function(){
                return _.extend({}, this.options.__static_query_context);
            }
        }
        //...........................................
        // 加载完毕，触发的事件
        UI.on("ui:redraw", function(){
            UI.listenUI(UI.uiFilter, "filter:change", function(flt){
                var pgr  = UI.uiPager.getData();
                pgr.skip = 0;
                UI.refresh(null, flt, pgr);
            });
            UI.listenUI(UI.uiPager,  "pager:change",  function(pgr){
                UI.refresh(null, null, pgr);
            });
        });
    },
    //...............................................................
    _draw_actions : function(isNarrow, callback) {
        var UI  = this;
        var opt = UI.options;
        // 绘制菜单
        var jActions = UI.arena.find(".srh-actions");
        if(jActions.size() > 0){
            if(opt.menu){
                var menu_setup = isNarrow ? [{
                    icon  : '<i class="fa fa-ellipsis-v"></i>',
                    items : opt.menu 
                }] : opt.menu;
                UI.uiMenu = new MenuUI({
                    parent     : UI,
                    gasketName : "menu",
                    setup : menu_setup
                }).render(callback);
                // 返回 true 表示有菜单
                return true;
            }
            // 删除菜单 
            else{
                jActions.remove();
            }
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 要加载的控件类型
        var uiTypes = ["flt","lst","pgr"]

        // 绘制菜单
        if(UI._draw_actions(false, function(){
            UI.defer_report("menu");
        })){
            uiTypes.push("menu");
        }

        // 加载 filter 控件
        seajs.use(opt.filter.uiType, function(FltUI){
            UI.uiFilter = new FltUI(_.extend({}, opt.filter.uiConf, {
                parent     : UI,
                gasketName : "filter"
            })).render(function(){
                UI.defer_report("flt");
            });
        });

        // 加载 list 控件
        seajs.use(opt.list.uiType, function(LstUI){
            UI.uiList = new LstUI(_.extend({}, opt.list.uiConf, {
                parent     : UI,
                gasketName : "list"
            })).render(function(){
                UI.defer_report("lst");
            });
        });

        // 加载 pager 控件
        seajs.use(opt.pager.uiType, function(PagerUI){
            UI.uiPager = new PagerUI(_.extend({}, opt.pager.uiConf, {
                parent     : UI,
                gasketName : "pager"
            })).render(function(){
                UI.defer_report("pgr");
            });
        });
        
        // 返回延迟加载列表
        return uiTypes;
    },
    //...............................................................
    // 根据 list.uiConf 里面的配置信息，查找某个字段的类型
    getFieldType : function(key) {
        return this.uiList.getFieldType(key);
    },
    //...............................................................
    resize : function(deep){
        var UI = this;
        var jSky = UI.arena.find(".srh-sky");
        var jActions = jSky.children(".srh-actions");
        var jFilter = jSky.children(".srh-filter");

        var w_sky = jSky.width();

        // 没有 action
        if(jActions.size() == 0){
            jFilter.css("width", "100%");
            jSky.css("height", jFilter.outerHeight(true));
        }
        // 有 action 的话，就需要仔细判断一下了
        else {
            if(jActions.width() == 0 || jActions.height() == 0){
                return;
            }
            var w_action = jActions.attr("org-width") * 1;
            // 记录原始宽度
            if(!w_action){
                w_action = jActions.outerWidth(true);
                jActions.attr("org-width", w_action);
            }

            // 开始计算
            var h_action = jActions.outerHeight();
            // if(!w_action){
            //     w_action = jActions.outerWidth(true);
            //     jActions.attr("org-width", w_action);
            //     jActions.attr("org-width-inner", jActions.children().outerWidth(true));
            //     jFilter.css("height", jActions.outerHeight());
            // }
            jFilter.css("height", h_action);
            
            // 太窄
            if(w_sky/2 < w_action){
                // 原来不是窄的，那么现在要绘制成窄的
                if(!jSky.attr("narrow")){
                    jSky.attr("narrow","true");
                    UI._draw_actions(true);
                }
            }
            // 并排
            else{
                // 原来是窄的，那么恢复回来
                if(jSky.attr("narrow")){
                    jSky.removeAttr("narrow");
                    UI._draw_actions(false);
                }
            }
            // 更新宽度
            jFilter.css("width", jSky.width() - jActions.outerWidth(true));
            jSky.css("height", jFilter.outerHeight(true));
        }
        

        // 计算中间部分的高度
        var jPager = UI.arena.children(".srh-pager");
        var jList = UI.arena.children(".srh-list");
        var lH = UI.arena.height() - jSky.outerHeight() - jPager.outerHeight();
        jList.css("height", lH);
    },
    //...............................................................
    refresh : function(callback, flt, pgr){
        var UI  = this;
        var opt = UI.options;
        // console.log("refresh!!!!")
        // zozoh@20151026:
        // 推迟运行，以便确保界面都加载完毕了
        // 这个问题，现在只发现在版本帝 Firefox 41.0.2 上有， Chrome 上没问题
        //window.setTimeout(function(){
        flt = flt || UI.uiFilter.getData();
        pgr = pgr || UI.uiPager.getData();

        // 创建查询上下文
        var qc = opt.queryContext.call(UI);
        _.extend(qc, pgr, {
            match : flt.match ? $z.toJson(flt.match) : '',
            sort  : flt.sort  ? $z.toJson(flt.sort)  : ''
        });
        
        //console.log("do_search",qc)

        // 记录一下之前激活的项目
        var activedId = UI.uiList.getActivedId();
        
        // 显示正在加载数据
        $z.invoke(UI.uiList, "showLoading");

        // 组合成查询条件，执行查询
        $z.evalData(UI.options.data, qc, function(re){
            // 将查询的结果分别设置到列表以及分页器里
            UI.uiList.setData(re ? re.list : []);
            UI.uiPager.setData(re.pager);

            // 如果之前有高亮内容，重新高亮
            if(activedId){
                UI.uiList.setActived(activedId);
            }

            // 触发事件回调
            UI.trigger("search:refresh", re);
            $z.invoke(UI.options, "on_refresh", [re], UI);

            // 调整尺寸
            UI.resize();

            // 回调
            if(_.isFunction(callback)){
                callback.call(UI, re.list, re.pager);
            }
        }, UI);
        //}, 0);

        // 返回自身dddd
        return UI;
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);