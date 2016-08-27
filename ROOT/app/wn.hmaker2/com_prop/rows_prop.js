(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com',
    'ui/form/form'
], function(ZUI, Wn, HmComMethods, FormUI){
//==============================================
var html = `
<div class="ui-code-template">
    <div code-id="block" class="ly-block">
        <div class="ly-b-nm">??</div>
        <div class="ly-a">
            <div class="ly-a-del"><i class="zmdi zmdi-delete"></i> {{hmaker.com._.del}}</div>
            <div move="next"><i class="zmdi zmdi-long-arrow-down"></i> {{hmaker.com._.move_down}}</div>
            <div move="prev"><i class="zmdi zmdi-long-arrow-up"></i> {{hmaker.com._.move_up}}</div>
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
        HmComMethods(this);
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
        }
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
        var bSeqs = UI.uiCom.getBlockSeqArray();
        for(var i=0; i<bSeqs.length; i++) {
            UI.ccode("block").appendTo(jLyW)
                .attr("seq", bSeqs[i])
                    .find(".ly-b-nm")
                    .text("B" + bSeqs[i]);
        }

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