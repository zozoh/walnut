(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/thing/support/th_methods',
    'ui/search2/search',
], function(ZUI, Wn, ThMethods, SearchUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-search" ui-fitparent="true"
    ui-gasket="main"></div>
*/};
//==============================================
return ZUI.def("ui.thing.th_search", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        var UI = ThMethods(this);

        UI.listenBus("change:meta", UI.setObj);
    },
    //..............................................
    _fill_context : function(uiSet) {
        uiSet.search = this;
    },
    //..............................................
    update : function(o, callback) {
        var UI  = this;
        var opt = UI.options;
        var bus = UI.bus();

        // 保存 HomeObj
        bus.setHomeObj(o);

        // 提出子控件需要的配置信息
        var conf = UI.getBusConf();
        //console.log("search", conf)

        // 加载搜索器
        new SearchUI(_.extend(conf, {
            parent : UI,
            gasketName : "main",
            menu : conf.searchMenu,
            menuContext : bus,
            filterWidthHint : conf.searchMenuFltWidthHint,
            data : function(params, callback){
                $z.invoke(conf.actions, "query", [_.extend(params, {
                    pid : bus.getHomeObjId()
                }), callback], bus);
            },
            filter : conf.searchFilter,
            list   : _.extend({}, conf.searchList, {
                fields : conf.fields,
                on_actived : function(th, jRow, prevObj) {
                    //console.log("on_actived");
                    $z.invoke(bus, "showObj", [th]);
                },
                on_blur : function(objs, jRows, nextObj) {
                    if(!nextObj){
                        //console.log("on_blur");
                        $z.invoke(bus, "showBlank");
                    }
                }
            }),
            sorter : $z.fallbackUndefined(conf.searchSorter, {
                    setup : [{
                            text : "按创建时间",
                            value : {ct:-1},
                        }, {
                            text : "按最后修改时间",
                            value : {lm:-1},
                        }, {
                            text : "按名称",
                            value : {th_nm:1},
                        }, {
                            text : "按类别",
                            value : {th_cate:1},
                        }, {
                            text : "按日期正序",
                            value : {th_date:1},
                        }, {
                            text : "按日期倒序",
                            value : {th_date:-1},
                        }],
                    storeKey : "th_search_sort_" + o.id
                }),
            pager : conf.searchPager,
        })).render(function(){
            // 刷新数据吧
            this.refresh(function(){
                //this.uiList.setActived(0);
                var args = Array.from(arguments);
                $z.doCallback(callback, args, UI.bus());
            });
            // 模拟点击
            //this.gasket.menu.arena.find(".menu-item").eq(3).click();
        });
    },
    //..............................................
    setObj : function(obj) {
        //console.log("setObj", obj);
        return this.gasket.main.uiList.update(obj);
    },
    //..............................................
    addObj : function(obj) {
        return this.gasket.main.uiList.add(obj, 0, 1);
    },
    //..............................................
    createObj : function(callback){
        var UI = this;

        var oHome = UI.getHomeObj();
        var text = UI.msg("thing.create_tip2", {
            text : UI.text(oHome.title || oHome.nm)
        });

        UI.prompt(text, {
            icon  : oHome.icon || '<i class="fa fa-plus"></i>',
            btnOk : "thing.create_do",
            ok : function(str){
                UI.invokeConfCallback("actions", "create", [str, function(newObj){
                    var jItem = UI.addObj(newObj);
                    UI.gasket.main.uiList.setActived(jItem);
                    $z.doCallback(callback, [newObj], UI);
                }]);                
            }
        });
    },
    //..............................................
    getChecked : function(){
        return this.gasket.main.uiList.getChecked();
    },
    //..............................................
    getFilterData : function(){
        return this.gasket.main.uiFilter.getData();
    },
    //..............................................
    removeChecked : function(callback, filter) {
        var UI = this;

        // 得到选中的东东
        var checkedObjs = UI.getChecked();

        // 得到选中的对象们
        var list = UI.getChecked();

        // 判断 th_live == 1 的对象
        var checkedObjs;
        if(_.isFunction(filter)) {
            checkedObjs = [];
            for(var i=0; i<list.length; i++) {
                var obj = filter(list[i]);
                if(obj)
                    checkedObjs.push(obj);
            }
        }
        // 不用过滤，直接来吧
        else {
            checkedObjs = list;
        }

        // 没有对象，显示警告
        if(checkedObjs.length == 0){
            UI.alert("thing.err.remove_none", "warn");
            return;
        }

        // 看看删除以后，应该高亮哪个对象
        var jN2  = UI.gasket.main.uiList.findNextItem(checkedObjs);
        var nit2 = jN2.length > 0 ? jN2.attr("oid") : 0;

        // 执行删除
        UI.invokeConfCallback("actions", "remove", [checkedObjs, function(){
            // 删除并试图高亮下一个对象
            var jN2 = UI.gasket.main.uiList.remove(checkedObjs);
            //console.log("jN2", jN2)
            // 高亮下一个
            if(jN2){
                UI.gasket.main.uiList.setActived(jN2);
                // 回调
                $z.doCallback(callback, [checkedObjs], UI);
            }
            // 刷新一下看看有木有东西，然后高亮第一个
            else{
                UI.gasket.main.uiList.setAllBlur();
                UI.refresh(function(){
                    UI.gasket.main.uiList.setActived(0);
                    // 回调
                    $z.doCallback(callback, [checkedObjs], UI);
                });
            }
        }]);
    },
    //..............................................
    setKeyword : function(str) {
        this.gasket.main.uiFilter.setKeyword(str);
        return this;
    },
    //..............................................
    getQueryContext : function(jumpToHead) {
        return this.gasket.main.getQueryContext(jumpToHead);
    },
    //..............................................
    refresh : function(callback, jumpToHead) {
        // 容忍参数类型
        if(_.isBoolean(callback)) {
            jumpToHead = callback;
            callback = undefined;
        }

        $z.invoke(this.bus(), "showBlank");
        this.gasket.main.refresh(callback, jumpToHead);
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);