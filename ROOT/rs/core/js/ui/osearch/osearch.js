(function($z){
$z.declare(['zui', 'ui/mask/mask'], function(ZUI, MaskUI){
//==============================================
function __check_ui_part(UI, key, uiTypes, uiConfs){
    var uiD = UI.options[key];
    var jq = UI.arena.find(".osearch-"+key);
    if(uiD && uiD.uiType){
        UI.setup.uiTypes.push(uiD.uiType);
        UI.setup.uiConfs.push(_.extend({},uiD.uiConf,{
            parent     : UI,
            gasketName : key
        }));
        UI.setup.jqs.push(jq);
        UI.setup.keys.push(key);
    }
    else{
        jq.remove();
    }
}
//==============================================
var html = function(){/*
<div class="ui-code-template">
    <ul code-id="check.dft" class="osearch-check-opts">
        <li tp="all">{{osearch.check.all}}</li>
        <li tp="none">{{osearch.check.none}}</li>
        <li tp="toggle">{{osearch.check.toggle}}</li>
    </ul>
</div>
<div class="ui-arena" ui-fitparent="true">
    <div class="osearch-sky">
        <div class="osearch-checkbox ui-noselect"></div>
        <div class="osearch-actions" ui-gasket="menu"></div>
        <div class="osearch-filter" ui-gasket="filter"></div>
    </div>
    <div class="osearch-list"  ui-gasket="list"></div>
    <div class="osearch-pager" ui-gasket="pager"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.osearch", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/osearch/osearch.css",
    i18n : "ui/osearch/i18n/{{lang}}.js",
    init : function(options){
        var UI = this;
        $z.setUndefined(options, "checkable", options.list.uiConf.checkable ? true : false);
        $z.setUndefined(options, "dftQuery", {
            pn   :1,
            skip : 0,
            pgsz : 3,
        });
        // 必须制定获取数据的方法
        if(!options.data){
            // 如果设置了执行器，默认用可执行命令来执行
            if(options.exec)
                options.data = "obj -match '<%=condition%>' -skip {{skip}} -limit {{pgsz}} -json -pager"
            else
                throw "osearch require data field!!!";
        }

        // 加载完毕，触发的事件
        UI.on("ui:redraw", function(){
            UI.listenUI(UI._pager, "pager:change", "do_change_page");
        });
    },
    //...............................................................
    events : {
        "click .osearch-check-opts [tp]" : function(e){
            var me = $(e.currentTarget);
            var tp = me.attr("tp");
            var UI = ZUI.checkInstance(me);
            switch(tp){
            case "all":
                UI._list.check();
                break;
            case "none":
                UI._list.uncheck();
                break;
            case "toggle":
                UI._list.toggle();
                break;
            }
        }
    },
    //...............................................................
    do_change_page : function(pg){
        this.do_search(null, pg);
    },
    //...............................................................
    do_action_new : function(){
        var UI = this;
        var o = UI.$el.data("@DATA");
        // 找到 form 的配置 
        //var 

        // 打开遮罩，创建 form
        new MaskUI({
            width   : "80%",
            height  : .56,
            setup : {
                uiType : "ui/oform/oform",
                uiConf : {
                    hideGroupTitleWhenSingle : false,
                    mode : "tabs",
                    fields : [{
                            key    : "nm",
                            title  : "名称"
                        },{
                            key    : "age",
                            title  : "年龄"
                        },{
                            title  : "教育经历",
                            items  : [{
                                key   : "aa",
                                title : "小学" 
                            }, {
                                key   : "bb",
                                title : "中学" 
                            }]
                        },{
                            key    : "nm",
                            title  : "名族"
                        },{
                            key    : "age",
                            title  : "婚姻"
                        }]
                }
            }
        }).render(function(){
            console.log(ZUI.dump_tree())
        });
    },
    do_action_delete : function(){
        var UI = this;
        console.log(ZUI.dump_tree())
    },
    do_action_edit : function(){
        var UI = this;
        alert("I am edit");
    },
    do_action_refresh : function(){
        var UI = this;
        alert("I am refresh");
    },
    //...............................................................
    redraw : function(){
        var UI = this;
        UI.setup = {
            keys    : [],
            uiTypes : [],
            uiConfs : [],
            jqs     : []
        };
        __check_ui_part(UI, "filter");
        __check_ui_part(UI, "list");
        __check_ui_part(UI, "pager");

        // 加载控件
        seajs.use(UI.setup.uiTypes, function(){
            for(var index in arguments){
                var SubUI = arguments[index];
                var conf = UI.setup.uiConfs[index];
                var jq = UI.setup.jqs[index];
                var key = UI.setup.keys[index];
                (function(index, uiType){
                    var _ui = (new SubUI(conf));
                    UI["_"+key] = _ui;
                    _ui.render(function(){
                        UI.defer_report(index, uiType);
                    });
                })(index,UI.setup.uiTypes[index]);
            };
        });

        // 绘制选择框
        UI._draw_check();

        var deferUiTypes = [].concat(UI.setup.uiTypes);

        // 绘制动作按钮
        if(UI._draw_actions()){
            deferUiTypes.push("ui/menu/menu");
        }

        // 标识这是一次异步重绘
        return deferUiTypes;
    },
    //...............................................................
    _draw_actions : function(){
        var UI = this;
        if(_.isArray(UI.options.actions)){
            var menuC = {
                parent       : UI,
                gasketName   : "menu",
                setup        : []
            };
            UI.options.actions.forEach(function(mi){
                // 特殊的快捷按钮
                if(_.isString(mi)){
                    var qkey = mi;
                    var handler = UI["do_action_" + qkey];

                    // 回调必须是个函数啊
                    if(!_.isFunction(handler))
                        throw "!!! osearch: unknown mi handler: " + qkey;
                    
                    // 添加菜单配置项
                    menuC.setup.push({
                        type    : "button",
                        text    : "i18n:" + qkey,
                        handler : handler
                    });
                }
                // 其他
                else{
                    menuC.setup.push(mi);
                }
            });
            // 显示按钮 
            seajs.use("ui/menu/menu", function(UIMenu){
                new UIMenu(menuC).render(function(){
                    UI.defer_report(UI.setup.uiTypes.length, "ui/menu/menu");
                });
            });
            // 返回 true，以便延迟加载
            return true;
        }
        return false;
    },
    //...............................................................
    _draw_check : function(){
        var UI = this;
        if(UI.options.checkable){
            var jq = UI.ccode("check.dft");
            UI.arena.find(".osearch-checkbox").empty().append(jq);
        }
    },
    //...............................................................
    resize : function(deep){
        var UI = this;
        var jSky = UI.arena.find(".osearch-sky");
        var jCheck = jSky.children(".osearch-checkbox");
        var jActions = jSky.children(".osearch-actions");
        var jFilter = jSky.children(".osearch-filter");

        // 得到 jSky 上下空白
        var sky_padding_v = jSky.outerHeight() - jSky.height();
        var sky_padding_h = jSky.outerWidth() - jSky.width();
        var flt_h = jFilter.outerHeight();
        var flt_left = jCheck.outerWidth() + jActions.outerWidth();
        var sky_h = flt_h + sky_padding_v;
        
        jSky.css("height", sky_h);
        jFilter.css({
            "position" : "absolute",
            "left"     : flt_left + sky_padding_h*2,
            "top"      : sky_padding_v / 2,
            "right"    : sky_padding_h / 2
        });
        jCheck.css("margin-top",   (sky_h - sky_padding_v - jCheck.outerHeight())/2);
        jActions.css("margin-top", (sky_h - sky_padding_v - jActions.outerHeight())/2);

    },
    //...............................................................
    setData : function(o){
        var UI = this;
        
        // 保存数据
        UI.$el.data("@DATA", o);

        // 将数据丢掉过滤器里，并取出查询信息
        UI._filter.setData(o);
        
        return UI.do_search(null, {
            skip : 0
        });
    },
    //...............................................................
    do_search : function(cnd, pg){
        //console.log("do_search",cnd, pg)
        var UI = this;
        cnd = cnd || UI._filter.getData();
        pg  = pg  || UI._pager.getData();

        // 组合成查询条件，执行查询
        var q = _.extend({}, UI.options.dftQuery, cnd, pg);
        $z.evalData(UI.options.data, q, function(re){
            // 将查询的结果分别设置到列表以及分页器里
            UI._list.setData(re ? re.list : [], true);
            UI._pager.setData(re? re.pager: {pn:0, pgnb:0, pgsz:0, nb:0, sum:0});
        }, UI);

        // 返回自身
        return UI;
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);