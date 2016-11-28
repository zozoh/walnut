/*
封装指针型设备的拖拽移动行为的处理
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
function is_pos_in_click_radius(pmvContext) {
    // 根据勾股定理，计算半径
    var w = Math.abs(pmvContext.x - pmvContext.posBegin.x);
    var h = Math.abs(pmvContext.y - pmvContext.posBegin.y);
    var dist = Math.sqrt(w*w + h*h);
    var cr = pmvContext.options.clickRadius;
    //console.log("is_pos_in_click_radius: ", w,h,dist,cr);
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
    // 这里是修改 trigger 矩形的地方
    $z.invoke(opt, "position", [], pmvContext);
    
    // 更新 helper 的位置
    pmvContext.rect.helper = opt.helperRect.call(pmvContext);
    
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
function auto_scroll_viewport(pmvc) {
    var opt = pmvc.options;
    if(!opt.autoScrollViewport)
        return;
    
    // 定义变量    
    var scroll_step    = 10;  // 每次滚动的步长
    var scroll_off     = 0;   // 滚动方向
    //.................................
    // 上下滚动
    //.................................
    var st = pmvc.$viewport.scrollTop();
    // 向下滚动
    if(pmvc.rect.helper.bottom > pmvc.rect.viewport.bottom
       && pmvc.rect.trigger.bottom < pmvc.$viewport[0].scrollHeight){
        scroll_off = 1;
    }
    // 向上滚动
    else if(st > 0 && pmvc.rect.helper.top < pmvc.rect.viewport.top){
        scroll_off = -1;
    }
    // 执行滚动
    if(scroll_off != 0) {
        var sval = scroll_off * scroll_step;
        // 上下滚动视口
        pmvc.$viewport.scrollTop(st + sval);
        // 补偿滚动
        pmvc.rect.helper.bottom -= sval;
        $z.rect_count_brwh(pmvc.rect.helper);
        do_update_helper(pmvc);
    }
    
    //.................................
    // 左右滚动
    //.................................
    var sl = pmvc.$viewport.scrollLeft();
    // 向右滚动
    if(pmvc.rect.helper.right > pmvc.rect.viewport.right
       && pmvc.rect.trigger.right < pmvc.$viewport[0].scrollWidth){
        scroll_off = 1;
    }
    // 向左滚动
    else if(sl > 0 && pmvc.rect.helper.left < pmvc.rect.viewport.left){
        scroll_off = -1;
    }
    // 执行滚动
    if(scroll_off != 0) {
        var sval = scroll_off * scroll_step;
        // 左右滚动视口
        pmvc.$viewport.scrollLeft(sl + sval);
        // 补偿滚动
        pmvc.rect.helper.right -= sval;
        $z.rect_count_brwh(pmvc.rect.helper);
        do_update_helper(pmvc);
    }
}
//...........................................................
function auto_update_trigger(pmvContext) {
    var opt = pmvContext.options;
    if(_.isArray(opt.autoUpdateTriggerBy) && opt.autoUpdateTriggerBy.length == 2) {
        var css  = $z.rectObj(pmvContext.rect.inview, opt.autoUpdateTriggerBy);
        //console.log("trigger:", css);
        pmvContext.$trigger.css(css);
    }
}
//...........................................................
function do_update_helper(pmvContext) {
    if(pmvContext.rect.helper){
        var css  = $z.rectObj(pmvContext.rect.helper, "top,left,width,height");
        //console.log("helper:", css);
        pmvContext.$helper.css(css);
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
function stop_pmoving(pmvContext) {
    // 移除监听事件
    pmvContext.$scroll.off("scroll", on_viewport_scroll);
    
    // 移除 trigger.pmv_mode_a 标识
    pmvContext.$trigger.removeAttr("pmv_mode_a");
    
    // 无论怎样，都要移除遮罩极其内容
    pmvContext.$mask.remove();
}
//...........................................................
function on_viewport_scroll(e) {
    var pmvContext = e.data;
    if(pmvContext.$drops){
        var sT = pmvContext.$scroll.scrollTop();
        var sL = pmvContext.$scroll.scrollLeft();
        //console.log("viewport scroll:", sT, sL);
        pmvContext.$drops.css("margin-top" , sT * -1);
        pmvContext.$drops.css("margin-left", sL * -1);
    }
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

        // 找到可被放置的对象，调用 on_drop 回调
        if(!pmvContext.$mask.attr("no-drag") && _.isArray(pmvContext.dropping)) {
            for(var i=0; i<pmvContext.dropping.length; i++) {
                var di = pmvContext.dropping[i];
                if(di.helper.attr("pmv-hover")){
                    $z.invoke(opt, "on_drop", [di.$ele], pmvContext);
                }
            }
        }
        
        // 结束
        stop_pmoving(pmvContext);
    }
    // 否则就认为是点击
    else {
        // 首先结束拖拽
        //console.log("stop_pmoving(pmvContext)");
        stop_pmoving(pmvContext);
        $(pmvContext.Event.target).click();
        // console.log($(pmvContext.Event.target).html())
    }
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
        
        // 修改辅助框位置，使其完全覆盖 trigger
        do_update_helper(pmvContext);
        
        // 自动滚动视口
        auto_scroll_viewport(pmvContext);

        // 改变 drop
        do_drag_and_drop(pmvContext);

        // 回调: 通知鼠标移动
        $z.invoke(opt, "on_ing", [], pmvContext);
        
    }
    // 否则如果没有结束，那么看看是否移出了点击区域
    // 如果出了区域，那么就是拖拽了，要做拖拽的初始化
    else if(!pmvContext.endInMs && !is_pos_in_click_radius(pmvContext)) {
        // 显示辅助框
        pmvContext.$helper.show();
        if(pmvContext.$drops) {
            // 显示放置的辅助层
            pmvContext.$drops.show();
            //console.log('标识 trigger.pmv_mode_a = "yes"');
            // 监视视口滚动事件
            pmvContext.$scroll.on("scroll", pmvContext, on_viewport_scroll);
        }
        
        // 回调:开始 
        $z.invoke(opt, "on_begin",  [], pmvContext);
        
        // 最后标识一下
        pmvContext.$trigger.attr("pmv_mode_a", "yes");

        // 修改辅助框位置，使其完全覆盖 trigger
        do_update_helper(pmvContext);

        // 回调: 通知鼠标移动
        $z.invoke(opt, "on_ing", [], pmvContext);

    }
}
//...........................................................
// 处理 mousedown 事件，这个是整个控件判断是否进入激活态的地方
function on_mousedown(e) {
    // 如果是鼠标的话，必须是左键
    if("mousedown" == e.type && 1!==e.which) {
        return;
    }
    
    // 之前的移动遮罩还在，那么什么也不做
    if($(e.currentTarget.ownerDocument.body).children(".pmv-mask").length>0)
        return;
    
    // 准备上下文
    var jContext = e.data.$context;
    var opt = options(jContext);

    // 没找到触发对象，啥都表做了
    var jTrigger  = opt.findTrigger.call(this, e);
    if(!jTrigger || jTrigger.length == 0) {
        return;
    }

    // 找到视口，没有视口也啥都表做了
    var jViewport = opt.findViewport.call(jTrigger, jContext, e);
    if(!jViewport || jViewport.size() == 0) {
        return;
    }
    
    // 找到自己所在的文档
    var doc = jViewport[0].ownerDocument;
    
    // 视口滚动监视对象，如果 jViewport 为 body 则监视 document
    var jScroll = jViewport[0].tagName == 'BODY'
                    ? $(doc)
                    : jViewport;

    //console.log("on_mousedown", jTrigger.attr("pmv_mode_a"));
    //.........................................
    // 预先计算触发者和视口的矩形，已经触发者相对于视口的矩形
    // 这里需要注意的是，如果视口是 body，则需要包括 margin
    var rect_trigger  = $z.rect(jTrigger);
    var rect_viewport = $z.rect(jViewport);
    var rect_inview   = $z.rect_relative(rect_trigger, rect_viewport);
    // console.log("rect_trigger", rect_trigger);
    // console.log("rect_viewport", rect_viewport);
    // console.log("rect_inview", rect_inview);
    //.........................................
    // 创建上下文
    var pmvContext = {
        Event     : e,
        doc       : doc,
        docBody   : doc.body,
        $docBody  : $(doc.body),
        $context  : jContext,
        $trigger  : jTrigger,
        $viewport : jViewport,
        $scroll   : jScroll,
        options   : opt,
        data      : opt.data,
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
            origin   : _.extend({}, rect_trigger),
            originInView : _.extend({}, rect_inview),
            trigger  : rect_trigger,
            inview   : rect_inview,
            helper   : undefined
        }
    };
    //.........................................
    // 设置一个全局遮罩层
    var jMask = $('<div class="pmv-mask">').appendTo(pmvContext.docBody).css({
        position  : "fixed", top:0, left:0, right:0, bottom:0,
        "z-index" : opt.maskZIndex,
        "padding" : 0
    });
    // 增加 mask 的类选择器 
    if(opt.maskClass)
        jMask.addClass(opt.maskClass);
    // 记录
    pmvContext.$mask = jMask;
    
    // 创建要 drop 的目标
    var jDrops = $z.invoke(opt, "findDropTarget", [], pmvContext);
    // console.log(jDrops.size())
    if(jDrops) {
        pmvContext.$drops = $('<div class="pmv-drops">').hide().css({
            "width"    : jViewport[0].scrollWidth,
            "height"   : jViewport[0].scrollHeight,
            "position" : "relative"
        }).appendTo(jMask);
        pmvContext.dropping = [];
        jDrops.each(function(){
            var di = {
                rect : $z.rect(this),
                $ele : $(this)
            };
            di.helper = $('<div class="pmv-dropi">').appendTo(pmvContext.$drops)
                .css(_.extend($z.rectObj(di.rect, "top,left,width,height"), {
                    position : "absolute"
                }));
            pmvContext.dropping.push(di);
        });
    }

    // 创建 helper 元素
    pmvContext.$helper = $('<div class="pmv-helper">').hide().appendTo(jMask).css({
        position : "fixed", "z-index" : opt.maskZIndex + 2
    });
    //.........................................
    // 在遮罩层监听事件
    jMask.on("mousemove", pmvContext, on_mask_mousemove);
    jMask.on("mouseup",   pmvContext, on_mask_mouseup);
    jMask.on("mouseleave", pmvContext, function(e){
        stop_pmoving(e.data);
    });
}
//...........................................................
function do_drag_and_drop(pmvContext) {
    if(!pmvContext.$mask.attr("no-drag") && _.isArray(pmvContext.dropping)) {
        var opt = pmvContext.options;
        for(var i=0; i<pmvContext.dropping.length; i++) {
            var di = pmvContext.dropping[i];
            // 在矩形中，是 enter 吗?
            if($z.rect_in(di.rect, pmvContext)){
                if(!di.helper.attr("pmv-hover")) {
                    di.helper.attr("pmv-hover", "yes");
                    $z.invoke(opt, "on_dragenter", [di.$ele, di.helper], pmvContext);
                    break;
                }
            }
            // 不在矩形中，是 leave 吗？
            else {
                if(di.helper.attr("pmv-hover")) {
                    $z.invoke(opt, "on_dragleave", [di.$ele, di.helper], pmvContext);
                    di.helper.removeAttr("pmv-hover");
                }
            }
        }
    }
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

    // 默认的查找 trigger 元素的方法
    if(!_.isFunction(opt.findTrigger)) {
        opt.findTrigger = function(){
            return $(this);
        };
    }

    // 默认的查找 viewport 元素的方法
    if(!_.isFunction(opt.findViewport)) {
        opt.findViewport = function($context, e){
            return $context;
        };
    }

    // 一个选择器指定的 drop 对象
    if(_.isString(opt.findDropTarget)){
        opt.__find_drop_target_selector = opt.findDropTarget;
        opt.findDropTarget = function(){
            return $(this.options.__find_drop_target_selector, this.docBody);
        };
    }

    // 默认值
    $z.setUndefined(opt, "maskZIndex", "999999");
    $z.setUndefined(opt, "mode", "both");
    $z.setUndefined(opt, "autoUpdateTriggerBy", opt.findDropTarget ? null : ["top","left"]);
    $z.setUndefined(opt, "delay", 100);
    $z.setUndefined(opt, "clickRadius", 3);
    $z.setUndefined(opt, "autoScrollViewport", _.isUndefined(opt.boundary));
    
    // 默认的 helperRect
    $z.setUndefined(opt, "helperRect", function(){
        var rect = _.extend({}, this.rect.trigger);
        rect.top  -= this.$viewport.scrollTop();
        rect.left -= this.$viewport.scrollLeft();
        return $z.rect_count_tlwh(rect);
    });

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
                // console.log("before", $z.rectObj(this.rect.trigger, "top,left,x,y"))
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
        $context : this
    }, on_mousedown);
    
    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil);

