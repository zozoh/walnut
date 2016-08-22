(function($z){
$z.declare([
    'zui',
    'wn/util',
    'app/wn.hmaker2/hm__methods_com'
], function(ZUI, Wn, HmComMethods){
//==============================================
var html = function(){/*
<div class="ui-arena hmc-columns"></div>
*/};
//==============================================
return ZUI.def("app.wn.hm_com_columns", {
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
        if(UI.arena.children(".hmc-col-block").size() == 0) {
            $('<div class="hmc-col-block" hm-droppable="yes">').appendTo(UI.arena.empty());
        }

        // 查找并记录最大的栏目序号
        UI.__max_block_seq = 0;
        var will_assign = [];
        UI.arena.children(".hmc-col-block").each(function(){
            var seq = $(this).attr("col-b-seq") * 1;
            if(seq)
                UI.__max_block_seq = Math.max(UI.__max_block_seq, seq * 1);
            else
                will_assign.push(this);
        });

        // 未分配的序号也分配一下
        for(var i=0; i<will_assign.length; i++){
            $(will_assign[i]).attr("col-b-seq", ++UI.__max_block_seq);
        }

    },
    //...............................................................
    // 查询自己有几个块，返回一个数组作为标识
    getBlockDataArray : function() {
        var UI = this;
        var re = [];
        this.arena.children(".hmc-col-block").each(function(){
            re.push(UI.getBlockData(this));
        });
        return re;
    },
    //...............................................................
    getBlockData : function(seq) {
        var jBlock = this.getBlock(seq);
        return {
            seq    : jBlock.attr("col-b-seq") * 1,
            width  : jBlock.attr("col-b-width") || "auto"
        };
    },
    //...............................................................
    getBlock : function(seq) {
        if(_.isElement(seq) || $z.isjQuery(seq))
            return $(seq);
        return this.arena.children('.hmc-col-block[col-b-seq="'+seq+'"]');
    },
    //...............................................................
    setBlockWidth : function(seq, width) {
        var UI = this;
        var jBlock = UI.getBlock(seq);
        jBlock.attr("col-b-width", width);

        UI.__update_block_style(jBlock);

        UI.notifyChange();
    },
    //...............................................................
    addBlock : function() {
        var UI = this;

        var jBlock = $('<div class="hmc-col-block" hm-droppable="yes">').appendTo(UI.arena)
            .attr("col-b-seq", ++UI.__max_block_seq);

        $z.blinkIt(jBlock);

        UI.notifyChange();
    },
    //...............................................................
    delBlock : function(seq) {
        this.getBlock(seq).remove();
        this.notifyChange();
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

        // 闪动一下做个标记
        $z.blinkIt(jBlock);

        // 通知其他控件更新
        this.notifyChange();
    },
    //...............................................................
    __update_block_style : function(seq) {
        var UI = this;
        var jBlock = UI.getBlock(seq);

        var width = jBlock.attr("col-b-width") || "auto";
        var css = {
            "width" : "", 
            "flex"  : ""
        }
        if("auto" != width) {
            css.width = width;
            css.flex  = "0 0 auto";
        }
        jBlock.css(css);
    },
    //...............................................................
    paint : function(com) {
        var UI = this;

        // 更新每个块的显示
        UI.arena.children(".hmc-col-block").each(function(){
            UI.__update_block_style(this);
        });
    },
    //...............................................................
    // 返回属性菜单， null 表示没有属性
    setupProp : function(){
        return {
            uiType : 'app/wn.hmaker2/com_prop/columns_prop.js',
            uiConf : {}
        };
    }
});
//===================================================================
});
})(window.NutzUtil);