(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing/support/th_methods',
    'ui/otiles/otiles'
], function(ZUI, Wn, DomUI, ThMethods, OTilesUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj-data-media th-obj-data-W"
    ui-fitparent="true" ui-gasket="main"></div>
*/};
//==============================================
return ZUI.def("ui.th_obj_data_media", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing/theme/thing-{{theme}}.css",
    i18n : "ui/thing/i18n/{{lang}}.js",
    //..............................................
    dragAndDrop : true,
    on_drop : function(files) {
        this.upload(files);
    },
    //..............................................
    init : function(opt){
        ThMethods(this);
    },
    //..............................................
    redraw : function(){
        var UI   = this;
        var conf = UI.getBusConf();
        
        // 创建列表
        new OTilesUI({
            parent : UI,
            gasketName : "main",
            arenaClass : "data-list",
            objTagName : "SPAN",
            on_open : function(o) {
                console.log("open", o);
            }
        }).render(function(){
            UI.defer_report("list");
        });

        // 返回延迟加载
        return ["list"];
    },
    //..............................................
    update : function(o, callback) {
        var UI   = this;
        UI.__OBJ = o;

        UI.gasket.main.showLoading();
        UI.invokeConfCallback("media", "list", [o, function(list){
            UI.gasket.main.hideLoading();
            console.log(list);
            UI.gasket.main.setData(list);
        }]);
    },
    //..............................................
    upload : function(files) {
        var UI = this;

        for(var i=0; i<files.length; i++){
            UI.__do_upload(files[i]);
        }
    },
    //..............................................
    __do_upload : function(f){
        var UI = this;
        var se = Wn.app().session;
        var conf = UI.getBusConf();

        console.log(f, se)

        // 准备个假的对象
        var fo = {
            nm : f.name,
            tp : $z.getSuffixName(f.name),
            mime : f.type,
            c : se.me,
            m : se.me,
            g : se.grp,
            len : f.size,
            ct : f.lastModified,
            lm : f.lastModified,
        };

        // 在列表里先创建上
        var jItem = UI.gasket.main.add(fo);
        $z.invoke(UI.gasket.main, "showProgress", [jItem, 0]);

        // 上传并显示进度
        UI.invokeConfCallback("media", "upload", [{
            obj  : UI.__OBJ,
            file : f,
            overwrite : conf.media.overwrite,
            progress : function(pe){
                $z.invoke(UI.gasket.main, "showProgress", [jItem, pe]);
            },
            done : function(newObj){
                $z.invoke(UI.gasket.main, "hideProgress", [jItem]);
                UI.gasket.main.update(newObj, jItem);
            },
            fail : function(re) {
                alert("upload fail");
                console.warn(re);
            }
        }]);
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);