(function($z){
$z.declare([
    'zui',
    'wn/util',
    'ui/support/dom',
    'ui/thing3/support/th3_methods',
    'ui/support/dom',
    'ui/o_view_obj/o_view_preview',
    'ui/otiles/otiles',
], function(ZUI, Wn, DomUI, ThMethods, DomUI, OPreviewUI, OTilesUI){
//==============================================
var html = function(){/*
<div class="ui-arena th3-media" ui-fitparent="true">
    <header ui-gasket="preview"></header>
    <section ui-gasket="list"></section>
    <div class="hide-file-selector">
        <input type="file" multiple>
    </div>
</div>
*/};
//==============================================
return ZUI.def("ui.th3.media", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/thing3/theme/th3-{{theme}}.css",
    i18n : "ui/thing3/i18n/{{lang}}.js",
    //..............................................
    dragAndDrop : true,
    on_drop : function(files) {
        this.upload(files);
    },
    //..............................................
    init : function(opt){
        var UI = ThMethods(this);
        $z.setUndefined(opt, "folderName", "media");

        var mode = opt.folderName;

        UI.listenBus("obj:selected", UI.on_selected);
        UI.listenBus("obj:blur", UI.showBlank);
        UI.listenBus(mode+":refresh", UI.on_refresh);
        UI.listenBus(mode+":remove", UI.on_remove);
        UI.listenBus(mode+":upload", UI.on_upload);
    },
    //..............................................
    events : {
        // 监控隐藏的上传按钮
        'change input[type="file"]' : function(e) {
            var UI   = this;
            var eleF = e.currentTarget;

            // 执行上传
            UI.upload(e.currentTarget.files);

            // 清空
            $(eleF).val("");
        }
    },
    //..............................................
    redraw : function(){
        var UI   = this;
        var opt  = UI.options;
        var mode = opt.folderName;

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
                Wn.execf('thing {{th_set}} '+mode+' {{id}} -ufc', {
                    th_set : obj.th_set,
                    id     : obj.id,
                }, function(re){
                    UI.doActionCallback(re, function(obj2){
                        Wn.saveToCache(obj2);
                    });
                });
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
    showBlank : function() {
        var UI = this;

        UI.showPreview(null);
        UI.gasket.list.setData([]);
    },
    //..............................................
    on_selected : function(eo) {
        var UI = this;

        // 寻找一个对象
        var objs = eo.data;

        // 显示空白
        if(!_.isArray(objs) || objs.length <=0) {
            UI.showBlank();
            return;
        }

        // 将第一个对象作为要显示的对象
        UI.__OBJ = objs[0];
        UI.refresh();
    },
    //..............................................
    getCheckedItems : function() {
        return this.gasket.list.getChecked();
    },
    //..............................................
    on_remove : function(eo) {
        var data = eo.data;
        var callback = null;
        if(_.isArray(data) && data.length > 0)
            callback = data[0];
        this.removeCheckedItems(callback);
    },
    //..............................................
    removeCheckedItems : function(callback){
        var UI  = this;
        var opt = UI.options;
        var o   = UI.__OBJ;
        var mode = opt.folderName;

        // 得到所有选中的对象
        var list = UI.gasket.list.getChecked();

        // 没有选中，则显示警告
        if(list.length == 0) {
            $z.markIt({
                target : UI.gasket.list.arena,
                text   : UI.msg("th3.data.delnone")
            }, callback);
        }
        // 执行删除
        else {
            var cmdText = 'thing {{th_set}} '+mode+' {{id}} -del';
            for(var i=0; i<list.length; i++) {
                var oM = list[i];
                cmdText += ' "' + oM.nm + '"'; 
            }
            Wn.execf(cmdText, o, function(re){
                UI.doActionCallback(re, function(list){
                    UI.refresh(callback);
                });
            });
        }
    },
    //..............................................
    on_refresh : function(eo) {
        var data = eo.data;
        var callback = null;
        if(_.isArray(data) && data.length > 0)
            callback = data[0];
        this.refresh(callback);
    },
    //..............................................
    on_upload : function(eo) {
        this.arena.find('input[type="file"]').click();
    },
    //..............................................
    refresh : function(callback) {
        var UI  = this;
        var opt = UI.options;
        var o   = UI.__OBJ;

        UI.gasket.list.showLoading();
        Wn.execf('thing {{th_set}} '+opt.folderName+' {{id}}', o, function(re){
            var list = $z.fromJson(re);
            $z.doCallback(callback, [list]);
            UI.doActionCallback(re, function(list){
                UI.gasket.list.hideLoading();
                UI.gasket.list.setData(list);
                UI.showPreview(UI.gasket.list.getActived(), callback);
            });
        });
    },
    //..............................................
    showPreview : function(oMedia, callback) {
        var UI = this;
        var man  = UI.getMainData();
        var conf = man.conf;

        if(!oMedia) {
            new DomUI({
                parent : UI,
                gasketName : "preview",
                fitparent : true,
                dom : UI.compactHTML(`<div class="ui-arena media-none">
                    {{th3.data.none_media_tip}}
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
                    tip  : 'i18n:th3.data.asthumb',
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

                        var thumbsz = conf.thumbSize || "256x256";
                        // console.log(conf.thumbSize);
                        var oTh = UI.__OBJ;
                        var thumbPh = 'id:'+oTh.th_set+'/data/'+oTh.id+'/thumb.jpg';
                        // console.log(o.thumb)
                        // 将这个图片 cp 一份到 thumb.jpg
                        Wn.execf('chimg id:{{srcId}} -s "{{thumbsz}}" {{taph}}; obj {{taph}}', {
                            srcId   : oThumbSrcId,
                            thumbsz : thumbsz,
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
                                UI.doActionCallback(re, function(oTh2){
                                    Wn.saveToCache(oTh2);
                                    UI.fireBus("meta:updated", [oTh2, "thumb"]);
                                });
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
        var man  = UI.getMainData();
        var conf = man.conf;
        var mode = opt.folderName;

        // 判断是否为覆盖模式，默认为 true
        var c2 = _.extend({}, conf[mode]);
        $z.setUndefined(c2, 'overwrite', true);

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
            var th = UI.__OBJ;
            var ph = "id:"+th.th_set+"/data/"+th.id+"/"+mode+"/";
            var url = "/o/upload/<%=ph%>";
            url += "?nm=<%=file.name%>";
            url += "&sz=<%=file.size%>";
            url += "&mime=<%=file.type%>";
            if(!c2.overwrite)
                url += '&dupp=${major}(${nb})${suffix}';
            $z.uploadFile({
                file : f,
                url  : url,
                ph   : "id:"+th.th_set+"/data/"+th.id+"/"+mode+"/",
                evalReturn : "ajax",
                progress : function(e){
                    var pe = e.loaded/e.total;
                    $z.invoke(UI.gasket.list, "showProgress", [jItem, pe]);
                },
                done : function(newObj){
                    Wn.execf('thing {{th_set}} '+mode+' {{id}} -ufc',th,function(){
                        $z.invoke(UI.gasket.list, "hideProgress", [jItem]);
                        UI.gasket.list.update(newObj, jItem);
                        $z.blinkIt(jItem);
                    });
                },
                fail : function(re) {
                    alert("upload failed");
                    console.warn(re);
                }
            });
        };

        // 如果是覆盖的话，看看有没有重名
        if(c2.overwrite) {
            var jItem = UI.gasket.list.findItem("nm:" + fo.nm);
            if(jItem.length > 0) {
                if(window.confirm(UI.msg("th3.data.overwrite_tip", fo))){
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