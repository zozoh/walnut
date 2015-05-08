define(function(require, exports, module) {
//=======================================================================
    var ZUI = require("zui");
    module.exports = ZUI.def("ui.wedit", {
        dom  : "ui/wedit/wedit.html",
        css  : "ui/wedit/wedit.css",
        i18n : "ui/wedit/i18n/{{lang}}.js",
        init : function(options) {
            //console.log("I am wedit init")
            this.listenModel("change:obj" ,this.wedit.on_change_obj);
            this.listenModel("save:start" ,this.wedit.on_save_start);
            this.listenModel("save:done"  ,this.wedit.on_save_done);
            this.listenModel("save:fail"  ,this.wedit.on_save_fail);
            this.listenModel("do:upload"  ,this.on_do_upload);
        },
        //...............................................................
        events : {
            // "click .ui-wedit-textarea"     : function(){
            //     var Mod = this.model;
            //     var obj  = Mod.get("obj");
            //     console.log(obj)
            //     Mod.set("obj", {x:1,y:1});
            // }
            // "keydown  .ui-console-inbox"     : on_keydown_at_inbox
            "click .ui-wedit-save" : function(){
                var Mod = this.model;
                var obj = Mod.get("obj");
                var content = this.arena.find('.ui-wedit-textarea').val();
                Mod.set("content", content);
            },
            "click .ui-wedit-abc" : function(){
                // var Mod = this.model;
                // require(["ui/mask/mask", "ui/uploader/uploader"], function(UIMask, UIUploader){
                //     new UIUploader({
                //         //model      : Mod,
                //         target     : "id:" + Mod.get("obj").pid,
                //         parent     : new UIMask({width:500, height: 600}),
                //         gasketName : "main"
                //     });
                // });
                var Mod = this.model;
                var obj = Mod.get("obj");
                Mod.trigger("cmd:exec", "upload -id "+obj.id);
            }
        },
        redraw : function(){
            var UI  = this;
            var Mod = UI.model;
            var obj = Mod.get("obj");
            Mod.trigger("cmd:exec", "cat id:"+obj.id, function(re){
                UI.wedit.on_redraw_content.call(UI,re);
            });
            Mod.trigger("change:obj", Mod, obj, {});
            //$(".ui-wedit-abc").click();
        },
        //...............................................................
        resize : function(){
        },
        //...............................................................
        wedit : {
            on_redraw_content : function(content) {
                //console.log("on_redraw_content: " + content)
                this.arena.find('.ui-wedit-textarea').val(content);
            },
            on_change_obj : function(Mod, obj){
                this.arena.find('.ui-wedit-title .ui-tt').text(obj.nm);
                this.arena.find('.ui-wedit-footer').text(obj.ph);
            },
            on_save_start : function(){
                var jq = this.arena.find(".ui-wedit-save .fa");
                jq.removeClass("fa-save fa-warning").addClass("fa-spinner fa-spin");
            },
            on_save_done : function(){
                var jq = this.arena.find(".ui-wedit-save .fa");
                jq.removeClass("fa-spinner fa-spin").addClass("fa-check");
                window.setTimeout(function(){
                    jq.removeClass("fa-check").addClass("fa-save");
                },1000);
            },
            on_save_fail : function(){
                var jq = this.arena.find(".ui-wedit-save .fa");
                jq.removeClass("fa-spinner fa-spin").addClass("fa-warning");
            }
        }
        //...............................................................
    });
//===================================================================
});
