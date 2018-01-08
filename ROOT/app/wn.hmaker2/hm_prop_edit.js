(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/menu/menu',
    'ui/mask/mask',
    'app/wn.hmaker2/support/hm__methods_panel',
    'app/wn.hmaker2/hm_prop_edit_block',
    'app/wn.hmaker2/hm_prop_edit_com',
], function(ZUI, Wn, MenuUI, MaskUI,
    HmMethods,
    EditBlockUI,
    EditComUI){
//==============================================
var html = `
<div class="ui-arena hm-prop-edit" ui-fitparent="yes">
    <div class="hm-prop-head">
        <div class="hm-com-info"></div>
        <div class="hm-com-lib" ui-gasket="libmenu"></div>
    </div>
    <div class="hm-prop-tabs">
        <ul class="hm-W">
            <li ptype="block"><%=hmaker.prop.tab_block%></li>
            <li ptype="com"><%=hmaker.prop.tab_com%></li>
        </ul>
    </div>
    <div class="hm-prop-body">
        <div class="hm-W">
            <div class="hm-prop-con" ptype="block" ui-gasket="block"></div>
            <div class="hm-prop-con" ptype="com"   ui-gasket="com"></div>
        </div>
    </div>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_prop_edit", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);

        UI.listenBus("active:page",   UI.doActiveOther);
        UI.listenBus("active:folder", UI.doActiveOther);
        UI.listenBus("active:rs",     UI.doActiveOther);
        UI.listenBus("active:other",  UI.doActiveOther);
        UI.listenBus("active:com",    UI.doActiveCom);
        
        UI.listenBus("change:block",  UI.doChangeBlock);
        UI.listenBus("change:com",    UI.doChangeCom);

        UI.listenBus("show:prop",    UI.switchTab);
    },
    //...............................................................
    events : {
        // 切换标签
        'click .hm-prop-tabs li[ptype]' : function(e) {
            this.switchTab($(e.currentTarget).attr("ptype"));
        },
        // 修改 comID
        "click .hm-prop-head .hm-com-info em" : function(e){
            var UI = this;
            var jq = $(e.currentTarget);

            // 组件内控件 ID 不可编辑
            if(jq.attr("in-lib"))
                return;

            // 进入编辑模式
            $z.editIt(jq, function(newval, oldval, jEle){
                var comNewId = $.trim(newval);
                if(comNewId != oldval) {
                    //console.log("change com ID", comNewId);
                    // 修改接口
                    if(UI.uiCom.setComId(comNewId)){
                        // 通知更新
                        UI.uiCom.notifyActived();
                        // 修改显示
                        jEle.text(comNewId);
                    }
                }
            });
        },
        // 显示皮肤选择器
        "click .hm-skin-box > .com-skin" : function(e) {
            e.stopPropagation();
            var UI    = this;
            var jSpan = $(e.currentTarget);

            // 得到可用皮肤列表
            var ctype = UI.uiCom.getComType();
            var skinList  = UI.getSkinListForCom(ctype);

            UI.showSkinList(jSpan, skinList, function(skin){
                // 保存皮肤信息
                UI.uiCom.setComSkin(skin);

                // 重新激活组件
                UI.uiCom.notifyActived();

                // 通知相关改动
                UI.uiCom.notifyBlockChange(null);
                UI.uiCom.notifyDataChange(null);
            });
        },
        // 显示已经加载的 css 类选择器
        "click .hm-skin-box > .page-css" : function(e) {
            var UI = this;
            UI.showCssSelectorList(e.currentTarget);
        },
        // 隐藏已经加载的 css 类选择器
        "mouseleave .hm-skin-box" : function(e) {
            this.hideCssSelectorList(e.currentTarget);
        },
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 确保有一个标签被选中
        UI.switchTab();

        // 块元素的属性编辑器
        new EditBlockUI({
            parent : UI,
            gasketName : "block"
        }).render(function(){
            UI.defer_report("block");
        });

        // 控件的属性编辑器
        new EditComUI({
            parent : UI,
            gasketName : "com"
        }).render(function(){
            UI.defer_report("com");
        });

        // 返回延迟加载
        return ["block", "com"];
    },
    //...............................................................
    switchTab : function(ptype) {
        var UI = this;
        ptype = ptype || UI.local("hm_prop_edit_tab") || "block";

        // 记录状态
        UI.local("hm_prop_edit_tab", ptype);

        UI.arena.find('.hm-prop-tabs li').removeAttr("current")
            .filter('[ptype="'+ptype+'"]').attr("current", "yes");

        UI.arena.find('.hm-prop-con').removeAttr("current")
            .filter('[ptype="'+ptype+'"]').attr("current", "yes");

        UI.resize(true);
    },
    //...............................................................
    doCreateLibItem : function(){
        var UI    = this;
        var oHome = UI.getHomeObj();

        // 显示一个遮罩层，
        new MaskUI({
            width  : 500,
            height : 200,
            mainClass : "hm-prop-lib-create",
            i18n : UI._msg_map,
            events : {
                // 创建组件
                "click footer b[enable]" : function(){
                    var uiMask = this;
                    var jMain  = this.$main;

                    // 得到组件名称
                    var libName = $.trim(jMain.find("input").val());
                    //console.log("create:", libName)

                    // 修改显示状态
                    jMain.attr("ing", "yes")
                        .find("input").prop("readonly", true);

                    // 得到组件的内容
                    var comId = UI.uiCom.getComId();
                    var html  = UI.pageUI().getHtml(comId, true);
                    //console.log(html)

                    // 执行创建
                    Wn.execf("hmaker id:{{id}} lib -write '{{libName}}'", html, {
                        id      : oHome.id,
                        libName : libName,
                    }, function(re){
                        //console.log("re:", re);
                        // 没有返回就是正常
                        if(re) {
                            UI.alert(re);
                            return;
                        }
                        // 设置控件的 libName
                        UI.uiCom.setComLibName(libName);
                        // 通知更新
                        UI.uiCom.notifyActived(null);
                        // 关闭
                        uiMask.close();
                        // 刷新资源树
                        UI.resourceUI().refresh();
                    });                      
                },
                // 输入组件名称
                "change input" : function(e) {
                    var jMain = this.$main;
                    var jTip  = jMain.find("footer .tip");

                    // 得到组件名称
                    var libName = $.trim($(e.currentTarget).val());

                    // 确保有输入名称
                    if(!libName) {
                        jMain.find("footer .tip")
                            .attr("mode", "warn")
                            .html(UI.msg("hmaker.lib.e_nm_blank"));
                        jMain.find("footer b").attr("enable", null);
                        return;
                    }

                    // 检查结果
                    jTip.html(UI.msg("hmaker.lib.nm_checking"));
                    Wn.execf("hmaker id:{{id}} lib -get '{{libName}}'", {
                        id      : oHome.id,
                        libName : libName,
                    }, function(re){
                        //console.log(re)
                        // 不存在的话则可以创建
                        if(/^e./.test(re)) {
                            jTip.attr("mode", "ok")
                                .html(UI.msg("hmaker.lib.nm_valid"));
                            jMain.find("footer b").attr("enable", "yes");
                        }
                        // 否则不能创建
                        else {
                            jTip.attr("mode", "warn")
                                .html(UI.msg("hmaker.lib.e_nm_exists"));
                            jMain.find("footer b").attr("enable", null);
                        }
                    });
                }
            }
        }).render(function(){
            var uiMask = this;
            uiMask.$main.html($z.compactHTML(`
                <header>{{hmaker.lib.create_tip}}</header>
                <section><input></section>
                <footer>
                    <div class="tip"></div>
                    <div class="ado">
                        <span class="ing">
                            <i class="fa fa-spinner fa-spin"></i>
                        </span>
                        <b>{{hmaker.lib.create}}</b>
                    </div>
                </footer>
            `, UI._msg_map));
            uiMask.$main.find("input").focus();
        });
    },
    //...............................................................
    doReloadLibItem : function(){
        var UI = this;

        // 找到最近的一个组件
        var jLibCom  = UI.uiCom.$el.closest(".hm-com[lib]");
        var uiLibCom = ZUI(jLibCom);

        // 重新加载
        uiLibCom.showLoading();
        UI.pageUI().reloadLibCode(uiLibCom.$el, function(uiCom){
            uiCom.notifyActived(null);
            UI.pageUI().invokeSkin("ready");
            UI.pageUI().invokeSkin("resize");
        });
    },
    //...............................................................
    doDetachLibItem : function(){
        var UI = this;
        UI.confirm("hmaker.lib.detach_tip", function(){
            // 找到最近的一个组件
            var jLibCom  = UI.uiCom.$el.closest(".hm-com[lib]");
            var uiLibCom = ZUI(jLibCom);

            // 重新加载
            uiLibCom.showLoading();
            UI.pageUI().reloadLibCode(uiLibCom.$el, function(uiCom){
                uiCom.setComLibName(null);
                uiCom.notifyActived(null);
                UI.pageUI().invokeSkin("ready");
                UI.pageUI().invokeSkin("resize");
            });
        });
    },
    //...............................................................
    doActiveOther : function(){
        //console.log("hm_prop_edit->doActiveOther:");
        // this.gasket.com.showBlank();
    },
    //...............................................................
    doActiveCom : function(uiCom) {
        var UI = this;

        // 保存实例
        UI.uiCom = uiCom;

        // 得到组件信息
        var comId  = uiCom.getComId();
        var ctype  = uiCom.getComType();
        var libNm  = uiCom.getComLibName();
        var skin   = uiCom.getComSkin();
        var comTxt = uiCom.getComDisplayText();

        // 准备信息显示的 HTML
        // var html = '<span>' + UI.msg("hmaker.com."+ctype+".icon") + '</span>';
        // html += '<b>' + comTxt + '</b>';
        // html += '<em>' + comId + '</em>';
        // var html = '<span class="com-skin">';
        // html += UI.msg("hmaker.com."+ctype+".icon");
        // html += '<b>' + comTxt + '</b>';
        // html += '</span>';
        // html += '<em>' + comId + '</em>';
        var jInfo  = UI.arena.find('>.hm-prop-head>.hm-com-info').empty();;
        var jSkBox = $('<span class="hm-skin-box" box-enabled="yes">').appendTo(jInfo);
        var jEm = $('<em>' + comId + '</em>').appendTo(jInfo);
        UI.updateSkinBox(jSkBox, {
            noneSkinText : UI.get_com_display_text(ctype, comId),
            skin : skin,
            getSkinIcon : function(skin) {
                return UI.msg("hmaker.com."+ctype+".icon");
            },
            getSkinText : function(skin){
                return UI.get_com_display_text(ctype, comId, skin);
            },
            cssSelectors : UI.uiCom.getComSelectors(),
            setSelectors : function(selectors){
                UI.uiCom.setComSelectors(selectors);
            }
        });


        // 设置信息
        //UI.arena.find('>.hm-prop-head>.hm-com-info').html(html);

        // 切换显示模式
        UI.arena.attr("link-lib", libNm ? "yes" : "no");

        // 有组件，显示组件的操作菜单
        if(libNm) {
            var isInLib = UI.uiCom.isInLib();
            jEm.attr("in-lib", isInLib ? "yes" : null);
            new MenuUI({
                parent : UI,
                gasketName : "libmenu",
                setup : [{
                    icon : UI.msg("hmaker.lib.icon"),
                    text : libNm,
                    items : [{
                        icon : '<i class="fa fa-chain-broken"></i>',
                        text : 'i18n:hmaker.lib.detach',
                        handler : UI.doDetachLibItem
                    }, {
                        icon : '<i class="zmdi zmdi-refresh-sync"></i>',
                        text : 'i18n:hmaker.lib.reload',
                        handler : UI.doReloadLibItem
                    }]
                }]
            }).render();
        }
        // 显示创建组件的按钮
        else {
            jEm.removeAttr("in-lib");
            new MenuUI({
                parent : UI,
                gasketName : "libmenu",
                setup : [{
                    icon : UI.msg("hmaker.lib.icon"),
                    text : 'i18n:hmaker.lib.create',
                    handler : UI.doCreateLibItem
                }]
            }).render();
        }

        // 总之最后重新设置一下尺寸
        UI.resize();

    },
    //...............................................................
    doChangeBlock : function(mode, uiCom, block) {
        if("panel" == mode)
            return;
        //console.log("hm_prop_edit::doChangeBlock:", mode, uiCom.uiName);
        this.gasket.block.update(uiCom, block);
    },
    //...............................................................
    doChangeCom : function(mode, uiCom, com) {
        if("panel" == mode)
            return;
        //console.log("hm_prop_edit::doChangeCom:", mode, uiCom.uiName);
        // 执行更新
        this.gasket.com.update(uiCom, com);
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var H  = UI.arena.height();
        var jHead = UI.arena.find(".hm-prop-head");
        var jTabs = UI.arena.find(".hm-prop-tabs");
        var jBody = UI.arena.find(".hm-prop-body");
        jBody.css("height", H - jHead.outerHeight() - jTabs.outerHeight());
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);