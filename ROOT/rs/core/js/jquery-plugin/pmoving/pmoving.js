/*
在给定的元素上，停靠一个滑块条，可以拖动的方式改变数值
*/
(function($, $z){
function Lv(rect) {
    return $z.tmpl("l:{{left}},r:{{right}},t:{{top}},b:{{bottom}},x:{{x}},y:{{y}}")(rect);
}
//...........................................................
// 获取或者设置上下文 
function options($ele, opt) {
    if(!opt)
        return $ele.data("@pmoving_OPT");
    $ele.data("@pmoving_OPT", opt);
}
//...........................................................
function do_update_helper(pmvContext) {
    var opt = pmvContext.options;
    if(_.isFunction(opt.helperPosition)) {
        var rect = opt.helperPosition.call(pmvContext);
        pmvContext.$helper.css($z.rectObj(rect, "top,left,width,height"));
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

    // 根据 opt.mode 限制 X 和 Y
    if("x" == opt.mode) {
        pmvContext.y = pmvContext.posBegin.y;
    }
    else if("y" == opt.mode) {
        pmvContext.x = pmvContext.posBegin.x;
    }

    //console.log("format_position", $z.rectObj(pmvContext, ["x", "y"]))
    // 利用 opt.position 修正 X 和 Y
    $z.invoke(opt, "position", [], pmvContext);
    
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
        //console.log(Lv(bdRect));

        // 矫正位置
        pmvContext.rect.boundary = $z.rect_clip_boundary(bdRect, pmvContext.rect.viewport);
        //console.log(" -> ", Lv(pmvContext.rect.boundary));
        if(isNaN(pmvContext.rect.boundary.top)) {
            console.log(" !!! viewport ", Lv(pmvContext.rect.viewport));
        }
        $z.rect_move_xy(pmvContext.rect.trigger, pmvContext.rect.boundary);
        //console.log(" ===> ", Lv(pmvContext.rect.trigger));

    }

    // 根据崭新的 X,Y，更新 inview 的矩形信息，以便回调们使用
    pmvContext.rect.inview = $z.rect_relative(pmvContext.rect.trigger, pmvContext.rect.viewport);
}
//...........................................................
function auto_update_trigger(pmvContext) {
    var opt = pmvContext.options;
    if(_.isArray(opt.autoUpdateTriggerBy) && opt.autoUpdateTriggerBy.length == 2) {
        var css  = $z.rectObj(pmvContext.rect.inview, opt.autoUpdateTriggerBy);
        pmvContext.$trigger.css(css);
    }
}
//...........................................................
function set_event_XY(pmvContext, e) {
    pmvContext.move.x = e.pageX - pmvContext.x;
    pmvContext.move.y = e.pageY - pmvContext.y;
    pmvContext.x = e.pageX;
    pmvContext.y = e.pageY;
}
//...........................................................
function on_mask_mouseup(e) {
    var pmvContext = e.data;
    var opt = pmvContext.options;
    pmvContext.endInMs = Date.now();
    
    // 更新上下文的位置信息
    set_event_XY(pmvContext, e);

    //console.log("on_mask_mouseup", pmvContext.$trigger.attr("pmv_mode_a"));

    // 如果 trigger.pmv_mode_a 表示在激活模式 
    if("yes" == pmvContext.$trigger.attr("pmv_mode_a")) {
        // 根据配置，格式化 trigger 的逻辑位置
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

    // 更新上下文的位置信息
    set_event_XY(pmvContext, e);

    //console.log("on_mask_mousemove", pmvContext.$trigger.attr("pmv_mode_a"));

    // 如果 trigger.pmv_mode_a 表示在激活模式 
    if("yes" == pmvContext.$trigger.attr("pmv_mode_a")) {
        // 根据配置，格式化位置
        format_position(pmvContext);

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
    //console.log("on_mousedown", jTrigger.attr("pmv_mode_a"));
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
        x : e.pageX,
        y : e.pageY,
        move : {x:0, y:0},
        rect : {
            viewport : rect_viewport,
            trigger  : rect_trigger,
            inview   : $z.rect_relative(rect_trigger, rect_viewport)
        }
    };
    //.........................................
    // 设置一个全局遮罩层，监听 
    var jMask = $('<div class="pmv-mask">').appendTo(document.body).css({
        position : "fixed", top:0, left:0, right:0, bottom:0,
        "z-index" : opt.maskZIndex
    });
    pmvContext.$mask = jMask;
    pmvContext.$helper = $('<div class="pmv-helper">').hide().appendTo(jMask).css({
        position : "fixed", "z-index" : opt.maskZIndex + 1
    });
    jMask.on("mousemove", pmvContext, on_mask_mousemove);
    jMask.on("mouseup", pmvContext, on_mask_mouseup);

    //.........................................
    // 设置延迟函数(opt.delay) 
    window.setTimeout(function(pmvContext){
        //console.log("in delay", pmvContext.$trigger.attr("pmv_mode_a"));

        // 如果没有 pmvContext.endInMs 表示要进入激活态 
        if(!pmvContext.endInMs) {
            // 显示辅助框
            pmvContext.$helper.show();
            //console.log('标识 trigger.pmv_mode_a = "yes"');
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
$.fn.extend({ "pmoving" : function(opt){
    // 销毁控件
    if("destroy" == opt){
        opt = options(this);
        if(opt)
            this.off("mousedown", opt.trigger, on_mousedown);
        return this;
    }

            // 如果已经存在了 pmoving，则首先销毁
    if(this.attr("pointer-moving-enabled")) {
        this.pmoving("destroy");
    }
    // 否则标识一下
    else {
        this.attr("pointer-moving-enabled", "yes");
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
            throw "invalid pmoving.helperPosition : " + opt.helperPosition;
        }
    }

    // 预先编译函数: position
    if(opt.position && !_.isFunction(opt.position)) {
        if(opt.position.gridX 
            && opt.position.gridY 
            && opt.position.stickX
            && opt.position.stickY)
        {
            opt.__grid_position = opt.position;
            opt.__pre_count_grid = function(){
                if(!this.__grid) {
                    var grid = this.options.__grid_position;
                    this.__grid = {
                        by : grid.by || "dock",
                        x  : $z.dimension(grid.gridX, this.rect.viewport.width),
                        y  : $z.dimension(grid.gridY, this.rect.viewport.height),
                        stick : {
                            x : $z.dimension(grid.stickX || 1, this.rect.viewport.width),
                            y : $z.dimension(grid.stickY || 1, this.rect.viewport.height),
                        }
                    };
                    // console.log(this.__grid)
                }
            };
            // this 就是 pmvContext
            opt.position = function() {
                // 确保格子被编译了
                this.options.__pre_count_grid.call(this);

                // 四个关键变量
                var gridX  = this.__grid.x;
                var gridY  = this.__grid.y;
                var stickX = this.__grid.stick.x;
                var stickY = this.__grid.stick.y;

                // 首先根据 x,y 更新一下 trigger 应该的位置 
                $z.rect_move_tl(this.rect.trigger, this, this.posAt);

                // 得到相对于视口的矩形，grid 是从视口开始的
                var rt = $z.rect_relative(this.rect.trigger, this.rect.viewport);
                
                // 开始计算这两个偏移量
                var offX = 0;
                var offY = 0;
                //console.log("before", $z.rectObj(this.rect.trigger, "top,left,x,y"))
                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // 根据中点来吸附
                if("center" == this.__grid.by) {
                    var red_x = rt.x * gridX;
                    if(red_x <= stickX || (gridX - red_x) <= stickX) {
                        offX = Math.round(rt.x / gridX) * gridX - rt.x;
                    }
                    var red_y = rt.y * gridY;
                    if(red_y <= stickY || (gridY - red_y) <= stickY) {
                        offY = Math.round(rt.y / gridY) * gridY - rt.y;
                    }
                }
                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                // 修正的方式，分别计算trigger两边，看看，那一边距离 grid 更近
                else {
                    var red_l = rt.left   % gridX;
                    var red_r = rt.right  % gridX;
                    var red_t = rt.top    % gridY;
                    var red_b = rt.bottom % gridY;
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    // 计算垂直边 (left,right)
                    // 采用左边
                    if(red_l <= red_r) {
                        var red = red_l;
                        if(red <= stickX || (gridX - red) <= stickX) {
                            offX = Math.round(rt.left / gridX) * gridX - rt.left;
                        }
                    }
                    // 采用右边
                    else {
                        var red = red_r;
                        if(red <= stickX || (gridX - red) <= stickX) {
                            offX = Math.round(rt.right / gridX) * gridX - rt.right;
                        }
                    }
                    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    // 计算水平边 (top,bottom)
                    // 采用上边
                    if(red_t <= red_b) {
                        var red  = red_t;
                        if(red <= stickY || (gridY - red) <= stickY) {
                            offY = Math.round(rt.top / gridY) * gridY - rt.top;
                        }
                    }
                    // 采用下边
                    else {
                        var red  = red_b;
                        if(red <= stickY || (gridY - red) <= stickY) {
                            offY = Math.round(rt.bottom / gridY) * gridY - rt.bottom;
                        }
                    }
                }

                // 计算位置
                var pos = {x:this.x, y:this.y};
                pos.x += offX;
                pos.y += offY;

                // 最后修改 trigger 的逻辑位置
                $z.rect_move_tl(this.rect.trigger, pos, this.posAt);
                //console.log( offX,  " :: ", this.x, pos.x, this.rect.trigger.left);

                //this.rect.trigger.
                //console.log("offX:", offX)
                // 移动逻辑指针的位置
                //this.x -= offX;
                // 计算垂直边
                // var redY = y % gridY;
                // if(redY < stickY || (gridY-redY) < stickY) {
                //     y2 = Math.round(this.y / gridY) * gridY;
                // }
                //console.log("after", $z.rectObj(this.rect.trigger, "top,left,x,y"))
                //console.log(this.x, "X:", x,"->", x2, " % ", redX, this.rect.viewport);
            }
        }
    }
    // 默认的 位置设定
    if(!_.isFunction(opt.position)) {
        opt.position = function(){
            $z.rect_move_tl(this.rect.trigger, this, this.posAt);
        };
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

