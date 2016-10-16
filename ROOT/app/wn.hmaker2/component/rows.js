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
            $('<div class="hmc-row-block" hm-droppable="yes">').appendTo(UI.arena.empty());
        }

        // 查找并记录最大的栏目序号
        UI.__max_block_seq = 0;
        var will_assign = [];
        UI.arena.children(".hmc-row-block").each(function(){
            var jBlock = $(this);
            // 确保有序号
            var seq = $(this).attr("row-b-seq") * 1;
            if(seq){
                UI.__max_block_seq = Math.max(UI.__max_block_seq, seq * 1);
                // 确保有 ID
                var bid = jBlock.attr("row-b-id");
                if(!bid) {
                    jBlock.attr("row-b-id", "B" + seq);
                }
            }
            // 延迟分配
            else {
                will_assign.push(this);
            }
        });

        // 未分配的序号也分配一下
        for(var i=0; i<will_assign.length; i++){
            var jBlock = $(will_assign[i]);
            // 分配 seq
            var seq = ++UI.__max_block_seq;
            jBlock.attr("row-b-seq", seq);
            // 确保有 ID
            var bid = jBlock.attr("row-b-id");
            if(!bid) {
                jBlock.attr("row-b-id", "B" + seq);
            }
        }

    },
    //...............................................................
    // 查询自己有几个块，返回一个数组作为标识
    getBlockDataArray : function() {
        var UI = this;
        var re = [];
        UI.arena.children(".hmc-row-block").each(function(){
            re.push(UI.getBlockData(this));
        });
        return re;
    },
    //...............................................................
    getBlockData : function(seq) {
        var jBlock = this.getBlock(seq);
        return {
            bid    : jBlock.attr("row-b-id"),
            seq    : jBlock.attr("row-b-seq") * 1,
            highlight : jBlock.attr("highlight") ? "yes" : null,
        };
    },
    //...............................................................
    getBlock : function(seq) {
        if(_.isElement(seq) || $z.isjQuery(seq))
            return $(seq).closest(".hmc-row-block");
        // 根据 ID
        if(_.isString(seq)){
            this.arena.children('.hmc-row-block[row-b-id="'+seq+'"]');
        }
        // 根据序列号
        return this.arena.children('.hmc-row-block[row-b-seq="'+seq+'"]');
    },
    //...............................................................
    addBlock : function() {
        var UI = this;

        // 分配序列号
        var seq = ++UI.__max_block_seq;

        // 插入 DOM
        var jBlock = $('<div class="hmc-row-block" hm-droppable="yes">').appendTo(UI.arena)
            .attr({
                "row-b-seq" : seq,
                "row-b-id"  : "B" + seq,
            });

        // 显示效果
        $z.blinkIt(jBlock);

        // 通知
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
    setBlockHighlight : function(seq) {
        var UI = this;
        var jBlock = (false === seq ? null : UI.getBlock(seq));
        // 先取消全部块高亮标记
        UI.arena.find(">.hmc-row-block").removeAttr("highlight");
        // 设置某个块高亮，那么其他的块就需要虚掉
        if(jBlock && jBlock.length > 0) {
            UI.arena.attr("highlight-mode", "yes");
            jBlock.attr("highlight", "yes");
        }
        // 否则就是表示取消全部高亮
        else {
            UI.arena.removeAttr("highlight-mode");
        }
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