(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com',
    'ui/form/form',
    'ui/mask/mask'
], function(ZUI, Wn, HmComMethods, FormUI, MaskUI){
//==============================================
return ZUI.def("app.wn.hm_com_navmenu_prop", {
    dom  : `
<div class="ui-code-template">
    <div code-id="item" class="cnavmp-item">
        <span><i class="fa fa-circle-thin"></i></span>
        <span key="newtab">
            <u><i class="fa fa-check-square"></i><i class="fa fa-square-o"></i></u>
            <em>{{hmaker.com.navmenu.newtab}}</em>
        </span>
        <span key="text"></span>
        <span key="href"></span>
    </div>
    <div code-id="empty" class="cnavmp-empty">
        <i class="fa fa-warning"></i> {{hmaker.com.navmenu.empty}}
    </div>
</div>
<div class="ui-arena hmc-navmenu-prop" ui-fitparent="yes">
    <section class="cnavmp-actions">
        <ul>
            <li act="createItem"><i class="fa fa-plus"></i> {{hmaker.com._.create}}</li>
            <li act="deleteItem"><i class="zmdi zmdi-delete"></i> {{hmaker.com._.del}}</li>
            <li act="movePrev"><i class="zmdi zmdi-long-arrow-up"></i> {{hmaker.com._.move_up}}</li>
            <li act="moveNext"><i class="zmdi zmdi-long-arrow-down"></i> {{hmaker.com._.move_down}}</li>
        </ul>
    </section>
    <section class="cnavmp-item-list"></section>
    <section class="cnavmp-form" ui-gasket="form"></section>
</div>`,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 各种动作
        'click .cnavmp-actions li' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var actionName = jq.attr("act");

            // 创建的话什么参数都不需要
            if("createItem" == actionName) {
                $z.invoke(UI.uiCom, actionName);
            }
            // 其他的需要一个当前高亮项
            else {
                var jItem = UI.currentItem();
                if(jItem.length == 0) {
                    alert(UI.msg('hmaker.com.navmenu.nocurrent'));
                    return;
                }
                var index = jItem.prevAll().length;
                $z.invoke(UI.uiCom, actionName, "createItem"==actionName?[]:[index]);
            }
        },
        // 切换当前项目
        'click .cnavmp-item' : function(e) {
            var UI = this;
            var jItem = $(e.currentTarget);
            var index = jItem.prevAll().length;

            UI.uiCom.selectItem(index);
        },
        // 编辑文字
        'click .cnavmp-item[current] span[key="text"]' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var jItem = jq.closest(".cnavmp-item");
            var index = jItem.prevAll().length;

            $z.editIt(jq, function(newval, oldval) {
                if(newval && newval!=oldval) {
                    UI.uiCom.updateItemField(index, "text", newval);
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
            var oHome = UI.getHomeObj();

            // 弹出遮罩层
            new MaskUI({
                app : UI.app,
                dom : 'ui/pop/pop.html',
                css : 'ui/pop/pop.css',
                events : {
                    "click .pm-btn-ok" : function(){
                        var href = this.body.getData();
                        UI.uiCom.updateItemField(index, "href", href);
                        this.close();
                    },
                    "click .pm-btn-cancel" : function(){
                        this.close();
                    }
                }, 
                setup : {
                    uiType : 'ui/support/edit_link',
                    uiConf : {
                        baseObj   : oHome
                    }
                }
            }).render(function(){
                this.body.setData(item.href);
            });
        },
        // 是否打开新窗口
        'click .cnavmp-item span[key="newtab"]' : function(e) {
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
    },
    //...............................................................
    currentItem : function() {
        return this.arena.find(".cnavmp-item[current]");
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        new FormUI({
            parent : UI,
            gasketName : "form",
            uiWidth : "all",
            on_change : function(key, val) {
                UI.fire("change:com", $z.obj(key, val));
            },
            fields : [{
                key   : "mode",
                title : "i18n:hmaker.com.navmenu.mode",
                type  : "string",
                editAs : "switch", 
                uiConf : {
                    items : [{
                        text : 'i18n:hmaker.com.navmenu.mode_default',
                        val  : 'default',
                    }, {
                        text : 'i18n:hmaker.com.navmenu.mode_aside',
                        val  : 'aside',
                    }]
                }
            }, {
                key   : "itemAlign",
                title : "i18n:hmaker.com.navmenu.itemAlign",
                type  : "string",
                editAs : "switch", 
                uiConf : {
                    items : [{
                        icon : '<i class="fa fa-align-left">',
                        val  : 'left',
                    }, {
                        icon : '<i class="fa fa-align-center">',
                        val  : 'center',
                    }, {
                        icon : '<i class="fa fa-align-right">',
                        val  : 'right',
                    }]
                }
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        return ["form"];
    },
    //...............................................................
    update : function(com) {
        var UI = this;
        var jList = UI.arena.find('.cnavmp-item-list').empty();

        // 首先得到所有的菜单项
        var items = UI.uiCom.getMenuItems();
        
        // 显示空
        if(items.length == 0) {
            jList.append(UI.ccode("empty"));
        }
        // 显示列表
        else {
            for(var item of items) {
                var jItem = UI.ccode("item").appendTo(jList);
                UI._update_item(jItem, item);
            }
        }

        // 让高亮的滚屏出来
        var jCurrentItem = jList.find('[current]');
        if(jCurrentItem.length > 0) {
            var rectList = $z.rect(jList);
            var rectItem = $z.rect(jCurrentItem);
            // console.log($z.rectObj(rectList,"top,bottom"), $z.rectObj(rectItem, "top,height"))
            if(rectItem.bottom >= rectList.bottom || rectItem.top <= rectList.top){
                jCurrentItem[0].scrollIntoView();
            }
        }

        // 更新 form
        UI.gasket.form.update(com);

        // 最后在调用一遍 resize
        UI.resize(true);
    },
    //...............................................................
    _update_item : function(jItem, item) {
        jItem.attr("current", item.current ? "yes" : null);
        jItem.children('[key="text"]').text(item.text);
        jItem.children('[key="href"]')
            .text(item.href || this.msg("hmaker.com.navmenu.nohref"))
            .attr("nohref", item.href ? null : "yes");
        jItem.children('[key="newtab"]').attr("open-newtab", item.newtab ? "yes" : null);
        return jItem;
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jS0 = UI.arena.find(".cnavmp-item-list");
        var jS1 = UI.arena.find(".cnavmp-actions");
        var jForm   = UI.arena.find(".cnavmp-form");

        var H = UI.arena.height();
        jForm.css({
            "height" : H - jS0.outerHeight(true) - jS1.outerHeight(true)
        });
    }
});
//===================================================================
});
})(window.NutzUtil);