/*
在给定的元素上，停靠一个滑块条，可以拖动的方式改变数值
*/
(function($, $z){
//...........................................................
// 获取或者设置上下文 
function options($ele, opt) {
    if(!opt)
        return $ele.data("@PointerMoving_OPT");
    $ele.data("@PointerMoving_OPT", opt);
}
//...........................................................
function do_update_helper(pmvContext) {
    var opt = pmvContext.options;
    if(_.isFunction(opt.helperPosition)) {
        var rect = opt.helperPosition.call(pmvContext);
        pmvContext.$helper.css({
            "top"    : rect.top,
            "left"   : rect.left,
            "width"  : rect.width,
            "height" : rect.height
        });
    }
}
//...........................................................
function is_end_in_click_radius(pmvContext) {
    // 根据勾股定理，计算半径
    var w = Math.abs(pmvContext.x - pmvContext.posBegin.x);
    var h = Math.abs(pmvContext.y - pmvContext.posBegin.y);
    var dist = Math.sqrt(w*w + h*h);
    var cr = pmvContext.options.clickRadius;
    //console.log("is_end_in_click_radius: ", w,h,dist,cr);
    return dist <= cr;
}
//...........................................................
function format_position(pmvContext) {
    var opt = pmvContext.options;

    // 利用 opt.position 修正 X 和 Y
    $z.invoke(opt, "position", [], pmvContext);

    // 根据 opt.mode 限制 X 和 Y
    if("x" == opt.mode) {
        pmvContext.y = pmvContext.posBegin.Y;
    }
    else if("y" == opt.mode) {
        pmvContext.x = pmvContext.posBegin.X;
    }
    
    // 根据 opt.boundary 计算 trigger 的位置
    // 这里要考虑 boundary 不能超过 viewport
    if(opt.boundary){
        // 计算 boudary
        var bdRect = {
            x : pmvContext.rect.trigger.x,
            y : pmvContext.rect.trigger.y
        };

        bdRect.width = $z.dimension(opt.boundary, pmvContext.rect.trigger.width);
        bdRect.height = $z.dimension(opt.boundary, pmvContext.rect.trigger.height);
        $z.rect_count_xywh(bdRect);

        // 矫正位置
        pmvContext.rect.boundary = $z.rect_clip_boundary(bdRect, pmvContext.rect.viewport);
        $z.rect_move_xy(pmvContext.rect.trigger, pmvContext.rect.boundary);
    }
}
//...........................................................
function auto_update_trigger(pmvContext) {
    var opt = pmvContext.options;
    if(_.isArray(opt.autoUpdateTriggerBy) && opt.autoUpdateTriggerBy.length == 2) {
        var key0 = opt.autoUpdateTriggerBy[0];
        var key1 = opt.autoUpdateTriggerBy[1];

        var rect = $z.rect_relative(pmvContext.rect.trigger, pmvContext.rect.viewport);

        // console.log("trigger", pmvContext.rect.trigger);
        // console.log("viewport", pmvContext.rect.viewport);
        // console.log("rect", rect);

        var css  = {};
        css[key0] = rect[key0];
        css[key1] = rect[key1];
        pmvContext.$trigger.css(css);
    }
}
//...........................................................
function on_mask_mouseup(e) {
    var pmvContext = e.data;
    var opt = pmvContext.options;
    pmvContext.endInMs = Date.now();
    pmvContext.x = e.pageX;
    pmvContext.y = e.pageY;

    console.log("on_mask_mouseup", pmvContext.$trigger.attr("pmv_mode_a"));

    // 如果 trigger.pmv_mode_a 表示在激活模式 
    if("yes" == pmvContext.$trigger.attr("pmv_mode_a")) {
        // 根据配置，格式化位置
        format_position(pmvContext);

        // 根据 opt.autoUpdateTriggerBy 更新 trigger 位置
        auto_update_trigger(pmvContext);

        // 回调: 通知鼠标移动以及结束
        $z.invoke(opt, "on_ing", [], pmvContext);
        $z.invoke(opt, "on_end", [], pmvContext);
        
        // 移除 trigger.pmv_mode_a 标识
        pmvContext.$trigger.removeAttr("pmv_mode_a");
    }
    // 否则如果在 opt.clickRadius 内释放
    else if (is_end_in_click_radius(pmvContext)){
        //console.log("is_end_in_click_radius");
        pmvContext.$trigger.click();
    }

    // 无论怎样，都要移除遮罩和辅助框
    pmvContext.$helper.remove();
    pmvContext.$mask.remove();
}
//...........................................................
function on_mask_mousemove(e) {
    var pmvContext = e.data;
    var opt = pmvContext.options;
    pmvContext.x = e.pageX;
    pmvContext.y = e.pageY;

    //console.log("on_mask_mousemove", pmvContext.$trigger.attr("pmv_mode_a"));

    // 如果 trigger.pmv_mode_a 表示在激活模式 
    if("yes" == pmvContext.$trigger.attr("pmv_mode_a")) {
        // 根据配置，格式化位置
        format_position(pmvContext);

        // 根据崭新的 X,Y，更新 trigger 的矩形信息，以便回调们使用
        $z.rect_move_tl(pmvContext.rect.trigger, pmvContext, pmvContext.posAt);

        // 根据 opt.autoUpdateTriggerBy 更新 trigger 位置
        auto_update_trigger(pmvContext);

        // 回调: 通知鼠标移动
        $z.invoke(opt, "on_ing", [], pmvContext);
        
        // 修改辅助框位置，使其完全覆盖 trigger
        do_update_helper(pmvContext);

        // 回调: 通知辅助框更新
        $z.invoke(opt, "on_update", [], pmvContext);
    }

}
//...........................................................
// 处理 mousedown 事件，这个是整个控件判断是否进入激活态的地方
function on_mousedown(e) {
    // 如果是鼠标的话，必须是左键
    if("mousedown" == e.type && 1!==e.which) {
        return;
    }

    var jViewport = e.data.$viewport;
    var jTrigger  = $(this);
    var opt = options(jViewport);
    console.log("on_mousedown", jTrigger.attr("pmv_mode_a"));
    //.........................................
    var rect_trigger  = $z.rect(jTrigger);
    var rect_viewport = $z.rect(jViewport);
    //.........................................
    // 创建上下文
    var pmvContext = {
        Event     : e,
        $trigger  : jTrigger,
        $viewport : jViewport,
        options   : opt,
        beginInMs : Date.now(),
        posAt : {
            x : e.pageX - rect_trigger.left,
            y : e.pageY - rect_trigger.top
        },
        posBegin : {
            x : e.pageX,
            y : e.pageY,
        },
        rect : {
            viewport : rect_viewport,
            trigger  : rect_trigger
        }
    };
    //.........................................
    // 设置一个全局遮罩层，监听 
    var jMask = $('<div class="pmv-mask">').appendTo(document.body).css({
        position : "fixed", top:0, left:0, right:0, bottom:0,
        "z-index" : opt.maskZIndex
    });
    pmvContext.$mask = jMask;
    pmvContext.$helper = $('<div class="pmv-helper">').appendTo(jMask).css({
        position : "fixed", "z-index" : opt.maskZIndex + 1
    });
    jMask.on("mousemove", pmvContext, on_mask_mousemove);
    jMask.on("mouseup", pmvContext, on_mask_mouseup);

    //.........................................
    // 设置延迟函数(opt.delay) 
    window.setTimeout(function(pmvContext){
        console.log("in delay", pmvContext.$trigger.attr("pmv_mode_a"));

        // 如果没有 pmvContext.endInMs 表示要进入激活态 
        if(!pmvContext.endInMs) {
            console.log('标识 trigger.pmv_mode_a = "yes"');
            pmvContext.$trigger.attr("pmv_mode_a", "yes");
            
            // 修改辅助框位置，使其完全覆盖 trigger
            do_update_helper(pmvContext);
            
            // 调用回调
            $z.invoke(opt, "on_begin",  [], pmvContext);
            $z.invoke(opt, "on_update", [], pmvContext);
        }
    }, opt.delay || 300, pmvContext);
}
//...........................................................
$.fn.extend({ "PointerMoving" : function(opt){
    // 销毁控件
    if("destroy" == opt){
        opt = options(this);
        if(opt)
            this.off("mousedown", opt.trigger, on_mousedown);
        return this;
    }

    // 确保有对象
    opt = opt || {};

    // 默认是自己的所有 children 被监视移动 
    $z.setUndefined(opt, "trigger", ">*");

    // 默认值
    $z.setUndefined(opt, "maskZIndex", "999999");
    $z.setUndefined(opt, "mode", "both");
    $z.setUndefined(opt, "autoUpdateTriggerBy", ["top","left"]);
    $z.setUndefined(opt, "delay", 100);
    $z.setUndefined(opt, "clickRadius", 3);
    $z.setUndefined(opt, "helperPosition", "trigger");

    // 预先编译函数: helperPosition
    if(opt.helperPosition && !_.isFunction(opt.helperPosition)) {
        // 时刻完全覆盖在 trigger 上面
        if("hover" == opt.helperPosition) {
            opt.helperPosition = function(){
                return $z.rect(this.$trigger);
            };
        }
        // 完全跟随 rect.trigger 计算结果
        else if("trigger" == opt.helperPosition) {
            opt.helperPosition = function(){
                return this.rect.trigger;
            };
        }
        // 完全跟随 rect.boundary 计算结果
        else if("boundary" == opt.helperPosition) {
            opt.helperPosition = function(){
                return this.rect.boundary;
            };
        }
        // 靠不支持
        else {
            throw "invalid PointerMoving.helperPosition : " + opt.helperPosition;
        }
    }

    // 预先编译函数: position
    if(opt.position && !_.isFunction(opt.position)) {
        if(opt.position.gridX && opt.position.gridY && opt.position.stickRadius){
            opt.position = function() {
                // TODO ....
            }
        }
    }

    // 记录配置信息
    options(this, opt);

    // 监控上下文的 mousedown 事件
    this.on("mousedown", opt.trigger, {
        $viewport : this
    }, on_mousedown);
    
    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

