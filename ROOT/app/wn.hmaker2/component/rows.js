(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hmc-rows"></div>
*/};
//==============================================
return ZUI.def("app.wn.hm_com_rows", {
    //...............................................................
    init : function(){
        HmComMethods(this);
    },
    //...............................................................
    redraw : function() {
        var UI = this;

        // 设置初始值的 DOM 结构
        if(!UI.arena.hasClass("ui-arena")){
            UI.arena = $($z.getFuncBodyAsStr(html, true)).appendTo(UI.$el.empty());
        }

        // 如果发现主区域没有任何一块，主动添加一个
        if(UI.arena.children(".hmc-row-block").size() == 0) {
            $('<div class="hmc-row-block">').appendTo(UI.arena.empty());
        }

        // 查找并记录最大的栏目序号
        UI.__max_block_seq = 0;
        var will_assign = [];
        UI.arena.children(".hmc-row-block").each(function(){
            var seq = $(this).attr("row-b-seq") * 1;
            if(seq)
                UI.__max_block_seq = Math.max(UI.__max_block_seq, seq * 1);
            else
                will_assign.push(this);
        });

        // 未分配的序号也分配一下
        for(var i=0; i<will_assign.length; i++){
            $(will_assign[i]).attr("row-b-seq", ++UI.__max_block_seq);
        }

    },
    //...............................................................
    // 查询自己有几个块，返回一个数组作为标识
    getBlockSeqArray : function() {
        var re = [];
        this.arena.children(".hmc-row-block").each(function(){
            re.push($(this).attr("row-b-seq") * 1);
        });
        return re;
    },
    //...............................................................
    getBlock : function(seq) {
        return this.arena.children('.hmc-row-block[row-b-seq="'+seq+'"]');
    },
    //...............................................................
    addBlock : function() {
        var UI = this;

        var jBlock = $('<div class="hmc-row-block">').appendTo(UI.arena)
            .attr("row-b-seq", ++UI.__max_block_seq);

        $z.blinkIt(jBlock);

        UI.notifyChange();
    },
    //...............................................................
    delBlock : function(seq) {
        var UI = this;

        var jBlock = UI.getBlock(seq);

        $z.removeIt(jBlock, function(){
            UI.notifyChange();
        });
    },
    //...............................................................
    // direction : "prev" || "next"
    moveBlock : function(seq, direction) {
        direction = direction || "next";
        
        var jBlock = this.getBlock(seq);

        // 向前
        if("prev" == direction) {
            var jPrev = jBlock.prev();
            if(jPrev.size() > 0) {
                jBlock.insertBefore(jPrev);
            }
        }
        // 向后
        else {
            var jNext = jBlock.next();
            if(jNext.size() > 0) {
                jBlock.insertAfter(jNext);
            }
        }

        this.notifyChange();
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    setupProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/rows_prop.js',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);