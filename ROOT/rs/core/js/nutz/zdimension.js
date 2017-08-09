/**
 * 本文件将提供 Nutz-JS 关于尺度计算的相关帮助函数
 * 本函数，依赖 zutil.js
 *
 * @author  zozoh(zozohtnt@gmail.com)
 * 2012-10 First created
 */
(function ($, $z) {
//.................................................
// 所有与矩形相关的计算
var zRect = {
    //.............................................
    /* 根据一个 DOM 元素生成一个矩形
        ele - Element|jQuery 对象 （盒子模型必须为 box-sizing: border-box;）
        opt - 配置参数 {
            // 矩形区域在元素盒子模型的取值范围
            //  - margin  : 包括到自己的外边距
            //  - border  : 「默认」包括边框，内边距
            //  - content : 仅包括内容区域（不包括内边距和边框）
            boxing : "margin|border|content"

            // 窗口滚动补偿
            // true 要考虑到文档的 scrollTop/Left
            // 默认 true
            scroll_c : true,

            // 窗口所在的矩形（要进行坐标系变换）
            viewport : Rect

            // 计算矩形时，要去掉滚动条，默认 false
            overflow : false

            // 判断 overflow 时用的元素，默认采用 ele 参数给定元素
            overflowEle : jQuery || Element
            
            // 人工为矩形增加一个边距，负数则表示扩大
            padding: 0;
        }
    */
    gen : function(ele, opt){
        // 得到目标
        var jEle = $(ele);

        // 目标必须有元素，否则返回 null
        if(jEle.length == 0){
            return null;
        }

        // 分析配置参数
        if(_.isBoolean(opt)) {
            opt = {scroll_c : opt};
        }
        // 如果是矩形
        else if(this.isRect(opt)) {
            opt = {viewport : opt};
        }
        // 总要有个配置参数的
        else {
            opt = opt || {};
        }

        // 设置默认值 
        $z.setUndefined(opt, "boxing", "border");
        $z.setUndefined(opt, "scroll_c", "true");
        $z.setUndefined(opt, "padding", 0);

        // 准备返回值
        var rect;

        // 如果计算 body 或者 document 或者 window
        var ele = jEle[0];
        // 可能是 document 或 window
        if(!ele.ownerDocument){
            rect = $z.winsz(ele);
        }
        // <body> 的话，也是计算整个窗口宽度
        else if (ele.tagName == 'BODY'){
            rect = $z.winsz(jEle[0].ownerDocument.defaultView);
        }
        // 开始计算，得到相对于 document 的坐标
        else {
            rect = jEle.offset();

            // 进行窗口的滚动补偿
            // Firefox/Chrome 对于 scrollTop/Left 元素不同
            // FF 用的是 document.documentElement
            // Chrome 用的是 document.body
            // 这里用 jQuery 来做一下兼容
            if (opt.scroll_c) {
                var $doc   = $(jEle[0].ownerDocument);
                //console.log("scrollTop/Left", $doc.scrollTop(), $doc.scrollLeft());
                rect.top  -= $doc.scrollTop();
                rect.left -= $doc.scrollLeft();
                //console.log("rect:", this.dumpValues(rect));
            }

            // 包括外边距
            if ("margin" == opt.boxing) {
                rect.width  = jEle.outerWidth(true);
                rect.height = jEle.outerHeight(true);
                var style   = window.getComputedStyle(jEle[0]);
                rect.top   -= $z.toPixel(style.marginTop);
                rect.left  -= $z.toPixel(style.marginLeft);
            }
            // 只包括内容
            else if("content" == opt.boxing) {
                rect.width  = jEle.width();
                rect.height = jEle.height();
                var style   = window.getComputedStyle(jEle[0]);
                rect.top   += $z.toPixel(style.paddingTop)
                              + $z.toPixel(style.borderTopWidth);
                rect.left  += $z.toPixel(style.paddingLeft)
                              + $z.toPixel(style.borderLeftWidth);;
            }
            // 默认就是 border-box
            else {
                rect.width  = jEle.outerWidth();
                rect.height = jEle.outerHeight();
            }
        }
        

        // 去掉滚动条
        if(opt.overflow){
            var scrollbar = $z.scrollbar(opt.overflowEle || jEle);
            // 水平滚动条导致高度减小
            if(scrollbar.x) {
                rect.height -= $z.scrollBarHeight();
            }
            // 垂直滚动条导致宽度减小
            if(scrollbar.y) {
                rect.width -= $z.scrollBarWidth();
            }
        }

        // 增加边距
        if(opt.padding) {
            rect.width  -= opt.padding * 2;
            rect.height -= opt.padding * 2;
            rect.left   += opt.padding;
            rect.top    += opt.padding;
        }


        // 进行坐标系变换
        if(opt.viewport) {
            return this.translate(rect, opt.viewport.left*-1, opt.viewport.top*-1);
        }

        // 计算其他值并返回
        return this.count_tlwh(rect);
    },
    //.............................................
    isRect : function(rect){
        return rect 
            && _.isNumber(rect.top)
            && _.isNumber(rect.left)
            && _.isNumber(rect.width)
            && _.isNumber(rect.height)
            && _.isNumber(rect.right)
            && _.isNumber(rect.bottom);
    },
    //.............................................
    // 根据四个数值，创建一个矩形
    //  - values : 包含四个数值的数组，如果是字符串，会被分隔
    //  - mode   : 数值的意义 tlwh,tlbr,brwh,xywh
    create : function(values, mode) {
        // 没值就返回空
        if(!values)
            return null;

        // 处理输入值
        if(_.isString(values)){
            values = values.split(/[\t ]*[,|][\t ]*/);
        }
        // 确保是四个值
        if(!_.isArray(values) || values.length != 4) {
            throw "zdimension.rect.create: must be Array(4) !" + values;
        }
        // 确保是数字
        for(var i=0; i<values.length; i++){
            var v = values[i] * 1;
            if(isNaN(v)){
                throw "zdimension.rect.create: invalid values["
                        + i + "] : " + values[i];
            }
            values[i] = v;
        }

        // 模式: brwh
        if("brwh" == mode) {
            return this.count_brwh({
                bottom : values[0],
                right  : values[1],
                width  : values[2],
                height : values[3],
            });
        }

        // 模式: ltbr
        if("ltwh" == mode) {
            return this.count_tlwh({
                left   : values[0],
                top    : values[1],
                width  : values[2],
                height : values[3],
            });
        }

        // 模式: tlbr
        if("tlbr" == mode) {
            return this.count_tlbr({
                top    : values[0],
                left   : values[1],
                bottom : values[2],
                right  : values[3],
            });
        }

        // 模式: xywh
        if("xywh" == mode) {
            return this.count_xywh({
                x      : values[0],
                y      : values[1],
                width  : values[2],
                height : values[3],
            });
        }

        // 「默认」模式: tlwh
        return this.count_tlwh({
            top    : values[0],
            left   : values[1],
            width  : values[2],
            height : values[3],
        });
    },
    //.............................................
    // 快速精简的矩形信息，以便人类查看
    dumpValues : function(rect, keys){
        if(!rect)
            return "-nil-";
        keys = keys || "ltwh";
        var info = {
            t : rect.top,
            l : rect.left,
            w : rect.width,
            h : rect.height,
            r : rect.right,
            b : rect.bottom,
            x : rect.x,
            y : rect.y
        };
        var vs = [];
        for(var i=0; i< keys.length; i++){
            vs.push(info[keys.charAt(i)]);
        }
        return vs.join(",");
    },
    //.............................................
    // 快速精简的点信息，以便人类查看
    dumpPos : function(pos) {
        if(!pos)
            return "-nil-";
        return $z.tmpl("x:{{x}},y:{{y}}")(pos);
    },
    //.............................................
    // 自动根据矩形对象的值进行填充
    autoCount : function(rect, quiet) {
        // 有 width, height
        if (_.isNumber(rect.width) && _.isNumber(rect.height)) {
            if (_.isNumber(rect.top) && _.isNumber(rect.left))
                return this.count_tlwh(rect);
            if (_.isNumber(rect.bottom) && _.isNumber(rect.right))
                return this.count_brwh(rect);
            if (_.isNumber(rect.x) && _.isNumber(rect.x))
                return this.count_xywh(rect);
        }
        // 有 top,left
        else if (_.isNumber(rect.top) && _.isNumber(rect.left)) {
            if (_.isNumber(rect.bottom) && _.isNumber(rect.right))
                return this.count_tlbr(rect);
        }
        // 不知道咋弄了
        if (!quiet)
            throw "Don't know how to count rect:" + $z.toJson(rect);
        return rect;
    },
    //.............................................
    // 根据 top,left,width,height 计算剩下的信息
    count_tlwh: function (rect) {
        rect.right = rect.left + rect.width;
        rect.bottom = rect.top + rect.height;
        rect.x = rect.left + rect.width / 2;
        rect.y = rect.top + rect.height / 2;
        return rect;
    },
    //.............................................
    // 根据 top,left,bottom,right 计算剩下的信息
    count_tlbr: function (rect) {
        rect.width = rect.right - rect.left;
        rect.height = rect.bottom - rect.top;
        rect.x = rect.left + rect.width / 2;
        rect.y = rect.top + rect.height / 2;
        return rect;
    },
    //.............................................
    // 根据 bottom,right,width,height 计算剩下的信息
    count_brwh: function (rect) {
        rect.top = rect.bottom - rect.height;
        rect.left = rect.right - rect.width;
        rect.x = rect.left + rect.width / 2;
        rect.y = rect.top + rect.height / 2;
        return rect;
    },
    //.............................................
    // 根据 x,y,width,height 计算剩下的信息
    count_xywh: function (rect) {
        var W2 = rect.width / 2;
        var H2 = rect.height / 2;
        rect.top = rect.y - H2;
        rect.bottom = rect.y + H2;
        rect.left = rect.x - W2;
        rect.right = rect.x + W2;
        return rect;
    },
    //.............................................
    // 将一个矩形转换为得到一个 CSS 的矩形描述
    // 即 right,bottom 是相对于视口的右边和底边的
    // keys 可选，比如 "top,left,width,height" 表示只输出这几个CSS的值
    // 如果不指定 keys，则返回的是 "top,left,width,height,right,bottom"
    // keys 也支持快捷定义:
    //   - "tlwh" : "top,left,width,height"
    //   - "tlbr" : "top,left,bottom,right"
    asCss: function (rect, vpWidth, vpHeight, keys) {
        // 支持 {width:xxx, height:xxx} 形式的参数
        var vp;
        if (_.isObject(vpWidth)) {
            vp = vpWidth
        } else {
            vp = {
                width: vpWidth,
                height: vpHeight
            }
        }
        // 计算
        var re = {
            top: rect.top,
            left: rect.left,
            width: rect.width,
            height: rect.height,
            right: vp.width - rect.right,
            bottom: vp.height - rect.bottom
        };

        // 返回
        if(keys){
            // 快捷定义
            keys = ({
                "tlwh" : "top,left,width,height",
                "tlbr" : "top,left,bottom,right",
            })[keys] || keys;
            // 挑选键
            return $z.pick(re, keys);
        }
        return re;
    },
    //.............................................
    // CCS (Change Coordinate System)
    // 变换窗口坐标系： 返回一个点相对于某矩形左上顶点的坐标
    //  - rect : 矩形
    //  - x : 点 X 轴坐标 （也可以是一个点对象{x,y}，那么后一个参数将被无视
    //  - y : 点 Y 周坐标
    // 返回新的相对于矩形的点对象
    ccs_point_tl : function (rect, x, y) {
        // 支持对象作为数据参数
        if (_.isObject(x) && _.isNumber(x.x) && _.isNumber(x.y)) {
            y = x.y;
            x = x.x;
        }
        // 变换新的点
        return {
            x : x - rect.left,
            y : y - rect.top
        };
    },
    //.............................................
    // 得到一个新 Rect，左上顶点坐标系相对于 base (Rect)
    // 如果给定 forCss=true，则将坐标系统换成 CSS 描述
    // baseScroll 是描述 base 的滚动，可以是 Element/jQuery
    // 也可以是 {x,y} 格式的对象
    // 默认为 {x:0,y:0} 
    relative: function (rect, base, forCss, baseScroll) {
        // 计算 base 的滚动
        if (_.isElement(baseScroll) || $z.isjQuery(baseScroll)) {
            var jBase = $(baseScroll);
            baseScroll = {
                x: jBase.scrollLeft(),
                y: jBase.scrollTop(),
            }
        }
        // 默认
        else if (!baseScroll) {
            baseScroll = {x: 0, y: 0};
        }

        // 计算相对位置
        var r2 = {
            width: rect.width,
            height: rect.height,
            top: rect.top - base.top + baseScroll.y,
            left: rect.left - base.left + baseScroll.x,
        };
        // 计算其余
        this.count_tlwh(r2);

        // 返回 
        return forCss ? this.asCss(r2, base) : r2;
    },
    //.............................................
    // 缩放矩形
    // - rect  : 要被缩放的矩形
    // - vp    : 相对的顶点 {x,y}，默认取自己的中心点
    // - zoomX : X 轴缩放
    // - zoomY : Y 轴缩放，默认与 zoomX 相等
    // 返回矩形自身
    zoom: function (rect, vp, zoomX, zoomY) {
        vp = vp || rect;
        zoomY = zoomY || zoomX;
        rect.top = (rect.top - vp.y) * zoomY + vp.y;
        rect.left = (rect.left - vp.x) * zoomX + vp.x;
        rect.width *= zoomX;
        rect.height *= zoomY;
        return this.count_tlwh(rect);
    },
    //.............................................
    // 移动矩形
    // - rect : 要被移动的矩形
    // - tX   : X 轴位移
    // - tY   : Y 周位移
    // 返回矩形自身
    translate: function (rect, tX, tY) {
        // 支持对象作为数据参数
        if (_.isObject(tX) && _.isNumber(tX.x) && _.isNumber(tX.y)) {
            tY = tX.y;
            tX = tX.x;
        }
        // 执行位移
        rect.top -= tY || 0;
        rect.left -= tX || 0;
        return this.count_tlwh(rect);
    },
    /*.............................................
    将给定矩形停靠到目标矩形的内边上
    
                    ^ Y+
                    |
            +------top-------+
            |       |        |
    X- <--left----center----right---> X+
            |       |        |
            +-----bottom-----+
                    |
                    V Y-

     - rect    : 执行停靠的矩形
     - target  : 停靠的目标
     - axis    : 停靠的方向 {X: "left", Y: "top"}
     - space   : 停靠的间距 {X: 0,      Y: 0}
                 如果停靠在中点(center) 则不管用
    */
    dockIn: function (rect, target, axis, space) {
        // 参数默认值
        axis = axis || {X: "left", Y: "top"};
        space = space || {X: 0, Y: 0};

        // X:轴将这个大矩形移动到目标区域指定位置
        // 左侧
        if ("left" == axis.X) {
            rect.left  = target.left + space.X;
            rect.right = rect.left + rect.width;
        }
        // 右侧
        else if ("right" == axis.X) {
            rect.right = target.right - space.X;
            rect.left  = rect.right - rect.width;
        }
        // 中部
        else if ("center" == axis.X) {
            rect.x = target.x;
            rect.left  = rect.x - (rect.width/2);
            rect.right = rect.left + rect.width;
        }
        // 靠什么鬼
        else {
            throw "rect_dockIn invaid axis.X : " + axis.X;
        }

        // Y:轴将这个大矩形移动到目标区域指定位置
        if ("top" == axis.Y) {
            rect.top    = target.top + space.Y;
            rect.bottom = rect.top + rect.height;
        }
        // 底部
        else if ("bottom" == axis.Y) {
            rect.bottom = target.bottom - space.Y;
            rect.top    = rect.bottom - rect.height;
        }
        // 中部
        else if ("center" == axis.Y) {
            rect.y = target.y;
            rect.top    = rect.y - (rect.height/2);
            rect.bottom = rect.top + rect.height;
        }
        // 不支持啊
        else {
            throw "rect_dockIn invaid axis.Y : " + axis.Y;
        }
    },
    //.............................................
    // 计算矩形面积
    area: function (rect) {
        return rect.width * rect.height;
    },
    //.............................................
    // 计算多个矩形的最小相并矩形
    // 参数数组就是一个个的矩形对象 
    union: function () {
        var rects = $z.toArgs(arguments, true);
        // 空
        if (!rects || rects.length == 0)
            return null;

        // 以第一个为基础
        var r2 = _.extend({}, rects[0]);

        // 只有一个
        if (rects.length == 1)
            return r2;
        // 多个
        for (var i = 1; i < rects.length; i++) {
            var r     = rects[i];
            r2.top    = Math.min(r2.top, r.top);
            r2.left   = Math.min(r2.left, r.left);
            r2.right  = Math.max(r2.right, r.right);
            r2.bottom = Math.max(r2.bottom, r.bottom);
        }
        // 返回
        return this.count_tlbr(r2);
    },
    //.............................................
    // 相并面积
    union_area: function () {
        var rects = $z.toArgs(arguments, true);
        var r2 = this.union.apply(rects);
        return this.area(r2);
    },
    //.............................................
    // 计算多个矩形的最大相交矩形，只有一个参数的话，永远返回 null
    overlap: function () {
        var rects = $z.toArgs(arguments, true);
        // 少于1个
        if (!rects || rects.length <= 1)
            return null;
        // 多个
        var r2 = _.extend({}, rects[0]);
        for (var i = 1; i < rects.length; i++) {
            var r     = rects[i];
            r2.top    = Math.max(r2.top, r.top);
            r2.left   = Math.max(r2.left, r.left);
            r2.right  = Math.min(r2.right, r.right);
            r2.bottom = Math.min(r2.bottom, r.bottom);
        }
        // 返回
        return this.count_tlbr(r2);
    },
    //.............................................
    // 相交面积
    overlap_area: function () {
        var rects = $z.toArgs(arguments, true);
        var r2 = this.overlap.apply(this, rects);
        return this.area(r2);
    },
    //.............................................
    // A 是否全部包含 B
    contains: function (rectA, rectB) {
        return rectA.top <= rectB.top
            && rectA.bottom >= rectB.bottom
            && rectA.left <= rectB.left
            && rectA.right >= rectB.right;
    },
    //.............................................
    // 一个点是否在矩形之中，是否算上边
    is_in: function (rect, pos, countBorder) {
        if (countBorder) {
            return rect.left <= pos.x
                && rect.right >= pos.x
                && rect.top <= pos.y
                && rect.bottom >= pos.y;
        }
        return rect.left < pos.x
            && rect.right > pos.x
            && rect.top < pos.y
            && rect.bottom > pos.y;
    },
    //.............................................
    // A 是否与 B 相交
    is_overlap: function (rectA, rectB) {
        return this.overlap_area(rectA, rectB) > 0;
    },
    //.............................................
    // 生成一个新的矩形
    // 用 B 限制 A，会保证 A 完全在 B 中
    // rectA   : 原始矩形
    // rectB   : 用来限制原始矩形的边界矩形
    // overlap : 表示实在放不下了就剪裁
    boundaryIn: function (rectA, rectB, overlap) {
        var re = {};
        // @移动上下边
        // 在上面，先修改 top
        if (rectA.y < rectB.y) {
            re.top = Math.max(rectA.top, rectB.top);
            re.bottom = re.top + rectA.height;
        }
        // 否则修改 bottom
        else {
            re.bottom = Math.min(rectA.bottom, rectB.bottom);
            re.top = re.bottom - rectA.height;
        }

        // @移动左右边
        // 在左边，先修改 left
        if (rectA.x < rectB.x) {
            re.left = Math.max(rectA.left, rectB.left);
            re.right = re.left + rectA.width;
        }
        // 否则修改 right
        else {
            re.right = Math.min(rectA.right, rectB.right);
            re.left = re.right - rectA.width;
        }

        // 取一下重叠部分
        if(overlap)
            return this.overlap(re, rectB);

        // 直接返回
        re.x = (re.left + re.right) / 2;
        re.y = (re.top + re.bottom) / 2;
        return re;
    },
    //.............................................
    // 修改 A ，将其中点移动到某个位置
    // 第二个参数对象只要有 x,y 就好了，因此也可以是另外一个 Rect
    move_xy: function (rect, pos) {
        rect.x = pos.x;
        rect.y = pos.y;
        return this.count_xywh(rect);
    },
    //.............................................
    // 修改 ，将其左上顶点移动到某个位置
    // 第二个参数对象只要有 x,y 就好了，因此也可以是另外一个 Rect
    // offset 表示一个偏移量，可选。通用用来计算移动时，鼠标与左上顶点的偏移
    move_tl: function (rect, pos, offset) {
        rect.top = pos.y - (offset ? offset.y : 0);
        rect.left = pos.x - (offset ? offset.x : 0);
        return this.count_tlwh(rect);
    },
    //.............................................
};
//.................................................
// 所有与 DOM 相关的操作
var zDom = {
    //.............................................
    // 获得某元素的某真实属性
    getProp : function(el, key) {
        var sty = window.getComputedStyle($(el)[0]);
        //console.log(sty);
        // 一组属性
        if(_.isArray(key)){
            var re = {};
            for(var i=0;i<key.length;i++) {
                re[key[i]] = stl[key[i]];
            }
            return re;
        }
        // 某个属性
        return sty[key];
    },
    //.............................................
    // 获取根元素的 fontSize 属性，返回的一定是一个整形
    // 因此，如果 fontSize 不是 "px" 我可以负责任的告诉你，这函数别用!
    getRootFontSize : function(doc) {
        doc = doc || document;
        var val = this.getProp(doc.documentElement, "font-size");
        return parseFloat(val);
    },
    //.............................................
    // 将一个尺度值(px|rem|%)转换成一个表示像素的整数
    // 这个 base 在同单位下表示不同含义:
    //  "px"  : 没啥含义
    //  "%"   : 100% 所代表的值
    //  "rem" : 1rem 所代表的值
    toPixel: function (str, base, dft) {
        var re;
        var m = /^([\d.]+)(px|rem|%)?$/.exec(str);
        if (m) {
            var n = parseFloat(m[1]);
            var u = m[2];
            // 百分比
            if ("%" == u){
                return n * base / 100;
            }
            // rem
            else if("rem" == u) {
                return n * base;
            }
            // 默认就是 px 啦
            return n;
        }
        // 靠返回默认，没有的话，用 0
        return dft || 0;
    },
    //.............................................
    // 获取转换成 measure 用的配置对象
    getMeasureConf: function(doc){
        var conf = this.winsz(doc, true);
        conf.baseSize = this.getRootFontSize(doc);
        return conf;
    },
    /*.............................................
    将一个表示尺度的字符串，变成一个表示像素的数值
      - key : 一般为 top,left,width,height,right,bottom
      - val : 可以支持 "rem|px|%"，默认就是 px
      - conf : 转换成数值需要的更多配置项
        {
            width  : 640,      // 百分比计算用到的宽度
            height : 800,      // 百分比计算用到的高度
            baseSize : 100,    // rem计算要用到的基础尺寸
                               // 也就是 <html> 元素的 fontSize
        }
    */
    toMeasureNum: function (key, val, conf) {
        // 首先分析一下值
        var m = /^([\d.]+)(px|rem|%)?$/.exec(val);
        if(m) {
            var n = parseFloat(m[1]);
            var u = m[2];
            // 百分比
            if ("%" == u){
                // 用高度
                if(/(top|height|bottom)/.test(key)){
                    return n * conf.height / 100;
                }
                // 用宽度
                return n * conf.width / 100;
            }
            // rem
            else if("rem" == u) {
                return n * conf.baseSize;
            }
            // 默认就是 px 啦
            return n;
        }
        // 默认的呢，试图转成数字
        return parseFloat(val);
    },
    /*.............................................
    将一个表示尺度的数值，变成一个表示符合 CSS 表示字符串
      - key : 一般为 top,left,width,height,right,bottom
      - n : 数值
      - conf : 转换需要的更多配置项
        {
            width  : 640,      // 百分比计算用到的宽度
            height : 800,      // 百分比计算用到的高度
            baseSize : 100,    // rem计算要用到的基础尺寸
                               // 也就是 <html> 元素的 fontSize
            unit : "px"        // 转换成什么单位，支持 "rem|px|%"，默认为 px
            precision : -1     // 精度，如果>=0，则表示限制精度
        }
    */
    toMeasureStr: function (key, n, conf) {
        var p = conf.precision || -1;
        var u = conf.unit;
        // 百分比
        if ("%" == u){
            // 用高度
            if(/(top|height|bottom)/.test(key)){
                return $z.toPercent(n / conf.height, p);
            }
            // 用宽度
            return $z.toPercent(n / conf.width, p);
        }
        // rem
        else if("rem" == u) {
            return $z.precise(n / conf.baseSize, p) + "rem";
        }
        // 默认就是 px 啦
        return $z.precise(n, p) + "px";
    },
    //.............................................
    // 滚动元素所在文档，让元素显示
    // TODO 以后在弄吧 -_-!
    // scrollDocToView : function(ele) {
    //     var jq = $(ele);
    //     // 得到元素的矩形
    //     var r = zRect.gen(jq, false);
    //     // 得到元素所在文档的各种尺度
    //     var eBody = jq.closest("body")[0];
    //     var sH = eBody.scrollHeight;
    //     var sW = eBody.scrollWidth;
    //     var sT = eBody.scrollTop;
    //     var sL = eBody.scrollLeft;
    //     var cH = eBody.clientHeight;
    //     var cW = eBody.clientWidth;
    //     // 计算滚动值
    //     var sTop  = 0;
    //     var sLeft = 0;

    //     // 垂直方向:
    //     if(sT > r.bottom || (sT+cH)<r.top){
    //         eBody.scrollTop = r.top - sT
    //     }

    // },
    //.............................................
    // 获得视口的矩形信息
    winsz: function (win, onlyWidthHeight) {
        win = win || window;
        // 哦是 document 对象，转 window
        if(win.defaultView)
            win = win.defaultView;

        // 来吧
        var rect;
        if (win.innerWidth) {
            rect = {
                width: win.innerWidth,
                height: win.innerHeight
            };
        }
        else if (win.document.documentElement) {
            rect = {
                width: win.document.documentElement.clientWidth,
                height: win.document.documentElement.clientHeight
            };
        }
        else {
            rect = {
                width: win.document.body.clientWidth,
                height: win.document.body.clientHeight
            };
        }
        // 只获取宽高的话...
        if(onlyWidthHeight)
            return rect;

        // 继续剩余的值
        rect.top = 0;
        rect.left = 0;

        return zRect.count_tlwh(rect);
    },
    //.............................................
};
//.................................................
// 帮助函数集
var zDimension = {
    rect : zRect,
    dom  : zDom,
};
//..................................................
// 挂载到 window 对象
window.NutzDimension = zDimension;
window.$D = zDimension;
$z.defineModule("zdimension", zDimension);
//===================================================================
})(window.jQuery, window.NutzUtil);