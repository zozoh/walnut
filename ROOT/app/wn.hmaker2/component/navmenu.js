(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = `
<div class="ui-arena hmc-navmenu"><ul class="mic-top"></ul></div>
`;
//==============================================
return ZUI.def("app.wn.hm_com_navmenu", {
    keepDom   : true,
    dom       : html,
    className : '!hm-com-navmenu',
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 高亮项目
        'click ul>li>a' : function(e) {
            var jA  = $(e.currentTarget);
            var jLi = jA.closest("li");

            // 确保禁止默认行为
            e.preventDefault();

            // 仅针对激活控件有效
            if(!this.isActived())
                return;

            // 本控件激活，那么就不要向上冒泡了，自己处理
            e.stopPropagation();

            // 激活当前项目
            this.selectItem(jLi);

            // 如果当前模式是区域选择，还需要同时高亮当前区域
            this.fire("show:prop", "com");

        },
        // 取消高亮
        'click .hmc-navmenu' : function(e) {
            // 仅针对激活控件有效
            if(!this.isActived())
                return;

            if($(e.target).closest('a').length == 0){
                this.unselectItem();
            }
        },
        // 编辑文字
        'click ul>li[current]>a' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            
            // 仅针对激活控件有效
            if(!this.isActived())
                return;

            var jItem = jq.closest("li");
            var index = jItem.attr("index") * 1;
            var item  = UI.getItemData(index);

            $z.editIt(jq, {
                text  : item.text,
                after : function(newval, oldval) {
                    if(newval && newval!=oldval) {
                        UI.updateItemField(index, "text", newval);
                    }
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
    $item : function(arg, quiet) {
        if(_.isNumber(arg))
            return this.arena.find("li").eq(arg);
        
        if($z.isjQuery(arg) || _.isElement(arg))
            return $(arg);
        
        if(quiet)
            return null;

        throw "navmenu $item Don't known how to found item by : " + arg;
    },
    //...............................................................
    getActivedItem : function(alwaysReturnjQuery) {
        var UI = this;
        var jItem = UI.arena.find('li[current]');
        if(alwaysReturnjQuery || jItem.length > 0)
            return jItem;
    },
    //...............................................................
    getActivedItemIndex : function() {
        for(var i=0; i<jItems.length; i++){
            if(jItems.eq(i).attr("current"))
                return i;
        }
        return this.getActivedItem(true).attr("index") * 1;
    },
    //...............................................................
    getItemIndex : function(index) {
        return this.$item(index).attr("index") * 1;
    },
    //...............................................................
    getItemData : function(index) {
        var jLi = this.$item(index);
        // 把 ICON 变成文字形式
        var icon = jLi.find(">a>i").prop("className");
        var text = jLi.find(">a>span").text();
        var m = /^(fa|zmdi) +((fa|zmdi)-.+)$/.exec(icon);
        if(m) {
            text = '<' + m[2] + '>' + text;
        }
        // 返回对象
        return {
            index     : jLi.attr("index") * 1,
            text      : text,
            href      : jLi.attr("href") || "",
            newtab    : jLi.attr("newtab") == "yes",
            current   : jLi.attr("current") == "yes",
            skin      : jLi.attr("skin") || "",
            selectors : jLi.attr("selectors") || "",
            depth     : jLi.parentsUntil(".hmc-navmenu", "ul").length - 1
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
            jLi.appendTo(jUl);
        }

        UI.updateItem(jLi, item, true);

        // 选中新增项目(顺便通知修改)
        UI.selectItem(jLi);

        return item;
    },
    //...............................................................
    selectItem : function(index) {
        var UI  = this;
        var jLi = UI.$item(index, true);

        if(jLi){
            // 已经激活了 ...
            if(jLi.attr("current")){
                return;
            }
            // 确保 jLi 有效
            if(jLi.length == 0)
                jLi = null;
        }

        // 移除其他的激活
        UI.arena.find('li[current]').removeAttr("current");

        // 标识自己的激活
        if(jLi)
            jLi.attr("current", "yes");

        // 确保自己顶级的 UL 和子 UL 的属性
        UI.__mark_hierarchy_class();

        // 标识自己以及自己的祖先 LI 都是 open-sub
        UI.__mark_ancestor_and_self_open(jLi);

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
        var old = UI.getItemData(jLi);

        // 格式化文字图标
        var m = /^<((fa|zmdi)-([a-z-]+))>(.*)$/.exec(item.text);
        console.log(m, item)
        if(m){
            item.icon = m[1];
            item.text = m[4];
        }
        if(!item.icon) {
            jLi.addClass("icon-hide").removeClass("icon-show")
                .find(">a>i").prop("className", "");
        }else{
            jLi.addClass("icon-show").removeClass("icon-hide")
                .find(">a>i").prop("className", m[2] + " " + item.icon);
        }
        // 更新文字
        if(item.text) {
            jLi.addClass("text-show").removeClass("text-hide")
                .find(">a>span").text(item.text);
        } else {
            jLi.addClass("text-hide").removeClass("text-show")
                .find(">a>span").text("");
        }

        // 更新属性
        jLi.attr({
            "newtab" : !item.newtab ? null : "yes",
            "href"   : item.href || null,
            "skin"   : item.skin || null,
            "selectors" : item.selectors || null,
        });
        jLi.children("a").attr("href", item.href || null);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 更新皮肤和选择器
        jLi.removeClass(old.skin)
           .removeClass(old.selectors)
              .addClass(item.skin)
              .addClass(item.selectors);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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
        var jMyParentUl = jLi.parent();

        // 如果删除的是高亮东东，试图高亮下一个
        var jNext;
        if(jLi.attr("current")) {
            jNext = jLi.next();
        }
        if(jNext && jNext.length == 0) {
            jNext = jLi.prev();
        }
        if(jNext && jNext.length == 0) {
            jNext = jMyParentUl.closest("li");
        }

        // 删除
        jLi.remove();

        //console.log(jMyParentUl.children().length, jMyParentUl.is(':empty'));
        // 删掉空 UL
        if(jMyParentUl.is(':empty') && !jMyParentUl.hasClass("ul-top"))
            jMyParentUl.remove();

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

        // 修改 DOM
        jLi.insertBefore(jTa);

        // 重新标定层级的类
        UI.__mark_hierarchy_class();

        // 通知
        UI.notifyDataChange("page");
    },
    //...............................................................
    moveNext : function(index) {
        var UI = this;
        var jLi = UI.$item(index);

        // 后面没了，就试图升级
        var jTa = jLi.next();
        if(jTa.length == 0){
            UI.moveParent(jLi);
            return;
        }

        // 修改 DOM
        jLi.insertAfter(jTa);

        // 重新标定层级的类
        UI.__mark_hierarchy_class();

        // 通知
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

        // 重新标定层级的类
        UI.__mark_hierarchy_class();

        // 重新标记子菜单打开状态
        UI.__mark_ancestor_and_self_open(jLi);

        // 通知修改
        UI.notifyDataChange("page");
    },
    //...............................................................
    moveParent : function(index) {
        var UI   = this;
        var jLi  = UI.$item(index);
        var item = UI.getItemData(jLi);

        // 如果本身就是顶级菜单，则啥也不干
        if(item.depth == 0)
            return;

        // 如果自己有后续节点，移动成为自己的子
        var jNexts = jLi.nextAll();
        if(jNexts.length > 0) {
            var jMyUl = jLi.children("ul");
            if(jMyUl.length == 0) {
                jMyUl = $('<ul>').appendTo(jLi);
            }
            jMyUl.append(jNexts);
        }

        // 得到自己所在的 ul，移动完如果其没内容了就删掉
        var jMyParentUl = jLi.parent();

        // 将自己成为自己父的平级节点
        var jParentLi = jLi.parent().closest("li");
        jLi.insertAfter(jParentLi);

        // 删掉空 UL
        if(jMyParentUl.is(':empty'))
            jMyParentUl.remove();

        // 重新标定层级的类
        UI.__mark_hierarchy_class();

        // 重新标记子菜单打开状态
        UI.__mark_ancestor_and_self_open(jLi);
        
        // 通知修改
        UI.notifyDataChange("page");
    },
    //...............................................................
    isTopItem : function(index) {
        var UI   = this;
        var jLi  = UI.$item(index);
        return jLi.parent().hasClass("mic-top");
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
    applyBlockCss : function(cssCom, cssArena) {
        // 标识前景色为链接的颜色
        if(cssArena.color) {
            this.addMySkinRule("ul li a", {color:cssArena.color});
            cssArena.color = null;
        }
        // 应用
        this.$el.css(cssCom);
        this.arena.css(cssArena);
    },
    //...............................................................
    paint : function(com) {
        var UI  = this;
        var jUl = this.arena.children("ul");
        //console.log("paint", com);

        // 标识自己的类型
        UI.$el.attr("navmenu-atype", com.atype);

        // 重新设置链接
        UI.arena.find("li").each(function(){
            var jLi  = $(this);
            var href = jLi.attr("href");
            jLi.children("a").attr("href", href || null);
        });

        // 确保自己顶级的 UL 和子 UL 的属性
        UI.__mark_hierarchy_class();

        // 重新标记子菜单打开状态
        var jLi = UI.getActivedItem();
        if(jLi)
            UI.__mark_ancestor_and_self_open(jLi);
        
        // 同步区域
        UI.syncToggleArea(com);

        // 确保自己的至少有一个项目
        if(jUl.children().length == 0) {
            UI.createItem();
        }
    },
    //...............................................................
    on_actived : function(){
        // 重新标记子菜单打开状态
        var jLi = this.getActivedItem();
        if(jLi)
            this.__mark_ancestor_and_self_open(jLi);
    },
    //...............................................................
    // 根据菜单的 DOM 标记各个层级的 class
    __mark_hierarchy_class : function(){
        var jUl = this.arena.children("ul");
        // 顶级
        jUl.addClass("ul-top");
        jUl.find(">li")
            .removeClass("li-sub")
            .addClass("li-top");
        // 次级
        jUl.find(">li>ul")
            .prop("className", "ul-sub ul-sub-0");
        jUl.find(">li>ul>li")
            .prop("className", "li-sub li-sub-0");

        // 更多子
        jUl.find(">li>ul>li ul")
            .prop("className", "ul-sub ul-sub-n");
        jUl.find(">li>ul>li li")
            .prop("className", "li-sub li-sub-n");

        // 最后重新标记每个项目的 index 以及子菜单个数
        jUl.find("li").each(function(index){
            var jLi = $(this);
            var jUl = jLi.children("ul");
            jLi.attr({
                "index"       : index,
                "sub-item-nb" : jUl.length>0?jUl.children().length:null,
            });
        });
    },
    //...............................................................
    __mark_ancestor_and_self_open : function(index) {
        var UI  = this;

        // 移除其他的打开菜单
        UI.arena.find('li[open-sub]').removeAttr("open-sub");

        // 标识自己
        if(!_.isNull(index) && !_.isUndefined(index)) {
            var jLi = UI.$item(index);
            UI.__open_my_ul(jLi);
        }
    },
    //...............................................................
    // 让自己所在的 ul 以及所有祖先的 ul 都被打开（标记ul所在 li的 open-sub)
    __open_my_ul : function(jLi) {
        var UI = this;
        var autoDock = UI.arena.attr("auto-dock");
        jLi.parentsUntil(".ul-top", "li").andSelf()
            .attr("open-sub", "yes")
                .each(function(){
                    var jMe = $(this);
                    var jUl = jMe.children("ul");
                    if(autoDock) {
                        if(jMe.hasClass("li-top") && "H" == autoDock){
                            $z.dockIn(jMe, jUl, "H", true);
                        }
                        // 其他子菜单一律停靠在垂直边
                        else {
                            $z.dockIn(jMe, jUl, "V", true);
                        }
                    }
                    // 否则移除自己的和子 ul 的位置 css 属性
                    else {
                        jMe.css("position", "");
                        jUl.css({
                            "position": "",
                            "top": "",
                            "left": "",
                            "right": "",
                            "bottom": "",
                        });
                    }
                });
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
    getSkinAttributes : function(){
        return ["auto-dock"]
    }
    //...............................................................
    // formatSize : function(prop, com, fromMode) {
        
    // }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);