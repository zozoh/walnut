(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `
<div class="ui-arena hmc-navmenu"><ul></ul></div>
`;
//==============================================
return ZUI.def("app.wn.hm_com_navmenu", {
    keepDom   : true,
    dom       : html,
    className : "!",
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 高亮项目
        'click ul>li' : function(e) {
            var jq = $(e.currentTarget);

            // 仅针对激活控件有效
            if(!this.isActived())
                return;

            // 如果当前模式是区域选择，还需要同时高亮当前区域
            var com = this.getData();

            // 激活当前项目
            this.selectItem(jq);
        },
        // 取消高亮
        // 'click .hmc-navmenu' : function(e) {
        //     if($(e.target).closest('li').length == 0){
        //         this.unselectItem();
        //     }
        // },
        // 编辑文字
        'click ul>li[current] a span' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            
            // 仅针对激活控件有效
            if(!this.isActived())
                return;

            var jItem = jq.closest("li");
            var index = jItem.prevAll().length;

            $z.editIt(jq, function(newval, oldval) {
                if(newval && newval!=oldval) {
                    UI.updateItemField(index, "text", newval);
                }
            });
        },
        // 仅仅是编辑文字的时候，不要导致删除
        "keydown input" : function(e) {
            e.stopPropagation();
        }
    },
    //...............................................................
    getMenuItems : function() {
        var UI = this;
        var re = [];

        UI.arena.find("li").each(function(){
            re.push(UI.getItemData(this));
        });

        return re;
    },
    //...............................................................
    $item : function(arg) {
        if(_.isNumber(arg))
            return this.arena.find("li").eq(arg);
        
        if($z.isjQuery(arg) || _.isElement(arg))
            return $(arg);

        throw "navmenu $item Don't known how to found item by : " + arg;
    },
    //...............................................................
    getActivedItem : function() {
        var UI = this;
        var jItem = UI.arena.find('li[current]');
        if(jItem.length > 0)
            return jItem;
    },
    //...............................................................
    getActivedItemIndex : function() {
        for(var i=0; i<jItems.length; i++){
            if(jItems.eq(i).attr("current"))
                return i;
        }
        return -1;
    },
    //...............................................................
    getItemData : function(index) {
        var jLi = this.$item(index);
        return {
            text    : jLi.find(">a>span").text(),
            href    : jLi.attr("href") || "",
            newtab  : jLi.attr("newtab") == "yes",
            current : jLi.attr("current") == "yes",
            depth   : jLi.parentsUntil(".hmc-navmenu", "ul").length - 1
        };
    },
    //...............................................................
    createItem : function(item) {
        var UI = this;

        item = item || {
            text : UI.msg("hmaker.com.navmenu.item_dft_text"),
            href : "",
            newtab : false
        };

        // 用 A 包裹文字是因为考虑到以后可能增加 icon 之类的前缀修饰元素
        var jLi = $('<li><a><i></i><span></span></a></li>');
        var jCurrentItem = UI.getActivedItem();
        if(jCurrentItem) {
            jLi.insertAfter(jCurrentItem);
        }else{
            var jUl = UI.arena.find("ul").last();
            jUl.appendTo(jUl);
        }

        UI.updateItem(jLi, item, true);

        // 选中新增项目(顺便通知修改)
        UI.selectItem(jLi);

        return item;
    },
    //...............................................................
    selectItem : function(index) {
        var UI  = this;
        var jLi = UI.$item(index);

        // 已经激活了 ...
        if(jLi.attr("current")){
            return;
        }

        UI.arena.find('li[current]').removeAttr("current");
        jLi.attr("current", "yes");

        // 同步区域修改（只有 atype=="toggleArea" 才会生效)
        UI.syncToggleArea();

        // 通知修改
        UI.notifyDataChange("page");
    },
    //...............................................................
    unselectItem : function() {
        this.arena.find('li[current]').removeAttr("current");

        // 通知修改
        this.notifyDataChange("page");  
    },
    //...............................................................
    // 将自身对于 ToggleArea 的设定加入一个 Map
    // 并返回一个结果对象
    // {
    //     map : {..},    // 结果集，即传入的 map
    //     com : {..},    // 本组件的数据
    // }
    joinToggleAreaMap : function(map, com) {
        com = com || this.getData();
        map = map || {};
        // 关联的某个布局 ...
        if(com.layoutComId) {
            // 找一下，具体的布局设置
            this.arena.find('li').each(function(){
                var jLi = $(this);
                var aid = jLi.attr("href");
                map[aid] = jLi.attr("current") || "no";
            });
        }
        return map;
    },
    //...............................................................
    updateItem : function(index, item, quiet) {
        //console.log(item)
        var UI  = this;
        var jLi = UI.$item(index);
        jLi.find("a>span").text(item.text);
        jLi.attr({
            "newtab" : !item.newtab ? null : "yes",
            "href"   : item.href || null,
        });

        if(!quiet)
            UI.notifyDataChange("page");
    },
    //...............................................................
    updateItemField : function(index, key, val, quiet) {
        var UI   = this;
        var jLi  = UI.$item(index);
        var item = UI.getItemData(jLi);
        item[key] = val;

        UI.updateItem(jLi, item, quiet);
    },
    //...............................................................
    deleteItem : function(index) {
        var UI = this;
        var jLi = UI.$item(index);

        // 如果删除的是高亮东东，试图高亮下一个
        var jNext;
        if(jLi.attr("current")) {
            jNext = jLi.next();
        }
        if(jNext && jNext.length == 0) {
            jNext = jLi.prev();
        }

        // 删除
        jLi.remove();

        // 高亮下一个
        if(jNext && jNext.length > 0) {
            UI.selectItem(jNext);
        }
        // 没的高亮了，就通知吧
        else{
            UI.notifyDataChange("page");
        }
    },
    //...............................................................
    movePrev : function(index) {
        var UI = this;
        var jLi = UI.$item(index);

        var jTa = jLi.prev();
        if(jTa.length == 0)
            return;

        jLi.insertBefore(jTa);
        UI.notifyDataChange("page");
    },
    //...............................................................
    moveNext : function(index) {
        var UI = this;
        var jLi = UI.$item(index);

        var jTa = jLi.next();
        if(jTa.length == 0)
            return;

        jLi.insertAfter(jTa);
        UI.notifyDataChange("page");
    },
    //...............................................................
    moveSub : function(index) {
        var UI = this;
        var jLi = UI.$item(index);

        var jTa = jLi.prev();
        if(jTa.length == 0)
            return;

        // 试图成为它的子
        var jTaUl = jTa.children('ul');
        if(jTaUl.length == 0) {
            jTaUl = $('<ul>').appendTo(jTa);
        }
        jLi.appendTo(jTaUl);

        // 通知修改
        UI.notifyDataChange("page");
    },
    //...............................................................
    _redraw_com : function() {
        var jUl = this.arena.find("ul");

        // 确保至少有一个菜单项
        if(jUl.children().length == 0){
            this.createItem();
        }
    },
    //...............................................................
    on_actived : function(prevCom) {
        var com = this.getData();
        this.pageUI().setToggleCurrent(com.layoutComId);
    },
    //...............................................................
    on_blur : function(nextCom) {
        this.pageUI().setToggleCurrent();
    },
    //...............................................................
    paint : function(com) {
        var UI  = this;

        console.log("paint", com);

        // 标识自己的类型
        UI.$el.attr("navmenu-atype", com.atype);
        
        // 同步区域
        UI.syncToggleArea(com);
    },
    //...............................................................
    // 如果自己的 atype 是 'toggleArea' 
    // 本函数将根据菜单的自身状态，同步设置对应的分栏
    syncToggleArea : function(com) {
        var UI = this;
        com = com || UI.getData();

        // 如果是区域显示，则找到对应分栏，设置属性
        if("toggleArea" == com.atype) {
            // 去掉自己所有的 newtab 设定
            UI.arena.find('li[newtab]').removeAttr("newtab");
            // 设置了关联的分栏控件，那么对其进行显示或者隐藏
            if(com.layoutComId) {
                // 归纳区域
                var map = UI.joinToggleAreaMap(null, com);
                //console.log(map)
                // 更新区域的显示和隐藏
                UI.pageUI().toggleLayout(com.layoutComId, map);
            }
            // 没设置的话，确保清除了页面上所有的 toggle 区域
            else {
                UI.pageUI().cleanToggleArea();
            }
        }
        // 否则查找所有的分栏，如果没有任何菜单关联它，则取消
        else if(com.layoutComId){
            // 首先让自己取消关联
            com.layoutComId = null;

            // 整页搜索
            UI.pageUI().cleanToggleArea();

            // 更新一下属性面板
            UI.saveData("page", com, true);
        }

        // 对于激活控件，更新一下 toggle 区域
        if(UI.isActived()) {
            this.pageUI().setToggleCurrent(com.layoutComId);
        }
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    getDataProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/navmenu_prop.js',
            uiConf : {}
        }
    },
    //...............................................................
    // formatSize : function(prop, com, fromMode) {
        
    // }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);