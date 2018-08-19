(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/layout/layout',
    'ui/thing3/support/th_util',
    'ui/thing3/support/th3_methods'
], function(ZUI, Wn, LayoutUI, Ths, ThMethods){
//==============================================
var html = function(){/*
<div class="ui-arena th3-main" ui-fitparent="true" ui-gasket="main">
</div>
*/};
//==============================================
return ZUI.def("ui.th3.th_main", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing3/theme/th3-{{theme}}.css",
    i18n : "ui/thing3/i18n/{{lang}}.js",
    //..............................................
    update : function(oDir, callback) {
        var UI  = this;
        var opt = UI.options;
        //console.log(opt)

        // 加载配置文件

        // 加载主界面
        new LayoutUI({
            parent : UI,
            gasketName : 'main',
            layout : 'ui/thing3/layout/col3_md_ma.xml',
            on_init : function(){
                ThMethods(this);
            },
            setup :{
                "search"  : 'ui/thing3/th3_search',
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
            }
        }).render(function(){
            $z.doCallback(callback, [], UI);
        });

        // 表示自己是异步加载
        // 待加载完毕，需要主动调用回调
        return true;
    },    
    //..............................................
});
//==================================================
});
})(window.NutzUtil);