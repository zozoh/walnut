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
        <div class="ly-b-sz">
            <b>{{hmaker.pos.width}}</b><em>auto</em>
        </div>
        <div class="ly-a">
            <div move="prev" balloon="up:hmaker.com._.move_left"><i class="zmdi zmdi-long-arrow-left"></i></div>
            <div move="next" balloon="up:hmaker.com._.move_right"><i class="zmdi zmdi-long-arrow-right"></i></div>
            <div class="ly-a-del" balloon="up:hmaker.com._.del"><i class="zmdi zmdi-delete"></i></div>
        </div>
    </div>
</div>
<div class="ui-arena hmc-columns-prop" ui-fitparent="yes">
    <section class="crp-layout">
        <div class="ly-wrapper"></div>
        <div class="ly-newer"><i class="zmdi zmdi-plus"></i></div>
    </section>
</div>`;
//==============================================
return ZUI.def("app.wn.hm_com_columns_prop", {
    dom  : html,
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    events : {
        // 添加新栏
        'click .ly-newer' : function(e) {
            this.uiCom.addBlock();
        },
        // 移动
        'click .ly-a [move]' : function(e){
            var jq = $(e.currentTarget);
            var seq = jq.closest(".ly-block").attr("seq") * 1;
            this.uiCom.moveBlock(seq, jq.attr("move"));
        },
        // 删除
        'click .ly-a-del' : function(e){
            var jq = $(e.currentTarget);
            var seq = jq.closest(".ly-block").attr("seq") * 1;
            this.uiCom.delBlock(seq);
        },
        // 修改宽度 
        'click .ly-b-sz em' : function(e) {
            var UI = this;
            var jq = $(e.currentTarget);
            var seq = jq.closest(".ly-block").attr("seq") * 1;

            console.log("hahah")

            $z.editIt(jq, function(newval, oldval) {
                var val = $.trim(newval) || "auto";
                if(val && val!=oldval) {
                    // 看看值是否合法，合法就进行后续处理
                    var m = /^(([\d.]+)(px)?(%)?|auto)$/.exec(val);
                    if(m) {
                        // 如果没有单位自动补上 px
                        if(m[2] && !m[3] && !m[4]){
                            val += "px";
                        }
                        // 后续处理
                        UI.uiCom.setBlockWidth(seq, val);
                    }
                }
            });
        }
    },
    //...............................................................
    update : function(com) {
        var UI = this;

        // 首先绘制块
        var jLyW  = UI.arena.find(".ly-wrapper").empty();
        var datas = UI.uiCom.getBlockDataArray();
        for(var b of datas) {
            var jBlock = UI.ccode("block").appendTo(jLyW);
            jBlock.attr("seq", b.seq);
            jBlock.find(".ly-b-nm").text("B" + b.seq);
            jBlock.find(".ly-b-sz em").text(b.width);
        }

        // 显示提示
        UI.balloon();

        // 最后在调用一遍 resize
        //UI.resize(true);
    },
    //...............................................................
    resize : function() {
        var UI = this;
        var jLayout = UI.arena.find(".crp-layout");
        var jForm   = UI.arena.find(".crp-form");

        var H = UI.arena.height();
        jForm.css({
            "height" : H - jLayout.outerHeight(true)
        });
    }
});
//===================================================================
});
})(window.NutzUtil);