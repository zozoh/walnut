(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `
<div class="ui-arena hmc-navmenu"><ul></ul></div>
`;
//==============================================
return ZUI.def("app.wn.hm_com_navmenu", {
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 高亮项目
        'click ul>li' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            if(!UI.isInActivedBlock(jq))
                return;
            
            e.stopPropagation();
            this.selectItem(jq);
        },
        // 取消高亮
        'click .hmc-navmenu' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            if(!UI.isInActivedBlock(jq))
                return;

            e.stopPropagation();
            UI.unselectItem();
        },
        // 编辑文字
        'click ul>li[current] a' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            if(!UI.isInActivedBlock(jq))
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
    getActivedItemIndex : function() {
        var UI = this;
        var jItem = UI.arena.find('li[current]');
        if(jItem.length > 0)
            return jItem.prevAll().length;
        return -1;
    },
    //...............................................................
    getItemData : function(index) {
        var jLi = this.$item(index);
        return {
            text : jLi.text(),
            href : jLi.attr("href") || "",
            newtab : jLi.attr("newtab") == "yes",
            current : jLi.attr("current") == "yes",
            toarChecked : jLi.attr("toar-checked") == "yes",
            toarId : jLi.attr("toar-id")
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
        var jUl = UI.arena.find("ul");
        var jLi = $('<li><a></a></li>').appendTo(jUl);
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

        // 通知修改
        UI.notifyChange();
    },
    //...............................................................
    unselectItem : function() {
        this.arena.find('li[current]').removeAttr("current");

        // 通知修改
        this.notifyChange();  
    },
    //...............................................................
    checkToggleAreaItem : function(index) {
        var UI  = this;
        var jLi = UI.$item(index);

        // 已经激活了 ...
        if(jLi.attr("toar-checked")){
            return;
        }

        UI.arena.find('li[toar-checked]').removeAttr("toar-checked");
        jLi.attr("toar-checked", "yes");

        // 通知修改
        UI.notifyChange();
    },
    //...............................................................
    uncheckToggleAreaItem : function() {
        this.arena.find('li[toar-checked]').removeAttr("toar-checked");

        // 通知修改
        this.notifyChange();  
    },
    //...............................................................
    updateItem : function(index, item, quiet) {
        var UI  = this;
        var jLi = UI.$item(index);
        jLi.children("a").text(item.text);
        jLi.attr({
            "newtab" : !item.newtab ? null : "yes",
            "href"   : item.href || null,
            "toar-checked" : item.toarChecked ? "yes" : null,
            "toar-id"      : item.toarId || null,
        });

        if(!quiet)
            UI.notifyChange();
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
            UI.notifyChange();
        }
    },
    //...............................................................
    movePrev : function(index) {
        var UI = this;
        var jLi = UI.$item(index);

        var jTa = jLi.prev();
        if(jTa.length > 0) {
            jLi.insertBefore(jTa);
        }

        UI.notifyChange();
    },
    //...............................................................
    moveNext : function(index) {
        var UI = this;
        var jLi = UI.$item(index);

        var jTa = jLi.next();
        if(jTa.length > 0) {
            jLi.insertAfter(jTa);
        }

        UI.notifyChange();
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 设置初始值的 DOM 结构
        if(!UI.arena.hasClass("ui-arena")){
            UI.arena = $(html).appendTo(UI.$el.empty());
            UI.createItem();
        }

        // 标记要删除的属性
        UI.arena.attr("del-attrs", "current");
    },
    //...............................................................
    paint : function(com) {
        var UI  = this;
        var jUl = UI.arena.children("ul");

        // 菜单的模式
        UI.arena.attr("mode", com.mode || "default");

        // 垂直模式的对齐方式
        if("aside" == com.mode) {
            jUl.css({
                "text-align" : com.itemAlign || "left",
            });   
        }
        // 默认水平方式的对齐方式
        else {
            _flex = {
                "left"   : "flex-start",
                "center" : "center",
                "right"  : "flex-end",
            };
            jUl.css({
                "justify-content" : _flex[com.itemAlign] || "flex-start"
            });
        }

    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    setupProp : function(){
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