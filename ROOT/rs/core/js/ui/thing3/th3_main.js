(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/layout/layout',
    'ui/thing3/support/th3_methods',
    'ui/thing3/support/th3_util',
], function(ZUI, Wn, LayoutUI, ThMethods, Ths){
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
                }
            },
            eventRouter : conf.eventRouter
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

        // 渲染布局
        bus.render(function(){
            // 调用回调，以便调用者知道异步加载已经完成
            $z.doCallback(callback, [], UI);
        });
    },
    //..............................................
    setCurrentObj : function(obj) {
        var man = this.__main_data;
        man.currentId = obj ? obj.id : null;
        // 本地记录一下
        this.local('th3_last_actived_id_'+man.home.id, man.currentId);
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);