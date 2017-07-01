/*
封装指针型设备的拖拽移动行为的处理
*/
(function($, $z, $D){
//...........................................................
function direction(val, lt, gt){
    if(val < 0)
        return lt;
    if(val > 0)
        return gt;
    return null;
}
//...........................................................
// 一些帮助函数(this均为 MVing)
var MVs = {
    //.......................................................
    // 构建时查找 $target/$viewport 的帮助函数
    findElement : function(key, dftKey){
        var MVing = this;
        var value = this.options[key];
        var re;

        if(_.isUndefined(value)){
            re = MVing[dftKey];
        }
        // 自定义目标
        else{
            re = $z.doCallback(value, [], MVing) || value;
        }
        
        // 返回判断 true 表示找到了， false 表示无效
        if(re && re.length > 0){
            MVing["$" + key] = re;
            return true;
        }
        return false;
    },
    //.......................................................
    // 计算鼠标指针
    updateCursor : function(x, y) {
        var MVing = this;
        var opt   = MVing.options;

        // 根据模式，约束 x | y
        if("x" == opt.mode) {
            y = MVing.posAt.client.y;
        }
        // 只能 Y 轴
        else if("y" == opt.mode){
            x = MVing.posAt.client.x;
        }

        // 更新
        if(MVing.cursor) {
            MVing.cursor.viewport = $D.rect.ccs_point_tl(MVing.rect.viewport, x, y),
            MVing.cursor.delta.x  = x - MVing.cursor.client.x;
            MVing.cursor.delta.y  = y - MVing.cursor.client.y;
            MVing.cursor.offset.x = x - MVing.posAt.client.x;
            MVing.cursor.offset.y = y - MVing.posAt.client.y;
            MVing.cursor.client.x = x;
            MVing.cursor.client.y = y;
            MVing.direction.x = direction(MVing.cursor.delta.x, "left", "right");
            MVing.direction.y = direction(MVing.cursor.delta.y, "up", "down");
        }
        // 创建
        else {
            MVing.cursor = {
                client   : {x:x, y:y},
                viewport : $D.rect.ccs_point_tl(MVing.rect.viewport, x, y),
                delta    : {x:0, y:0},
                offset   : {x:0, y:0},
            };
            MVing.direction = {
                x : null, y : null
            };
        }
    },
    //.......................................................
    // 设置边界计算函数
    setupBoundary : function() {
        var MVing = this;
        var opt   = MVing.options;
        var bb    = opt.boundaryBy;
        var rcTa  = MVing.rect.target;

        // 自定义函数
        if(_.isFunction(bb)){
            MVing.boundaryBy = bb;
        }
        // 采用内置函数
        else if(!_.isUndefined(bb)){
            // 数字
            if(_.isNumber(bb)){
                // 0 咯，表示中心点
                if(bb == 0) {
                    MVing.__boundary_w = 0;
                    MVing.__boundary_h = 0;
                }
                // 浮点数表示宽高的百分比
                else if(bb > -1 && bb < 1) {
                    MVing.__boundary_w = bb * rcTa.width;
                    MVing.__boundary_h = bb * rcTa.height;
                }
                // 那么就是要根据变成计算咯
                else {
                    MVing.__boundary_w = bb + rcTa.width;
                    MVing.__boundary_h = bb + rcTa.height;
                }
            }
            // 百分比
            else if(/^((\d*\.\d+)|(\d+))%$/.test(bb)){
                var s = bb.substring(0, bb.length-1) * 0.01;
                MVing.__boundary_w = s * rcTa.width;
                MVing.__boundary_h = s * rcTa.height;
                console.log(MVing.__boundary_w, MVing.__boundary_h)
            }
            // 额，什么鬼？
            else {
                throw "Unsupport boundaryBy: " + bb;
            }

            // 设置函数
            MVing.boundaryBy = function(){
                var x = this.rect.current.x;
                var y = this.rect.current.y;
                var w = this.__boundary_w;
                var h = this.__boundary_h;
                return $D.rect.create([x, y, w, h], "xywh");
            };
        }
    },
    //.......................................................
    // 创建遮罩层相关 DOM 结构
    setupMask : function() {
        var MVing = this;
        var opt   = MVing.options;
        
        // 创建顶级元素 
        MVing.$mask = $('<div class="z-moving-mask">').css({
            position : "fixed",
            top:0, left:0, right:0, bottom:0,
            "z-index" : opt.maskZIndex
        }).appendTo(document.body);

        if(opt.maskClass)
            MVing.$mask.addClass(opt.maskClass);

        // 创建遮罩内部的元素
        MVing.mask = {
            $viewport : $('<div class="z-mvm-viewport">').appendTo(MVing.$mask),
            $sensors  : $('<div class="z-mvm-sensors">').appendTo(MVing.$mask),
            $target   : $('<div class="z-mvm-target">').appendTo(MVing.$mask),
            $assline  : $('<canvas class="z-mvm-assline">').appendTo(MVing.$mask),
        }

        // 获取绘制接口
        MVing.mask.G2Dass = MVing.mask.$assline[0].getContext("2d");

        // 设置遮罩层的显示属性
        MVing.$mask.children().css({
            "position" : "fixed",
            "overflow" : "hidden",
            top:0, left:0, 
        });
        // 视口
        MVing.mask.$viewport
            .css($z.pick(MVing.rect.viewport,
                        ["top","left","width","height"]));
        // 感应器
        MVing.mask.$sensors.css({
            right:0, bottom:0
        });
        // 目标
        MVing.mask.$target
            .css($z.pick(MVing.rect.target,
                        ["top","left","width","height"]));
        // 辅助线绘制层
        MVing.mask.$assline.css({
            right:0, bottom:0
        }).attr({
            width  : MVing.$mask.width(),
            height : MVing.$mask.height()
        });
    },
    //.......................................................
    __add_builtin_sensor : function(MVing, name, rectA, rectB, func) {
        var opt   = MVing.options;

        // 向上滚动
        MVing.sensors.push({
            name : name,
            rect : rectA,
            inViewport : false,
            visibility : false,
            matchBreak : true,
            scrollStep : opt.scrollStep * -1,
            handler : func,
        });
        // 向下滚动
        MVing.sensors.push({
            name : name,
            rect : rectB,
            inViewport : false,
            visibility : false,
            matchBreak : true,
            scrollStep : opt.scrollStep,
            handler : func,
        });
        // 设置响应函数函数
        MVing.sensorFunc[name] = {
            "enter" : function(sensor){
                if(!sensor._S_HDL) {
                    sensor._S_HDL = window.setInterval(function(sensor, MVing){
                        sensor.handler.call(MVing);
                    }, opt.scrollInterval, sensor, MVing);
                }
            },
            "leave" : function(sensor){
                if(sensor._S_HDL) {
                    window.clearInterval(sensor._S_HDL);
                    sensor._S_HDL = null;
                }
            }
        }
    },
    //.......................................................
    // 创建所有的感应器
    setupSensors : function() {
        var MVing = this;
        var opt   = MVing.options;

        // 准备感应器
        MVing.sensors = [];
        MVing.sensorFunc = {};

        // 内置滚动感应器
        if(opt.scrollSensor) {
            var ss   = opt.scrollSensor;
            var rcVp = MVing.rect.viewport;
            // 垂直滚动感应器
            if(ss.y){
                var sV = $z.dimension(ss.y, rcVp.height);
                MVs.__add_builtin_sensor(MVing, "_scroll_v",
                    $D.rect.create([0,0,rcVp.top-sV,0], "tlbr"),
                    $D.rect.create([rcVp.bottom-sV,0,0,0], "tlbr"),
                    function(sensor){
                        console.log("move y:" + sensor.scrollStep);
                    }
                );
            }
            // 水平滚动感应器
            if(ss.x){
                var sV = $z.dimension(ss.y, rcVp.height);
                MVs.__add_builtin_sensor(MVing, "_scroll_v",
                    $D.rect.create([rcVp.top, 0, rcVp.bottom, rcVp.right + sV], "tlbr"),
                    $D.rect.create([rcVp.top, rcVp.right - sV, rcVp.bottom, 0], "tlbr"),
                    function(sensor){
                        console.log("move x:" + sensor.scrollStep);
                    }
                );
            }
        }

        // 加入自定义的感应器
        if(_.isArray(opt.sensors)){
            MVing.sensors = MVing.sensors.concat(opt.sensors);
        }
        // 动态计算的感应器
        else if(_.isFunction(opt.sensors)) {
            var ses = opt.sensors.call(MVing);
            MVing.sensors = MVing.sensors.concat(ses);
        }

        // 加入自定义的感应器方法
        _.extend(MVing.sensorFunc, opt.sensorFunc);

        // 循环绘制可见的感应器
        var rcVp = MVing.rect.viewport;
        var baSc = MVing.viewportScroll;
        for(var i=0; i<MVing.sensors.length; i++) {
            var sen = MVing.sensors[i];
            // 无视不可见的感应器
            if(!sen.visibility)
                continue;

            var jSen = $('<div class="z-mvm-sit"><section></section></div>');
            var css;

            // 绘制视口内感应器
            if(sen.inViewport) {
                sen.$helper = jSen.appendTo(MVing.mask.$viewport);
                css = $D.rect.relative(sen.rect, rcVp, true, baSc);
                css.position = "absolute";
            }
            // 绘制视口外感应器
            else {
                sen.$helper = jSen.appendTo(MVing.mask.$sensors);
                css = $z.pick(sen.rect, ["top","left","width","height"]);
                css.position = "fixed";
            }

            // 为感应器设置 css
            jSen.css(css);
        }

        // 搞完，收工 ^_^
    },
    //.......................................................
    // 根据当前上下文的指针信息，计算目标矩形，考虑到边界等约束
    calculateTarget : function(){
        var MVing = this;
        var opt   = MVing.options;

        // 关键变量
        var rcTa = MVing.rect.target;
        var rcVp = MVing.rect.viewport;
        var cucl = MVing.cursor.client;
        var post = MVing.posAt.target;

        // 要计算的矩形
        var rect = $D.rect.create([0,0,rcTa.width, rcTa.height], "tlwh");

        // 根据鼠标指针当前位置，计算出目标矩形的新位置
        rect.top  = cucl.y - post.y;
        rect.left = cucl.x - post.x;

        // 重算矩形其他属性，并更新上下文矩形信息
        MVing.rect.current = $D.rect.count_tlwh(rect);

        // TODO 这里进行栅格的磁力辅助线约束

        // 约束边界
        var bRe = $z.invoke(MVing, "boundaryBy", [rcTa], MVing);
        if(bRe){
            //console.log($D.rect.dumpValues(bRe));
            var bRe2 = $D.rect.boundaryIn(bRe, rcVp);
            $D.rect.move_xy(rect, bRe2);
        }

        // 计算当前目标与视口关系
        var baSc = MVing.viewportScroll;
        MVing.css.rect = $D.rect.relative(rect, rcVp, true, baSc);
        MVing.css.current = $z.pick(MVing.css.rect, opt.cssBy);
    },
    //.......................................................
    // 将 mask.$target 更新到上下文计算好的矩形形状和位置
    updateTargetCss : function(){
        var MVing = this;
        MVing.mask.$target
            .css($z.pick(MVing.rect.current,
                        ["top","left","width","height"]));
    },
    //.......................................................
};
//...........................................................
// 根据鼠标的移动判断是否进入移动时
function on_mousemove(e) {
    var MVing = e.data;
    var opt   = MVing.options;

    // 更新指针
    MVs.updateCursor.call(MVing, e.clientX, e.clientY);

    // 如果在感应器之内，看看是否移出了这个感应器

    // 看看是否进入了某个感应器

    // 如果有上下文，那么必然进入了移动时
    if(window.__nutz_moving) {
        // console.log(MVing.cursor.client, MVing.cursor.viewport)

        // 计算当前矩形 (考虑边界以及移动约束)
        // 并计算 css 段
        MVs.calculateTarget.call(MVing);

        //console.log(MVing.rect.viewport)

        // 更新目标的遮罩替身 css 位置
        MVs.updateTargetCss.call(MVing);

        // 回调: on_ing
        $z.invoke(opt, "on_ing", [], MVing);

        // 绘制辅助线

    }
    // 判断是否可以进入（移动超过了阀值即可）
    else if( !MVing.endInMs 
            && (Math.abs(MVing.cursor.offset.x) > opt.fireRedius 
                || Math.abs(MVing.cursor.offset.y) > opt.fireRedius)) {
        
        console.log("enter moving", MVing)

        // 标识进入移动时
        window.__nutz_moving = MVing;

        // 设置边界计算函数
        MVs.setupBoundary.call(MVing);

        // 创建遮罩层
        MVs.setupMask.call(MVing);

        // 附加感应器
        MVs.setupSensors.call(MVing);

        // 计算当前矩形 (考虑边界以及移动约束)
        // 并计算 css 段
        MVs.calculateTarget.call(MVing);

        // 更新目标的遮罩替身 css 位置
        MVs.updateTargetCss.call(MVing);

        // 回调: on_begin
        $z.invoke(opt, "on_begin", [], MVing);

        // 回调: on_ing
        $z.invoke(opt, "on_ing", [], MVing);

        // 绘制辅助线 
        
    }
    // else{
    //     console.warn("weird!", MVing)
    // }
}
//...........................................................
// 退出销毁移动时
function on_mouseup(e){
    var MVing = e.data;
    var opt   = MVing.options;

    // 标识结束
    MVing.endInMs = Date.now();

    console.log("I am up", MVing === window.__nutz_moving, MVing.$mask)

    // 去掉监听
    $(MVing.win)
        .off("mouseup", on_mouseup)
        .off("mousemove", on_mousemove);

    // 已经启用了，才销毁
    if(window.__nutz_moving) {
        // 回调: on_end
        $z.invoke(opt, "on_end", [], MVing);

        console.log("remove IT")

        // 释放遮罩层
        MVing.$mask.remove();

        // 销毁上下文
        window.__nutz_moving = null;
    }
}
//...........................................................
// 进入移动时的入口函数
function on_mousedown(e){
    // 从上下文原型中构建副本
    var MVing = _.extend({}, e.data);
    var opt   = MVing.options;
    console.log("I am mousedown")
    //...........................................
    // 确定触发者和开始时间
    MVing.$trigger  = $(e.currentTarget);
    MVing.startInMs = Date.now();
    //...........................................
    // 找到目标和视口
    if(!MVs.findElement.call(MVing, "target", "$trigger"))
        return;
    if(!MVs.findElement.call(MVing, "viewport", "$selection"))
        return;
    //...........................................
    // 计算视口的滚动补偿
    MVing.viewportScroll = {
        x : MVing.$viewport[0].scrollLeft,
        y : MVing.$viewport[0].scrollTop,
    };
    //...........................................
    // 准备收集各种尺寸和位置
    MVing.rect = {};
    // 指定了视口的尺寸
    if(_.isFunction(opt.viewportRect)){
        MVing.rect.viewport = opt.viewportRect.call(MVing);
    }
    // 指定了元素
    else if($z.isRect(opt.viewportRect)){
        MVing.rect.viewport = opt.viewportRect;
    }
    // 默认自行计算
    else {
        MVing.rect.viewport = $D.rect.gen(MVing.$viewport,{
            boxing   : "content",
            scroll_c : true,
        });
    }
    //...........................................
    // 目标尺寸
    MVing.rect.target = $D.rect.gen(MVing.$target, {
        boxing   : "border",
        scroll_c : true,
        viewport : opt.viewportRect ? MVing.rect.viewport : null
    });
    MVing.rect.current = _.extend({}, MVing.rect.target);
    //...........................................
    // 确定位置/指针/方向
    var mX = e.clientX;
    var mY = e.clientY;
    // 位置
    MVing.posAt = {
        target   : $D.rect.ccs_point_tl(MVing.rect.target, mX, mY),
        client   : {x:mX, y:mY},
        viewport : $D.rect.ccs_point_tl(MVing.rect.viewport, mX, mY),
    };
    // 指针/方向
    MVs.updateCursor.call(MVing, mX, mY);
    //...........................................
    // 准备当前目标与视口关系
    MVing.css = {};
    //...........................................
    // 监控退出和进入
    $(MVing.win)
        .on("mouseup",   MVing, on_mouseup)
        .on("mousemove", MVing, on_mousemove);
}
//...........................................................
$.fn.extend({ "moving" : function(opt){
    // 如果窗口已经处于移动时，则抛错
    if(window.__nutz_moving)
        throw "NutzMoving: Already in moving!";

    // 确保配置对象不为空
    opt = opt || {};

    // 准备配置参数的默认值
    $z.setUndefined(opt, "trigger", "> *");
    $z.setUndefined(opt, "cssBy", ["top","left"]);
    $z.setUndefined(opt, "fireRedius", 3);
    $z.setUndefined(opt, "maskZIndex", 3);
    $z.setUndefined(opt, "scrollSensor", {
        x: 30, y: 30
    });

    // 设置默认滚动步长，当然，没设 scrollSensor 的话，也没卵用 
    $z.setUndefined(opt, "scrollStep", 10);
    opt.scrollStep = Math.abs(opt.scrollStep) || 10;
    // 滚动的时间间隔默认为 50ms
    $z.setUndefined(opt, "scrollInterval", 50);

    // 准备上下文的原型，在 mousedown 的时候会 copy
    var doc = this[0].ownerDocument;
    var MVing = {
        options : opt,
        win     : doc.defaultView,
        doc     : doc,
        body    : doc.body,
        $body   : $(doc.body),
        $selection : this,
    };

    // 监控鼠标事件 mousedown 以便进入移动时
    this.on("mousedown", opt.trigger, MVing, on_mousedown);
    
    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil, window.NutzDimension);

