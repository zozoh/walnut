define(function(require, exports, module) {
//=======================================================================
    var ZUI = require("zui");
    module.exports = ZUI.def("ui.oedit", {
        dom  : "ui/oedit/oedit.html",
        css  : "ui/oedit/oedit.css",
        i18n : "ui/oedit/i18n/{{lang}}.js",
        init : function(options) {
            //console.log("I am oedit init")
            this.listenModel("save:start" ,this.oedit.on_save_start);
            this.listenModel("save:done"  ,this.oedit.on_save_done);
            this.listenModel("save:fail"  ,this.oedit.on_save_fail);
            this.listenModel("change:obj" ,this.oedit.on_change_obj);
        },
        //...............................................................
        events : {
            "click .ui-oedit-save" : function(){
                 var Mod = this.model;
                var str = this.arena.find('.ui-oedit-main textarea').val();
                var obj = $z.fromJson(str);
                Mod.set("obj", obj);
                Mod.trigger("update:obj");
            }
        },
        redraw : function(){
            var Mod = this.model;
            var obj = Mod.get("obj");
            Mod.trigger("cmd:exec", "cat -id "+obj.id);
            Mod.trigger("change:obj", Mod, obj, {});
            //this.gasket.actions.find('.ui-oedit-abc').click();         
        },
        //...............................................................
        resize : function(){
            console.log("I am oedit resize")
            var jMain = this.arena.find('.ui-oedit-main');
            var w = jMain.width();
            var h = jMain.height();
            jMain.find('textarea').css({
                width : w,
                height : h
            })
        },
        //...............................................................
        oedit : {
            on_change_obj : function(Mod, obj){
                var Mod = this.model;
                var obj = Mod.get("obj");
                var str = $z.toJson(obj, null, 4);
                this.arena.find('.ui-oedit-textarea').val(str);
                this.arena.find('.ui-oedit-title .ui-tt').text(obj.nm);
                this.arena.find('.ui-oedit-footer').text(obj.path);
            },
            on_save_start : function(){
                var jq = this.arena.find(".ui-oedit-save .fa");
                jq.removeClass("fa-save fa-warning").addClass("fa-spinner fa-spin");
            },
            on_save_done : function(){
                var jq = this.arena.find(".ui-oedit-save .fa");
                jq.removeClass("fa-spinner fa-spin").addClass("fa-check");
                window.setTimeout(function(){
                    jq.removeClass("fa-check").addClass("fa-save");
                },1000);
            },
            on_save_fail : function(){
                var jq = this.arena.find(".ui-oedit-save .fa");
                jq.removeClass("fa-spinner fa-spin").addClass("fa-warning");
            }
        }
        //...............................................................
    });
//===================================================================
});
