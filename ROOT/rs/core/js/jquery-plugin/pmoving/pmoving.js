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
function is_pos_in_click_radius(pmvc) {
    // 根据勾股定理，计算半径
    var w = Math.abs(pmvc.x - pmvc.posBegin.x);
    var h = Math.abs(pmvc.y - pmvc.posBegin.y);
    var dist = Math.sqrt(w*w + h*h);
    var cr = pmvc.options.clickRadius;
    //console.log("is_pos_in_click_radius: ", w,h,dist,cr);
    return dist <= cr;
}
//...........................................................
function format_position(pmvc) {
    var opt = pmvc.options;

    // 根据 opt.mode 限制 X 和 Y
    if("x" == opt.mode) {
        pmvc.y = pmvc.posBegin.y;
    }
    else if("y" == opt.mode) {
        pmvc.x = pmvc.posBegin.x;
    }

    //console.log("format_position", $z.rectObj(pmvc, ["x", "y"]))
    // 利用 opt.position 修正 X 和 Y
    // 这里是修改 trigger 矩形的地方
    opt.position.call(pmvc);
  
    // 根据 opt.boundary 计算 trigger 的位置
    // 这里要考虑 boundary 不能超过 viewport
    if(opt.boundary){
        // 计算 boudary
        var bdRect = {
            x : pmvc.rect.trigger.x,
            y : pmvc.rect.trigger.y
        };

        bdRect.width = $z.dimension(opt.boundary, pmvc.rect.trigger.width);
        bdRect.height = $z.dimension(opt.boundary, pmvc.rect.trigger.height);
        $z.rect_count_xywh(bdRect);
        //console.log(Lv(bdRect));

        // 矫正位置
        pmvc.rect.boundary = $z.rect_clip_boundary(bdRect, pmvc.rect.viewport);
        //console.log(" -> ", Lv(pmvc.rect.boundary));
        if(isNaN(pmvc.rect.boundary.top)) {
            console.log(" !!! viewport ", Lv(pmvc.rect.viewport));
        }
        $z.rect_move_xy(pmvc.rect.trigger, pmvc.rect.boundary);
        //console.log(" ===> ", Lv(pmvc.rect.trigger));
    }

    // 算出 trigger 相对于 viewport 的矩形，以备后续计算使用
    pmvc.rect.inview = $z.rect_relative(
        pmvc.rect.trigger, pmvc.rect.viewport, false, pmvc.$viewport
    );

}
//...........................................................
// 更新 trigger 相对于 viewport 正确的 CSS 顶点位置
function update_trigger_css(pmvc) {
    var opt = pmvc.options;
    pmvc.css.trigger = $z.rect_relative(
                            pmvc.rect.trigger,
                            pmvc.rect.viewport,
                            true,
                            pmvc.$viewport);
    pmvc.css.picked = $z.rectObj(pmvc.css.trigger, opt.updateTriggerBy);
}
//...........................................................
function on_enter_sensor(e) {
    var pmvc = e.data;
    var jSen = $(e.currentTarget);
    var step = jSen.attr("step") * 1;
    var prop = jSen.attr("prop");
    pmvc.H_sensor = window.setInterval(function(){
        // 进入了 drop 目标，则无视
        // if(pmvc.$mask.find('.pmv-dropi[pmv-hover]').length > 0) {
        //     window.clearInterval(pmvc.H_sensor);
        //     pmvc.H_sensor = null;
        //     return;
        // }

        var old_v = pmvc.$viewport[0][prop];
        //console.log("in sensor", old_v);
        pmvc.$viewport[0][prop] = old_v + step;
        var new_v = pmvc.$viewport[0][prop];
        // 有效的滚动，更新 trigger 的位置
        if(old_v != new_v) {
            //console.log("scroll to", new_v);
            on_mask_mousemove(e);
        }
        // 否则停止滚动
        else {
            window.clearInterval(pmvc.H_sensor);
            pmvc.H_sensor = null;
        }
    }, 50);
}
function on_leave_sensor(e) {
    var pmvc = e.data;
    if(pmvc.H_sensor){
        window.clearInterval(pmvc.H_sensor);
        pmvc.H_sensor = null;
    }
}
//...........................................................
function auto_apply_trigger_css(pmvc) {
    var opt = pmvc.options;
    if(opt.autoUpdateTrigger) {
        pmvc.$trigger.css(pmvc.css.picked);
    }
}
//...........................................................
function set_event_XY(pmvc, e) {
    pmvc.posDelta.x = e.clientX - pmvc.x;
    pmvc.posDelta.y = e.clientY - pmvc.y;
    pmvc.x = e.clientX;
    pmvc.y = e.clientY;
    pmvc.posPage.x = e.pageX;
    pmvc.posPage.y = e.pageY;
    pmvc.posViewport.x = pmvc.x - pmvc.rect.viewport.left
                            + pmvc.$viewport.scrollLeft();
    pmvc.posViewport.y = pmvc.y - pmvc.rect.viewport.top
                            + pmvc.$viewport.scrollTop();
}
//...........................................................
function stop_pmoving(pmvc) {
    // 调用结束事件
    var opt = pmvc.options;
    $z.invoke(opt, "on_end", [], pmvc);

    // 移除 trigger.pmv_mode_a 标识
    pmvc.$trigger.removeAttr("pmv_mode_a");

    // 确保关闭 sensor
    on_leave_sensor({data:pmvc});

    // 移除监听事件
    if(pmvc.$scroll)
        pmvc.$scroll.off("scroll", on_viewport_scroll);
        
    // 无论怎样，都要移除遮罩极其内容
    pmvc.$mask.remove();
}
//...........................................................
// 让遮罩层的 viewport 与实际的 viewport 同步滚动
function on_viewport_scroll(e) {
    var pmvc = e.data;
    
    var sT = pmvc.$scroll.scrollTop();
    var sL = pmvc.$scroll.scrollLeft();
    //console.log("viewport scroll:", sT, sL);
    pmvc.$MVPw.css({
        "margin-top"  : sT * -1,
        "margin-left" : sL * -1,
    });
}
//...........................................................
function on_mask_mouseup(e) {
    var pmvc = e.data;
    var opt = pmvc.options;
    pmvc.endInMs = Date.now();
    
    // 更新上下文的位置信息
    set_event_XY(pmvc, e);

    //console.log("on_mask_mouseup", pmvc.$trigger.attr("pmv_mode_a"));

    // 如果 trigger.pmv_mode_a 表示在激活模式 
    if("yes" == pmvc.$trigger.attr("pmv_mode_a")) {
        // 根据配置，格式化 trigger 的逻辑位置
        format_position(pmvc);

        // 更新 trigger 的 CSS
        update_trigger_css(pmvc);

        // 根据 opt.updateTriggerBy 更新 trigger 位置
        auto_apply_trigger_css(pmvc);

        // 回调: 通知鼠标移动以及结束
        $z.invoke(opt, "on_ing", [], pmvc);

        // 处理拖拽
        if(pmvc.drops) {
            var jHover = pmvc.$mask.find('.pmv-dropi[pmv-hover]');
            if(jHover.length > 0) {
                var as    = jHover.attr("d-as");
                var index = jHover.attr("d-index") * 1;
                var di    = pmvc.drops[as][index];
                
                // 调用离开事件
                $z.invoke(opt, "on_dragleave", [di.$ele, di.$helper], pmvc);

                // 调用放置事件
                $z.invoke(opt, "on_drop", [di.$ele], pmvc);
            }
        }

        // 结束
        stop_pmoving(pmvc);
    }
    // 否则就认为是点击
    else {
        // 首先结束拖拽
        //console.log("stop_pmoving(pmvc)");
        stop_pmoving(pmvc);
        $(pmvc.Event.target).click();
        // console.log($(pmvc.Event.target).html())
    }
}
//...........................................................
function on_mask_mousemove(e) {
    var pmvc = e.data;
    var opt = pmvc.options;

    // 更新上下文的位置信息
    set_event_XY(pmvc, e);

    // 判断是否需要自动滚动
    var j

    // 如果 trigger.pmv_mode_a 表示在激活模式 
    if("yes" == pmvc.$trigger.attr("pmv_mode_a")) {
        // 根据配置，格式化位置
        format_position(pmvc);

        // 更新 trigger 的 CSS
        update_trigger_css(pmvc);

        // 根据 opt.updateTriggerBy 更新 trigger 位置
        auto_apply_trigger_css(pmvc);
        
        // 修改辅助框位置，使其完全覆盖 trigger
        opt.do_update_helper.call(pmvc);
        
        // 改变 drop
        do_drag_and_drop(pmvc);

        // 回调: 通知鼠标移动
        $z.invoke(opt, "on_ing", [], pmvc);
        
    }
    // 否则如果没有结束，那么看看是否移出了点击区域
    // 如果出了区域，那么就是拖拽了，要做拖拽的初始化
    else if(!pmvc.endInMs && !is_pos_in_click_radius(pmvc)) {
        // 显示辅助框
        pmvc.$helper.show();

        // 显示拖拽辅助框
        if(pmvc.drops) {
            pmvc.drops.$inside.show();
            pmvc.drops.$outside.show();
        }

        // 监视视口滚动事件
        // !! zozoh: 非常奇怪，这个设置写在 on_mousedown 就会不行
        // !! 检测不到滚动，写在这里就行。诡异啊
        pmvc.$scroll = pmvc.$viewport[0].tagName == 'BODY'
                        ? $(pmvc.doc)
                        : pmvc.$viewport;
        pmvc.$scroll.on("scroll", pmvc, on_viewport_scroll);

        // 同步滚动
        on_viewport_scroll({data:pmvc});
        
        // 回调:开始 
        $z.invoke(opt, "on_begin",  [], pmvc);
        
        // 最后标识一下
        pmvc.$trigger.attr("pmv_mode_a", "yes");

        // console.log("beforeRect", $z.pick(pmvc.rect.trigger, /^(t|l|w|h)/));
        // console.log("before CSS", $z.pick(pmvc.css.trigger, /^(t|l|w|h)/));

        // 根据配置，格式化位置
        format_position(pmvc);

        // console.log("afterRect", $z.pick(pmvc.rect.trigger, /^(t|l|w|h)/));
        // console.log("after CSS", $z.pick(pmvc.css.trigger, /^(t|l|w|h)/));

        // 更新 trigger 的 CSS
        update_trigger_css(pmvc);

        // 修改辅助框位置，使其完全覆盖 trigger
        opt.do_update_helper.call(pmvc);

        // 回调: 通知鼠标移动
        $z.invoke(opt, "on_ing", [], pmvc);

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
    var win = doc.defaultView;
    
    //console.log([e.pageX, e.pageY], [e.clientX, e.clientY], [e.screenX, e.screenY]) 
    //console.log("on_mousedown", jTrigger.attr("pmv_mode_a"));
    //.........................................
    // 预先计算触发者和视口的矩形，已经触发者相对于视口的矩形
    // 这里需要注意的是，如果视口是 body，则需要包括 margin
    var rect_win      = $z.winsz(win);
    var rect_trigger  = $z.rect(jTrigger,  false, true);
    var rect_viewport = $z.rect(jViewport, false, true);
    var mX = e.clientX;
    var mY = e.clientY;
    // console.log("rect_trigger", rect_trigger);
    // console.log("rect_viewport", rect_viewport);
    // console.log("rect_inview", rect_inview);
    //.........................................
    // 创建上下文
    var pmvc = {
        Event     : e,
        win       : win,
        doc       : doc,
        docBody   : doc.body,
        $docBody  : $(doc.body),
        $context  : jContext,
        $trigger  : jTrigger,
        $viewport : jViewport,
        $scroll   : null,
        options   : opt,
        data      : opt.data,
        beginInMs : Date.now(),
        posAt : {
            x : mX - rect_trigger.left,
            y : mY - rect_trigger.top
        },
        posBegin    : { x : mX , y : mY },
        posDelta    : { x : 0  , y : 0  },
        posViewport : { x : -1 , y : -1 },
        posPage     : { x : -1 , y : -1 },
        x : mX,
        y : mY,
        rect : {
            win          : rect_win,
            viewport     : rect_viewport,
            origin       : _.extend({}, rect_trigger),
            trigger      : rect_trigger,
        },
        css : {}
    };
    //.........................................
    // 设置全局遮罩层
    setup_mask(pmvc);
    
    // 设置视口
    setup_viewport(pmvc);

    // 同步滚动
    //on_viewport_scroll({data:pmvc});

    //.........................................
    // 在遮罩层监听事件
    pmvc.$mask.on("mousemove", pmvc, on_mask_mousemove);
    pmvc.$mask.on("mouseup",   pmvc, on_mask_mouseup);
    pmvc.$mask.on("mouseleave", pmvc, function(e){
        stop_pmoving(e.data);
    });
    pmvc.$mask.on("mouseenter", ".pmv-si", pmvc, on_enter_sensor);
    pmvc.$mask.on("mouseleave", ".pmv-si", pmvc, on_leave_sensor);
}
//...........................................................
function setup_mask(pmvc) {
    var opt = pmvc.options;

    var jMask = $('<div class="pmv-mask">').appendTo(pmvc.docBody).css({
        position  : "fixed", top:0, left:0, right:0, bottom:0,
        "z-index" : opt.maskZIndex,
        "padding" : 0
    });
    // 增加 mask 的类选择器 
    if(opt.maskClass)
        jMask.addClass(opt.maskClass);
    // 记录
    pmvc.$mask = jMask;

    // 计算 viewport 的 css
    var cssViewport = $z.rectCss(pmvc.rect.viewport, pmvc.rect.win);
    var vpTop    = $z.toPixel(cssViewport.top);
    var vpLeft   = $z.toPixel(cssViewport.left);
    var vpRight  = $z.toPixel(cssViewport.right);
    var vpBottom = $z.toPixel(cssViewport.bottom);

    // 遮罩里面增加对应的视口以及
    pmvc.$MVP = $('<div class="pmv-viewport">')
                    .css($z.pick(cssViewport, /^(top|left|width|height)$/))
                    .css({
                        "position" : "absolute",
                    })
                    .appendTo(pmvc.$mask);

    // 计算感应区大小
    var ssW = $z.dimension(opt.sensorSize, pmvc.rect.viewport.width);
    var ssH = $z.dimension(opt.sensorSize, pmvc.rect.viewport.height);

    // 视口增加滚动感应区
    pmvc.$sensor = $(`<div class="pmv-sensors">
        <div class="pmv-si" axis="Y" key="N" step="-10" prop="scrollTop"></div>
        <div class="pmv-si" axis="Y" key="S" step="10"  prop="scrollTop"></div>
        <div class="pmv-si" axis="X" key="W" step="-10" prop="scrollLeft"></div>
        <div class="pmv-si" axis="X" key="E" step="10"  prop="scrollLeft"></div>
    </div>`).appendTo(pmvc.$mask);

    // 设置感应区
    pmvc.$sensor.children('[key="N"]').css({
        "position" : "fixed",
        "z-index"  : opt.maskZIndex + 2,
        "height"   : ssH + vpTop,
        "top"      : 0,
        "left"     : vpLeft,
        "right"    : vpRight,
    });
    pmvc.$sensor.children('[key="S"]').css({
        "position" : "fixed",
        "z-index"  : opt.maskZIndex + 2,
        "height"   : ssH + vpBottom,
        "bottom"   : 0,
        "left"     : vpLeft,
        "right"    : vpRight,
    });
    pmvc.$sensor.children('[key="W"]').css({
        "position" : "fixed",
        "z-index"  : opt.maskZIndex + 2,
        "width"    : ssW + vpLeft,
        "left"     : 0,
        "top"      : vpTop,
        "bottom"   : vpBottom,
    });
    pmvc.$sensor.children('[key="E"]').css({
        "position" : "fixed",
        "z-index"  : opt.maskZIndex + 2,
        "width"    : ssW + vpRight,
        "right"    : 0,
        "top"      : vpTop,
        "bottom"   : vpBottom,
    });
}
//...........................................................
function setup_viewport(pmvc) {
    var opt = pmvc.options;

    // 创建包裹层
    pmvc.$MVPw = $('<div class="pmv-viewport-W">').css({
        "position" : "relative",
        "width"    : "100%",
        "height"   : "100%",
    }).appendTo(pmvc.$MVP);

    // 创建要 drop 的目标
    var jDrops = $z.invoke(opt, "findDropTarget", [], pmvc);

    // 过滤掉所有不可见的元素
    if(jDrops)
        jDrops = jDrops.filter(":visible");

    // console.log(jDrops.size())
    if(jDrops && jDrops.length > 0) {
        // 在上下文中进行准备
        pmvc.drops = {
            insides  : [],
            $inside  : $('<div class="pmv-drops" inside="yes">'),
            outsides : [],
            $outside : $('<div class="pmv-drops" outside="yes">'),

        };
        // 初始化各个拖拽目标
        jDrops.each(function(){
            var di = {
                rect : $z.rect(this, false, true),
                $ele : $(this)
            };
            // 有面积才显示
            if((di.rect.width * di.rect.height) > 0 ){
                di.$helper = $('<div class="pmv-dropi">');
                // 在 viewport 内部: 计算相对视口的位置
                if(di.$ele.closest(pmvc.$viewport).length > 0) {
                    di.$helper.attr({
                        "d-as"    : "insides",
                        "d-index" : pmvc.drops.insides.length
                    }).appendTo(pmvc.drops.$inside);
                    di.rect = $z.rect_relative(
                        di.rect, pmvc.rect.viewport, false, pmvc.$viewport
                    );
                    pmvc.drops.insides.push(di);

                }
                // 在 viewport 外部: 就 fix
                else {
                    di.$helper.attr({
                        "d-as"    : "outsides",
                        "d-index" : pmvc.drops.outsides.length
                    }).appendTo(pmvc.drops.$outside);
                    di.rect = $z.rect_relative(
                        di.rect, pmvc.rect.win, false, pmvc.$docBody
                    );
                    pmvc.drops.outsides.push(di);
                }
            }
        });

        // 看看是否需要缩放视口内的拖拽目标
        if(opt.compactDropsRect) {
            // 得到视口内矩形列表
            var rects = [];
            for(var di of pmvc.drops.insides) {
                rects.push(di.rect);
            }

            // 缩放视口内矩形
            $z.rect_compact(rects, _.extend({}, opt.compactDropsRect, {
                scrollTop  : pmvc.$docBody.scrollTop(),
                scrollLeft : pmvc.$docBody.scrollLeft(),
            }))

            // 得到视口外矩形列表
            var rects = [];
            for(var di of pmvc.drops.outsides) {
                rects.push(di.rect);
            }

            // 缩放视口外矩形
            $z.rect_compact(rects, _.extend({}, opt.compactDropsRect, {
                scrollTop  : pmvc.$docBody.scrollTop(),
                scrollLeft : pmvc.$docBody.scrollLeft(),
            }))

        }
        // 处理所有视口内相对 drops 显示
        if(pmvc.drops.insides.length > 0 ) {
            // 计入 DOM
            pmvc.drops.$inside.hide().appendTo(pmvc.$MVPw);
            // 修正各项 CSS
            for(var di of pmvc.drops.insides) {
                var css_di = $z.rectCss(di.rect, pmvc.rect.viewport);
                di.$helper
                    .css("position", "absolute")
                    .css($z.rectObj(css_di, "top,left,width,height"));
            }
        }
        // 处理所有视口外绝对 drops 显示
        if(pmvc.drops.outsides.length > 0 ) {
            // 准备一个包裹 outside drops 的元素补偿页面的滚动
            var jDropOut = $('<div>').css({
                "margin-left" : 0 - pmvc.$docBody.scrollLeft(),
                "margin-top"  : 0 - pmvc.$docBody.scrollTop(),
            }).appendTo(pmvc.$mask);
            // 计入 DOM
            pmvc.drops.$outside.hide().css({
                "position" : "relative",
            }).appendTo(jDropOut);
            // 修正各项 CSS
            for(var di of pmvc.drops.outsides) {
                di.$helper
                    .css("position", "absolute")
                    .css($z.rectObj(di.rect, "top,left,width,height"));
            }
        }

    }

    // 创建 helper 元素
    pmvc.$helper = $('<div class="pmv-helper">')
        .hide()
        .appendTo(pmvc.$MVPw)
        .css({
            "position" : "absolute", "z-index" : opt.maskZIndex + 1,
            "width"    : pmvc.rect.origin.width,
            "height"   : pmvc.rect.origin.height,
        });
}
//...........................................................
function do_drag_and_drop(pmvc) {
    if(pmvc.drops) {
        var opt = pmvc.options;
        //........................................
        // 如果已经有了一个 hover，看看是不是还在其中 ...
        var jHover = pmvc.$mask.find(".pmv-dropi[pmv-hover]");
        if(jHover.length > 0) {
            var as    = jHover.attr("d-as");
            var index = jHover.attr("d-index") * 1;
            var di    = pmvc.drops[as][index];
            // 视口内部
            if('insides' == as){
                if($z.rect_in(di.rect, pmvc.posViewport)){
                    return;
                }
            }
            // 视口外部
            else if('outsides' == as) {
                if($z.rect_in(di.rect, pmvc.posPage)){
                    return;
                }
            }
            // 靠，什么鬼？！
            else {
                throw "wrong mode [" + as + "] of di " + index;
            }
            // 调用离开事件
            $z.invoke(opt, "on_dragleave", [di.$ele, di.$helper], pmvc);
            // 移除标记
            jHover.removeAttr("pmv-hover");
        }
        //........................................
        // 查找一下，看看哪个匹配的
        //........................................
        // 先看看外部的
        for(var di of pmvc.drops.outsides) {
            if($z.rect_in(di.rect, pmvc.posPage)) {
                di.$helper.attr("pmv-hover", "yes");
                $z.invoke(opt, "on_dragenter", [di.$ele, di.$helper], pmvc);
                return;
            }
        }
        // 在看看内部的
        for(var di of pmvc.drops.insides) {
            if($z.rect_in(di.rect, pmvc.posViewport)) {
                di.$helper.attr("pmv-hover", "yes");
                $z.invoke(opt, "on_dragenter", [di.$ele, di.$helper], pmvc);
                return;
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

    // 默认感应区大小为视口的 10%
    $z.setUndefined(opt, "sensorSize", "10%");

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
    $z.setUndefined(opt, "maskZIndex", 999999);
    $z.setUndefined(opt, "mode", "both");
    $z.setUndefined(opt, "autoUpdateTrigger", opt.findDropTarget ? false : true);
    $z.setUndefined(opt, "updateTriggerBy", ["top","left"]);
    $z.setUndefined(opt, "delay", 100);
    $z.setUndefined(opt, "clickRadius", 3);
    $z.setUndefined(opt, "autoScrollViewport", _.isUndefined(opt.boundary));  
    $z.setUndefined(opt, "do_update_helper", function() {
        this.$helper.css(this.css.picked);
    });

    // 默认更新 trigger 位置的算法
    $z.setUndefined(opt, "position", function() {
        $z.rect_move_tl(this.rect.trigger, this, this.posAt);
    });

    // 默认的拖拽压缩
    var dft_compact_drops = {
        paddingX  : 10,
        paddingY  : 10,
        width     : "50%",       // 宽度压缩比例
        height    : "auto",      // 高度压缩比例
    };
    if("NE" == opt.compactDropsRect) {
        opt.compactDropsRect = _.extend(dft_compact_drops, {
            positionX : "right",
            positionY : "top",
        });
    }
    else if("NW" == opt.compactDropsRect) {
        opt.compactDropsRect = _.extend(dft_compact_drops, {
            positionX : "left",
            positionY : "top",
        });
    }
    else if("SE" == opt.compactDropsRect) {
        opt.compactDropsRect = _.extend(dft_compact_drops, {
            positionX : "right",
            positionY : "bottom",
        });
    }
    else if("SW" == opt.compactDropsRect) {
        opt.compactDropsRect = _.extend(dft_compact_drops, {
            positionX : "left",
            positionY : "bottom",
        });
    }
    

    // 如果是 fixDrop 那么最小缩放区域是多少
    $z.setUndefined(opt, "minDropZoom", {});
    $z.setUndefined(opt.minDropZoom, "X", "50%");
    $z.setUndefined(opt.minDropZoom, "Y", 24);

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

