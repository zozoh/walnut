(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing/support/th_methods',
    'ui/support/dom',
    'ui/o_view_obj/o_view_preview',
    'ui/otiles/otiles',
], function(ZUI, Wn, DomUI, ThMethods, DomUI, OPreviewUI, OTilesUI){
//==============================================
var html = function(){/*
<div class="ui-arena th-obj-data-media th-obj-data-W" ui-fitparent="true">
    <header ui-gasket="preview"></header>
    <section ui-gasket="list"></section>
    <!--aside>{{thing.data.drag_tip}}</aside-->
</div>
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

        // 创建默认 Preview 视图
        UI.showPreview(null, function(){
            UI.defer_report("preview");
        });
        
        // 创建列表
        new OTilesUI({
            parent : UI,
            gasketName : "list",
            arenaClass : "data-list",
            objTagName : "SPAN",
            renameable : true,
            on_open : function(o) {
                console.log("open", o);
            },
            on_actived : function(o) {
                UI.showPreview(o);
            },
            on_blur : function(jItems, nextObj) {
                if(!nextObj)
                    UI.showPreview(null);
            }
        }).render(function(){
            UI.defer_report("list");
        });

        // 返回延迟加载
        return ["preview", "list"];
    },
    //..............................................
    update : function(o, callback) {
        var UI   = this;
        UI.__OBJ = o;

        UI.gasket.list.showLoading();
        UI.invokeConfCallback("media", "list", [o, function(list){
            UI.gasket.list.hideLoading();
            UI.gasket.list.setData(list);
            UI.showPreview(UI.gasket.list.getActived(), callback);
        }]);
    },
    //..............................................
    showPreview : function(oMedia, callback) {
        var UI = this;

        if(!oMedia) {
            new DomUI({
                parent : UI,
                gasketName : "preview",
                fitparent : true,
                dom : UI.compactHTML(`<div class="ui-arena media-none">
                    {{thing.data.none_media_tip}}
                </div>`)
            }).render(function(){
                $z.doCallback(callback);
            });
        }
        // 显示预览
        else {
            new OPreviewUI({
                parent : UI,
                gasketName : "preview",
            }).render(function(){
                this.update(oMedia);
                $z.doCallback(callback);
            });
        }
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
    
        // 定义上传并显示进度的函数
        var __run = function(jItem) {
            $z.invoke(UI.gasket.list, "showProgress", [jItem, 0]);
            UI.invokeConfCallback("media", "upload", [{
                obj  : UI.__OBJ,
                file : f,
                overwrite : conf.media.overwrite,
                progress : function(pe){
                    $z.invoke(UI.gasket.list, "showProgress", [jItem, pe]);
                },
                done : function(newObj){
                    $z.invoke(UI.gasket.list, "hideProgress", [jItem]);
                    UI.gasket.list.update(newObj, jItem);
                    $z.blinkIt(jItem);
                },
                fail : function(re) {
                    alert("upload fail");
                    console.warn(re);
                }
            }]);
        }

        // 如果是覆盖的话，看看有没有重名
        if(conf.media.overwrite) {
            var jItem = UI.gasket.list.findItem("nm:" + fo.nm);
            if(jItem.length > 0) {
                if(window.confirm(UI.msg("thing.data.overwrite_tip", fo))){
                    __run(jItem);
                }
            }
            // 否则创建一个新项目
            else {
                var jItem = UI.gasket.list.add(fo);
                __run(jItem);
            }
        }
        // 否则肯定是创建新的咯在列表里先创建上
        else {
            var jItem = UI.gasket.list.add(fo);
            __run(jItem);
        }
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);