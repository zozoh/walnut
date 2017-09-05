(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/support/hm__methods_panel',
    'ui/form/form',
    'ui/menu/menu',
    'ui/mask/mask'
], function(ZUI, Wn, HmMethods, FormUI, MenuUI, MaskUI){
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
        <span key="href"></span>
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
        'click .cnavmp-item[current] span[key="href"]' : function(e) {
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
                    data     : item.href,
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

                // 获取分栏下的区域列表
                var areaiList = UI.pageUI().getLayoutAreaList(comId);

                // 得到自己所在的分栏，看看是否是所选分栏
                var myLayouts = [];
                UI.uiCom.$el.parents(".hm-area").each(function(){
                    var jMyArea = $(this);
                    myLayouts.push({
                        comId  : jMyArea.closest(".hm-layout").attr("id"),
                        areaId : jMyArea.attr("area-id")
                    });
                });

                // 看看是否选择了自己所在的布局链
                var myAreaId = null;
                for(var i=0; i<myLayouts.length; i++) {
                    var myl = myLayouts[i];
                    if(myl.comId == comId) {
                        myAreaId = myl.areaId;
                        break;
                    }
                }

                // 得到自己已经选择的区域列表
                var usedAreaMap = UI.uiCom.joinToggleAreaMap();

                // 最后得到自己应该显示的下拉列表项目
                var items = [];
                for(var ao of areaiList) {
                    // 自己所在的区域不可选
                    if(ao.areaId == myAreaId) 
                        continue;

                    // 已经使用过的区域，也无视
                    if(usedAreaMap[ao.areaId])
                        continue;

                    items.push(ao.areaId);
                }

                // 打开编辑对话框
                this.openEditLinkPanel({
                    title    : "i18n:hmaker.com.navmenu.toar_edit_title",
                    tip      : "i18n:hmaker.com.navmenu.toar_edit_tip",
                    items    : items,
                    icon     : '<i class="zmdi zmdi-view-dashboard"></i>',
                    text     : function(o){return o;},
                    value    : function(o){return o;},
                    emptyItem : {
                        text  : "i18n:hmaker.com.navmenu.toar_area_none",
                        value : ""
                    },
                    data     : item.href,
                    callback : function(href){
                        UI.uiCom.updateItemField(index, "href", href);
                        // 最后触发一下控件重绘
                        UI.uiCom.notifyDataChange("panel");
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
                        return UI.msg("hmaker.com."+ag.ctype+".name")+"#"+ag.cid;
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
                jItem.find('[key="text"]').text(item.text);
                jItem.children('[key="href"]')
                    .text(item.href || this.msg("hmaker.com.navmenu.nohref"))
                    .attr("nohref", item.href ? null : "yes");

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