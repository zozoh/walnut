(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_panel',
    'ui/form/form'
], function(ZUI, Wn, HmMethods, FormUI){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="block" class="ly-block">
        <div class="ly-b-nm">??</div>
        <div class="ly-a">
            <div class="ly-a-del" balloon="up:hmaker.com._.del"><i class="zmdi zmdi-delete"></i></div>
            <div move="next" balloon="up:hmaker.com._.move_down"><i class="zmdi zmdi-long-arrow-down"></i></div>
            <div move="prev" balloon="up:hmaker.com._.move_up"><i class="zmdi zmdi-long-arrow-up"></i></div>
        </div>
    </div>
</div>
<div class="ui-arena hmc-rows-prop" ui-fitparent="yes">
    <section class="crp-layout">
        <div class="ly-wrapper"></div>
        <div class="ly-newer"><i class="zmdi zmdi-plus"></i></div>
    </section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_rows_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        HmMethods(this);
    },
    //...............................................................
    events : {
        'click .ly-newer' : function(e) {
            this.uiCom.addBlock();
        },
        'click .ly-a [move]' : function(e){
            var jq = $(e.currentTarget);
            var seq = jq.closest(".ly-block").attr("seq") * 1;
            this.uiCom.moveBlock(seq, jq.attr("move"));
        },
        'click .ly-a-del' : function(e){
            var jq = $(e.currentTarget);
            var seq = jq.closest(".ly-block").attr("seq") * 1;
            this.uiCom.delBlock(seq);
        },
        // 高亮/取消对应的栏
        'click .ly-block' : function(e) {
            var jMe = $(e.target);
            // 动作按钮不会触发本行为
            if(jMe.closest(".ly-a").length > 0){
                return;
            }
            // 准备数据
            var UI = this;
            var jBlock = jMe.closest(".ly-block");
            var seq = jBlock.attr("seq") * 1;
            // 取消高亮
            if(jBlock.attr("highlight")){
                jBlock.removeAttr("highlight");
                UI.uiCom.setBlockHighlight(false);
            }
            // 高亮自己
            else {
                UI.arena.find(".ly-block").removeAttr("highlight");
                jBlock.attr("highlight", "yes");
                UI.uiCom.setBlockHighlight(seq);
            }
        }
    },
    //...............................................................
    depose : function(){
        this.uiCom.setBlockHighlight(false);
    },
    // //...............................................................
    // redraw : function() {
    //     var UI = this;

    //     new FormUI({
    //         parent : UI,
    //         gasketName : "form",
    //         fields : [{
    //             key    : 'fixwidth',
    //             title  : 'i18n:hmaker.com.rows.fixwidth',
    //             tip    : 'i18n:hmaker.com.rows.fixwidth_tip',
    //             uiWidth : 60,
    //             type   : 'string',
    //             editAs : 'input',
    //         }, {
    //             key    : 'fixwidth',
    //             title  : 'i18n:hmaker.com.rows.padding',
    //             uiWidth : 60,
    //             type   : 'string',
    //             editAs : 'input',
    //             uiConf : {
    //                 unit : "px"
    //             }
    //         }]
    //     }).render(function(){
    //         UI.defer_report("form");
    //     });
        
    //     return "form";
    // },
    //...............................................................
    update : function(com) {
        var UI = this;

        // 首先绘制块
        var jLyW  = UI.arena.find(".ly-wrapper").empty();
        var datas = UI.uiCom.getBlockDataArray();
        for(var b of datas) {
            var jBlock = UI.ccode("block").appendTo(jLyW);
            jBlock.attr(_.pick(b, "seq", "bid", "highlight"));
            jBlock.find(".ly-b-nm").text(b.bid);
        }

        // 显示提示
        UI.balloon();

        // 最后在调用一遍 resize
        //UI.resize(true);
    },
    //...............................................................
    resize : function() {
        // var UI = this;
        // var jLayout = UI.arena.find(".crp-layout");
        // var jForm   = UI.arena.find(".crp-form");

        // var H = UI.arena.height();
        // jForm.css({
        //     "height" : H - jLayout.outerHeight(true)
        // });
    }
});
//===================================================================
});
})(window.NutzUtil);