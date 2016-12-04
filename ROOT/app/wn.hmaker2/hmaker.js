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
    'app/wn.hmaker2/hm_other',
], function(ZUI, Wn, MaskUI,
    HmMethods, HmPanelMethods, 
    HmResourceUI, 
    HmPageUI, 
    HmPropUI,
    HmFolderUI, 
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
            MainUI = HmFolderUI;
        }
        // 如果是网页，显示 PageUI
        else if(/^text\/html$/.test(o.mime)){
            MainUI = HmPageUI;
        }
        // 其他的显示错误的 UI
        else {
            MainUI = HmOtherUI;
        }

        // 加载主界面
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
    },
    //...............................................................
    doPublish : function(o) {
        var UI = this;

        // 得到主目录
        var oHome = UI.getHomeObj();

        // 得到当前页
        var oPage = UI.getCurrentEditObj();
        var rph = Wn.getRelativePath(oHome, oPage);

        // 准备命令字符串
        var cmdText = "hmaker publish id:" + this.getHomeObjId();

        // 指定了发布的页面
        if(o) {
            var rph = Wn.getRelativePath(oHome, oPage);
            cmdText +=  " -src '"+rph+"'";
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
        }]);
    },
    //...............................................................
    openNewSitePanel : function() {
        var UI   = this;
        
        UI.browser().chuteUI().refresh(function(){
            console.log(new Date())
        });
    },
    //...............................................................
    openSiteConf : function() {
        var UI = this;
        var oHome = UI.getHomeObj();

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

                        // 更新皮肤
                        UI.fire("change:site:skin");
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
                        key : "title",
                        title : UI.msg("hmaker.site.title"),
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
            this.body.setData(_.pick(oHome, "title", "hm_target_release", "hm_target_debug", "hm_site_skin"));
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