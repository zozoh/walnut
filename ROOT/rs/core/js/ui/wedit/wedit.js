(function($z){
$z.declare(['zui', 'wn/util'], function(ZUI, Wn){
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
            var obj = UI.app.obj;
            var content = UI.arena.find('.ui-wedit-textarea').val();
            UI.on_save_start();
            // 写入元数据
            if(UI.$el.attr("meta")){
                var o2 = $z.fromJson(content);
                o2.id = obj.id;
                _.extend(UI.app.obj, o2);
                $.ajax({
                    type: "POST",
                    url: "/o/set/id:" + obj.id,
                    contentType: "application/jsonrequest",
                    data: $z.toJson(o2)
                }).done(function (re) {
                    UI.on_change_obj(UI.app.obj);
                    UI.on_save_done();
                }).fail(function (re) {
                    UI.on_save_fail();
                    throw "fail to save!";
                });
            }
            // 写入内容
            else{
                $.ajax({
                    type: "POST",
                    url: "/o/write/id:" + obj.id,
                    data: content
                }).done(function (re) {
                    UI.on_save_done();
                }).fail(function (re) {
                    UI.on_save_fail();
                    throw "fail to save!";
                });
            }
        }
    },
    redraw : function(){
        var UI  = this;
        var obj = UI.app.obj;
        UI.$el.attr("meta", obj.__obj_meta_rw);
        // 读元数据
        if(UI.$el.attr("meta")){
            Wn.exec("obj id:"+obj.id, function(re){
                UI.app.obj = $z.fromJson(re);
                UI.on_redraw_content(re);
                UI.on_change_obj(UI.app.obj);
            });
        }
        // 读内容
        else{
            UI.on_change_obj(UI.app.obj);
            Wn.exec("cat id:"+obj.id, function(re){
                UI.on_redraw_content(re);
            });
        }
        //$(".ui-wedit-abc").click();
    },
    //...............................................................
    resize : function(){
    },
    //...............................................................
    on_redraw_content : function(content) {
        //console.log("on_redraw_content: " + content)
        this.arena.find('.ui-wedit-textarea').val(content);
    },
    on_change_obj : function(obj){
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
    //...........................................................
});
//===================================================================
});
})(window.NutzUtil);