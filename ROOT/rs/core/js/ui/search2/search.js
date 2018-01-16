(function($z){
$z.declare([
    'zui',
    'ui/search2/support/search_methods',
    'ui/menu/menu',
    'ui/pop/pop'
], function(ZUI, SearchMethods, MenuUI, POP){
//==============================================
var html = function(){/*
<div class="ui-arena search" ui-fitparent="true">
    <header>
        <div class="search-filter-con" ui-gasket="filter"></div>
        <div class="search-menu-con" ui-gasket="menu"></div>
    </header>
    <section ui-gasket="list"></section>
    <footer>
        <div class="search-sorter-con" ui-gasket="sorter"></div>
        <div class="search-pager-con"  ui-gasket="pager"></div>
    </footer>
</div>
*/};
//==============================================
return ZUI.def("ui.search2", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/search2/theme/search-{{theme}}.css",
    i18n : "ui/search2/i18n/{{lang}}.js",
    //...............................................................
    init : function(options){
        var UI  = SearchMethods(this);
        var opt = options;
        //...........................................
        // 设置默认值
        $z.setUndefined(opt, "filter", {});
        $z.setUndefined(opt, "list", {
            fields : [{
                key : "nm",
                title : "Name",
            }, {
                key : "id",
                title : "ID"
            }]
        });
        $z.setUndefined(opt, "pager", {});
        //...........................................
        // 必须制定获取数据的方法
        // 如果是字符串，通常格式类似
        //   obj -match '<%=match%>' -skip {{skip}} -limit {{limit}} -l -json -pager -sort '<%=sort%>'
        if(!opt.data) {
            throw "SearchUI required [data] field!";
        }
        if(_.isString(opt.data) && !UI.exec) {
            throw "SearchUI required [exec] function to run data command";
        }
        //...........................................
        // 格式化子 UI
        UI.__fmt_subUIs(opt, "filter", 'ui/search2/search_filter');
        UI.__fmt_subUIs(opt, "sorter", 'ui/search2/search_sorter');
        UI.__fmt_subUIs(opt, "list",   'ui/table/table');
        UI.__fmt_subUIs(opt, "pager",  'ui/search2/search_pager');
        //...........................................
        // 检查菜单, string 表示的为快捷菜单项
        var __check_menu_item = function(items, index) {
            var mi = items[index];
            // 快捷菜单
            if(_.isString(mi)){
                items[index] = UI.__quick_menu(mi);
            }
            // 自定义的快捷菜单
            else if(mi.qkey){
                items[index] = _.extend(UI.__quick_menu(mi.qkey), mi);
            }
            // 如果是菜单组，递归
            else if(_.isArray(mi.items)) {
                for(var i=0; i<mi.items.length; i++){
                    __check_menu_item(mi.items, i);
                }
            }
        };
        // 先检查顶级菜单
        if(_.isArray(opt.menu)){
            for(var i=0; i<opt.menu.length; i++){
                __check_menu_item(opt.menu, i);
            }
        }
        //...........................................
        // 默认查询上下文
        if(!_.isFunction(opt.queryContext)) {
            opt.__static_query_context = opt.queryContext || {};
            opt.queryContext = function(){
                return _.extend({}, opt.__static_query_context);
            };
        }
        //...........................................
        // 默认动作模板上下文上下文
        if(!_.isFunction(opt.cmdTmplContext)) {
            opt.__static_cmdTmpl_context = opt.cmdTmplContext || {};
            opt.cmdTmplContext = function(){
                return _.extend({}, opt.__static_cmdTmpl_context);
            };
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        // 要加载的控件类型
        var uiTypes = [];

        // 默认尝试用宽模式绘制菜单
        if(UI._draw_menu(false, function(){
            UI.defer_report("menu");
        })){
            uiTypes.push("menu");
        }

        // 加载 filter 控件
        if(opt.filter) {
            uiTypes.push("filter");
            seajs.use(opt.filter.uiType, function(FilterUI){
                UI.uiFilter = new FilterUI(_.extend({}, opt.filter.uiConf, {
                    parent     : UI,
                    gasketName : "filter"
                })).render(function(){
                    UI.defer_report("filter");
                    UI.listenUI(UI.gasket.filter, "filter:change", function(flt){
                        UI.refresh();
                    });
                });
            });
        } else {
            UI.arena.find(">header").remove();
        }

        // 加载 sorter 控件
        if(opt.sorter) {
            uiTypes.push("sorter");
            seajs.use(opt.sorter.uiType, function(SorterUI){
                UI.uiSorter = new SorterUI(_.extend({}, opt.sorter.uiConf, {
                    parent     : UI,
                    gasketName : "sorter"
                })).render(function(){
                    UI.defer_report("sorter");
                    UI.listenUI(UI.gasket.sorter, "sorter:change", function(flt){
                        UI.refresh();
                    });
                });
            });
        } else {
            UI.arena.find(">footer>.search-sorter-con").remove();
        }

        // 加载 list 控件
        if(opt.list) {
            uiTypes.push("list");
            seajs.use(opt.list.uiType, function(LstUI){
                UI.uiList = new LstUI(_.extend({}, opt.list.uiConf, {
                    parent     : UI,
                    gasketName : "list"
                })).render(function(){
                    UI.defer_report("list");
                });
            });
        }

        // 加载 pager 控件
        if(opt.pager) {
            uiTypes.push("pager");
            seajs.use(opt.pager.uiType, function(PagerUI){
                UI.uiPager = new PagerUI(_.extend({}, opt.pager.uiConf, {
                    parent     : UI,
                    gasketName : "pager"
                })).render(function(){
                    UI.defer_report("pager");
                    UI.listenUI(UI.gasket.pager,  "pager:change",  function(){
                        UI.refresh();
                    });
                });
            });
        } else {
            UI.arena.find(">footer>.search-pager-con-con").remove();
        }

        // 返回延迟加载列表
        return uiTypes;
    },
    //...............................................................
    // 根据 list.uiConf 里面的配置信息，查找某个字段的类型
    getFieldType : function(key) {
        return $z.invoke(this.uiList, "getFieldType", [key]);
    },
    //...............................................................
    _pop_form_mask : function(title, obj, cmdTmpl, callback) {
        var UI  = this;
        var opt = UI.options;

        POP.openFormPanel({
            title : title,
            data  : obj || {},
            form  : _.extend({
                    uiWidth : "all",
                    mergeData : false,
                }, opt.formConf, {
                    fields : opt.list.uiConf.fields
                }),
            // 点击了表单的确认
            callback : function(data){
                // 如果数据不符合规范，form 控件会返回空的
                if(data) {
                    // 根据数据得到 JSON 字符串
                    var json = $z.toJson(data).replace("'","\\'");
                    // 组合命令模板上下文
                    var cc = opt.cmdTmplContext.call(UI);
                    _.extend(cc, obj, {
                        json : json,
                        data : data,
                    });
                    // 生成命令并
                    var cmdText = $z.tmpl(cmdTmpl)(cc);
                    //console.log(cmdText);
                    UI.exec(cmdText, function(re){
                        var newObj = $z.fromJson(re);
                        callback(newObj);
                    });
                }
            }
        }, UI);
    },
    //...............................................................
    // 打开创建对话框
    openCreatePanel : function() {
        var UI  = this;
        var opt = UI.options;

        UI._pop_form_mask("i18n:new",
            {},
            opt.edtCmdTmpl["create"],
            function(newObj){
                UI.uiList.add(newObj);
                UI.uiList.setActived(UI.uiList.getObjId(newObj));
                UI.uiList.resize();

                UI.trigger("search:create", newObj);
                $z.invoke(opt, "on_create", [newObj], UI);
            });
    },
    //...............................................................
    // 弹出编辑对象的表单，str 表示某对象的下标或者ID 如果不传，那么将选择当前的对象
    openEditPanel : function(str) {
        var UI   = this;
        var obj = _.isUndefined(str)
                    ? UI.uiList.getActived()
                    : UI.uiList.getData(str);
        if(!obj){
            UI.alert(UI.msg("srh.e.noactived"));
            return;
        }
        // 开始执行 ...
        var opt  = UI.options;
        UI._pop_form_mask("i18n:edit",
            obj,
            opt.edtCmdTmpl["edit"],
            function(newObj){
                UI.uiList.update(newObj);
            });
    },
    //...............................................................
    // 删除选中的项目
    deleteChecked : function(){
        var UI   = this;
        var opt  = UI.options;
        var objs = UI.uiList.getChecked();
        if(!objs || objs.length == 0){
            UI.alert(UI.msg("srh.e.nochecked"));
            return;
        }

        // 开始删除
        UI.confirm("delwarn", function(){
            var tmpl = $z.tmpl(opt.edtCmdTmpl["delete"]);
            var str = "";
            objs.forEach(function(obj){
                str += tmpl(obj) + ";\n";
            });
            UI.exec(str, function(re){
                // 出错了
                if(/^e./.test(re)) {
                    UI.alert(re, "warn");
                    return;
                }
                // 在界面上移除
                var jN2 = null;
                for(var i=0; i<objs.length; i++){
                    jN2 = UI.uiList.remove(objs[i].id);
                }
                if(jN2){
                    UI.uiList.setActived(jN2);
                }
            });
        });
    },
    //...............................................................
    refresh : function(callback){
        var UI  = this;
        var opt = UI.options;
         //console.log("refresh!!!!")
        // zozoh@20151026:
        // 推迟运行，以便确保界面都加载完毕了
        // 这个问题，现在只发现在版本帝 Firefox 41.0.2 上有， Chrome 上没问题
        //window.setTimeout(function(){
        var cri = UI.uiFilter ? UI.uiFilter.getData() : {};
        var srt = UI.uiSorter ? UI.uiSorter.getData() : {};
        var pgr = UI.uiPager  ? UI.uiPager.getData()  : {
                                    skip : 0, limit: 0
                                };

        // console.log(pgr)

        // 创建查询上下文
        var qc = opt.queryContext.call(UI);
        _.extend(qc, pgr, {
            match    : cri ? $z.toJson(cri) : '{}',
            matchObj : cri,
            sort     : srt ? $z.toJson(srt) : '{}',
            sortObj  : srt,
        });

        //console.log("do_search",qc)

        // 记录一下之前激活的项目
        var activedId = UI.uiList.getActivedId();

        // 显示正在加载数据
        UI.showLoading();

        // 组合成查询条件，执行查询
        $z.evalData(opt.data, qc, function(reo){
            UI.hideLoading();

            // console.log(reo)
            // 如果是数组，则格式化数据
            if(_.isArray(reo)) {
                reo = {
                    list  : reo,
                    pager : {}
                };
            }

            // 更新分页器
            if(UI.uiPager)
                UI.uiPager.setData(reo.pager);

            // 设置数据
            UI.uiList.setData(reo ? reo.list : []);

            // 如果之前有高亮内容，重新高亮
            if(activedId){
                UI.uiList.setActived(activedId);
            }

            // 触发事件回调
            UI.trigger("search:refresh", reo);
            $z.invoke(UI.options, "on_refresh", [reo], UI);

            // 调整尺寸
            UI.resize();

            // 回调
            $z.doCallback(callback, [reo.list,reo.pager], UI);
        }, UI);
        //}, 0);

        // 返回自身dddd
        return UI;
    },
    //..............................................
    resize : function(deep){
        var UI   = this;
        var opt  = UI.options;
        var jSky   = UI.arena.find(">header");
        var jAsdie = UI.arena.find(">aside");
        var jMenu = jSky.children(".search-menu-con");
        var jFilter = jSky.children(".search-filter-con");

        // 如果已经初始化了菜单部分的原始宽度
        // 则自动判断动作菜单的宽窄模式
        if(jMenu.attr("prime-width")) {
            var w_sky = jSky.width();
            var isNarrow = jMenu.attr("narrow-mode") ? true : false;
            var w_menu   = jMenu.attr("prime-width") * 1;
            var w_flt    = w_sky - w_menu;
            
            // 得到 filter 宽度的参考值
            var fltWidthHint = $z.dimension(opt.filterWidthHint || "50%", w_sky);

            // 改成窄模式
            if(w_flt < fltWidthHint) {
                if(!isNarrow) {
                    UI._draw_menu(true);
                }
            }
            // 改成宽模式
            else {
                if(isNarrow) {
                    UI._draw_menu(false);
                }
            }
        }
        

        // 计算中间部分的高度
        var jList = UI.arena.find(">section");
        var jPager = UI.arena.find(">footer");
        var lH = UI.arena.height()
                     - jSky.outerHeight() 
                     - jAsdie.outerHeight()
                     - jPager.outerHeight();
        jList.css("height", lH);
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);