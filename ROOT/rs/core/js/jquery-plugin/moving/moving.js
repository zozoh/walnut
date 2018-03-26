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
    findElement : function(key, dftKey, keepNotNull){
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
        // 一定是有值的
        else if(keepNotNull) {
            MVing["$" + key] = MVing[dftKey];
            return true;
        }
        return false;
    },
    //.......................................................
    // 计算鼠标指针
    updateCursor : function(x, y) {
        var MVing = this;
        var opt   = MVing.options;

        // 转换一下，成为窗体绝对位置
        var winX = x;
        var winY = y;
        if(MVing.isCusorRelativeClient) {
            winX += this.rect.client.left;
            winY += this.rect.client.top;
        }

        // 根据模式，约束 x | y
        if(MVing.posAt){
            if("x" == opt.mode) {
                y    = MVing.posAt.client.y;
                winY = MVing.posAt.win.y;
            }
            // 只能 Y 轴
            else if("y" == opt.mode){
                x    = MVing.posAt.client.x;
                winX = MVing.posAt.win.x;
            }
        }
        // 创建初始点击位置
        else {
            MVing.posAt = {
                win      : {x:winX, y:winY},
                client   : $D.rect.ccs_point_tl(MVing.rect.client, winX, winY),
                viewport : $D.rect.ccs_point_tl(MVing.rect.viewport, winX, winY),
                target   : $D.rect.ccs_point_tl(MVing.rect.target, winX, winY),
            };
        }

        // 更新
        if(MVing.cursor) {
            MVing.cursor.delta.x  = winX - MVing.cursor.win.x;
            MVing.cursor.delta.y  = winY - MVing.cursor.win.y;
            MVing.cursor.offset.x = winX - MVing.posAt.win.x;
            MVing.cursor.offset.y = winY - MVing.posAt.win.y;
            MVing.cursor.win.x    = winX;
            MVing.cursor.win.y    = winY;
            MVing.cursor.client   = $D.rect.ccs_point_tl(MVing.rect.client, winX, winY),
            MVing.cursor.viewport = $D.rect.ccs_point_tl(MVing.rect.viewport, winX, winY),
            MVing.direction.delta.x = direction(MVing.cursor.delta.x, "left", "right");
            MVing.direction.delta.y = direction(MVing.cursor.delta.y, "up", "down");
            MVing.direction.offset.x = direction(MVing.cursor.offset.x, "left", "right");
            MVing.direction.offset.y = direction(MVing.cursor.offset.y, "up", "down");
        }
        // 创建
        else {
            // 鼠标
            MVing.cursor = {
                win      : {x:winX, y:winY},
                client   : $D.rect.ccs_point_tl(MVing.rect.client, winX, winY),
                viewport : $D.rect.ccs_point_tl(MVing.rect.viewport, winX, winY),
                delta    : {x:0, y:0},
                offset   : {x:0, y:0},
            };
            // 方向
            MVing.direction = {
                delta  : {x : null, y : null},
                offset : {x : null, y : null},
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
            $sensors  : $('<div class="z-mvm-sensors">').appendTo(MVing.$mask),
            $client   : $('<div class="z-mvm-client">').appendTo(MVing.$mask),
            $target   : $('<div class="z-mvm-target">').appendTo(MVing.$mask),
        }
        MVing.mask.$clientCon
            = $('<div class="z-mvm-client-con"></div>')
                .appendTo(MVing.mask.$client);

        // 如果 client 和 viewport 是重合的
        if(MVing.isViewportSameWithClient){
            MVing.viewportIsClient  = true;
            MVing.mask.$viewport    = MVing.mask.$client.addClass("z-mvm-viewport");
            MVing.mask.$viewportCon = MVing.mask.$clientCon.addClass("z-mvm-viewport-con");
        }
        // 否则在 client 里面增加视口对象
        else {
            MVing.viewportIsClient = false;
            MVing.mask.$viewport
                = $('<div class="z-mvm-viewport"></div>')
                    .appendTo(MVing.mask.$clientCon);
            MVing.mask.$viewportCon
                = $('<div class="z-mvm-viewport-con"></div>')
                    .appendTo(MVing.mask.$viewport);
        }

        // 设置遮罩层的显示属性
        MVing.$mask.children().css({
            "position" : "fixed",
            "overflow" : "hidden",
            top:0, left:0, 
        });

        // 鼠标响应区
        MVing.mask.$client.css($z.pick(MVing.rect.client, ["top","left","width","height"]));
        MVing.mask.$clientCon.css({
            "width":"100%", "height":"100%", "position":"relative",
            "overflow":"hidden"
        });
        MVing.mask.$clientPlaceHolder
            = $('<aside class="cl-placeholder">')
                .appendTo(MVing.mask.$clientCon).css({
                    "width"  : MVing.$client[0].scrollWidth,
                    "height" : MVing.$client[0].scrollHeight,
                });
        MVing.mask.$clientCon[0].scrollTop  = MVing.$client[0].scrollTop;
        MVing.mask.$clientCon[0].scrollLeft = MVing.$client[0].scrollLeft;

        // 视口: 不重叠，创建新的
        if(!MVing.viewportIsClient){
            var rrvp = $D.rect.relative(
                            MVing.rect.viewport,
                            MVing.rect.client,
                            true,
                            MVing.$client);
            MVing.mask.$viewport.css(_.extend({
                "position" : "absolute"
            }, $z.pick(rrvp, ["top","left","width","height"])));
            MVing.mask.$viewportCon.css({
                "width":"100%", "height":"100%", "position":"relative",
                "overflow":"hidden"
            });
        }

        // 窗口级感应器
        MVing.mask.$sensors.css({
            right:0, bottom:0
        });

        // 目标
        MVing.mask.$target
            .css($z.pick(MVing.rect.target,
                        ["top","left","width","height"]));
    },
    //.......................................................
    __update_assist : function(MVing){
        var opt  = MVing.options;
        if(opt.assist) {
            // 如果没有创建辅助层，创建它
            if(!MVing.mask.assist) {
                MVing.mask.assist = {
                    $root : $z.svg.createRoot({
                        "position" : "fixed",
                        "width"  : "100%",
                        "height" : "100%",
                        top:0, left:0
                    }).appendTo(MVing.$mask)
                };

                // 添加辅助线
                var lineStyle = {
                    "stroke" : "#0FF",
                    "stroke-width" : .5
                };
                MVing.mask.assist.$lineX = $z.svg.create("line", null, lineStyle)
                    .appendTo(MVing.mask.assist.$root);
                MVing.mask.assist.$lineY = $z.svg.create("line", null, lineStyle)
                    .appendTo(MVing.mask.assist.$root);
            }
            // 更新坐标轴
            if(opt.assist.axis){
                var key_x = opt.assist.axis[0];
                var key_y = opt.assist.axis[1];
                var aX  = MVing.rect.current[key_x];
                var aY  = MVing.rect.current[key_y];
                var aVP = MVing.rect.viewport;
                if(opt.assist.axisFullScreen) {
                    if($D.rect.isRect(opt.assist.axisFullScreen)){
                        aVP = opt.assist.axisFullScreen;
                    }else{
                        aVP = $D.rect.create([0,0,MVing.$mask.width(),MVing.$mask.height()]);
                    }
                }

                // 更新 X 轴方向
                MVing.mask.assist.$lineX.attr({
                    "x1" : aX,  "y1" : aVP.top,
                    "x2" : aX,  "y2" : aVP.bottom,
                });

                // 更新 Y 轴方向
                MVing.mask.assist.$lineY.attr({
                    "x1" : aVP.left,   "y1" : aY,
                    "x2" : aVP.right,  "y2" : aY,
                });
            }
        }
    },
    //.......................................................
    __add_builtin_sensor : function(MVing, name, rectA, rectB, func) {
        var opt   = MVing.options;

        // 向上滚动
        MVing.sensors.push({
            name : name,
            rect : rectA,
            scope : "win",
            visible : false,
            matchBreak : false,
            scrollStep : opt.scrollStep * -1,
            handler : func,
        });
        // 向下滚动
        MVing.sensors.push({
            name : name,
            rect : rectB,
            scope : "win",
            visible : false,
            matchBreak : false,
            scrollStep : opt.scrollStep,
            handler : func,
        });
        // 设置响应函数函数
        MVing.sensorFunc[name] = {
            "enter" : function(sen){
                if(!sen._S_HDL) {
                    sen._S_HDL = window.setInterval(function(sen, MVing){
                        // 如果移动都结束了，那么这个也要停止
                        if(MVing.endInMs || !sen.actived) {
                            if(sen._S_HDL) {
                                window.clearInterval(sen._S_HDL);
                                sen._S_HDL = null;
                            }
                        }
                        // 调用响应函数
                        else{
                            sen.handler.call(MVing, sen);
                        }
                    }, opt.scrollInterval, sen, MVing);
                }
            },
            "hover" : function(sen) {
                // 如果移动都结束了，那么这个也要停止
                if(MVing.endInMs || !sen.actived) {
                    if(sen._S_HDL) {
                        window.clearInterval(sen._S_HDL);
                        sen._S_HDL = null;
                    }
                }
            },
            "leave" : function(sen){
                if(sen._S_HDL) {
                    window.clearInterval(sen._S_HDL);
                    sen._S_HDL = null;
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
        MVing.currentSensor = [];

        // 加入自定义的感应器
        if(_.isArray(opt.sensors)){
            MVing.sensors = MVing.sensors.concat(opt.sensors || []);
        }
        // 动态计算的感应器
        else if(_.isFunction(opt.sensors)) {
            var ses = opt.sensors.call(MVing) || [];
            MVing.sensors = MVing.sensors.concat(ses);
        }

        // 内置滚动感应器
        if(opt.scrollSensor) {
            var ss   = opt.scrollSensor;
            var rcVp = MVing.rect.client;
            var win  = $z.winsz();
            var eClient = MVing.$client[0];
            // 垂直滚动感应器
            if(ss.y){
                var sV = $z.dimension(ss.y, rcVp.height);
                MVs.__add_builtin_sensor(MVing, "_scroll_v",
                    $D.rect.create([0,0,rcVp.top+sV,win.width], "tlbr"),
                    $D.rect.create([rcVp.bottom-sV,0,win.height,win.width], "tlbr"),
                    function(sen){
                        // 滚动视口
                        var oldS = eClient.scrollTop;
                        eClient.scrollTop = oldS + sen.scrollStep;
                        this.mask.$clientPlaceHolder.css({
                            "width"  : eClient.scrollWidth,
                            "height" : eClient.scrollHeight,
                        });
                        this.mask.$clientCon[0].scrollTop = eClient.scrollTop;
                        // 重新计算目标位置
                        if(oldS != eClient.scrollTop) {
                            MVs.syncClientScroll.call(this);
                            MVs.calculateTarget.call(this);
                            MVs.updateTargetCss.call(this);
                            $z.invoke(opt, "on_ing", [], this);
                            MVs.__update_assist(this);
                        }
                    }
                );
            }
            // 水平滚动感应器
            if(ss.x){
                var sV = $z.dimension(ss.y, rcVp.height);
                MVs.__add_builtin_sensor(MVing, "_scroll_h",
                    $D.rect.create([rcVp.top, 0, rcVp.bottom, rcVp.left + sV], "tlbr"),
                    $D.rect.create([rcVp.top, rcVp.right - sV, rcVp.bottom, win.width], "tlbr"),
                    function(sen){
                        // 滚动视口
                        var oldS = eClient.scrollLeft;
                        eClient.scrollLeft = oldS + sen.scrollStep;
                        this.mask.$clientPlaceHolder.css({
                            "width"  : eClient.scrollWidth,
                            "height" : eClient.scrollHeight,
                        });
                        this.mask.$clientCon[0].scrollLeft = eClient.scrollLeft;
                        // 重新计算目标位置
                        if(oldS != eClient.scrollLeft) {
                            MVs.syncClientScroll.call(this);
                            MVs.calculateTarget.call(this);
                            MVs.updateTargetCss.call(this);
                            $z.invoke(opt, "on_ing", [], this);
                            MVs.__update_assist(this);
                        }
                    }
                );
            }
        }

        // 加入自定义的感应器方法
        _.extend(MVing.sensorFunc, opt.sensorFunc);

        // 循环绘制可见的感应器
        var baSc = {
            x : MVing.$client[0].scrollLeft,
            y : MVing.$client[0].scrollTop,
        };
        for(var i=MVing.sensors.length-1; i>=0; i--) {
            var sen = MVing.sensors[i];
            // 记录感应器下标
            sen.index = i;

            // 感应器默认为不激活
            sen.actived = false;

            // 确保是 jQuery 对象
            if(_.isElement(sen.$ele)){
                sen.$ele = $(sen.$ele);
            }
            
            // 设置默认值
            $z.setUndefined(sen, "visible", true);
            $z.setUndefined(sen, "matchBreak", true);

            // 计算感应器范围
            if(!sen.scope) {
                // 视口
                if(sen.$ele && sen.$ele.parent().closest(MVing.$viewport).length>0){
                    sen.scope = "viewport";
                }
                // 鼠标捕捉区
                else if(sen.$ele && sen.$ele.parent().closest(MVing.$client).length>0){
                    sen.scope = "client";
                }
                // 默认为顶级
                else {
                    sen.scope = "win";
                }
            }

            // 计算感应器矩形
            // 不是矩形的话，自动计算一个
            if(!$D.rect.isRect(sen.rect)){
                if(!sen.$ele){
                    throw "sensor without rect and $ele "+$z.toJson(sen);
                }
                var padding = sen.rect;
                if("win"==sen.scope || !MVing.isCusorRelativeClient){
                    sen.rect = $D.rect.gen(sen.$ele, padding);
                    // console.log("sen", i, sen.text, sen.data.mime, padding,
                    //         $D.rect.dumpValues(sen.rect));
                }
                // 否则取一下相对
                else {
                    sen.rect = MVing.getRectInClient(sen.$ele, padding);
                }
            }

            // 记录感应器原始矩形
            sen._org_rect = _.extend({}, sen.rect);

            // 无视不可见的感应器
            if(!sen.visible)
                continue;

            var jSen = $('<div class="z-mvm-sit"><section><aside md="x"></aside><aside md="y"></aside></section></div>');
            var css;

            // 绘制鼠标捕捉区感应器
            if("client" == sen.scope) {
                sen.$helper = jSen.appendTo(MVing.mask.$clientCon);
                css = $D.rect.relative(sen.rect, MVing.rect.client, true, baSc);
                css = $z.pick(css, ["top","left","width","height"]);
                css.position = "absolute";
                //console.log(sen.text, baSc, sen.rect, css)
            }
            // 绘制视口内感应器
            else if("viewport" == sen.scope) {
                sen.$helper = jSen.appendTo(MVing.mask.$viewportCon);
                css = $D.rect.relative(sen.rect, MVing.rect.viewport, true, baSc);
                css = $z.pick(css, ["top","left","width","height"]);
                css.position = "absolute";
            }
            // 绘制窗体感应器
            else {
                sen.$helper = jSen.appendTo(MVing.mask.$sensors);
                css = $z.pick(sen.rect, ["top","left","width","height"]);
                css.position = "fixed";
            }

            // 为感应器设置 css
            jSen.css(css).attr("se-name", sen.name);

            if(sen.className)
                jSen.addClass(sen.className);

            if(sen.disabled)
                jSen.attr("se-disabled", "yes");

            // 为感应器设置文字
            if(sen.text){
                $('<span>').html(sen.text).appendTo(jSen.find("section"));
            }
        }

        // 搞完，收工 ^_^
    },
    //.......................................................
    // 根据视口的滚动偏移量，修改所有的 inViewport 的传感器
    syncClientScroll : function(){
        var MVing = this;

        // 计算视口滚动的偏移量
        var offX = MVing.clientScroll.x - MVing.$client[0].scrollLeft;
        var offY = MVing.clientScroll.y - MVing.$client[0].scrollTop;
        //console.log("offX:", offX, "offY:", offY)

        // 循环处理各个传感器
        for(var i=0; i<MVing.sensors.length; i++) {
            var sen = MVing.sensors[i];
            // 只考虑 client 内的传感器
            if("win" != sen.scope){
                sen.rect.top  = sen._org_rect.top  + offY;
                sen.rect.left = sen._org_rect.left + offX;
                $D.rect.count_tlwh(sen.rect);
                // console.log("S"+sen.index,sen.text,
                //     $D.rect.dumpValues(sen.rect, "tl"),
                //     $D.rect.dumpValues(sen._org_rect, "tl"))
            }
        }
    },
    //.......................................................
    // 根据当前上下文的指针信息，计算目标矩形，考虑到边界等约束
    calculateTarget : function(){
        var MVing = this;
        var opt   = MVing.options;

        // 要计算的矩形
        var rect = $D.rect.create([
            0, 0, MVing.rect.target.width, MVing.rect.target.height]
            ,"tlwh");

        // 根据鼠标指针当前位置，计算出目标矩形的新位置
        rect.top  = MVing.cursor.win.y - MVing.posAt.target.y;
        rect.left = MVing.cursor.win.x - MVing.posAt.target.x;

        // 重算矩形其他属性，并更新上下文矩形信息
        MVing.rect.current = $D.rect.count_tlwh(rect);

        // TODO 这里进行栅格的磁力辅助线约束

        // 约束边界
        var bbTa = $z.invoke(MVing, "boundaryBy", [], MVing);
        if(bbTa){
            //console.log($D.rect.dumpValues(bRe));
            var bRe2 = $D.rect.boundaryIn(bbTa, MVing.rect.viewport);
            $D.rect.move_xy(rect, bRe2);
        }

        // 计算当前目标与视口关系
        var baseScroll = MVing.ignoreViewportScroll
                            ? null
                            : MVing.$client;
        MVing.css.rect = $D.rect.relative(rect, MVing.rect.viewport, true, baseScroll);
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
    /*.......................................................
    匹配感应器，返回一个这样的数据结构:
    {
        enter : [0],
        leave : [2,4],
        hover : [0,3,5,6]
    }
    同时它会更新 MVing.currentSensor 为 hover 的全部数据
    */
    matchedSensors : function(){
        var MVing = this;
        // 无感应器，呵呵吧
        if(!MVing.sensors || MVing.sensors.length == 0)
            return;

        // 查一下当前都匹配上了谁 
        var re = {
            enter : [],
            hover : [],
        };
        for(var i=MVing.sensors.length-1; i>=0; i--) {
            var sen = MVing.sensors[i];
            // 目标中心点为准，看看是不是在感应区内
            if($D.rect.is_in(sen.rect, MVing.rect.current)){
                // 禁止的感应器，不做任何操作
                if(!sen.disabled)
                    re.hover.push(sen.index);
                // 匹配上就退出
                if(sen.matchBreak)
                    break;
            }
        }

        // if(re.hover.length>0) {
        //     console.log("match hover", re.hover);
        // }

        // 根据匹配上的进行计算
        var last  = MVing.currentSensor || [];
        for(var i=0; i<re.hover.length; i++) {
            var senIndex = re.hover[i];
            // 之前就已经有了，则去掉，余下的表示 leave
            var x = last.indexOf(senIndex);
            if(x >= 0){
                last[x] = -1;
            }
            // 不在里面的，表示首次进入
            else {
                re.enter.push(senIndex);
            }
        }
        // 填入 leave
        re.leave = _.without(last, -1);
        MVing.currentSensor = re.hover;
        // 返回
        return re;
    },
    //.......................................................
    // 调用感应器回调
    invokeSensorFunc : function(eventName, senIndexes){
        var MVing = this;
        // 无感应器，呵呵吧
        if(!MVing.sensors || MVing.sensors.length == 0 || senIndexes.length == 0)
            return;
        // 依次查找
        for(var i=0; i<senIndexes.length; i++) {
            var sen = MVing.sensors[senIndexes[i]];
            var funcSet = MVing.sensorFunc[sen.name];
            //console.log("invoke", eventName, sen);
            if(funcSet) {
                var func = funcSet[eventName];
                if(_.isFunction(func)){
                    //console.log("call", eventName, sen);
                    func.call(MVing, sen);
                }
            }
            // 如果进入感应器
            if("enter" == eventName) {
                sen.actived = true;
                if(sen.$helper)
                    sen.$helper.attr("se-actived", "yes");
            }
            // 如果离开感应器
            else if("leave" == eventName) {
                sen.actived = false;
                if(sen.$helper)
                    sen.$helper.removeAttr("se-actived");
            }
        }
    },
    //.......................................................
};
//...........................................................
// 根据鼠标的移动判断是否进入移动时
function on_mousemove(e) {
    var MVing = e.data;
    var opt = MVing.options;
    
    // 更新指针
    MVs.updateCursor.call(MVing, e.clientX, e.clientY);

    // 如果在感应器之内，看看是否移出了这个感应器

    // 看看是否进入了某个感应器

    // 如果有上下文，那么必然进入了移动时
    if(window.__nutz_moving) {
        MVing = window.__nutz_moving;
        opt   = MVing.options;

        // console.log("A", typeof MVing.mask, MVing == window.__nutz_moving, 
        //     window.__nutz_moving.mask);
        
        // 计算当前矩形 (考虑边界以及移动约束)
        // 并计算 css 段
        MVs.calculateTarget.call(MVing);

        // 匹配感应器
        var ms = MVs.matchedSensors.call(MVing);
        //console.log(ms)
        if(ms) {
            MVs.invokeSensorFunc.call(MVing, "leave", ms.leave);
            MVs.invokeSensorFunc.call(MVing, "enter", ms.enter);
            MVs.invokeSensorFunc.call(MVing, "hover", ms.hover);
        }

        // 更新目标的遮罩替身 css 位置
        MVs.updateTargetCss.call(MVing);

        // 回调: on_ing
        $z.invoke(opt, "on_ing", [], MVing);

        // 绘制辅助线
        MVs.__update_assist(MVing);
    }
    // 判断是否可以进入（移动超过了阀值即可）
    else if( !MVing.endInMs 
            && (Math.abs(MVing.cursor.offset.x) > opt.fireRedius 
                || Math.abs(MVing.cursor.offset.y) > opt.fireRedius)) {
        
        //console.log("enter moving", MVing)

        // 标识进入移动时
        window.__nutz_moving = MVing;

        // 设置边界计算函数
        MVs.setupBoundary.call(MVing);

        // 创建遮罩层
        MVs.setupMask.call(MVing);

        // 回调: on_begin
        $z.invoke(opt, "on_begin", [], MVing);

        // 附加感应器
        MVs.setupSensors.call(MVing);

        // 计算当前矩形 (考虑边界以及移动约束)
        // 并计算 css 段
        MVs.calculateTarget.call(MVing);

        // 更新目标的遮罩替身 css 位置
        MVs.updateTargetCss.call(MVing);

        // 回调: on_ing
        $z.invoke(opt, "on_ing", [], MVing);

        // 绘制辅助线 
        MVs.__update_assist(MVing);
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

    // 去掉监听
    $(MVing.win)
        .off("mouseup", on_mouseup)
        .off("mousemove", on_mousemove);

    // 已经启用了，才销毁
    if(window.__nutz_moving) {
        // 回调: on_end
        $z.invoke(opt, "on_end", [], MVing);

        // 释放遮罩层
        MVing.$mask.remove();

        // 销毁上下文
        window.__nutz_moving = null;

        // 因为启用了拖拽，那么在100ms内禁止页面上的点击
        window.__forbid_click = true;
        window.setTimeout(function(){
            window.__forbid_click = undefined;
        }, 100);
    }
}
//...........................................................
// 进入移动时的入口函数
function on_mousedown(e){
    // 必须鼠标左键才行
    if(1 != e.which){
        return;
    }
    // 从上下文原型中构建副本
    var MVing = _.extend({
        Event : e,
    }, e.data);
    // 生成配置项的新副本
    var opt = _.extend({}, MVing.options);
    MVing.options = opt;
    //console.log("I am mousedown")
    //...........................................
    // 确定触发者和开始时间
    MVing.$trigger  = $(e.currentTarget);
    MVing.startInMs = Date.now();
    //...........................................
    // 调用初始化
    $z.invoke(opt, "init", [], MVing);
    //...........................................
    // 找到目标和视口
    if(!MVs.findElement.call(MVing, "target", "$trigger"))
        return;
    if(!MVs.findElement.call(MVing, "client", "$selection"))
        return;
    if(!MVs.findElement.call(MVing, "viewport", "$client", true))
        return;
    //...........................................
    // 视口原始的滚动
    MVing.clientScroll = {
        x : MVing.$client[0].scrollLeft,
        y : MVing.$client[0].scrollTop,
    };
    // 判断拖拽目标是否在捕获区域内
    MVing.isTargetInClient   = MVing.$target.parent().closest(MVing.$client).length>0;
    MVing.isViewportInClient = MVing.$viewport.parent().closest(MVing.$client).length>0;
    MVing.isViewportSameWithClient = MVing.$viewport[0] == MVing.$client[0];
    MVing.isCusorRelativeClient = true;
    //...........................................
    // 准备收集各种尺寸和位置
    MVing.rect = {};
    //..........................................
    // 指定了视口的尺寸
    if(_.isFunction(opt.clientRect)){
        MVing.rect.client = opt.clientRect.call(MVing);
    }
    // 直接指定了矩形对象
    else if($z.isRect(opt.clientRect)){
        MVing.rect.client = opt.clientRect;
    }
    // 没指定对象，那么就 client 就表示整个屏幕
    else {
        MVing.isCusorRelativeClient = false;
    }
    // 默认自行计算
    if(!MVing.rect.client) {
        //console.log("count default")
        MVing.rect.client = $D.rect.gen(MVing.$client, {
            boxing   : "border",
            scroll_c : true,
        });
    }
    //...........................................
    // 指定了鼠标事件的捕捉区域，会根据这个区域进行偏移
    if(_.isFunction(opt.viewportRect)){
        MVing.rect.viewport = opt.viewportRect.call(MVing);
    }
    // 直接指定了矩形对象
    else if($z.isRect(opt.viewportRect)){
        MVing.rect.viewport = opt.viewportRect;
    }
    // 默认自行计算
    if(!MVing.rect.viewport) {
        // 复用 client 的矩形
        if(MVing.isViewportSameWithClient) {
            MVing.rect.viewport = MVing.rect.client;
        }
        // 重新计算
        else {
            MVing.rect.viewport = $D.rect.gen(MVing.$viewport, 
                MVing.isViewportInClient ? MVing.rect.client : null);
        }
    }
    //...........................................
    // 目标尺寸
    MVing.rect.target = $D.rect.gen(MVing.$target, {
        boxing   : "border",
        scroll_c : true,
        viewport : MVing.isTargetInClient && MVing.isCusorRelativeClient
                        ? MVing.rect.client 
                        : null
    });
    MVing.rect.current = _.extend({}, MVing.rect.target);
    //...........................................
    // 确定位置/指针/方向
    MVs.updateCursor.call(MVing, e.clientX, e.clientY);
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

    if("destroy" == opt) {
        this.off("mousedown", on_mousedown);
        return this;
    }

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
    $z.setUndefined(opt, "scrollStep", 20);
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
        getRectInClient : function(jq, padding) {
            var re = $D.rect.gen(jq, {
                boxing   : "border",
                scroll_c : true,
                viewport : this.rect.client,
                padding  : padding
            });
            //console.log($D.rect.dumpValues(re));
            return re;
        }
    };

    // 监控鼠标事件 mousedown 以便进入移动时
    this.on("mousedown", opt.trigger, MVing, on_mousedown);
    
    // 返回自身以便链式赋值
    return this;
}});
//...........................................................
})(window.jQuery, window.NutzUtil, window.NutzDimension);

