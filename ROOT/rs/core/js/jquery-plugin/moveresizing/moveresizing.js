/*
在给定的元素上，停靠一个滑块条，可以拖动的方式改变数值
*/
(function($, $z){
//...........................................................
function Lv(rect) {
    return $z.tmpl("l:{{left}},r:{{right}},t:{{top}},b:{{bottom}},x:{{x}},y:{{y}}")(rect);
}
function Lxy(pos) {
    return $z.tmpl("x:{{x}},y:{{y}}")(pos);
}
//...........................................................
var html = $z.getFuncBodyAsStr(function(){/*
<div class="mvrz-ass">
    <div class="mvrza-grp" md="L">
        <div class="mvrza-hdl" hd="NW"></div>
        <div class="mvrza-hdl" hd="W"></div>
        <div class="mvrza-hdl" hd="SW"></div>
    </div>
    <div class="mvrza-grp" md="C">
        <div class="mvrza-hdl" hd="N"></div>
        <div class="mvrza-hdl" hd="S"></div>
    </div>
    <div class="mvrza-grp" md="R">
        <div class="mvrza-hdl" hd="NE"></div>
        <div class="mvrza-hdl" hd="E"></div>
        <div class="mvrza-hdl" hd="SE"></div>
    </div>
</div>
*/}, true);
//...........................................................
// 这个对象描述了手柄模式的计算方式
var HDLc = {
    NW : ["left", "top"],
    W  : ["left"],
    SW : ["left", "bottom"],
    N  : ["top"],
    S  : ["bottom"],
    NE : ["right", "top"],
    E  : ["right"],
    SE : ["right", "bottom"]
};
//...........................................................
function on_begin() {
    var opt = this.options;
    // console.log("开始时，记录一下移动的模式，是手柄还是整体移动");
    if(this.$trigger.hasClass("mvrza-hdl")){
        this.hdlMode = this.$trigger.attr("hd");
    }
    // 标识遮罩的特殊属性
    this.$mask.addClass("mvrz-mask");

    // 初始化块矩形
    this.$block = this.$trigger.closest('[mvrz-block]');
    this.$wrapper = this.$block.children().first();
    this.$ass = this.$wrapper.children(".mvrz-ass").first();
    this.$con = this.$ass.next();
    this.rect.block = $z.rect(this.$block);
    this.rect.blockInView = $z.rect_relative(this.rect.block, this.rect.viewport, true);

    // 调用回调
    $z.invoke(opt, "on_client_begin", [], this);
}
//...........................................................
function on_end() {
    var opt = this.options;
    $z.invoke(opt, "on_client_end", [], this);
}
//...........................................................
function on_ing() {
    // console.log("ing:", Lv(this.rect.trigger))
    // console.log("   atPos", Lxy(this.posAt));
    // 如果是手柄方式
    if(this.hdlMode) {
        // 计算顶点
        _.extend(this.rect.block, $z.rectObj(this.rect.trigger, HDLc[this.hdlMode]));

        // 重新计算矩形其他尺寸
        $z.rect_count_tlbr(this.rect.block);

        // 标识遮罩层熟悉
        this.$mask.attr("mvrz-hd", this.hdlMode);
    }
    // 默认的，就是移动自身咯
    else {
        _.extend(this.rect.block, this.rect.trigger);
        // 标识遮罩层熟悉
        this.$mask.attr("mvrz-hd", "move");
    }
    // 最后更新一下块的位置
    this.rect.blockInView = $z.rect_relative(this.rect.block, this.rect.viewport, true);
    $z.invoke(this.options, "updateBlockBy", [this.rect.blockInView], this);

    // 回调:改变尺寸
    if(this.hdlMode) {
        $z.invoke(this.options, "on_resize", [this.rect.blockInView], this);
    }
    // 回调:移动
    else {
        $z.invoke(this.options, "on_move", [this.rect.blockInView], this);
    }
    // 回调:改变
    $z.invoke(this.options, "on_change", [this.rect.blockInView], this);
}
//...........................................................
function format_trigger_dom(opt) {
    var jTri = $(this).attr("mvrz-block", "yes");
    var jW   = jTri.children().first();
    var jAss = jW.children(".mvrz-ass").first();
    if(jAss.size() == 0) {
        jAss = $(html).prependTo(jW);
    }
    if(opt.hdlZIndex > 0) {
        jAss.find(".mvrza-hdl").css("z-index", opt.hdlZIndex);
    }
}
//...........................................................
function find_trigger_element(e) {
    var jHdl = $(e.target).closest(".mvrza-hdl");
    // 嗯是要搞手柄 ...
    if(jHdl.size() > 0)
        return jHdl;
    // 默认就是移动整个块
    return $(this);
}
//...........................................................
$.fn.extend({ "moveresizing" : function(opt){
    // 销毁控件
    if("destroy" == opt){
        this.pmoving("destroy");
        return this;
    }

    // 主动格式化
    if("format" == opt) {
        var opt = this.data("@MVRZ_OPT");
        this.find(opt.trigger).each(function(){
            format_trigger_dom.call(this, opt);
        });    
        return;
    }

    // 确保有配置对象
    opt = opt || {};

    // 保存配置对象
    this.data("@MVRZ_OPT", opt);

    // 默认是自己的所有 children 被监视移动 
    $z.setUndefined(opt, "trigger", ">*");   // 自己也需要 trigger 的
    $z.setUndefined(opt, "anchorVertex", "top,left");

    // 默认修改 $block 的风格的逻辑
    if(!_.isNull(opt.updateBlockBy)) {
        opt.updateBlockBy = opt.updateBlockBy || "top,left,width,height";
        if(!_.isFunction(opt.updateBlockBy)) {
            opt.__update_block_by = opt.updateBlockBy;
            opt.updateBlockBy = function(rect) {
                this.$block.css($z.rectObj(
                    this.rect.blockInView, 
                    this.options.__update_block_by
                ));
            };
        }
    }

    // 为所有的 trigger 创建辅助节点
    this.moveresizing("format");

    // 保存用户自定义的 on_begin/on_end
    opt.on_client_begin = opt.on_begin;
    opt.on_client_end   = opt.on_end;

    // 监听上
    this.pmoving(_.extend(opt, {
        findTrigger         : find_trigger_element,
        helperPosition      : "hover",
        autoUpdateTriggerBy : null,
        on_begin            : on_begin,
        on_ing              : on_ing,
        on_end              : on_end
    }));
    
    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

