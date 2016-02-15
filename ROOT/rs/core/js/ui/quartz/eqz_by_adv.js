(function($z){
$z.declare([
    'zui',
    "ui/quartz/quartz",
], function(ZUI, Quartz){
//==============================================
var html = function(){/*
<div class="ui-arena qz-adv" ui-fitparent="yes">
    <div class="qz-explain"></div>
    <section>
        <div class="qz-adv-st">
            <i class="fa fa-warning"></i>
            <i class="fa fa-check"></i>
        </div>
        <input>
    </section>
    <div class="qz-adv-tip"><%=quartz.adv_tip%></div>
</div>
*/};
//==============================================
return ZUI.def("ui.quartz_by_adv", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    events : {
        "change input" : function(){
            var UI = this;

            var jSec   = UI.arena.find("section");
            var jInput = jSec.find("input");
            var qzs = $.trim(jInput.val());

            // 尝试解析
            try{
                var qz = Quartz(qzs);
                jSec.removeClass("invalid");
                jInput.attr("old-val", qzs);
                UI._update_explain();

            }catch(E){
                jSec.addClass("invalid");
                UI.arena.find(".qz-explain").text(UI.msg("quartz.adv_invalid"));
            }
        }
    },
    //...............................................................
    _update_explain : function(qz) {
        var UI = this;
        qz = Quartz(qz || UI.getData());
        UI.arena.find(".qz-explain").text(qz.toText(UI.msg("quartz.exp")));
    },
    //...............................................................
    setData : function(qz){
        var UI = this;
        qz = Quartz(qz);

        // 显示说明
        UI._update_explain(qz);

        var qzs = qz.toString();
        UI.arena.find("input").val(qzs).attr("old-val", qzs);
    },
    //...............................................................
    getData : function(){
        var UI = this;
        var jSec   = UI.arena.find("section");
        var jInput = jSec.find("input");

        if(jSec.hasClass("invalid"))
            return jInput.attr("old-val");

        return jInput.val();
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);