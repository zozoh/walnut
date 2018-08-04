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
return ZUI.def("ui.thing.th_obj_data_media", {
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
        $z.setUndefined(opt, "folderName", "media");
    },
    //..............................................
    redraw : function(){
        var UI   = this;
        var opt  = UI.options;
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
                //console.log("open", o);
                var url = "/a/open/"+(window.wn_browser_appName||"wn.browser");
                $z.openUrl(url, "_blank", "GET", {
                    "ph" : "id:" + o.id
                });
            },
            on_rename : function(o) {
                var obj = UI.__OBJ;
                UI.invokeConfCallback(opt.folderName, "on_rename", [obj, o, function(obj2){
                    Wn.saveToCache(obj2);
                }]);
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
        this.__OBJ = o;
        this.refresh(callback);
    },
    //..............................................
    removeCheckedItems : function(callback){
        var UI  = this;
        var opt = UI.options;
        var o   = UI.__OBJ;

        // 得到所有选中的对象
        var list = UI.gasket.list.getChecked();

        // 没有选中，则显示警告
        if(list.length == 0) {
            $z.markIt({
                target : UI.gasket.list.arena,
                text   : UI.msg("thing.data.delnone")
            }, callback);
        }
        // 执行删除
        else {
            UI.invokeConfCallback(opt.folderName, "remove", [o, list, function(){
                UI.refresh(callback);
            }]);
        }
    },
    //..............................................
    refresh : function(callback) {
        var UI  = this;
        var opt = UI.options;
        var o   = UI.__OBJ;

        UI.gasket.list.showLoading();
        UI.invokeConfCallback(opt.folderName, "list", [o, function(list){
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
                commands : ["fullscreen", "download", "open", {
                    icon : '<i class="zmdi zmdi-image"></i>',
                    tip  : 'i18n:thing.data.asthumb',
                    handler :function(o){
                        // 对对象进行一下处理
                        var coverType = /^video\//.test(o.mime)?"video":"image";
                        var oThumbSrcId = o.id;

                        // 如果是视频，那么就需要确保它是有  _preview 的，如果没有，生成一个
                        if("video" == coverType) {
                            if(!o.video_cover) {
                                var re = Wn.exec('videoc id:'+o.id+' -mode "preview_image" -o');
                                if(!re || /^e./.test(re)){
                                    UI.alert(re||"Some Error Happend!", "warn");
                                    return;
                                }
                                o = $z.fromJson(re);
                            }
                            // 视频转换失败
                            if(!o.video_cover) {
                                UI.alert("obj [" + o.nm + "] without video_cover "
                                        + o.id, "warn");
                                return;
                            }
                            // 记录图片源
                            oThumbSrcId = o.video_cover;
                        }

                        var conf = UI.getBusConf();
                        console.log(conf.thumbSize);
                        var oTh = UI.__OBJ;
                        var thumbPh = 'id:'+oTh.th_set+'/data/'+oTh.id+'/thumb.jpg';
                        console.log(o.thumb)
                        // 将这个图片 cp 一份到 thumb.jpg
                        Wn.execf('chimg id:{{srcId}} -s "{{thumbsz}}" {{taph}}; obj {{taph}}', {
                            srcId   : oThumbSrcId,
                            thumbsz : conf.thumbSize,
                            taph    : 'id:'+oTh.th_set+'/data/'+oTh.id+'/thumb.jpg'
                        }, function(re){
                            if(!re || /^e./.test(re)){
                                UI.alert(re||"Some Error Happend!", "warn");
                                return;
                            }
                            var reo = $z.fromJson(re);
                            
                            Wn.execf("thing {{th_set}} update {{id}} -fields '<%=json%>'", {
                                th_set : oTh.th_set,
                                id     : oTh.id,
                                json   : $z.toJson({
                                    thumb : 'id:' + reo.id,
                                    th_cover_tp  : coverType
                                })
                            }, function(re){
                                oTh = $z.fromJson(re);
                                Wn.saveToCache(oTh);
                                // 修改 thing 对象
                                UI.fire("change:meta", [oTh]);
                            })
                        })
                    }
                }]
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
        var UI   = this;
        var opt  = UI.options;
        var se   = Wn.app().session;
        var conf = UI.getBusConf();

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
            UI.invokeConfCallback(opt.folderName, "upload", [{
                obj  : UI.__OBJ,
                file : f,
                overwrite : conf[opt.folderName].overwrite,
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
        if(conf[opt.folderName].overwrite) {
            var jItem = UI.gasket.list.findItem("nm:" + fo.nm);
            if(jItem.length > 0) {
                if(window.confirm(UI.msg("thing.data.overwrite_tip", fo))){
                    __run(jItem);
                }
            }
            // 否则创建一个新项目
            else {
                var jItem = UI.gasket.list.add(fo, -1);
                __run(jItem);
            }
        }
        // 否则肯定是创建新的咯在列表里先创建上
        else {
            var jItem = UI.gasket.list.add(fo, -1);
            __run(jItem);
        }

        // 无论如何，滚动到 jItem 可以显示的地方
        jItem[0].scrollIntoView(false);
    },
    //..............................................
});
//==================================================
});
})(window.NutzUtil);