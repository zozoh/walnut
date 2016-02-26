(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker/hmaker_nav'
], function(ZUI, Wn, NavUI){
//==============================================
var html = function(){/*
<div class="ui-arena hmaker" ui-fitparent="yes">
    <div class="hmaker-nav-con" mode="inside" ui-gasket="nav"></div>
    <div class="hmaker-main">
        <div class="ue-bar1">{{hmaker.comlib_add}}</div>
        <div class="ue-shelf"></div>
        <div class="ue-bar2">
            <div class="ue-ssize">
                <input name="x"><em>x</em><input name="y">
                <span>
                    <i class="fa fa-laptop highlight" val=""></i>
                    <i class="fa fa-tablet" val="800x600"></i>
                    <i class="fa fa-mobile" val="400x600"></i>
                </span>
            </div>
            <div class="ue-com-menu"></div>
        </div>
        <div class="ue-stage" mode="pc">
            <div class="ue-screen">abcdeadsfaafadsfafda</div>
        </div>
    </div>
    <div class="hmaker-deta">
        <div class="ue-com-title"></div>
        <div class="ue-com-prop"></div>
    </div>
</div>
*/};
//==============================================
return ZUI.def("app.wn.bp_ide", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "theme/app/wn.hmaker/hmaker.css",
    i18n : "app/wn.hmaker/i18n/{{lang}}.js",
    //...............................................................
    events : {
        "click .ue-bar1" : function(e){
            this.arena.find(".hmaker-main").toggleClass("shelf-hide");
        },
        "click .ue-ssize i" : function(e){
            var jq = $(e.currentTarget);
            // 高亮
            jq.parent().children().removeClass("highlight");
            jq.addClass("highlight");
            // 设置
            this.updateScreenSize(jq.attr("val"));
        },
        "change .ue-ssize input" : function(e){
            var UI  = this;
            var jq  = $(e.currentTarget);
            var val = jq.val();
            // 没值，用默认的
            if(!val){
                jq.val(jq.attr("oldv") || "");
                return;
            }
            // 确保是数字 
            if(!/^\d+$/.test(jq.val())){
                alert(UI.msg("hmaker.e_nonb"));
                jq.val(jq.attr("oldv") || "");
                return;
            }
            // 设置吧
            var jIx = UI.arena.find(".ue-ssize input[name=x]");
            var jIy = UI.arena.find(".ue-ssize input[name=y]");
            var val = (jIx.val()*1) + "x" + (jIy.val()*1);
            this.updateScreenSize(val);
        }
    },
    //...............................................................
    updateScreenSize : function(val){
        var UI  = this;
        var jIx = UI.arena.find(".ue-ssize input[name=x]");
        var jIy = UI.arena.find(".ue-ssize input[name=y]");
        var jSt = UI.arena.find(".ue-stage");
        var jSc = jSt.children(".ue-screen");
        // 限制宽高
        if(val) {
            jSt.attr("mode", "mobile");
            var m = /^(\d+)x(\d+)$/.exec(val);
            var w = m[1] * 1;
            var h = m[2] * 1;
            jSc.css({
                width  : w,
                height : h
            });
            jIx.val(w).attr("oldv",w); jIy.val(h).attr("oldv",h);
        }
        // 全屏模式
        else {
            jSt.attr("mode", "pc");
            jSc.css({
                width  : "",
                height : ""
            });
            jIx.val("").removeAttr("oldv"); jIy.val("").removeAttr("oldv");
        }
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;

        var jNavC = UI.arena.children(".hmaker-nav-con");
        var jMain = UI.arena.children(".hmaker-main");
        var jDeta = UI.arena.children(".hmaker-deta");

        // 指定了外部的大纲视图，自己内部的大纲视图就删掉吧
        if(opt.outline && opt.outline.size()>0) {
            jNavC.attr("mode", "outside").appendTo(opt.outline.empty());
            UI.addElement(opt.outline);
            //console.log("outside outline:" + UI.$outline.html());
        }
        
        // 创建侧边栏 UI
        new NavUI({
            parent : UI,
            gasketName : "nav"
        }).render();
    },
    //...............................................................
    update : function(o) {
        var UI = this;
        //console.log("I am screen update:", o);

        
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);