(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/pop/pop',
    'ui/thing/support/th_methods',
    'ui/thing/th_search',
    'ui/thing/th_obj',
], function(ZUI, Wn, DomUI, POP, ThMethods, ThSearchUI, ThObjUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-manager" ui-fitparent="true">
    <div class="th-search-con" ui-gasket="search"></div>
    <div class="th-obj-con" ui-gasket="obj"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.thing.th_manager", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    init : function(opt){
        ThMethods(this);

        this.listenBus("setup", this.openSetup, true);
        this.listenBus("do:import", this.openImport, true);
        this.listenBus("do:export", this.openExport, true);
    },
    //..............................................
    _fill_context : function(uiSet) {
        uiSet.manager = this;
        $z.invoke(this.gasket.search, "_fill_context", [uiSet]);
        $z.invoke(this.gasket.obj   , "_fill_context", [uiSet]);
    },
    //..............................................
    isOnlySearch : function(){
        return "only-search" == this.displayMode;
    },
    //..............................................
    __reset_display_mode : function(){
        var UI  = this;
        var bus = UI.bus();
        var conf = UI.getBusConf();

        // 准备显示模式
        var disMode = [];
        
        // 索引界面
        if(conf.meta || conf.detail) {
            disMode.push("index");
        }
        // 多媒体和附件界面
        if(conf.media || conf.attachment) {
            disMode.push("data");
        }

        // 设定显示模式
        UI.displayMode = disMode.length > 0 
                         ? disMode.join("-")
                         : "only-search";
        bus.arena.attr({
            "dis-mode": UI.displayMode
        });

        // 移除对象部分
        if(UI.isOnlySearch()) {
            UI.arena.find(">.th-obj-con").remove();
        }
    },
    //..............................................
    update : function(oDir, callback) {
        var UI  = this;
        var opt = UI.options;
        //console.log(opt)

        // 设置主目录
        UI.setHomeObj(oDir);

        // 修正显示模式
        UI.__reset_display_mode();

        // 准备延迟加载项
        UI.defer(["search", "blank"], function(){
            UI.gasket.search.update(oDir, function(){
                $z.doCallback(callback, [oDir], UI);
            });
        });

        // 加载搜索器
        new ThSearchUI({
            parent : UI,
            gasketName : "search",
            bus : UI,
        }).render(function(){
            UI.defer_report("search");
        });

        // 默认显示空白
        UI.showBlank(function(){
            UI.defer_report("blank");
        });

        // 表示自己是异步加载
        return true;
    },
    //..............................................
    // 做一个 search.getChecked 的假名，因为自定义命令菜单会调用到
    getData : function() {
        return this.uis("search").getChecked();
    },
    //..............................................
    openSetup : function() {
        var UI = this;
        var conf  = UI.getBusConf();
        var oHome = UI.getHomeObj();
        
        POP.openUIPanel({
            title : UI.msg("thing.conf.title", oHome),
            width : 640,
            arenaClass : "th-design-mask",
            setup : {
                uiType : "ui/thing/th_design",
            },
            ready : function(uiDesign){
                uiDesign.update(oHome);
            },
            close : function(uiDesign){
                if(uiDesign.isChanged()) {
                    window.location.reload();
                }
            },
            btnOk : null,
            btnCancel : null,
        }, UI);
    },
    //..............................................
    openImport : function() {
        var UI = this;
        var conf  = UI.getBusConf();
        var oHome = UI.getHomeObj();
        
        POP.openUIPanel({
            title  : "i18n:thing.import.title",
            width  : 640,
            height : 480,
            closer : true,
            arenaClass : "th-wizard-mask",
            setup : {
                uiType : "ui/thing/support/th_import",
                uiConf : {
                    thingSetId   : oHome.id,
                    accept       : conf.dataImport.accept,
                    processTmpl  : conf.dataImport.processTmpl,
                    uniqueKey    : conf.dataImport.uniqueKey,
                    mapping      : conf.dataImport.mapping,
                    fixedForm    : conf.dataImport.fixedForm,
                    afterCommand : conf.dataImport.afterCommand,
                    done : function() {
                        // 关闭窗口
                        this.parent.close();
                        // 刷新数据
                        UI.invokeUI("search", "refresh", [true]);
                    }
                }
            },
            btnOk : null,
            btnCancel : null,
        }, UI);
    },
    //..............................................
    openExport : function() {
        var UI = this;
        var conf  = UI.getBusConf();
        var oHome = UI.getHomeObj();
        
        POP.openUIPanel({
            title  : "i18n:thing.export.title",
            width  : 640,
            height : 480,
            closer : true,
            arenaClass : "th-wizard-mask",
            setup : {
                uiType : "ui/thing/support/th_export",
                uiConf : {
                    thingSetId   : oHome.id,
                    thingSetNm   : oHome.nm,
                    exportType   : conf.dataExport.exportType,
                    pageRange    : conf.dataExport.pageRange,
                    pageBegin    : conf.dataExport.pageBegin,
                    pageEnd      : conf.dataExport.pageEnd,
                    audoDownload : conf.dataExport.audoDownload,
                    mapping      : conf.dataExport.mapping,
                    processTmpl  : conf.dataExport.processTmpl,
                    queryContext : UI.uis("search").getQueryContext(),
                    done : function() {
                        this.parent.close();
                    }
                }
            },
            btnOk : null,
            btnCancel : null,
        }, UI);
    },
    //..............................................
    showObj : function(o, callback) {
        var UI = this;

        if(UI.isOnlySearch()){
            $z.doCallback(callback, [o], UI);
            return;
        }

        // 如果已经是对象加载器了，直接更新
        if(UI.gasket.obj.uiName == "ui.th_obj") {
            UI.gasket.obj.update(o, function(){
                $z.doCallback(callback, [o], UI);
            });
        }
        // 加载对象编辑器
        else {
            new ThObjUI({
                parent : UI,
                gasketName : "obj",
                bus : UI,
            }).render(function(){
                this.update(o, function(){
                    $z.doCallback(callback, [o], UI);
                });
            });
        }
    },
    //..............................................
    showBlank : function(callback) {
        var UI = this;

        if(UI.isOnlySearch()){
            $z.doCallback(callback, [], UI);
            return;
        }

        // 替换掉索引项
        new DomUI({
            parent : UI,
            gasketName : "obj",
            dom : `<div class="th-obj-blank">
                <i class="fa fa-hand-o-left"></i>
                {{thing.blank}}
            </div>`
        }).render(function(){
            $z.doCallback(callback, [], UI);
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);