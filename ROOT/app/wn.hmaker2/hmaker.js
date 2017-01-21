(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/mask/mask',
    'app/wn.hmaker2/support/hm__methods',
    'app/wn.hmaker2/support/hm__methods_panel',
    'app/wn.hmaker2/hm_resource',
    'app/wn.hmaker2/hm_page',
    'app/wn.hmaker2/hm_prop',
    'app/wn.hmaker2/hm_folder',
    'app/wn.hmaker2/hm_lib',
    'app/wn.hmaker2/hm_code',
    'app/wn.hmaker2/hm_other',
], function(ZUI, Wn, MaskUI,
    HmMethods, HmPanelMethods, 
    HmResourceUI, 
    HmPageUI, 
    HmPropUI,
    HmFolderUI,
    HmLibUI,
    HmCodeUI,
    HmOtherUI){
//==============================================
var html = function(){/*
<div class="ui-arena hmaker" ui-fitparent="yes">
    <div class="hm-con-main" ui-gasket="main"></div>
    <div class="hm-con-resource" ui-gasket="resource"></div>
    <div class="hm-con-prop" ui-gasket="prop"></div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.hmaker2", {
    __hmaker__ : "1.0",
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.hmaker2/hmaker.css",
    i18n : "app/wn.hmaker2/i18n/{{lang}}.js",
    //...............................................................
    init : function() {
        var UI = HmMethods(this);
        
        UI.listenSelf("active:rs", function(o){
            UI.changeMain(o);
        });
    },
    //...............................................................
    redraw : function(){
        var UI  = this;

        UI.showLoading();
        
        // 资源面板
        HmPanelMethods(new HmResourceUI({
            parent : UI,
            gasketName : "resource"
        })).render(function(){
            UI.defer_report("resource");
        });

        // 属性面板
        HmPanelMethods(new HmPropUI({
            parent : UI,
            gasketName : "prop"
        })).render(function(){
            UI.defer_report("prop");
        });

        // 返回延迟加载
        return ["resource", "prop"];
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        UI.__home_id = o.id;
        UI.gasket.resource.update(o, function(){
            UI.hideLoading();
        });
    },
    //...............................................................
    changeMain : function(o) {
        var UI = this;

        var MainUI, PropUI;

        // 如果是文件夹，那么显示 FolderUI
        if('DIR' == o.race) {
            // 特殊的目录: lib
            if(o.pid == UI.getHomeObjId()) {
                if('lib' == o.nm)
                    MainUI = HmLibUI
            }
            // 默认采用文件夹方式浏览
            MainUI = MainUI || HmFolderUI;
        }
        // 如果是网页，显示 PageUI
        else if(/^text\/html$/.test(o.mime)){
            // 无后缀的用编辑器编辑
            if(!$z.getSuffixName(o.nm)) {
                MainUI = HmPageUI;
            }
            // 其他的用文本编辑
            else {
                MainUI = HmCodeUI;
            }
        }
        // 如果是 css 或者 js 也是用代码视图
        else if(/^(css|js)$/.test(o.tp)) {
            MainUI = HmCodeUI;
        }
        // 其他的显示预览界面
        else {
            MainUI = HmOtherUI;
        }

        // TODO 这里要搞一下 hm_page 的 update，好像有问题，暂时先注掉
        // 直接更新主界面
        // if(UI.gasket.main && (MainUI.uiName == UI.gasket.main.uiName)) {
        //     UI.gasket.main.update(o);
        // }
        // 重新加载主界面
        //else {
            HmMethods(new MainUI({
                parent : UI,
                gasketName : "main"
            })).render(function(){
                // 更新菜单
                var actions = $z.invoke(this, "getActions") || [];
                var menuSetup = Wn.extendActions(actions, false, true);
                UI.parent.browser().updateMenu(menuSetup, this);

                // 更新主界面
                this.update(o);
            });
        //}
    },
    //...............................................................
    doPublish : function(oPage) {
        var UI = this;

        // 得到主目录
        var oHome = UI.getHomeObj();

        // 准备命令字符串
        var cmdText = "hmaker publish id:" + this.getHomeObjId();

        // 指定了发布的页面
        if(oPage) {
            var rph = Wn.getRelativePath(oHome, oPage);
            cmdText +=  " -src '"+rph+"' -keep";
        }

        // 执行命令
        Wn.processPanel(cmdText, {
            welcome    : UI.msg("hmaker.page.publish"),
            arenaClass : "hm-publish-mask"
        }, function(urls, jMsg) {
            jMsg.attr("mode", "result").empty();
            // 发布完毕后，显示一个访问链接以及二维码
            for(var url of urls) {
                url = $.trim(url);
                // 出错了
                if(/^!/.test(url)) {
                    $(`<div class="hm-warn"><i class="zmdi zmdi-alert-octagon"></i><em></em></div>`)
                        .appendTo(jMsg)
                            .find("em").text(url);
                }
                // 显示网址
                else {
                    var jDiv = $(`<div class="hm-enter"></div>`).appendTo(jMsg);
                    $('<img>')
                        .attr("src", "/api/"+oHome.d1+"/qrcode?ts="
                                + Date.now()
                                + "&url=" + encodeURIComponent(url))
                            .appendTo(jDiv);;
                    $('<a>')
                        .attr({
                            "href" : url,
                            "target" : "_blank"
                        }).text(UI.msg("hmaker.page.enter"))
                            .appendTo(jDiv);
                }
            }
            
        });
    },
    //...............................................................
    openCreatePanel : function() {
        var UI   = this;
        var oHome = UI.getHomeObj();

        // 显示新建文件对象面板
        Wn.createPanel(oHome, function(newObj){
            UI.resourceUI().refresh(function(){
                this.setActived(newObj.id);
            });
        }, [{
            race : "FILE",
            tp   : "html",
            text : "i18n:hmaker.html",
            tip  : "i18n:hmaker.html_tip",
        }, {
            race : "DIR",
            tp   : "folder",
            text : "i18n:hmaker.folder",
            tip  : "i18n:hmaker.folder_tip",
        }, {
            race : "FILE",
            tp   : "css",
            text : "i18n:hmaker.css",
            tip  : "i18n:hmaker.css_tip",
        }, {
            race : "FILE",
            tp   : "js",
            text : "i18n:hmaker.js",
            tip  : "i18n:hmaker.js_tip",
        }], {
            create : function(obj, callback) {
                // 确保 css/js 在正确的目录里
                if(/^(css|js)$/.test(obj.tp)) {
                    var re = Wn.exec('obj -o -race DIR -check id:'+oHome.id+"/"+obj.tp);
                    if(/^e./.test(re)){
                        UI.alert(re);
                        return;
                    }
                    var oDir = $z.fromJson(re);
                    obj.pid = oDir.id;
                    // 确保文件名以正确的后缀结尾
                    if(obj.tp != $z.getSuffixName(obj.nm)){
                        obj.nm += "." + obj.tp;
                    }
                }

                // 执行创建
                var cmdText = $z.tmpl("obj -o -new \'<%=obj%>\'")($z.toJson(obj));
                Wn.exec(cmdText, callback);
            }
        });
    },
    //...............................................................
    openNewSitePanel : function(copySite) {
        var UI    = this;
        var oHome = UI.getHomeObj();

        // 确定站点的路径
        new MaskUI({
            width  : 600,
            height : 500,
            setup : {
                uiType : "app/wn.hmaker2/support/ui_new_site",
                uiConf : {
                    oSiteHome : oHome,
                    copySite  : copySite,
                    done : function(oNewHome) {
                        // 关闭遮罩
                        this.parent.close();
                        
                        // 打开站点配置信息进一步编辑站点属性
                        UI.openSiteConfPanel(oNewHome, function(){
                            // 刷新侧边栏后 ... 
                            UI.browser().chuteUI().refresh(function(){
                                // 编辑完毕后切换到这个站点
                                UI.browser().setData(oNewHome, "hmaker2");
                            });
                        });
                    }
                }
            }
        }).render();
    },
    //...............................................................
    doDeleteSite : function() {
        var UI    = this;
        var oHome = UI.getHomeObj();

        // 试图从侧边栏获取下一个要激活的 Path
        var nextItem  = null;
        var uiSidebar = UI.browser().chuteUI().gasket.sidebar;
        if(uiSidebar) {
            nextItem = uiSidebar.getNextItem();
        }

        // 向用户确认一下要删除
        UI.confirm("hmaker.site.del_confirm", function(){
            var cmdText = 'rm -rfv id:' + oHome.id;
            Wn.logpanel(cmdText, function(){
                // 关闭日志面板
                this.close();

                // 刷新侧边栏
                UI.browser().chuteUI().refresh(function(){
                    if(nextItem) {
                        uiSidebar.clickItem(nextItem.ph, nextItem.editor);
                    }
                });
            });
        });
    },
    //...............................................................
    openSiteConfPanel : function(oHome, callback) {
        var UI = this;
        var oHome = oHome || UI.getHomeObj();

        // 显示弹出层
        new MaskUI({
            dom : 'ui/pop/pop.html',
            css : 'ui/pop/pop.css',
            width  : 600,
            height : 500,
            events : {
                "click .pm-btn-ok" : function(){
                    var uiMask  = this;
                    var conf = uiMask.body.getData();
                    //console.log(conf)
                    // 更新配置信息
                    Wn.exec("obj id:"+oHome.id+" -u -o", $z.toJson(conf), function(re){
                        // 保存站点对象
                        var obj  = $z.fromJson(re);
                        Wn.saveToCache(obj);

                        // 关闭对话框
                        uiMask.close();

                        // 调用回调
                        $z.doCallback(callback, [obj], UI);
                    });
                },
                "click .pm-btn-cancel" : function(){
                    this.close();
                }
            }, 
            setup : {
                uiType : "ui/form/form",
                uiConf : {
                    app  : UI.app,
                    exec : UI.exec,
                    uiWidth : "all",
                    fields  : [{
                        key : "nm",
                        title : UI.msg("hmaker.site.nm"),
                        type : "string",
                        editAs : "input"
                    }, UI.__form_fld_pick_folder({
                        key       : "hm_target_release",
                        title     : "i18n:hmaker.site.hm_target_release",
                        lastObjId : "hmaker_pick_hm_target_publish",
                    // }), UI.__form_fld_pick_folder({
                    //     key       : "hm_target_debug",
                    //     title     : "i18n:hmaker.site.hm_target_debug",
                    //     lastObjId : "hmaker_pick_hm_target_publish",
                    }), {
                        key   : "hm_site_skin",
                        title : UI.msg("hmaker.site.skin"),
                        icon  : UI.msg("hmaker.icon.skin"),
                        type  : "string",
                        editAs : "droplist",
                        uiConf : {
                            items : "obj ~/.hmaker/skin/* -json -l",
                            icon  : UI.msg("hmaker.icon.skin"),
                            text  : null,
                            value : function(o){
                                return o.nm;
                            },
                            emptyItem : {}
                        }
                    }]
                }
            }
        }).render(function(){
            this.arena.find(".pm-title").html(UI.msg('hmaker.site.conf'));
            this.body.setData(_.pick(oHome, "nm", "hm_target_release", "hm_target_debug", "hm_site_skin"));
        });
    },
    //...............................................................
    doChangeSiteConf : function() {
        var UI = this;
        UI.openSiteConfPanel(null, function(oHome){
            UI.fire("change:site:skin");

            // 刷新侧边栏后 ... 
            UI.browser().chuteUI().refresh(function(){
                this.gasket.sidebar.highlightItem(oHome.ph, "hmaker2");
            });
        });
    },
    //...............................................................
    getCurrentEditObj : function() {
        return $z.invoke(this.gasket.main, "getCurrentEditObj", []);
    },
    //...............................................................
    getCurrentTextContent : function() {
        return $z.invoke(this.gasket.main, "getCurrentTextContent", []);
    },
    //...............................................................
    __form_fld_pick_folder : function(fld) {
        var UI = this;
        return {
            key    : fld.key,
            title  : UI.text(fld.title),
            type   : "string",
            dft    : null,
            uiType : "ui/picker/opicker",
            uiConf : {
                setup : {
                    lastObjId : fld.lastObjId,
                    filter    : function(o) {
                        return 'DIR' == o.race;
                    },
                    objTagName : 'SPAN',
                },
                parseData : function(str){
                    var m = /id:(\w+)/.exec(str);
                    if(m)
                        return Wn.getById(m[1], true);
                    if(str)
                        return Wn.fetch(str, true);
                    return null;
                },
                formatData : function(o){
                    return o ? "~/" + Wn.getRelativePathToHome(o) : null;
                }
            }
        };
    },
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);