(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
    'ui/menu/menu',
    'ui/pop/pop'
], function(ZUI, Wn, HmMethods, FormUI, MenuUI, POP){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="item" class="cnavmp-item">
        <span class="info">
            <span class="bullet">
                <span for="link" balloon="right:hmaker.com.navmenu.newtab"><i class="zmdi zmdi-open-in-new"></i></span>
                <span for="toggleArea"><i class="zmdi zmdi-label-alt"></i></span>
            </span>
            <span key="text"></span>
        </span>
        <span key="link" class="hm-link-box">
            <span class="cel-icon"></span>
            <span class="cel-href"></span>
        </span>
        <span key="skin" class="hm-skin-box" box-enabled="yes"></span>
    </div>
    <div code-id="empty" class="cnavmp-empty">
        <i class="fa fa-warning"></i> {{hmaker.com.navmenu.empty}}
    </div>
</div>
<div class="ui-arena hmc-navmenu-prop" ui-fitparent="yes">
    <section class="cnavmp-form" ui-gasket="form"></section>
    <section class="cnavmp-actions" ui-gasket="actions"></section>
    <section class="cnavmp-item-list"></section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_navmenu_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        HmMethods(this);
    },
    //...............................................................
    events : {
        // 切换当前项目
        'click .cnavmp-item' : function(e) {
            var UI = this;
            var jItem = $(e.currentTarget);
            var index = jItem.prevAll().length;

            UI.uiCom.selectItem(index);
        },
        // 取消当前项目的高亮
        'click .cnavmp-item-list, .cnavmp-actions' : function(e){
            var UI = this;
            var jq = $(e.target);

            // 点在有效区域不能取消
            if(jq.closest(".menu").length > 0 
                || jq.closest(".cnavmp-item").length > 0) {
                return;
            }
            // 取消高亮
            UI.uiCom.selectItem(false);
        },
        // 编辑文字
        'click .cnavmp-item[current] span[key="text"]' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var jItem = jq.closest(".cnavmp-item");
            var index = jItem.prevAll().length;
            var item  = UI.uiCom.getItemData(index);

            $z.editIt(jq, {
                text  : item.text,
                after : function(newval, oldval) {
                    if(newval && newval!=oldval) {
                        UI.uiCom.updateItemField(index, "text", newval);
                    }
                }
            });
        },
        // 编辑链接
        'click .cnavmp-item[current] span[key="link"]' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var jItem = jq.closest(".cnavmp-item");
            var index = jItem.prevAll().length;
            var item  = UI.uiCom.getItemData(index);
            
            // 得到控件的显示类型
            var atype = jq.closest(".cnavmp-item-list").attr("atype");

            // 打开链接对话框
            if('link' == atype) {
                this.openEditLinkPanel({
                    href     : item.href,
                    callback : function(href){
                        UI.uiCom.updateItemField(index, "href", href);
                    }
                });
            }
            // 打开对应区域对话框
            else if('toggleArea' == atype) {
                // 得到当前的分栏
                var comId = UI.gasket.form.getData("layoutComId");
                
                // 如果没有分栏，则显示警告
                if(!comId){
                    UI.alert("hmaker.com.navmenu.toar_no_bar");
                    return;
                }

                this.openPickAreaPanel({
                    comId  : comId, 
                    uiCom  : UI.uiCom, 
                    areaId : item.href,
                    callback : function(areaId) {
                        UI.uiCom.updateItemField(index, "href", areaId);
                    }
                });
            } 
            // 不支持的类型
            else {
                UI.alert("Unsupport atype: " + atype);
            }
        },
        // 是否打开新窗口
        'click .cnavmp-item span.bullet span[for="link"]' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var jItem = jq.closest(".cnavmp-item");
            var index = jItem.prevAll().length;
            var item  = UI.uiCom.getItemData(index);

            // 没必要冒泡了，因为任何非激活的项目都可以点击这个
            // 点了这个，会导致本空间的所有 dom 被重刷的
            e.stopPropagation();

            UI.uiCom.updateItemField(index, "newtab", !item.newtab);
        },
        // 显示可选皮肤
        'click .cnavmp-item > [key="skin"] > .com-skin' : function(e) {
            e.stopPropagation();
            var UI    = this;
            var jBox  = $(e.currentTarget);
            var jItem = jBox.closest(".cnavmp-item");
            var index = jItem.attr("index") * 1;
            var item  = UI.uiCom.getItemData(index);
            
            // 得到对应皮肤列表
            var skinList = this.getSkinListForMenuItem();
            
            // 显示列表
            this.showSkinList(jBox, skinList, function(skin){
                UI.uiCom.updateItemField(index, "skin", skin);
            });
        },
        // 阻止 cssSelector 的弹出体冒泡事件，否则就会导致点弹出体切换高亮区域
        // 这个比较超出预期的现象
        "click .hm-skin-box > .page-css > div" : function(e) {
            e.stopPropagation();
        },
    },
    //...............................................................
    currentItem : function() {
        return this.arena.find(".cnavmp-item[current]");
    },
    //...............................................................
    checkCurrentItemIndex : function(){
        var UI = this;

        var jItem = UI.currentItem();
        if(jItem.length == 0) {
            UI.alert(UI.msg('hmaker.com.navmenu.nocurrent'));
            return -1;
        }

        return jItem.prevAll().length;
    },
    //...............................................................
    redraw : function() {
        var UI = this;

         // 通用样式设定
        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "all",
            fitparent : false,
            on_change : function(key, val) {
                // 如果是改变了类型
                if("atype" == key) {
                    // 重新刷新一下分栏列表
                    if("toggleArea" == val){
                        this.disableField("autoCurrent");
                        this.enableField("layoutComId");
                        UI.__reload_toggleAreaItems();
                    }
                    // 否则禁止分栏列表
                    else {
                        this.enableField("autoCurrent");
                        this.disableField("layoutComId");
                    }
                }
                // 最后通知修改
                var com = UI.uiCom.saveData("panel", $z.obj(key, val), true);

                // 更新菜单条项目编辑模式
                UI._update_menu_items(com);

                // 最后清除一下
                if("layoutComId" == key)
                    UI.pageUI().cleanToggleArea();

            },
            parseData : function(com){
                // 根据菜单的链接类型，来处理下拉列表的启用状态
                if("toggleArea" == com.atype){
                    this.enableField("layoutComId");
                    this.disableField("autoCurrent");
                }
                // 禁用分栏选择下拉列表
                else {
                    this.disableField("layoutComId");
                    this.enableField("autoCurrent");
                }
                return com;
            },
            fields : [{
                key   : "atype",
                title : "i18n:hmaker.com.navmenu.atype.title",
                type  : "string",
                dft : "link",
                editAs : "switch",
                uiConf : {
                    items : [{
                        text : 'i18n:hmaker.com.navmenu.atype.link',
                        val  : 'link',
                    }, {
                        text : 'i18n:hmaker.com.navmenu.atype.toggleArea',
                        val  : 'toggleArea',
                    }]
                }
            }, {
                key   : "layoutComId",
                title : "i18n:hmaker.com.navmenu.atype.layoutComId",
                type  : "string",
                disabled : true,
                editAs : "droplist",
                uiConf : {
                    icon  : function(ag){
                        return UI.msg("hmaker.com."+ag.ctype+".icon");
                    },
                    text  : function(ag){
                        return ag.cid + "(" + UI.msg("hmaker.com."+ag.ctype+".name")+")";
                    },
                    value : function(ag){
                        return ag.cid;
                    },
                    items : [],
                    emptyItem : {}
                }
            }, {
                key   : "autoCurrent",
                title : "i18n:hmaker.com.navmenu.autoCurrent",
                tip   : "i18n:hmaker.com.navmenu.autoCurrent_tip",
                type  : "boolean",
                disabled : false,
                uiWidth : "auto",
                editAs : "toggle",
            }, {
                key   : "autoShowSub",
                title : "i18n:hmaker.com.navmenu.autoShowSub",
                tip   : "i18n:hmaker.com.navmenu.autoShowSub_tip",
                type  : "boolean",
                disabled : false,
                uiWidth : "auto",
                editAs : "toggle",
            }]
        }).render(function(){
            // console.log("after render: UI.parent", UI.parent);
            if(!UI.parent)
                return;
            UI.__reload_toggleAreaItems();
            UI.defer_report("form");
        });

        // 动作按钮
        new MenuUI({
            parent : UI,
            gasketName : "actions",
            tipDirection : "up",
            setup : [{
                icon : '<i class="zmdi zmdi-plus"></i>',
                text : 'i18n:hmaker.com.navmenu.add',
                handler : function(){
                    UI.uiCom.createItem();
                }
            }, {
                icon : '<i class="zmdi zmdi-delete"></i>',
                tip  : 'i18n:hmaker.com.navmenu.del',
                handler : function(){
                    var index = UI.checkCurrentItemIndex();
                    if(index >= 0)
                        UI.uiCom.deleteItem(index);
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-return"></i>',
                tip  : 'i18n:hmaker.com.navmenu.mv_parent',
                handler : function(){
                    var index = UI.checkCurrentItemIndex();
                    if(index >= 0)
                        UI.uiCom.moveParent(index);
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-tab"></i>',
                tip  : 'i18n:hmaker.com.navmenu.mv_sub',
                handler : function(){
                    var index = UI.checkCurrentItemIndex();
                    if(index >= 0)
                        UI.uiCom.moveSub(index);
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-up"></i>',
                tip  : 'i18n:hmaker.com.navmenu.mv_prev',
                handler : function(){
                    var index = UI.checkCurrentItemIndex();
                    if(index >= 0)
                        UI.uiCom.movePrev(index);
                }
            }, {
                icon : '<i class="zmdi zmdi-long-arrow-down"></i>',
                tip  : 'i18n:hmaker.com.navmenu.mv_next',
                handler : function(){
                    var index = UI.checkCurrentItemIndex();
                    if(index >= 0)
                        UI.uiCom.moveNext(index);
                }
            }, {
                icon : '<i class="zmdi zmdi-edit"></i>',
                tip  : 'i18n:hmaker.com.navmenu.quickedit',
                handler : function(){
                    UI.openQuickEditPanel();
                }
            }]
        }).render(function(){
            UI.defer_report("actions");
        });

        // 返回延迟加载
        return ["form", "actions"];
    },
    //...............................................................
    __reload_toggleAreaItems : function(){
        // console.log("__reload_toggleAreaItems")
        var UI   = this;
        var items = UI.pageUI().getLayoutList();

        var cLayout = UI.gasket.form.getFormCtrl("layoutComId");
        var clData  = cLayout.getData();
        cLayout.setItems(items);
        cLayout.setData(clData);
    },
    //...............................................................
    openQuickEditPanel : function(){
        var UI = this;

        // 得到当前菜单项目的编辑字符串
        var items = UI.uiCom.getMenuItems();
        // console.log(items);
        // console.log(str);
        // console.log();

        // 打开编辑界面
        POP.openEditTextPanel({
            title    : "i18n:hmaker.com.navmenu.quickedit",
            data     : UI.__menu_items_to_str(items),
            callback : function(str){
                //console.log(str)
                var items2 = UI.__str_to_menu_items(str);
                //console.log(items2)
                UI.uiCom.setMenuItems(items2);
            }
        }, UI);
    },
    //...............................................................
    __menu_items_to_str : function(items) {
        var str = "";
        for(var i=0; i<items.length; i++) {
            var it = items[i];
            // 缩进
            str += $z.dupString("  ", it.depth||0);
            // 当前项
            if(it.current)
                str += "*";
            // 新窗口
            if(it.newtab)
                str += "+";
            // 文字
            str += it.text;
            // 链接
            if(it.href)
                str += " : " + it.href;
            // 换行
            str += "\n";
        }
        return str;
    },
    //...............................................................
    /*
    菜单项对象格式为：
    {
        "index"     : 0,
        "text"      : "AAA",
        "href"      : "/index",
        "newtab"    : false,
        "current"   : false,
        "skin"      : "xxx",
        "selectors" : "yyy",
        "depth"     : 0
    }
    转换成一行字符串，格式为
    
    AAA(xxx).yyy : /index
      *+BBB 
    
    - 前面的空格表示缩进，一个缩进用2个空格表示
    - `+` 字符表示新窗口打开
    - `*` 表示当前项
    - (xxx) 表示 skin，如果没有 skin 则为空
    - .yyy 表示选择器，如果没有则为空
    - `:` 后面的为链接，如果没有，则为空
    */
    __str_to_menu_items : function(str) {
        var lines = str.split(/\r?\n/g);
        var items = [];
        for(var i=0; i<lines.length; i++) {
            var line = lines[i];
            var trim = $.trim(line);
            // 无视空行
            if(!trim)
                continue;
            // 准备解析
            var it = {
                index : i,
                depth : $z.countStrHeadIndent(line, 2)
            };
            // 来吧
            var m = /^[ \t]*([*]?)([+]?)([^:]+):?(.*)$/.exec(trim);
            // 靠，格式错误
            if(!m)
                continue;
            // 来吧
            it.text = $.trim(m[3]);  // 文字
            it.href = $.trim(m[4]) || ""; // 链接
            it.newtab = m[2] ? true : false; // 新窗口
            it.current = m[1] ? true : false; // 当前项
            // 记入结果
            items.push(it);
        }
        return items;
    },
    //...............................................................
    update : function(com) {
        //console.log("prop", com);
        var UI = this;

        // 更新菜单条项目编辑模式
        var jList = UI._update_menu_items(com);

        // 让高亮的滚屏出来
        var jCurrentItem = jList.find('[current]');
        if(jCurrentItem.length > 0) {
            var rectList = $D.rect.gen(jList);
            var rectItem = $D.rect.gen(jCurrentItem);
            // console.log($z.pick(rectList,"top,bottom"), $z.pick(rectItem, "top,height"))
            if(rectItem.bottom >= rectList.bottom || rectItem.top <= rectList.top){
                jCurrentItem[0].scrollIntoView(false);
            }
        }

        // 更新 form
        UI.gasket.form.setData(com);

        // 打开提示
        UI.balloon();

        // 最后在调用一遍 resize
        UI.resize(true);
    },
    //...............................................................
    // 根据 com.atype 来表示菜单列表的显示模式
    _update_menu_items : function(com) {
        var UI = this;

        // 修改模式
        UI.arena.find('.cnavmp-item-list').attr({
            "atype" : com.atype || "link"
        });

        // 清空列表
        var jList = UI.arena.find('.cnavmp-item-list').empty();

        // 首先得到所有的菜单项
        var items = UI.uiCom.getMenuItems();
        
        // 显示空
        if(items.length == 0) {
            jList.append(UI.ccode("empty"));
        }
        // 显示列表
        else {
            // 如果指定了一个区域，那么到对应的布局看看，归纳一个 Map
            var areaList = UI.pageUI().getLayoutAreaList(com.layoutComId);
            var areaMap = {};
            for(var ao of areaList)
                areaMap[ao.areaId] = ao;

            // 循环绘制项目
            for(var i=0; i<items.length; i++) {
                var item  = items[i];
                var jItem = UI.ccode("item").appendTo(jList);

                // 校验项目数据
                // if(item.toarId && !areaMap[item.toarId]){
                //     item.toarId = null;
                //     item.toarChecked = false;
                //     UI.uiCom.updateItem(i, item, true);
                // }

                // 绘制项目
                jItem.attr({
                    "index"   : item.index,
                    "current" : item.current ? "yes" : null,
                    "newtab"  : item.newtab ? "yes"  : null,
                });

                // 显示文字项目
                jItem.find('[key="text"]').text(item.text);

                // 显示链接
                var jLink = jItem.children('[key="link"]');
                UI.setLinkToBox(jLink, item.href);

                // 显示皮肤
                var jBox = jItem.children('[key="skin"]');
                UI.updateSkinBox(jBox, {
                    skin : item.skin,
                    getSkinText : function(skin){
                        return this.getSkinTextForMenuItem(skin);
                    },
                    cssSelectors : item.selectors, 
                    setSelectors : function(selectors) {
                        var index = this.closest(".cnavmp-item").attr("index") * 1;
                        UI.uiCom.updateItemField(index, "selectors", selectors);
                    }
                });

                // 缩进
                if(item.depth > 0) {
                    jItem.attr("sub", "yes").css("margin-left", item.depth * 32);
                }
            }
        }

        // 返回
        return jList;
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jForm    = UI.arena.find(">.cnavmp-form");
        var jActions = UI.arena.find(">.cnavmp-actions");
        var jList    = UI.arena.find(">.cnavmp-item-list");

        // 规定了菜单项的高度
        var H  = UI.arena.height();
        var h0 = jForm.outerHeight(true);
        var h1 = jActions.outerHeight(true);
        jList.css("height", H - h0 - h1);

        // 首先释放所有项目原先设置的宽度
        jList.find('span[key="text"], span[key="href"]')
            .css("width", "");

        // 确保每个项目的链接不超过
        var jItems = jList.find('>.cnavmp-item');
        jItems.each(function(){
            var jItem = $(this);
            var W     = jItem.width();
            jItem.children('[key="text"], [key="href"]').each(function(){
                var jSpan = $(this);
                var my_w  = jSpan.outerWidth();
                if(my_w >= W) {
                    jSpan.css("width", W);
                }
            });
        });
    }
});
//===================================================================
});
})(window.NutzUtil);