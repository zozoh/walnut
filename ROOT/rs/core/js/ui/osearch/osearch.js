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

function normalize_sub(options, key, dft) {
    var ud = options[key];
    // 未定义采用默认的
    if(!ud){
        options[key] = dft;
    }
    // 定义了类型
    else if(_.isString(ud)){
        options[key] = {
            uiType : ud,
            uiConf : {}
        };
    }
    // 其他的必须保证有 uiType
    else if(ud.uiType){
        $z.setUndefined(ud, "uiConf", {});
    }
    // 靠，什么玩意
    else{
        throw "osearch invalid define for [" + key + "]:\n" + $z.toJson(ud);
    }
}
//==============================================
var html = function(){/*
<div class="ui-arena" ui-fitparent="true">
    <div class="osearch-sky">
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
            pgsz : 50,
        });
        // 必须制定获取数据的方法
        if(!options.data){
            // 如果设置了执行器，默认用可执行命令来执行
            if(UI.exec)
                options.data = "obj -match '<%=condition%>' -skip {{skip}} -limit {{pgsz}} -json -pager -sort 'nm:1'"
            else
                throw "osearch require data field!!!";
        }

        // 确保 filter,list,pager 三个参数格式标准 
        normalize_sub(options, "filter", {uiType:"ui/osearch/ofilter",uiConf:{}});
        normalize_sub(options, "list", {uiType:"ui/otable/otable",uiConf:{checkable:true}});
        normalize_sub(options, "pager",  {uiType:"ui/osearch/opager",uiConf:{}});

        // 加载完毕，触发的事件
        UI.on("ui:redraw", function(){
            UI.listenUI(UI._filter, "filter:change", "do_change_filter");
            UI.listenUI(UI._pager,  "pager:change",  "do_change_page");
            // UI.arena.find(".menu-item").first().click();
        });
    },
    //...............................................................
    do_change_page : function(pg){
        this.do_search(null, pg);
    },
    //...............................................................
    do_change_filter : function(q){
        this.do_search(q, null);
    },
    //...............................................................
    __check_obj_fld : function(key){
        var UI = this;
        var D = UI.$el.data("@DATA");
        //console.log("osearch.__check_obj_fld:", D.uiForm);
        // 找到 form 的配置 
        var conf = ZUI.loadResource(D[key]);
        if(!conf) {
            var eKey = "osearch.e.o_no_" + key;
            alert(UI.msg(eKey));
            throw eKey + ":\n" + $z.toJson(D);
        }
        return conf;
    },
    //...............................................................
    __mask_form : function(cmd){
        var UI = this;
        return _.extend({
            setup : {
                uiType : "ui/oform/oform",
                uiConf : _.extend(UI.__check_obj_fld("uiForm"), {
                    exec : UI.exec,
                    i18n : UI._msg_map,
                    actions : [{
                        text : "i18n:ok",
                        cmd  : cmd
                    },{
                        text : "i18n:cancel",
                        handler : function(){
                            this.parent.close();
                        }
                    }]
                }, UI.options.formConf)
            }
        }, UI.options.maskConf);
    },
    //...............................................................
    do_action_new : function(){
        var UI = this;
        var D = UI.$el.data("@DATA");
        var race = UI.options.new_race || 'FILE';
        new MaskUI(UI.__mask_form({
            command  : "obj id:"+D.id+" -new '<%=json%>'",
            complete : function(re){
                var obj = $z.fromJson(re);
                UI._list.addLast(obj);
                UI._list.resize();
                this.parent.close();
            }
        })).render(function(){
            this.body.setData({});
        });
    },
    do_action_delete : function(){
        var UI = this;
        var objs = UI._list.getChecked();
        if(!(objs && objs.length>0)){
            var a_obj = UI._list.getActived();
            if(a_obj){
                objs = [a_obj];
            }
        }

        // 生成命令
        if(objs && objs.length > 0){
            var str = "";
            objs.forEach(function(obj){
                str += "rm -rf id:"+obj.id+"\n";
            });
            // 执行命令
            //console.log(str)
            UI.exec(str, function(){
                UI.do_search();
            });
        }
        // 警告
        else{
            alert(UI.msg("osearch.e.nochecked"));
        }
    },
    do_action_edit : function(){
        var UI = this;
        var obj = UI._list.getActived();
        if(!obj){
            alert(UI.msg("osearch.e.noactived"));
            return;
        }
        new MaskUI(UI.__mask_form({
            command  : "obj id:"+obj.id+" -u '<%=json%>'",
            complete : function(re){
                UI.do_search();
                UI._list.resize();
                this.parent.close();
            }
        })).render(function(){
            this.body.setData(obj);
        });
    },
    do_action_refresh : function(){
        var UI = this;
        UI.do_search();
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
    resize : function(deep){
        var UI = this;
        var jSky = UI.arena.find(".osearch-sky");
        var jActions = jSky.children(".osearch-actions");
        var jFilter = jSky.children(".osearch-filter");

        // 得到 jSky 上下空白
        // var sky_padding_v = jSky.outerHeight() - jSky.height();
        // var sky_padding_h = jSky.outerWidth() - jSky.width();
        // var flt_h = jFilter.outerHeight();
        // var flt_right = jActions.outerWidth();
        // var sky_h = flt_h + sky_padding_v;
        
        // jSky.css("height", sky_h);
        // jFilter.css({
        //     "position" : "absolute",
        //     "right"    : flt_right + sky_padding_h*2,
        //     "top"      : sky_padding_v / 2,
        //     "left"     : sky_padding_h / 2
        // });
        // jActions.css("margin-top", (sky_h - sky_padding_v - jActions.outerHeight())/2);
        var w_sky = jSky.width();
        var w_action = jActions.outerWidth(true);
        var w_filter = w_sky - w_action - 10;
        if(w_filter<200){
            w_filter = w_sky;
        }
        jFilter.css({
            "width"  : w_filter
        });

        // 计算中间部分的高度
        var jPager = UI.arena.children(".osearch-pager");
        var jList = UI.arena.children(".osearch-list");
        var lH = UI.arena.height() - jSky.outerHeight() - jPager.outerHeight();
        jList.css("height", lH);

    },
    //...............................................................
    getData : function(){
        var D = this.$el.data("@DATA");
        console.log("osearch.getData:", D.uiForm)
        return D;
    },
    //...............................................................
    setData : function(D, callback){
        var UI = this;
        
        // 保存数据
        UI.$el.data("@DATA", D);

        //console.log("A osearch.setData:", D.uiForm)

        // 将数据丢掉过滤器里，并取出查询信息
        UI._filter.setData(D);

        //console.log("B osearch.setData:", D.uiForm)
        
        return UI.do_search(null, {
            skip : 0
        }, callback);
    },
    //...............................................................
    refresh : function(){
        this.do_search();
    },
    //...............................................................
    getQuery : function(q, pg){
        var UI = this;
        q  = q  || UI._filter.getData();
        pg = pg || UI.options.dftQuery;
        return _.extend({}, UI.options.dftQuery, q, pg);
    },
    //...............................................................
    do_search : function(q, pg, callback){
        //console.log("do_search",q, pg)
        var UI = this;

        // zozoh@20151026:
        // 推迟运行，以便确保界面都加载完毕了
        // 这个问题，现在只发现在版本帝 Firefox 41.0.2 上有， Chrome 上没问题
        window.setTimeout(function(){
            var q = UI.getQuery(q, pg);
            
            // 显示正在加载数据
            if(UI._list)
                UI._list.showLoading();

            // 组合成查询条件，执行查询
            $z.evalData(UI.options.data, q, function(re){
                // 将查询的结果分别设置到列表以及分页器里
                UI._list.setData(re ? re.list : [], true, callback);
                UI._pager.setData(re? re.pager: {pn:0, pgnb:0, pgsz:0, nb:0, sum:0});
            }, UI);
        }, 0);

        // 返回自身
        return UI;
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);