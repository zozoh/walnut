/*
在 $pel 内创建一组 DOM，用来提供用户的文件上传界面
对应的 options 可以有以下选项
..............................................................
目标，下面两个值，targetId 更优先。如果目标是一个文件夹，则在其内创建
如果目标是一个文件，则看模块的
 - target   : 上传的目标对象的 ID

校验
 - name : "$REGEX"  # 名称的正则表达式校验
 - type : "$REGEX"  # 类型的校验
 - mime : "$REGEX"  # 内容类型的校验

*/
define(function(require, exports, module) {
//===================================================================
    var ZUI = require("zui");
    module.exports = ZUI.def("ui.uploader", {
        //...............................................................
        dom  : "ui/uploader/uploader.html",
        css  : "ui/uploader/uploader.css",
        i18n : "ui/uploader/i18n/{{lang}}.js",
        //...............................................................
        init : function(options){
            console.log("jjjjj init uploaderd")
        },
        //...............................................................
        redraw : function() {
            // this.listenTo(this.model, "ui:ready",   this.wedit.on_ready);
            console.log("I am uploader redraw")
            this.$el.delegate(".ui-uploader-list", "dragover",  this.handlers.on_dragover);
            this.$el.delegate(".ui-uploader-list", "dragleave", this.handlers.on_dragleave);
            this.$el.find(".ui-uploader-list")[0]
                .addEventListener("drop", this.handlers.on_drop, false);
            console.log(this)
            this.$el.find(".ui-loader-dest").text(this.options.target);
        },
        depose : function(){
            console.log("I am uploader depose")
            this.$el.find(".ui-uploader-list")[0]
                .removeEventListener("drop", this.handlers.on_drop, true);
        },
        //...............................................................
        events : {
            "click .ui-uploader-choose" : function() {
                alert(this.msg("e.noimplement"));
            },
            "click .ui-uploader-done" : function() {
                if(this.parent)
                    this.parent.destroy();
            }
        },
        //...............................................................
        resize : function(){
            // var W = parseInt(this.regions.list.width() * 0.8);
            // console.log(W)
            // this.regions.list.find(".ui-uploader-item .progress")
            //     .each(function(){
            //     $(this).css("width", W);
            // });
        },
        //...............................................................
        handlers : {
            on_dragover : function(e){
                e.stopPropagation();
                e.preventDefault();
                $(this).addClass("ui-uploader-list-dragover");
            },
            on_dragleave : function(e){
                $(this).removeClass("ui-uploader-list-dragover");
            },
            on_drop : function(e) {
                $(this).removeClass("ui-uploader-list-dragover");
                e.stopPropagation();
                e.preventDefault();
                var UI = ZUI.checkInstance(this);
                UI.uploader.addFiles.call(UI, e.dataTransfer.files);
                UI.uploader.doUpload.call(UI);
            }
        },
        //...............................................................
        uploader : {
            addFiles : function(files) {
                this.arena.find(".ui-uploader-tip").remove();
                for(var i=0;i<files.length;i++){
                    var file = files[i];
                    console.log(file)
                    var jItem = this.ccode("list.item");
                    jItem.data("FILE", file).find(".fname").text(file.name);
                    this.arena.find(".ui-uploader-list").append(jItem);
                }
                this.resize();
            },
            doUpload : function(){
                var UI = this;
                var jItem  = this.arena.find(".ui-uploader-item")
                                .not(".ui-uploader-item-done")
                                .not(".ui-uploader-item-fail")
                                .first();
                // 没有更多项目了，返回
                if(jItem.size()<=0) 
                    return;
                // 开始上传
                var file = jItem.data("FILE");
                var xhr = new XMLHttpRequest();
                // 检查
                if (!xhr.upload) {
                    throw "XMLHttpRequest object don't support upload for your browser!!!";
                }
                // 进度回调
                xhr.upload.addEventListener("progress", function(e) {
                    UI.uploader.updateProgress.call(UI, jItem, e.loaded, e.total);
                }, false);
                // 完成的处理
                xhr.onreadystatechange = function(e) {
                    if (xhr.readyState == 4) {
                        // 上传成功，标记完成，并处理下一个项目
                        if (xhr.status == 200) {
                            jItem.addClass("ui-uploader-item-done");
                            jItem.find(".thumbnail .fa").prop("className", "fa fa-check-circle");
                            UI.uploader.doUpload.call(UI);
                        }
                        // 出错处理
                        else {
                            jItem.addClass("ui-uploader-item-fail");
                            jItem.find(".thumbnail .fa").prop("className", "fa fa-warning");
                            alert('Fail to upload "' + file.name + '"\n\n' + xhr.responseText);
                        }
                    }
                };
                // 准备请求对象头部信息
                var url = "/o/upload/"+this.options.target
                                + "?nm="   + file.name
                                + "&sz="   + file.size
                                + "&mime=" + file.type
                xhr.open("POST", url, true);
                xhr.setRequestHeader('Content-type', "application/x-www-form-urlencoded; charset=utf-8");
                // 修改提示图标的标签
                jItem.find(".thumbnail .fa").prop("className", "fa fa-spinner fa-spin");
                // 执行上传
                xhr.send(file);
            },
            updateProgress : function(jItem, loaded, total){
                var jOuter = jItem.find(".outer");
                var jInner = jItem.find(".inner");
                var jNb    = jItem.find(".nb");
                var W = jOuter.width();
                var per = loaded / total;
                jNb.text(parseInt(per * 1000) / 10 + "%");
                jInner.css("width", W * per);
            }
        }
        //...............................................................
    });
//===================================================================
});