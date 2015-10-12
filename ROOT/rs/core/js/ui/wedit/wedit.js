(function($z){
$z.declare('zui', function(ZUI){
//==============================================
var html = function(){/*
<div class="ui-arena">
    <div  class="ui-wedit-sky">
        <div class="ui-wedit-title">
            <i class="fa fa-file-text"></i>
            <span class="ui-tt"></span>
        </div>
        <div class="ui-wedit-actions">
            <b class="ui-btn ui-wedit-save">
                <i class="fa fa-save"></i><span>{{save}}</span>
            </b>
        </div>
    </div>
    <div class="ui-wedit-main">
        <textarea class="ui-wedit-textarea"></textarea>
    </div>
    <div class="ui-wedit-footer"></div>
</div>
*/};
//==============================================
return ZUI.def("ui.wedit", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
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
            var UI  = this;
            var Mod = UI.model;
            var obj = Mod.get("obj");
            var content = UI.arena.find('.ui-wedit-textarea').val();
            // 写入元数据
            if(UI.$el.attr("meta")){
                var o2 = $z.fromJson(content);
                o2.id = obj.id;
                Mod.set("obj", o2);
                Mod.trigger("update:obj");
            }
            // 写入内容
            else{
                Mod.set("content", content);
            }
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
        UI.$el.attr("meta", obj.__obj_meta_rw);
        // 读元数据
        if(UI.$el.attr("meta")){
            Mod.trigger("cmd:exec", "obj id:"+obj.id, function(re){
                UI.wedit.on_redraw_content.call(UI,re);
            });
        }
        // 读内容
        else{
            Mod.trigger("cmd:exec", "cat id:"+obj.id, function(re){
                UI.wedit.on_redraw_content.call(UI,re);
            });
        }
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
    //...........................................................
});
//===================================================================
});
})(window.NutzUtil);