(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/search2/search',
], function(ZUI, Wn, SearchUI){
//==============================================
var html = function(){/*
<div class="ui-arena th3-search" ui-fitparent="true"
    ui-gasket="main"></div>
*/};
//==============================================
return ZUI.def("ui.th3.search", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing3/theme/th3-{{theme}}.css",
    i18n : "ui/thing3/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        var UI = this;
        UI.listenBus("meta:updated", UI.on_update);
        UI.listenBus("list:remove", UI.on_remove, 'all');
        UI.listenBus("list:refresh", UI.on_refresh, 'all');
        UI.listenBus("do:cleanup", UI.on_do_cleanup, 'all');
        UI.listenBus("do:restore", UI.on_do_restore, 'all');
        UI.listenBus("list:add", UI.on_add);
    },
    //..............................................
    update : function() {
        var UI  = this;
        var man = UI.getMainData();

        // 提出子控件需要的配置信息
        var conf = man.conf;
        //console.log("search", conf)

        // 加载搜索器
        new SearchUI(_.extend(conf, {
            parent : UI,
            gasketName : "main",
            menu : conf.searchMenu,
            menuContext : UI,
            filterWidthHint : conf.searchMenuFltWidthHint,
            data : function(params, callback){
                params = _.extend({},params, {
                    pid : UI.getHomeObjId()
                });
                $z.setUndefined(params, "skip",  0);
                $z.setUndefined(params, "limit", 50);
                var cmdText = $z.tmpl("thing {{pid}} query -skip {{skip}}"
                                    + " -limit {{limit}}"
                                    + " -json -pager")(params);
                // 增加排序项
                if(params.sort) {
                    cmdText += " -sort '" + params.sort + "'";
                }
                //console.log(cmdText)
                // 因为条件可能比较复杂，作为命令的输入妥当一点
                Wn.exec(cmdText, params.match || "{}", function(re) {
                    UI.doActionCallback(re, callback);
                });
            },
            filter : conf.searchFilter,
            list   : _.extend({}, conf.searchList, {
                fields : conf.fields,
                on_actived : function(th, jRow, prevObj) {
                    UI.thMain().setCurrentObj(th);
                },
                on_checked : function(jRows) {
                    // 取出数据
                    var objs = this.getObj(jRows, true);
                                       
                    // 显示通知
                    UI.fireBus('obj:selected', objs);
                },
                on_open : function(obj) {
                    UI.fireBus('obj:open', obj);
                },
                on_blur : function(objs, jRows, nextObj) {
                    if(!nextObj){
                        UI.thMain().setCurrentObj(null);
                        UI.fireBus('obj:blur');
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
                    storeKey : "th_search_sort_" + man.home.id
                }),
            pager : conf.searchPager,
            on_refresh : function() {
                // 如果刷新后没有数据高亮，那么通知 blur
                if(!this.uiList.hasActived()) {
                    UI.fireBus('obj:blur');
                }
            }
        })).render(function(){
            // 刷新数据后，显示高亮项目
            this.refresh(function(){
                if(man.currentId) {                   
                    UI.setActivedAndDoCallback(man.currentId)
                }
            });
        });
    },
    //..............................................
    updateObj : function(obj) {
        //console.log("setObj", obj);
        return this.gasket.main.uiList.update(obj);
    },
    //..............................................
    setActived : function(arg) {
        return this.gasket.main.uiList.setActived(arg, true);
    },
    //..............................................
    setActivedAndDoCallback : function(arg) {
        return this.gasket.main.uiList.setActived(arg);
    },
    //..............................................
    addObj : function(obj) {
        return this.gasket.main.uiList.add(obj, 0, 1);
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
    removeChecked : function(callback) {
        var UI = this;

        // 得到选中的东东
        var checkedObjs = UI.getChecked();

        // 得到选中的对象们
        var list = UI.getChecked();

        // 判断 th_live == 1 的对象
        var checkedObjs = [];
        for(var i=0; i<list.length; i++) {
            var obj = list[i];
            if(obj.th_live >= 0)
                checkedObjs.push(obj);
        }

        // 没有对象，显示警告
        if(checkedObjs.length == 0){
            UI.alert("th3.err.remove_none", "warn");
            return;
        }

        // 看看删除以后，应该高亮哪个对象
        var jN2  = UI.gasket.main.uiList.findNextItem(checkedObjs);
        var nit2 = jN2.length > 0 ? jN2.attr("oid") : 0;

        // 准备命令
        var cmdText = "thing " + UI.getHomeObjId() + " delete -l";
        for(var i=0; i<checkedObjs.length; i++) {
            var obj = checkedObjs[i];
            cmdText += " " + obj.id;
        }
        // 执行删除
        Wn.exec(cmdText, function(re) {
            UI.doActionCallback(re, function(){
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
            });
        });
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
    on_remove : function(eo) {
        this.removeChecked();
    },
    //..............................................
    on_refresh : function(eo) {
        this.refresh.apply(this, eo.data);
    },
    //..............................................
    on_update : function(eo) {
        var obj = eo.data[0];
        var key = eo.data[1];
        // 标识一下，这样 list 更新的时候，会强制更新缩略图的
        obj.__force_update = 'thumb' == key;
        this.updateObj(obj);
    },
    //..............................................
    on_do_cleanup : function(eo) {
        var UI = this;
        UI.confirm("th3.clean_confirm", {
            icon : "warn",
            ok : function(){
                var cmdText = "thing " + UI.getHomeObjId() + " clean";
                Wn.logpanel(cmdText, function(){
                    UI.fireBus('obj:blur');
                    UI.refresh(true);
                });
            }
        });
    },
    //..............................................
    on_do_restore : function(eo) {
        var UI  = this;

        // 得到选中的对象们
        var list = UI.getChecked();
        // 判断 th_live == -1 的对象
        var checkedObjs = [];
        for(var i=0; i<list.length; i++) {
            var obj = list[i];
            if(obj.th_live < 0)
                checkedObjs.push(obj);
        }

        // 没有对象，显示警告
        if(checkedObjs.length == 0){
            UI.alert("th3.err.restore_none", "warn");
            return;
        }

        // 组装命令
        var cmdText = "thing " + UI.getHomeObjId() + " restore -l";
        for(var i=0; i<checkedObjs.length; i++) {
            var obj = checkedObjs[i];
            cmdText += " " + obj.id;
        }

        // 执行命令后清空对象显示，并刷新列表
        Wn.exec(cmdText, function(re) {
            UI.fireBus('obj:blur');
            UI.refresh(true);
        });
    },
    //..............................................
    on_add : function(eo) {
        var UI = this;
        var obj = eo.data;
        if(obj) {
            UI.addObj(obj);
            UI.setActived(obj);
            UI.thMain().setCurrentObj(obj);
        }
    },
    //..............................................
    refresh : function(callback, jumpToHead) {
        var UI = this;
        // 容忍参数类型
        if(_.isBoolean(callback)) {
            jumpToHead = callback;
            callback = undefined;
        }
        // 调用刷新
        UI.gasket.main.refresh(callback, jumpToHead);
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);