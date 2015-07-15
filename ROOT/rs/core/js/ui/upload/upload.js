/*
在 $pel 内创建一组 DOM，用来提供用户的文件上传界面
对应的 options 可以有以下选项
..............................................................
使用方法:

var UploadUI = require("ui/upload/upload");
new UploadUI({
    $pel  : $(document.body),
    //.......................................
    // 设置标题，如果 false 将隐藏标题栏
    // 自定义标题支持字符串模板，占位符为 target 的所有字段 
    title : false|true|"自定义",
    //.......................................
    // 一个 WnObj 的实例表示上传目标 
    target : {..},
    //.......................................
    // 如果是多文件上传，重名文件是否自动替换
    // 默认 false
    replaceable : false | true,
    //.......................................
    // 声明校验函数，或者一个正则表达式验证文件名
    valicate : function(file){
        throw "如果错误抛出错误消息"
    } || "^.+[.]png$" || /^.+[.]png$/
}).render();

*/
define(function(require, exports, module) {
//===================================================================
    var ZUI = require("zui");
    module.exports = ZUI.def("ui.upload", {
        //...............................................................
        dom  : "ui/upload/upload.html",
        css  : "ui/upload/upload.css",
        i18n : "ui/upload/i18n/{{lang}}.js",
        //...............................................................
        init : function(options){
            //console.log("jjjjj init uploadd")
        },
        //...............................................................
        redraw : function() {
            // this.listenTo(this.model, "ui:ready",   this.wedit.on_ready);
            var UI = this;
            var opt = UI.options;
            var title = opt.title;
            // 上传到目录
            if(opt.target.race == 'DIR'){
                UI.$el.delegate(".ui-upload-arena", "dragover",  UI.multi.on_dragover);
                UI.$el.delegate(".ui-upload-arena", "dragleave", UI.multi.on_dragleave);
                UI.$el.find(".ui-upload-arena")[0].addEventListener("drop", UI.multi.on_drop, true);
                if(title === true || title == undefined)
                    title = UI.msg("upload.multi.sky");
                UI.arena.find(".ui-upload-arena").append(UI.ccode("multi.main"));
            }
            // 替换单个文件
            else if(UI.options.target.race == 'FILE'){
                UI.$el.delegate(".ui-upload-arena", "dragover",  UI.single.on_dragover);
                UI.$el.delegate(".ui-upload-arena", "dragleave", UI.single.on_dragleave);
                UI.$el.find(".ui-upload-arena")[0].addEventListener("drop", UI.single.on_drop, true);
                if(title === true || title == undefined)
                    title = UI.msg("upload.single.sky");
                UI.arena.find(".ui-upload-arena").append(UI.ccode("single.main"));
            }
            // 不可能
            else{
                throw "!!! wrong target race : '"+UI.options.target.race+"'";
            }
            // 更新标题 
            if(title){
                title = (_.template(title))(opt.target);
                UI.$el.find(".ui-upload-sky").html(title);
            }else{
                UI.$el.find(".ui-upload-sky").hide();
            }
        },
        depose : function(){
            //console.log("I am upload depose")
            var ele = this.$el.find(".ui-upload-arena")[0];
            ele.removeEventListener("drop", this.multi.on_drop, true);
            ele.removeEventListener("drop", this.single.on_drop, true);
        },
        //...............................................................
        events : {
            
        },
        //...............................................................
        resize : function(){
            var UI = this;
            var jSky    = UI.arena.find(".ui-upload-sky");
            var jMain   = UI.arena.find(".ui-upload-arena");
            var jFooter = UI.arena.find(".ui-upload-footer");
            var H       = UI.arena.height();
            var hSky    = jSky.outerHeight();
            var hFooter = jFooter.outerHeight(); 
            jMain.css("height", H - hSky - hFooter);
        },
        //...............................................................
        single : {
            on_dragover : function(e){
                e.stopPropagation();
                e.preventDefault();
                $(this).attr("drag","over");
            },
            on_dragleave : function(e){
                $(this).removeAttr("drag");
            },
            on_drop : function(e) {
                var UI = ZUI.checkInstance(this);
                var jSingle = $(this).removeAttr("drag").find(".ui-upload-single");
                e.stopPropagation();
                e.preventDefault();
                if("yes" == jSingle.attr("ing")) {
                    alert(UI.msg("upload.single.err_ing"));
                    return;
                }
                var UI = ZUI.checkInstance(this);
                // 只能允许一个文件
                if(e.dataTransfer.files.length != 1) {
                    alert(UI.msg("upload.single.err_multi"));
                    return;
                }
                // 得到这个文件
                var file = e.dataTransfer.files[0];
                // 开始上传
                jSingle.attr("ing", "yes");
                $z.uploadFile({
                    file : file,
                    progress : function(e){  
                        UI.updateProgress.call(UI, jSingle, e.loaded, e.total);
                    },
                    evalReturn : "ajax",
                    done : function(re){
                    },
                    fail : function(re){
                        alert(re.msg);
                    },
                    complete : function(re, status){
                        if("fail" == status)
                            alert(re.msg);
                        jSingle.attr("ing", "no")
                            .find(".inner").css("width", "1px");
                    },
                    // 下面是上传到服务器的目标设置
                    // 上传的目标url是一个字符串模板，会用本对象自身的键值来填充
                    url  : "/o/upload/id:"+UI.options.target.id
                                + "?nm={{file.name}}"
                                + "&sz={{file.size}}"
                                + "&mime={{file.type}}"
                });
            }
        },
        //...............................................................
        multi : {
            on_dragover : function(e){
                e.stopPropagation();
                e.preventDefault();
                $(this).attr("drag","over");
            },
            on_dragleave : function(e){
                $(this).removeAttr("drag");
            },
            on_drop : function(e) {
                $(this).removeAttr("drag");
                e.stopPropagation();
                e.preventDefault();
                var UI = ZUI.checkInstance(this);
                UI.multi.addFiles.call(UI, e.dataTransfer.files);
                UI.multi.doUpload.call(UI);
            },
            addFiles : function(files) {
                var jList = this.arena.find(".ui-upload-multi").empty();
                for(var i=0;i<files.length;i++){
                    var file = files[i];
                    console.log(file)
                    var jItem = this.ccode("list.item");
                    jItem.data("FILE", file).find(".fname").text(file.name);
                    jList.append(jItem);
                }
                this.resize();
            },
            doUpload : function(){
                var UI = this;
                var jItem  = this.arena.find(".ui-upload-item")
                                .not(".ui-upload-item-done")
                                .not(".ui-upload-item-fail")
                                .first();
                // 没有更多项目了，返回
                if(jItem.size()<=0) 
                    return;
                // 开始上传
                $z.uploadFile({
                    file : jItem.data("FILE"),
                    progress : function(e){  
                        UI.updateProgress.call(UI, jItem, e.loaded, e.total);
                    },
                    // 上传前修改提示图标的标签
                    beforeSend : function(xhr){
                        jItem.find(".thumbnail .fa").prop("className","fa fa-spinner fa-spin");
                        console.log(jItem.find(".rname .fa").size())
                        jItem.find(".remote .fa").addClass("fa-spin");
                    },
                    evalReturn : "ajax",
                    done : function(re){
                        jItem.addClass("ui-upload-item-done");
                        jItem.find(".thumbnail .fa").prop("className", "fa fa-check-circle");
                        jItem.find(".rname").text(re.nm);
                    },
                    fail : function(re){
                        jItem.addClass("ui-upload-item-fail");
                        jItem.find(".thumbnail .fa").prop("className", "fa fa-warning");
                        jItem.find(".rname").text(re.msg)
                    },
                    complete : function(re, status){
                        jItem.find(".remote .fa").removeClass("fa-spin");
                        UI.multi.doUpload.call(UI);
                    },
                    // 下面是上传到服务器的目标设置
                    // 上传的目标url是一个字符串模板，会用本对象自身的键值来填充
                    url  : "/o/upload/id:"+UI.options.target.id
                                + "?nm={{file.name}}"
                                + "&sz={{file.size}}"
                                + "&mime={{file.type}}"
                                + (UI.options.replaceable?"":"&dupp=${major}(${nb})${suffix}")
                });
            }
        },
        //...............................................................
        updateProgress : function(jq, loaded, total){
            var jOuter = jq.find(".outer");
            var jInner = jq.find(".inner");
            var jNb    = jq.find(".nb");
            var W = jOuter.width();
            var per = loaded / total;
            jNb.text(parseInt(per * 1000) / 10 + "%");
            jInner.css("width", W * per);
        }
    });
//===================================================================
});