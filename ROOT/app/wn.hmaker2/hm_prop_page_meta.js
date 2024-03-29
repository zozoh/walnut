(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/form/form',
    'ui/menu/menu',
    'ui/pop/pop',
    'ui/list/list',
    'app/wn.hmaker2/support/hm__methods_panel',
], function(ZUI, Wn, FormUI, MenuUI, POP, ListUI, HmMethods){
//==============================================
var html = `
<div class="ui-arena hm-prop-page-meta" ui-fitparent="yes">
    <div class="pp-attr">
        <header>
            <i class="zmdi zmdi-alert-circle-o"></i>
            <b>{{hmaker.page.attr}}</b>
            <a class="hppm-meta">
                {{hmaker.page.meta}}
            </a>
            <a class="hppm-lock">
                <i class="zmdi zmdi-lock-open"></i>
                <span>{{hmaker.page.guard}}</span>
            </a>
        </header>
        <section ui-gasket="form"></section>
    </div>
    <div class="pp-hierarchy">
        <header>
            <i class="fas fa-sitemap"></i>
            <b>{{hmaker.page.hierarchy}}</b>
        </header>
        <section>
            <textarea spellcheck="false"></textarea>
            <div class="tip">{{hmaker.page.hier_tip}}</div>
        </section>
    </div>
    <div class="pp-links">
        <header>
            <i class="zmdi zmdi-link"></i>
            <b>{{hmaker.page.links}}</b>
        </header>
        <aside ui-gasket="menu"></aside>
        <section><div ui-gasket="links"></div></section>
    </div>
</div>
`;
//==============================================
return ZUI.def("app.wn.hm_prop_page_meta", {
    dom  : html,
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
    },
    //...............................................................
    events : {
        // 编辑页面更多属性
        "click .pp-attr header a.hppm-meta" : function(){
            this.doEditMore();
        },
        // 编辑页面保护
        "click .pp-attr header a.hppm-lock" : function(){
            this.doEditPageGuard();
        },
        // 编辑页面的层级结构
        "change .pp-hierarchy textarea" : function(){
            this.savePageHierarchy();
        },
        "keydown .pp-hierarchy textarea" : function(e) {
            if(13 == e.which && (e.metaKey || e.ctrlKey)) {
                this.savePageHierarchy();
            }
        }
    },
    //...............................................................
    redraw : function(){
        var UI = this;

        // 防守一下，有时候 hmaker 没加载
        if(!UI.hmaker())
            return;

        // 页面设置
        new FormUI({
            parent : UI,
            gasketName : "form",
            arenaClass : "page-form",
            fitparent : false,
            on_change : function(){
                var attr = this.getData();
                UI.pageUI().setPageAttr(attr);
            },
            uiWidth : "all",
            fields : [{
                key    : "title",
                title  : "i18n:hmaker.page.title",
                type   : "string",
                editAs : "input"
            }, {
                key    : "margin",
                title  : "i18n:hmaker.page.margin",
                type   : "string",
                dft    : "",
                emptyAsNull : false,
                editAs : "input"
            }, {
                key    : "seokwd",
                title  : "i18n:hmaker.page.seokwd",
                type   : "string",
                dft    : "",
                emptyAsNull : false,
                editAs : "input"
            }, {
                key    : "seodescription",
                title  : "i18n:hmaker.page.seodescription",
                type   : "string",
                dft    : "",
                emptyAsNull : false,
                editAs : "text",
                uiConf : {
                    height: 100
                }
            }, {
                key    : "color",
                title  : "i18n:hmaker.prop.color",
                type   : "string",
                dft    : "",
                emptyAsNull : false,
                editAs : "color",
            }, {
                key    : "background",
                title  : "i18n:hmaker.prop.background",
                type   : "string",
                dft    : "",
                emptyAsNull : false,
                editAs : "background",
                uiConf : UI.getBackgroundImageEditConf()
            }]
        }).render(function(){
            UI.defer_report("form");
        });

        // 链接资源的操作菜单
        new MenuUI({
            parent : UI,
            gasketName : "menu",
            setup : [{
                icon : '<i class="fa fa-edit"></i>',
                text : "i18n:hmaker.page.links_edit",
                handler : function(){
                    UI.doEditResources();
                }
            }]
        }).render(function(){
            UI.defer_report("menu");
        });

        // 链接资源列表
        new ListUI({
            parent : UI,
            gasketName : "links",
            activable  : false,
            checkable  : false,
            escapeHtml : false,
            arenaClass : "hm-pg-links",
            idKey : "ph",
            icon : function(o) {
                return UI.getObjIcon(o);
            },
            text : function(o) {
                return o.rph || o.ph;
            }
        }).render(function(){
            UI.defer_report("links");
        });

        // 返回延迟加载
        return ["form", "menu", "links"];
    },
    //...............................................................
    __get_items_for_tsid: function(oPage){
        var UI = this;
        
        var list = [];
        // 显示可用数据集
        if(_.isArray(oPage.hm_list_tsid) && oPage.hm_list_tsid.length>0) {
            for(var i=0; i<oPage.hm_list_tsid.length;i++) {
                var tsid = oPage.hm_list_tsid[i];
                var oTS = Wn.getById(tsid);
                list.push({
                    icon  : oTS.icon,
                    text  : oTS.nm,
                    value : oTS.id
                });
            }
        }
        // 显示个空
        else {
            list.push({
                text  : UI.msg("none"),
                value : null
            });
        }
        return list;
    },
    //...............................................................
    __get_items_for_api: function(oPage){
        var UI = this;
        
        var list = [];
        // 显示可用数据集
        if(_.isArray(oPage.hm_list_api) && oPage.hm_list_api.length>0) {
            for(var i=0; i<oPage.hm_list_api.length;i++) {
                var api = oPage.hm_list_api[i];
                list.push({
                    icon  : '<i class="fa fa-plug"></i>',
                    text  : api,
                    value : api
                });
            }
        }
        // 显示个空
        else {
            list.push({
                text  : UI.msg("none"),
                value : null
            });
        }
        return list;
    },
    //...............................................................
    doEditPageGuard : function() {
        var UI = this;
        var homeId = this.getHomeObjId();
        var oPage  = this.pageUI().getCurrentEditObj(true);

        // 准备 input 的 assist
        var inAss = {
            icon : '<i class="zmdi zmdi-caret-down"></i>',
            uiType : "ui/form/c_list",
            uiConf : {
                drawOnSetData : true,
                items : 'hmaker id:'+homeId+' links -key "rph,nm,tp,title" -site',
                escapeHtml : false,
                icon  : function(o){
                    // 页面
                    if('html' == o.tp && !$z.getSuffixName(o.nm)) {
                        return  '<i class="fa fa-file"></i>';
                    }
                    // 其他遵守 walnut 的图标规范
                    return Wn.objIconHtml(o);
                },
                text : function(o) {
                    var str = '<span>/' + o.rph + '</span>';
                    if(o.title) {
                        str += '<em>' + o.title + '</em>';
                    }
                    return str;
                },
                value : function(o) {
                    return "${URI_BASE}/" + o.rph;
                },
            },
        };

        // 准备数据
        var guard = _.extend({enabled : !_.isEmpty(oPage.hm_pg_guard)}, oPage.hm_pg_guard);

        // 准备一个同步函数
        var sync_form_status = function(uiForm, data) {
            if(data.enabled) {
                uiForm.enableField("nologin", "nophone");
            } else {
                uiForm.disableField("nologin", "nophone");
            }
        };

        // 打开对话框
        POP.openUIPanel({
            title  : "i18n:hmaker.page.guard",
            escape : false,
            width  : 640,
            height : 480,
            setup : {
                uiType : "ui/form/form",
                uiConf : {
                    mergeData : false,
                    hideDisabled : false,
                    uiWidth : "all",
                    on_change : function(key, val){
                        var data = this.getData();
                        if("enabled" == key && val) {
                            data.nologin = data.nologin || '${URI_BASE}';
                            data.nophone = data.nophone || '${URI_BASE}';
                            this.setData(data);
                        }
                        sync_form_status(this, data);
                    },
                    fields : [{
                        key    : "enabled",
                        title  : "i18n:hmaker.page.gu_enable",
                        type   : "boolean",
                        uiType : "@toggle",
                    }, {
                        key : "nologin",
                        title : "i18n:hmaker.page.gu_nologin",
                        dft : "",
                        uiType : "@input",
                        uiConf : {
                            assist : inAss
                        }
                    }, {
                        key : "nophone",
                        title : "i18n:hmaker.page.gu_nophone",
                        dft : "",
                        uiType : "@input",
                        uiConf : {
                            assist : inAss
                        }
                    }]
                }
            },
            ready : function(uiBody) {
                uiBody.setData(guard);
                //console.log(guard)
                sync_form_status(uiBody, guard);
            },
            btnOk : 'i18n:hmaker.page.guard_save',
            ingOk : 'i18n:hmaker.page.guard_saving',
            ok : function(uiBody, jBtn, uiMask) {
                var data = uiBody.getData();
                var d2 = data.enabled ? $z.pick(data,["nologin","nophone"]) : null;
                var json = $z.toJson({hm_pg_guard:d2});

                Wn.execf('obj id:{{id}} -u \'<%=json%>\' -o', {
                    id : oPage.id,
                    json : json
                }, function(re){
                    var oP2 = $z.fromJson(re);
                    Wn.saveToCache(oP2);
                    UI.fire("update:obj", oP2);
                    uiMask.close();
                });
                return false;
            }
        }, UI);
    },
    //...............................................................
    doEditMore : function() {
        var UI = this;
        var oPage = this.pageUI().getCurrentEditObj(true);
        var rph = UI.getRelativePath(oPage);

        // 打开对话框
        POP.openUIPanel({
            title  : "i18n:hmaker.page.meta",
            width  : 640,
            height : 480,
            setup : {
                uiType : "ui/form/form",
                uiConf : {
                    mergeData : false,
                    on_change : function(key, val){
                        var uiForm = this;
                        uiForm.showPrompt(key, "spinning");
                        Wn.execf('obj id:{{homeId}}/{{rph}}'
                                    +' -u \'{{key}}:"{{val}}"\';'
                                    + 'hmaker id:{{homeId}} syncmeta {{rph}}', {
                            homeId : UI.getHomeObjId(),
                            rph : rph,
                            key : key,
                            val : val
                        }, function(re){
                            uiForm.hidePrompt();
                            // 错误
                            if(/^e./.test(re)) {
                                uiForm.showPrompt(key, "warn", re);
                            }
                            // 重新更新一下表单
                            else {
                                var reo = $z.fromJson(re);
                                uiForm.setData(reo);
                            }
                        });
                    },
                    fields : [{
                        key : "hm_pg_tsid",
                        title : "i18n:hmaker.page.meta_hm_pg_tsid",
                        uiType : "@droplist",
                        uiConf : {
                            items : function(){
                                return UI.__get_items_for_tsid(oPage);
                            }
                        },
                    }, {
                        key : "hm_pg_api",
                        title : "i18n:hmaker.page.meta_hm_pg_api",
                        uiType : "@droplist",
                        uiConf : {
                            items : function(){
                                return UI.__get_items_for_api(oPage);
                            }
                        }
                    }, {
                        key : "hm_api_method",
                        title : "i18n:hmaker.page.meta_hm_api_method",
                        uiType : "@label",
                    }, {
                        key : "hm_api_return",
                        title : "i18n:hmaker.page.meta_hm_api_return",
                        uiType : "@label",
                    }]
                }
            },
            ready : function(uiBody) {
                uiBody.setData(oPage);
            },
            btnCancel : null,
        }, UI);
    },
    //...............................................................
    setPageHierarchy : function(oPg) {
         var UI = this;
        var jHier = UI.arena.find(".pp-hierarchy");
        var jText = jHier.find("textarea");
        
        jText.val(oPg.hm_hierarchy || "");
    },
    //...............................................................
    savePageHierarchy : function() {
        var UI = this;
        var jHier = UI.arena.find(".pp-hierarchy");
        var jText = jHier.find("textarea");
        var jIcon = jHier.find('header i');

        // 修改图标状态
        var oldClassName = jIcon[0].className;
        jIcon[0].className = "fa fa-spinner fa-pulse";

        // 存储到服务器
        var pageUI = UI.pageUI();
        pageUI.setPageAttr({hm_hierarchy:jText.val()}, function(){
            jIcon[0].className = oldClassName;
            UI.refresh();

            // 最后调用一下皮肤
            pageUI.invokeSkin("ready");
            pageUI.invokeSkin("resize");
        });
    },
    //...............................................................
    doEditResources : function(){
        var UI = this;
        var homeId = UI.getHomeObjId();

        POP.openUIPanel({
            title  : "i18n:hmaker.page.pick_rs",
            width  : 400,
            height : "80%",
            setup : {
                uiType : "ui/list/list",
                uiConf : {
                    activable  : false,
                    checkable  : true,
                    escapeHtml : false,
                    arenaClass : "hm-rs-list",
                    idKey : "rph",
                    icon : function(o) {
                        return UI.getObjIcon(o);
                    },
                    text : function(o) {
                        return o.rph || o.ph;
                    }
                }
            },
            ready : function(uiBody) {
                uiBody.showLoading();
                var cmdText = 'hmaker id:'+homeId+" rs -path"
                Wn.execf('hmaker id:{{homeId}} rs -match "{{match}}" -path "css,js" -key "{{key}}"',{
                    homeId : homeId,
                    match  : "[.](css|js)$",
                    key    : "nm,tp,mime,ph,rph"
                }, function(re){
                    // 隐藏加载提示
                    uiBody.hideLoading();

                    // 防错
                    if(/^e./.test(re)){
                        UI.alert(re);
                        return;
                    }

                    // 显示
                    var list = $z.fromJson(re);
                    uiBody.setData(list);

                    // 标识已经选中的
                    var links = (UI.pageUI().getPageAttr().links || []);
                    for(var i=0; i<links.length; i++) {
                        uiBody.check(links[i].rph);
                    }
                });
            },
            ok : function(uiBody) {
                // 得到数据
                var objs   = uiBody.getChecked();
                //console.log(objs)
                // 设置
                UI.pageUI().setPageAttr({links:objs});

                // 清空一下缓存
                UI.pageUI().cleanCssSelectors();

                // 刷新列表
                UI.gasket.links.setData(objs);
            },
        }, UI);
    },
    //...............................................................
    refresh : function(){
        var UI = this;
        //console.log("page refresh");
        var pageUI = UI.pageUI(true);
        if(pageUI) {
            var attr = pageUI.getPageAttr(true);
            UI.gasket.form.setData(attr);
            UI.setPageHierarchy(attr);
            UI.gasket.links.setData(attr.links);
        }
    },
    //...............................................................
    resize : function(){
        var UI = this;
        var jAttr  = UI.arena.find(">.pp-attr");
        var jHier  = UI.arena.find(">.pp-hierarchy");
        var jLinks = UI.arena.find(">.pp-links");
        
        // 计算链接区域整体高度
        var H  = UI.arena.height();
        var H0 = jAttr.outerHeight(true);
        var H1 = jHier.outerHeight(true);
        var H2 = H - H0 - H1 - 4;
        jLinks.css("height", Math.max(H2, 240));

        // 计算链接列表起始位置
        var jLinksHead  = jLinks.children("header");
        var jLinksAside = jLinks.children("aside");
        var jLinksList  = jLinks.children("section");
        jLinksList.css("top", jLinksHead.outerHeight(true)
                            + jLinksAside.outerHeight(true));
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);