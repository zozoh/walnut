(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/layout/layout',
    'ui/thing3/support/th3_methods',
    'ui/thing3/support/th3_util',
    'ui/pop/pop'
], function(ZUI, Wn, LayoutUI, ThMethods, Ths, POP){
//==============================================
var html = function(){/*
<div class="ui-arena th3-main" ui-fitparent="true" ui-gasket="main">
</div>
*/};
//==============================================
return ZUI.def("ui.th3.main", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing3/theme/th3-{{theme}}.css",
    i18n : "ui/thing3/i18n/{{lang}}.js",
    //..............................................
    update : function(oDir, callback) {
        var UI  = this;
        var opt = UI.options;
        //console.log(opt)

        // 加载配置文件
        var oThConf = Wn.fetch("id:"+oDir.id+"/thing.js");
        Wn.read(oThConf, function(json) {
            // 格式化配置对象
            var conf = $z.fromJson(json);
            Ths.evalConf(UI, conf, opt, oDir);

            // 初始化本地数据
            UI.__main_data = {
                home      : oDir,
                conf      : conf,
                currentId : UI.local('th3_last_actived_id_'+oDir.id)
            };
            
            // 加载主界面
            UI.__do_redraw(conf, callback);
        });

        // 表示自己是异步加载
        // 待加载完毕，需要主动调用回调
        return true;
    },
    //..............................................
    __do_redraw : function(conf, callback) {
        var UI = this;
        var man = this.__main_data;

        // 准备主界面布局对象
        var bus = new LayoutUI({
            parent : UI,
            gasketName : 'main',
            layout : conf.layout || 'ui/thing3/layout/col3_md_ma.xml',
            on_before_init : function(){
                ThMethods(this);
            },
            setup :{
                "list"    : 'ui/thing3/th3_search',
                "meta"    : 'ui/thing3/th3_meta',
                "content" : 'ui/thing3/th3_content',
                "media"   : {
                    uiType :'ui/thing3/th3_media',
                    uiConf : {folderName:"media"}
                },
                "attachment"  : {
                    uiType :'ui/thing3/th3_media',
                    uiConf : {folderName:"attachment"}
                },
                "create" : {
                    uiType : 'ui/thing3/th3_obj_create'
                }
            },
            eventRouter : conf.eventRouter,
            localKey : 'th_local_layout_' + man.home.id
        });

        // 监听各个区域，一旦准备好就要触发更新数据
        bus.listenSelf("area:ready", function(eo) {
            //console.log("area:ready", eo);
            UI.resize(true);
            for(var key in eo.uis) {
                var ui = ThMethods(eo.uis[key]);
                $z.invoke(ui, "update");
            }
        });

        // 监听打开 config
        bus.listenSelf("open:config", function(eo){
            UI.openConfigSetup();
        });

        // 监听导入数据
        bus.listenSelf("do:import", function(eo){
            UI.openImport();
        });

        // 监听导出数据
        bus.listenSelf("do:export", function(eo){
            UI.openExport();
        });

        // 渲染布局
        bus.render(function(){
            // 调用回调，以便调用者知道异步加载已经完成
            $z.doCallback(callback, [], UI);
        });
    },
    //..............................................
    openConfigSetup : function() {
        var UI = this;
        var man   = this.__main_data;
        var conf  = man.conf;
        var oHome = man.home;
        
        POP.openUIPanel({
            title : UI.msg("th3.conf.title", oHome),
            width : 640,
            arenaClass : "th-design-mask",
            setup : {
                uiType : "ui/thing3/design/th3_design",
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
        var man   = this.__main_data;
        var conf  = man.conf;
        var oHome = man.home;
        
        POP.openUIPanel({
            title  : "i18n:th3.import.title",
            width  : 640,
            height : 480,
            closer : true,
            arenaClass : "th3-wizard-mask",
            setup : {
                uiType : "ui/thing3/wiz/th3_import",
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
                        UI.subUI('main').fire('list:refresh');
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
        var man   = this.__main_data;
        var conf  = man.conf;
        var oHome = man.home;
        
        POP.openUIPanel({
            title  : "i18n:th3.export.title",
            width  : 640,
            height : 480,
            closer : true,
            arenaClass : "th3-wizard-mask",
            setup : {
                uiType : "ui/thing3/wiz/th3_export",
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
                    queryContext : UI.subUI("main/list").getQueryContext(),
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
    /*
    setup : {
        method   : "func",  // the funcion name should be called
        args     : [],      // default as objs
        menuItem  :{..},    // MenuItem Object
        jBtn     : jQuery   // menu button which be clicked,
        callback : F()      // anync callback
    }
    */
    invokeExtCommand : function(setup) {
        var thM  = this;
        var man  = this.__main_data;
        var objs = thM.subUI("main/list").getChecked();
        var methodName = '__ext_' + setup.method;
        // 设置默认参数
        var context = _.extend({
            args : [objs]
        }, setup, {
            UI   : thM,
            bus  : thM.gasket.main,
            objs : objs,
            man  : man,
            POP  : POP,
            Wn   : Wn,
        });
        // 调用
        return $z.invoke(thM, methodName, context.args, context);
    },
    //..............................................
    setCurrentObj : function(obj) {
        var man = this.__main_data;
        man.currentId = obj ? obj.id : null;
        // 本地记录一下
        this.local('th3_last_actived_id_'+man.home.id, man.currentId);
    },
    //..............................................
    getCurrentObj : function() {
        var man = this.__main_data;
        if(man.currentId) {
            return Wn.getById(man.currentId, true);
        }
        return null;
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);