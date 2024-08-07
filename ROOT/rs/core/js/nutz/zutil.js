/**
 * 本文件将提供 Nutz-JS 最基本的帮助函数定义支持，是 Nutz-JS 所有文件都需要依赖的基础JS文件
 *
 * @author  zozoh(zozohtnt@gmail.com)
 * 2012-10 First created
 */
(function () {
//===================================================================
    var INDENT_BY = "    ";

    var KEY_CODE = {
        WIN: 91,
        COMMAND: 91,
        // 特殊字符
        SHIFT: 16,
        CTRL: 17,
        CONTROL: 17,
        ALT: 18,
        CAPE_LOCK: 20,
        ESCAPE: 27,
        HOME: 36,
        SPACE: 32,
        BACKSPACE: 8,
        COMMA: 188,
        DELETE: 46,
        END: 35,
        ENTER: 13,
        PAGE_DOWN: 34,
        PAGE_UP: 33,
        PERIOD: 190,
        TAB: 9,
        '~': 192,
        '+': 187,
        '=': 187,
        EQUAL: 187,
        // 方向
        LEFT: 37,
        UP: 38,
        RIGHT: 39,
        DOWN: 40,
        // 字母
        A: 65,
        B: 66,
        C: 67,
        D: 68,
        E: 69,
        F: 70,
        G: 71,
        H: 72,
        I: 73,
        J: 74,
        K: 75,
        L: 76,
        M: 77,
        N: 78,
        O: 79,
        P: 80,
        Q: 81,
        R: 82,
        S: 83,
        T: 84,
        U: 85,
        V: 86,
        W: 87,
        X: 88,
        Y: 89,
        Z: 90,
        // 数字
        0: 48,
        1: 49,
        2: 50,
        3: 51,
        4: 52,
        5: 53,
        6: 54,
        7: 55,
        8: 56,
        9: 57
    };

    var zUtil = {
        keyCode: function () {
            return $.extend({}, KEY_CODE);
        },
        getKeyCodeValue: function (codeName) {
            return KEY_CODE[codeName];
        },
        getKeyCodeName: function (codeValue) {
            for (var key in KEY_CODE) {
                if (KEY_CODE[key] == codeValue)
                    return key;
            }
        },
        bodyToTop: function () {
            var $mybody = document.body;
            $mybody.scrollTop = 0;
        },
        bodyToBottom: function () {
            var $mybody = document.body;
            $mybody.scrollTop = $mybody.scrollHeight;
        },
        // 修改窗口标题
        // title 标题名称
        // blink 是否闪烁直到有人点了屏幕
        changeWindowTitle: function (title, blink) {
            if (typeof blink == 'undefined') {
                document.title = title;
            } else {
                // 闪烁显示内容
                var message = {
                    time: 0,
                    title: document.title,
                    timer: null,
                    //
                    init: function () {
                        $(document).one('click', function() {
                            message.clear();
                        })
                    },
                    // 显示新消息提示
                    show: function () {
                        // 定时器，设置消息切换频率闪烁效果就此产生
                        message.timer = setTimeout(function () {
                            message.time++;
                            message.show();
                            if (message.time % 2 == 0) {
                                document.title = "【新消息】" + title
                            }
                            else {
                                document.title = "【　　　】" + title
                            }
                        }, 600);
                        return [message.timer, message.title];
                    },
                    // 取消新消息提示
                    clear: function () {
                        clearTimeout(message.timer);
                        document.title = message.title;
                    }
                };
                message.init();
                message.show();
            }
        },
        // 全屏幕
        toggleFullScreen: function () {
            if (!document.fullscreenElement &&    // alternative standard method
                !document.mozFullScreenElement && !document.webkitFullscreenElement && !document.msFullscreenElement) {  // current working methods
                if (document.documentElement.requestFullscreen) {
                    document.documentElement.requestFullscreen();
                } else if (document.documentElement.msRequestFullscreen) {
                    document.documentElement.msRequestFullscreen();
                } else if (document.documentElement.mozRequestFullScreen) {
                    document.documentElement.mozRequestFullScreen();
                } else if (document.documentElement.webkitRequestFullscreen) {
                    document.documentElement.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
                }
            } else {
                if (document.exitFullscreen) {
                    document.exitFullscreen();
                } else if (document.msExitFullscreen) {
                    document.msExitFullscreen();
                } else if (document.mozCancelFullScreen) {
                    document.mozCancelFullScreen();
                } else if (document.webkitExitFullscreen) {
                    document.webkitExitFullscreen();
                }
            }
        },
        //.............................................
        isFullScreen: function () {
            return document.fullScreenElement
                || document.mozFullScreenElement
                || document.webkitFullscreenElement
                || document.msFullScreenElement;
        },
        //.............................................
        enterFullScreen: function () {
            var root = document.documentElement;
            if (_.isFunction(root.requestFullscreen))
                root.requestFullscreen();
            else if (_.isFunction(root.mozRequestFullScreen))
                root.mozRequestFullScreen();
            else if (_.isFunction(root.webkitRequestFullscreen))
                root.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
            else if (_.isFunction(root.msRequestFullscreen))
                root.msRequestFullscreen();
        },
        //.............................................
        exitFullScreen: function () {
            var root = document;
            if (_.isFunction(root.exitFullscreen))
                root.exitFullscreen();
            else if (_.isFunction(root.mozCancelFullScreen))
                root.mozCancelFullScreen();
            else if (_.isFunction(root.webkitExitFullscreen))
                root.webkitExitFullscreen();
            else if (_.isFunction(root.msExitFullscreen))
                root.msExitFullscreen();
        },
        //.............................................
        // 提供 AMD/CMD 支持功能
        defineModule: function (mdName, mdObj) {
            if (typeof define === "function") {
                // CMD
                if (define.cmd) {
                    define(function (require, exports, module) {
                        module.exports = mdObj;
                    });
                }
                // AMD
                else {
                    define(mdName, [], function () {
                        return mdObj;
                    });
                }
            }
        },
        //.............................................
        // 安全的调用回调
        doCallback: function (callback, args, context, dftFunc) {
            // 支持 context 为函数的形式
            if (_.isFunction(context)) {
                dftFunc = context;
                context = undefined;
            }
            // 确保有函数参数
            args = args || [];

            // 有函数
            if (_.isFunction(callback)) {
                return callback.apply(context || this, args);
            }
            // 默认逻辑
            if (_.isFunction(dftFunc)) {
                return dftFunc.apply(context || this, args);
            }
            // 神马都木有
            if (args.length == 0)
                return null;
            if (args.length == 1)
                return args[0];
            return args;
        },
        //.............................................
        // 处理 underscore 的模板
        tmpl: function (str, settings) {
            return _.template(str, settings || {
                escape: /\{\{([\s\S]+?)\}\}/g
            });
        },
        /*.............................................
        解析模板，得到一个对象，既有模板，又有占位符名称
        {
            obj  : {..}  // 模板的占位符
            tmpl : F()   // 模板渲染函数
        }
        本函数默认占位符形式为 ${xxx}
        */
        parseTmpl: function(str, regex, genKey) {
            if(!regex) {
                regex = /\$\{([\s\S]+?)\}/g;
                genKey = function(key) {
                    return '${' + key + '}';
                }
            }
            else if(_.isString(regex)){
                regex = new RegExp(regex, "g");
            }
            else {
                throw "Invalid param regex: " + regex;
            }
            // 首先先搜索一遍
            var R1 = new RegExp(regex, "g");
            var m = R1.exec(str);
            var obj = {};
            var list = [];
            var pos = 0;
            while(m) {
                var beg = R1.lastIndex - m[0].length;
                if(pos < beg) {
                    list.push(str.substring(pos, beg));
                }
                var s   = m[1];
                var m2  = /^([^?]+)(\?(.*))?$/.exec(s);
                var key = m2[1];
                var val = m2[3] || key;
                obj[key] = val;

                // 推入变量
                list.push(genKey(key));

                // 继续执行
                pos = R1.lastIndex;
                m = R1.exec(str);
            }
            // 加入末尾
            if(pos < str.length) {
                list.push(str.substring(pos));
            }
            // 再编译一下
            var s2 = list.join('');
            var tmpl = _.template(s2, {
                escape: new RegExp(regex, "g")
            });
            // 得到返回结果
            return {
                obj  : obj,
                tmpl : tmpl
            };
        },
        //.............................................
        // 本地存储保存某用户的某个界面的设置
        // appName : 应用名称
        // me      : 用户标识
        // key     : 保存的键
        // val     : 值, undefined 表示获取，null 表示删除
        // 如果不支持 localStorage，则抛错
        local: function (appName, me, key, val) {
            if (!localStorage)
                throw "Browser don't support localStorage! " + appName + "-" + key;

            var appConf = $z.fromJson(localStorage.getItem(appName) || "{}");
            var myConf = appConf[me] || {};
            // 设置
            if (!_.isUndefined(val)) {
                myConf[key] = val;
                appConf[me] = myConf;
                localStorage.setItem(appName, $z.toJson(appConf));
                return myConf;
            }
            // 删除
            else if (_.isNull(val)) {
                delete myConf[key];
                appConf[me] = myConf;
                localStorage.setItem(appName, $z.toJson(appConf));
                return myConf;
            }
            // 获取
            else {
                return myConf[key];
            }
        },
        //.............................................
        // 让一段代码最后执行
        defer: function (func) {
            window.setTimeout(func, 0);
        },
        //.............................................
        // 获取当前文档所有被选择的文字内容
        //  - forceReturnArray true 表示强制返回数组
        // 返回一个字符串数组，表示各个 Range 所选择的内容
        getSelectedTexts: function (forceReturnArray) {
            var sel = getSelection();
            var re = [];
            if (sel) {
                for (var i = 0; i < sel.rangeCount; i++) {
                    var rag = sel.getRangeAt(i);
                    if (!rag.collapsed)
                        re.push(rag.toString());
                }
            }
            return re.length == 0 ? (forceReturnArray ? re : null) : re;
        },
        //.............................................
        // 将一个字符串，根据 Javascript 的类型进行转换
        strToJsObj: function (v, type) {
            // 指定了类型
            if (type) {
                switch (type) {
                    case 'string':
                        if (_.isString(v))
                            return v;
                        return v || null;
                    case 'float':
                        var re = v * 1;
                        return v == re ? re : v;
                    case 'int':
                        var re = v * 1;
                        return v == re ? parseInt(re) : v;
                    case 'object':
                        // 弱弱的尝试一下转换成 Json 对象
                        try {
                            return zUtil.fromJson(v);
                        }
                            // 嗯，好像不行，变字符串吧
                        catch (E) {
                            return v;
                        }
                    case 'boolean':
                        if (_.isBoolean(v))
                            return v;
                        if (_.isUndefined(v))
                            return false;
                        return /^(true|yes|on|ok)$/.test(v);
                    default:
                        throw "strToJsObj unknown type [" + type + "] for: " + v;
                }
            }

            // 没指定类型，那么自动判断
            // 数字
            if (/^-?[\d.]+$/.test(v)) {
                return v * 1;
            }
            // 日期
            var regex = /^(\d{4})-(\d{2})-(\d{2})$/;
            if (regex.test(v)) {
                return zUtil.parseDate(v, regex);
            }
            // 布尔
            regex = /^ *(true|false|yes|no|on|off) *$/i;
            var m = regex.exec(v);
            if (m) {
                return /^true|yes|on$/i.test(m[1]);
            }

            // 返回自身了事
            return v;
        },
        //.............................................
        toggleAttr: function (jq, attNm, valOn, valOff) {
            jq = $(jq);
            // 没有 valOn 那么，默认当做 true
            if (_.isUndefined(valOn)) {
                if (jq.attr(attNm)) {
                    jq.removeAttr(attNm);
                } else {
                    jq.attr(attNm, true || valOn);
                }
            }
            // 否则用 valOn 来判断
            else {
                if (jq.attr(attNm) == valOn) {
                    if (_.isUndefined(valOff))
                        jq.removeAttr(attNm);
                    else
                        jq.attr(attNm, valOff);
                } else {
                    jq.attr(attNm, valOn);
                }
            }
            // 返回以便链式赋值
            return jq;
        },
        //.............................................
        // 计算尺寸的绝对像素数值
        //  -v : 要计算的尺寸值的类型可以是
        //       500    - 整数，直接返回表示像素
        //       .12    - 浮点，相当于一个百分比，可以大于 1.0
        //       "12%"  - 百分比，相当于 .12
        //       "5px"  - 像素，直接返回 5
        //       "5rem" - 根据根元素计算比例
        // - base : 百分比的基数
        // @return 绝对像素数值
        dimension: function (v, base) {
            // 数字
            if(_.isNumber(v)){
                if(v>1 || v<-1)
                    return v;
                return v*base;
            }
            // 分析字符串
            var m = /^(-?\d*\.?\d+)(%|px|rem)?$/.exec(v);
            // ！靠~不知道是啥
            if(!m) {
                throw  "fail to dimension : " + v;
            }

            // rem
            if('rem' == m[2]) {
                var rem = $D.dom.getRootFontSize();
                return m[1] * rem;
            }
            // %
            else if('%' == m[2]) {
                return m[1] / 100 * base;
            }
            // px
            return m[1] * 1;
        },
        //.............................................
        toPixel: function (str, base, dft) {
            var re;
            var m = /^([\d.]+)(px)?(%)?$/.exec(str);
            if (m) {
                // %
                if (m[3])
                    return m[1] * base / 100;
                // 要不是 px 要不直接就是数字
                return m[1] * 1;
            }
            // 靠！返回默认
            return dft;
        },
        //.............................................
        // 将一个浮点数变成百分比字符串
        // 比如 toPercent(.23) => 23%
        //  - n     : 浮点数
        //  - prec  : 精确到小数点几位，默认 2
        //  - fixed : 是否一定显示小数点后面的尾数，默认 false
        toPercent: function (n, prec, fixed) {
            prec = _.isNumber(prec) && prec >= 0 ? prec : 2;
            var m = $z.precise(n * 100, prec);

            var str = m + "";

            // 用零补足
            if (fixed && prec > 0) {
                var pos = str.indexOf('.');
                // 全补
                if (pos < 0) {
                    str += "." + $z.dupString('0', prec);
                }
                // 补足
                else {
                    var c = prec - str.length + pos + 1;
                    if (c > 0) {
                        str += $z.dupString('0', c);
                    }
                }
            }

            // 返回
            return str + "%";
        },
        //.............................................
        // 将是数字精确到小数点后制定位置
        // 余下的四舍五入。 对于 p 默认为 0 即去掉小数部分
        // 如果 p < 0，则表示不限制精度了
        precise: function (n, p) {
            if (p >= 0) {
                var y = Math.pow(10, p);
                return Math.round(n * y) / y;
            }
            return n;
        },
        //.............................................
        // 获取两个数的最大公约数
        // greatest common divisor(gcd)
        gcd : function(a,b){
            a = Math.round(a);
            b = Math.round(b);
            if(b){
                return this.gcd(b,a%b);
            }
            return a;
        },
        //.............................................
        gcds : function() {
            var args = Array.from(arguments);
            var list = _.flatten(args);
            // 没数
            if(list.length == 0)
                return NaN;
            // 一个是自己
            if(list.length == 1) {
                return list[0];
            }
            // 两个以上
            var gcd = this.gcd(list[0], list[1]);
            for(var i=2; i<list.length; i++) {
                gcd = this.gcd(gcd, list[i]);
            }
            // 返回
            return gcd;
        },
        //.............................................
        // 获取两个数的最小公倍数 
        // lowest common multiple (LCM)
        lcm  : function(a, b) {
            a = Math.round(a);
            b = Math.round(b);
            return a * b / this.gcd(a, b);
        },
        //.............................................
        lcms : function() {
            var args = Array.from(arguments);
            var list = _.flatten(args);
            // 没数
            if(list.length == 0)
                return NaN;
            // 一个是自己
            if(list.length == 1) {
                return list[0];
            }
            // 两个以上
            var lcm = this.lcm(list[0], list[1]);
            for(var i=2; i<list.length; i++) {
                lcm = this.lcm(lcm, list[i]);
            }
            // 返回
            return lcm;
        },
        //.............................................
        obj: function (key, val) {
            if (_.isString(key)) {
                var re = {};
                //re[key] = val;
                zUtil.setValue(re, key, val);
                return re;
            }
            return key;
        },
        //.............................................
        // 处理函数的参数表，将其变成普通数组
        //  - flatten : 表示展平嵌套数组
        //  - deeply  : 表示深层展开，否则只展开一层
        toArgs: function (list, flatten, deeply) {
            // 靠! IE 不支持，用传统方法吧
            //var args = Array.from(list);
            var args = [];
            if(list && _.isNumber(list.length))
                for(var i=0; i<list.length; i++) {
                    args.push(list[i]);
                }
            if (flatten)
                return _.flatten(args, !deeply);
            return args;
        },
        //.............................................
        // 挑选属性，正则表达式如果以 ! 开头表示取反
        pick: function (obj, regex, asValueArray) {
            if (!regex)
                return obj;

            // 准备返回值
            var re = asValueArray ? [] : {};

            // 如果是字符串，并且不是 ^ 开头的正则，作为半角
            // 逗号分隔的 key 列表
            if (_.isString(regex) && !/^(!)?\^/.test(regex)) {
                regex = regex.split(/[ \t]*,[ \t]*/);
            }

            // 数组，表示值的列表，直接读取
            if (_.isArray(regex)) {
                for (var i = 0; i < regex.length; i++) {
                    var key = regex[i];
                    // 推入数组
                    if (asValueArray) {
                        re.push(obj[key]);
                    }
                    // 计入对象
                    else {
                        re[key] = obj[key];
                    }
                }
                // 嗯，返回吧
                return re;
            }

            // 解析正则表达式
            var not = false;
            var REG = _.isRegExp(regex) ? regex : null;
            if (!REG) {
                var str = $.trim(regex.toString());
                // !^ 开头的正则表达式 
                var m = /^(!)?\^/.exec(str);
                if (m) {
                    if (m[1]) {
                        not = true;
                        str = str.substring(1);
                    }
                    REG = new RegExp(str);
                }
                // 不可能啊
                else {
                    throw "Impossible! " + regex;
                }
            }

            // 开始过滤字段
            for (var key in obj) {
                if (REG.test(key)) {
                    if (not)
                        continue;
                    // 推入数组
                    if (asValueArray) {
                        re.push(obj[key]);
                    }
                    // 计入对象
                    else {
                        re[key] = obj[key];
                    }
                }
                // 取反的情况
                else if (not) {
                    // 推入数组
                    if (asValueArray) {
                        re.push(obj[key]);
                    }
                    // 计入对象
                    else {
                        re[key] = obj[key];
                    }
                }
            }

            // 返回
            return re;
        },
        //.............................................
        dump: {
            rectV: function (rect) {
                return $z.tmpl("l:{{left}},r:{{right}},t:{{top}},b:{{bottom}},x:{{x}},y:{{y}}")(rect);
            },
            pos: function (pos) {
                return $z.tmpl("x:{{x}},y:{{y}}")(pos);
            }
        },
        //.............................................
        // 判断一个给定对象是否是矩形对象
        isRect: function (obj) {
            return obj
                && _.isNumber(obj.top)
                && _.isNumber(obj.left)
                && _.isNumber(obj.width)
                && _.isNumber(obj.height)
                && _.isNumber(obj.bottom)
                && _.isNumber(obj.right)
                && _.isNumber(obj.x)
                && _.isNumber(obj.y);
        },
        //.............................................
        // 获取一个元素相对于页面的矩形信息，包括 top,left,right,bottom,width,height,x,y
        // 其中 x,y 表示中央点
        //  - ele : 要计算的元素， body 被认为是窗体
        //  - invludeMargin : 矩形信息是否包括外边距，默认不包含
        //  - toScreen      : 指明矩形是相对于窗口的，所以要考虑到文档的 scrollTop/Left
        rect: function (ele, includeMargin, toScreen) {
            var jEle = $(ele);

            // 如果计算 body 或者 document 或者 window
            if (jEle[0].tagName == 'BODY')
                return $z.winsz(jEle[0].ownerDocument.defaultView);

            // 开始计算，得到相对于 document 的坐标
            var rect = jEle.offset();

            // 切换到 screen 坐标系
            if (toScreen) {
                var body = jEle[0].ownerDocument.body;
                rect.top -= body.scrollTop;
                rect.left -= body.scrollLeft;
            }

            // 包括外边距，有点麻烦
            if (includeMargin && jEle.size() > 0) {
                rect.width = jEle.outerWidth(true);
                rect.height = jEle.outerHeight(true);
                var style = window.getComputedStyle(jEle[0]);
                rect.top -= this.toPixel(style.marginTop);
                rect.left -= this.toPixel(style.marginLeft);
            }
            // 否则就简单了
            else {
                rect.width = jEle.outerWidth();
                rect.height = jEle.outerHeight();
            }

            // 计算其他值
            return this.rect_count_tlwh(rect);
        },
        //.............................................
        rectObj: function (rect, keys) {
            if (_.isString(keys)) {
                keys = keys.split(/[ \t]*,[ \t]*/);
            }
            var re = {};
            for (var i = 0; i < keys.length; i++) {
                var key = keys[i];
                re[key] = rect[key];
            }
            return re;
        },
        //.............................................
        rectValues: function (rect, keys) {
            if (_.isString(keys)) {
                keys = keys.split(/[ \t]*,[ \t]*/);
            }
            var re = [];
            for (var i = 0; i < keys.length; i++) {
                re.push(rect[keys[i]]);
            }
            return re;
        },
        //.............................................
        rectDump: function (rect) {
            return this.tmpl("[{{left}},{{top}}]w={{width}},h={{height}}")(rect);
        },
        //.............................................
        // 自动根据矩形对象的值进行判断
        rect_count_auto: function (rect, quiet) {
            // 有 width, height
            if (_.isNumber(rect.width) && _.isNumber(rect.height)) {
                if (_.isNumber(rect.top) && _.isNumber(rect.left))
                    return this.rect_count_tlwh(rect);
                if (_.isNumber(rect.bottom) && _.isNumber(rect.right))
                    return this.rect_count_brwh(rect);
                if (_.isNumber(rect.x) && _.isNumber(rect.x))
                    return this.rect_count_xywh(rect);
            }
            // 有 top,left
            else if (_.isNumber(rect.top) && _.isNumber(rect.left)) {
                if (_.isNumber(rect.bottom) && _.isNumber(rect.right))
                    return this.rect_count_tlbr(rect);
            }
            // 不知道咋弄了
            if (!quiet)
                throw "Don't know how to count rect:" + this.toJson(rect);
            return rect;
        },
        //.............................................
        // 根据 top,left,width,height 计算剩下的信息
        rect_count_tlwh: function (rect) {
            rect.right = rect.left + rect.width;
            rect.bottom = rect.top + rect.height;
            rect.x = rect.left + rect.width / 2;
            rect.y = rect.top + rect.height / 2;
            return rect;
        },
        //.............................................
        // 根据 top,left,bottom,right 计算剩下的信息
        rect_count_tlbr: function (rect) {
            rect.width = rect.right - rect.left;
            rect.height = rect.bottom - rect.top;
            rect.x = rect.left + rect.width / 2;
            rect.y = rect.top + rect.height / 2;
            return rect;
        },
        //.............................................
        // 根据 bottom,right,width,height 计算剩下的信息
        rect_count_brwh: function (rect) {
            rect.top = rect.bottom - rect.height;
            rect.left = rect.right - rect.width;
            rect.x = rect.left + rect.width / 2;
            rect.y = rect.top + rect.height / 2;
            return rect;
        },
        //.............................................
        // 根据 x,y,width,height 计算剩下的信息
        rect_count_xywh: function (rect) {
            var W2 = rect.width / 2;
            var H2 = rect.height / 2;
            rect.top = rect.y - H2;
            rect.bottom = rect.y + H2;
            rect.left = rect.x - W2;
            rect.right = rect.x + W2;
            return rect;
        },
        //.............................................
        // 得到一个新 Rect，左上顶点坐标系相对于 base
        // 如果给定 forCss=true，则将坐标系统换成 CSS 描述
        // baseScroll 是描述 base 的滚动，可以是 Element/jQuery
        // 也可以是 {scrollTop,scrollLeft} 格式的对象
        // 默认为 {scrollTop:0,scrollLeft:0} 
        rect_relative: function (rect, base, forCss, baseScroll) {
            // 计算 base 的滚动
            if (_.isElement(baseScroll) || $z.isjQuery(baseScroll)) {
                var jBase = $(baseScroll);
                baseScroll = {
                    scrollTop: jBase.scrollTop(),
                    scrollLeft: jBase.scrollLeft(),
                }
            }
            // 默认
            else if (!baseScroll) {
                baseScroll = {scrollTop: 0, scrollLeft: 0};
            }

            // 计算相对位置
            var r2 = {
                width: rect.width,
                height: rect.height,
                top: rect.top - base.top + baseScroll.scrollTop,
                left: rect.left - base.left + baseScroll.scrollLeft,
            };
            // 计算其余
            this.rect_count_tlwh(r2);

            // 返回 
            return forCss ? this.rectCss(r2, base) : r2;
        },
        //.............................................
        // 缩放矩形
        // - rect  : 要被缩放的矩形
        // - vp    : 相对的顶点 {x,y}，默认取自己的中心点
        // - zoomX : X 轴缩放
        // - zoomY : Y 轴缩放，默认与 zoomX 相等
        // 返回矩形自身
        rect_zoom_tlwh: function (rect, vp, zoomX, zoomY) {
            vp = vp || rect;
            zoomY = zoomY || zoomX;
            rect.top = (rect.top - vp.y) * zoomY + vp.y;
            rect.left = (rect.left - vp.x) * zoomX + vp.x;
            rect.width *= zoomX;
            rect.height *= zoomY;
            return this.rect_count_tlwh(rect);
        },
        //.............................................
        // 移动矩形
        // - rect : 要被移动的矩形
        // - tX   : X 轴位移
        // - tY   : Y 周位移
        // 返回矩形自身
        rect_translate: function (rect, tX, tY) {
            // 支持对象作为数据参数
            if (_.isObject(tX) && _.isNumber(tX.x) && _.isNumber(tX.y)) {
                tY = tX.y;
                tX = tX.x;
            }
            // 执行位移
            rect.top += tY || 0;
            rect.left += tX || 0;
            return this.rect_count_tlwh(rect);
        },
        //.............................................
        // 将一个矩形转换为得到一个 CSS 的矩形描述
        // 即 right,bottom 是相对于视口的右边和底边的
        rectCss: function (rect, vpWidth, vpHeight) {
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
            return {
                top: rect.top,
                left: rect.left,
                width: rect.width,
                height: rect.height,
                right: vp.width - rect.right,
                bottom: vp.height - rect.bottom
            };
        },
        //.............................................
        // 将给定矩形停靠到目标矩形的内边上
        //  - rect   : 执行停靠的矩形
        //  - target : 停靠的目标
        rect_dockIn: function (rect, target, axis, padding) {
            axis = axis || {X: "left", Y: "top"};
            padding = padding || {X: 0, Y: 0};
            console.log(rect)
            console.log(target)
            // X:轴将这个大矩形移动到目标区域指定位置
            if ("left" == axis.X) {
                rect.left = target.left + padding.X;
                $z.rect_count_tlwh(rect);
            }
            // 右侧
            else if ("right" == axis.X) {
                rect.right = target.right - padding.X;
                $z.rect_count_brwh(rect);
            }
            // 中部
            else if ("center" == axis.X) {
                rect.x = target.x;
                $z.rect_count_xywh(rect);
            }
            // 靠什么鬼
            else {
                throw "rect_dockIn invaid axis.X : " + axis.X;
            }
            // Y:轴将这个大矩形移动到目标区域指定位置
            if ("top" == axis.Y) {
                rect.top = target.top + padding.Y;
                $z.rect_count_tlwh(rect);
            }
            // 底部
            else if ("bottom" == axis.Y) {
                rect.bottom = target.bottom - padding.Y;
                $z.rect_count_brwh(rect);
            }
            // 中部
            else if ("center" == axis.Y) {
                rect.y = target.y;
                $z.rect_count_xywh(rect);
            }
            // 不支持啊
            else {
                throw "rect_dockIn invaid axis.Y : " + axis.Y;
            }
        },
        //.............................................
        // 处理一组矩形，保留其相对位置，并将其尽量缩小
        // 试图浮动在当前页面上
        //  - rects  : 矩形数组 
        //  - opt    : 配置参数 {
        //     target     : Rect,    // 要放置的目标区域矩形，默认整个窗口
        //     scrollTop  : 0,       // 目标区域顶部滚动
        //     scrollLeft : 0,       // 目标区域左侧滚动
        //     positionX  : "left",  // X 轴的放置位置
        //     positionY  : "top",   // Y 轴的放置位置
        //     paddingX   : 10,      // X 轴边距
        //     paddingY   : 10       // Y 轴边距
        //     width      : "50%",   // 宽度压缩比例
        //     maxTimes   : 3,       // Y 轴最大区域的倍数
        //     stickRadius: 10,      // 辅助线吸附半径
        // }
        rect_compact: function (rects, opt) {
            // 无需处理
            if (!_.isArray(rects) || rects.length == 0) {
                return;
            }

            // TODO： 好像效果不好，暂时注释掉 ...
            // // 设置默认参数
            opt.target = opt.target || this.winsz();
            opt.scrollTop = opt.scrollTop || 0;
            opt.scrollLeft = opt.scrollLeft || 0;
            opt.positionX = opt.positionX || "left";
            opt.positionY = opt.positionY || "top";
            opt.paddingX = opt.paddingX || 10;
            opt.paddingY = opt.paddingY || 10;
            opt.width = opt.width || "50%";
            opt.maxTimes = opt.maxTimes || 3;
            opt.stickRadius = opt.stickRadius || 10;

            // // 得到一个大矩形
            // var BigRect = $z.rect_union(rects);

            // // 停靠在给定区域里面
            // this.rect_dockIn(BigRect, opt.target, {
            //     X : opt.positionX, Y : opt.positionY
            // }, {
            //     X : opt.paddingX,  Y : opt.paddingY
            // });

            // // 缩放宽度比例
            // var scaleX = $z.dimension(opt.width, 1);
            // if(scaleX != 1) {
            //     // 缩放大矩形的宽度
            //     BigRect.width *= scaleX;
            //     this.rect_count_tlwh(BigRect);
            //     // 得到缩放操作的相对顶点 
            //     var vp = {
            //         x : BigRect.left,
            //         y : BigRect.right,
            //     };
            //     // 依次处理矩形缩放
            //     for(var r of rects) {
            //         console.log("before:", this.rectDump(r), vp, scaleX);
            //         this.rect_zoom_tlwh(r, vp, scaleX, 1);
            //         console.log(" after:", this.rectDump(r));
            //     }
            // }


            // 建立调整的辅助线
            var lines = this.rect_adjustlines_create(rects, "Y", "round");
            //this.rect_adjustlines_dump(lines, "after create");

            // 消除微小的线段间隔
            lines = this.rect_adjustlines_stickBy(lines, opt.stickRadius);
            //this.rect_adjustlines_dump(lines, "after stickBy");

            // 补偿滚动
            this.rect_adjustlines_scroll(lines, opt.scrollTop);

            // 寻找最小线段间隔，作为调整的单位
            var unit = this.rect_adjustlines_minSpace(lines);
            //console.log("UNIT:", unit);

            // 将线调整到最小间隔的倍数，最大 3 倍
            this.rect_adjustlines_compact(lines, unit, opt.maxTimes);
            //this.rect_adjustlines_dump(lines, "after compact");

            // 应用修改到矩形的对应字段
            this.rect_adjustlines_apply(lines);

            // 重新设置矩形们其他的尺寸
            for (var i in rects) {
                this.rect_count_tlbr(rects[i]);
            }
        },
        //.............................................
        // 针对一组矩形，建立矩形调整线对象
        // 该对象是一个数组，每个元素字段为
        /*
         {
         space  : 0,   // 与上一根线的距离，第一根线永远是 -1
         offset : 0,   // 线的位移
         refers : [{   // 关联的矩形对象字段
         rect : Rect   // 关联的矩形对象
         key  : "top"  // 字段
         }]
         }
         */
        // 其中参数
        //  - rects : 矩阵对象数组
        //  - axis  : 可以是 "X" 或者 "Y" 表示建立的是哪个轴的参考线
        //  - offsetFunc : F(v):Number 表示如何计算 offset 的
        //                  > "int"   : 执行 parseInt
        //                  > "round" : 执行 Math.round
        //                  > "ceil"  : 执行 Math.ceil
        //                  > "floor" : 执行 Math.floor
        //                  > Func    : 执行自定义
        //                  > 默认不做任何修改
        // 本函数就是将矩形们的边进行统计，生成一条条的线对象，以备后续操作
        // 同样位移的矩形边会被归纳到同样的线对象里面，以便统一调整
        // @return 从小到大排序过的线对象
        rect_adjustlines_create: function (rects, axis, offsetFunc) {
            // 准备 offsetFunc
            if (!_.isFunction(offsetFunc)) {
                switch (offsetFunc) {
                    case "int" :
                        offsetFunc = function (v) {
                            return parseInt(v);
                        };
                        break;
                    case "round" :
                        offsetFunc = function (v) {
                            return Math.round(v);
                        };
                        break;
                    case "ceil" :
                        offsetFunc = function (v) {
                            return Math.ceil(v);
                        };
                        break;
                    case "floor" :
                        offsetFunc = function (v) {
                            return Math.floor(v);
                        };
                        break;
                }
            }
            // 准备字段
            var keys = "X" == axis ? ["left", "right"] : ["top", "bottom"];

            // 准备归纳的表
            var map = {};

            // 循环查找
            for (var i in rects) {
                var r = rects[i];
                for (var ki in keys) {
                    var key = keys[ki];
                    var v = offsetFunc ? offsetFunc(r[key]) : r[key];
                    var lo = map[v];
                    var ref = {rect: r, key: key};
                    // 新建
                    if (!lo) {
                        map[v] = {offset: v, refers: [ref]};
                    }
                    // 插入
                    else {
                        lo.refers.push(ref);
                    }
                }
            }

            // 对于数组排序
            var list = _.values(map).sort(function (a, b) {
                return a.offset == b.offset ? 0 :
                    a.offset > b.offset ? 1 : -1;
            });

            // 计算线与线之间的距离
            if (list.length > 0) {
                list[0].space = -1;
                for (var i = 1; i < list.length; i++) {
                    var l0 = list[i - 1];
                    var l1 = list[i];
                    l1.space = l1.offset - l0.offset;
                }
            }

            // 返回结果
            return list;
        },
        //.............................................
        // 调试打印
        rect_adjustlines_dump: function (lines, title) {
            // 应用一下先
            //this.rect_adjustlines_apply(lines);
            // 打印一组线
            if (_.isArray(lines)) {
                console.log("~~~~~ DUMP", lines.length, "lines:", title || "");
                for (var i = 0; i < lines.length; i++) {
                    var lo = lines[i];
                    this.rect_adjustlines_dump(lo, i);
                }
                return;
            }
            // 打印一条线
            var lo = lines;
            var str = _.isUndefined(title) ? "" : title;
            str += "  " + this.alignRight(lo.offset, 4, ' ');
            str += " / " + this.alignLeft(lo.space, 4, ' ');
            str += " {";
            for (var ri in lo.refers) {
                var ref = lo.refers[ri];
                str += "@" + ref.key;
                // 仅显示高度
                if (/^(top|bottom)$/.test(ref.key)) {
                    str += '[' + ref.rect.top + "," + ref.rect.bottom + ']';
                }
                // 仅显示宽度
                else {
                    str += '[' + ref.rect.left + "," + ref.rect.right + ']';
                }
                str += " | ";
            }
            str += "}";
            console.log(str);
        },
        //.............................................
        // 在排序后的线段组归纳线，将所有的线，在指定半径内的都归纳到一条线上
        // 并返回新的线数组
        rect_adjustlines_stickBy: function (lines, radius) {
            var list = [];
            if (lines.length > 0) {
                // 准备第一个对象
                var lo = lines[0];
                var last = {
                    space: lo.space,
                    offset: lo.offset,
                    refers: [].concat(lo.refers),
                };
                // 循环加入后面的
                for (var i = 1; i < lines.length; i++) {
                    lo = lines[i];
                    // 加入
                    if (lo.space <= radius) {
                        last.refers.push.apply(last.refers, lo.refers);
                    }
                    // 创建一个新的
                    else {
                        list.push(last);
                        last = {
                            space: lo.space,
                            offset: lo.offset,
                            refers: [].concat(lo.refers),
                        };
                    }
                }
                // 最后一个加入返回列表
                list.push(last);
            }
            return list;
        },
        //.............................................
        // 统一移动各条线
        rect_adjustlines_scroll: function (lines, offset) {
            if (lines.length > 0) {
                for (var i = 0; i < lines.length; i++) {
                    lines[i].offset += offset;
                }
            }
        },
        //.............................................
        // 在排序后的线段组寻找调整线之间小的间距
        // @return -1 表示线段组不足两条
        rect_adjustlines_minSpace: function (lines) {
            var re = -1;
            if (lines.length > 1) {
                re = lines[1].space;
                for (var i = 2; i < lines.length; i++) {
                    re = Math.min(re, lines[i].space);
                }
            }
            return re;
        },
        //.............................................
        // 依次处理排序后的线段组的 offset，令其与前一个线段间隔为
        // 指定单位的倍数
        //  - lines    : 排序后的调整线数组
        //  - unit     : 线段间隔单位
        //  - maxTimes :「选」最大倍数，默认不限制
        rect_adjustlines_compact: function (lines, unit, maxTimes) {
            //console.log("-> rect_adjustlines_compact:");
            if (lines.length > 1) {
                for (var i = 1; i < lines.length; i++) {
                    var lo = lines[i];
                    //this.rect_adjustlines_dump(lo, i);
                    var times = Math.round(lo.space / unit);
                    //console.log("times_org:", times);
                    if (maxTimes) {
                        times = Math.min(times, maxTimes);
                    }
                    //console.log("times_max:", times);
                    lo.space = Math.round(times * unit);
                    lo.offset = lines[i - 1].offset + lo.space;
                    //this.rect_adjustlines_dump(lo, i);
                }
            }
        },
        //.............................................
        // 将调整线的 offset 重新应用到对应矩形的边
        rect_adjustlines_apply: function (lines) {
            if (lines.length > 0) {
                // 更新顶点
                for (var fi in lines) {
                    var lo = lines[fi];
                    for (var si in lo.refers) {
                        var ref = lo.refers[si];
                        ref.rect[ref.key] = lo.offset;
                    }
                }
                // 重新设置矩形们其他的尺寸
                for (var fi in lines) {
                    var lo = lines[fi];
                    for (var si in lo.refers) {
                        var ref = lo.refers[si];
                        this.rect_count_tlbr(ref.rect);
                    }
                }
            }
        },
        //.............................................
        // 计算矩形面积
        rect_area: function (rect) {
            return rect.width * rect.height;
        },
        //.............................................
        // 根据指定的 jQuery 集合，计算所有元素的最小矩形
        rect_union_by: function (jq) {
            var rects = [];
            jq.each(function () {
                rects.push(zUtil.rect(this));
            });
            return zUtil.rect_union.apply(this, rects);
        },
        //.............................................
        // 计算多个矩形的最小相并矩形
        rect_union: function () {
            var rects = this.toArgs(arguments, true);
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
                var r = rects[i];
                r2.top = Math.min(r2.top, r.top);
                r2.left = Math.min(r2.left, r.left);
                r2.right = Math.max(r2.right, r.right);
                r2.bottom = Math.max(r2.bottom, r.bottom);
            }
            // 返回
            return this.rect_count_tlbr(r2);
        },
        //.............................................
        // 相并面积
        rect_union_area: function () {
            var rects = this.toArgs(arguments, true);
            var r2 = this.rect_union(rects);
            return this.rect_area(r2);
        },
        //.............................................
        // 计算多个矩形的最大相交矩形，只有一个参数的话，永远返回 null
        rect_overlap: function (rectA, rectB) {
            var rects = this.toArgs(arguments, true);
            // 少于1个
            if (!rects || rects.length <= 1)
                return null;
            // 多个
            var r2 = rects[0];
            for (var i = 1; i < rects.length; i++) {
                var r = rects[i];
                r2.top = Math.max(r2.top, r.top);
                r2.left = Math.max(r2.left, r.left);
                r2.right = Math.min(r2.right, r.right);
                r2.bottom = Math.min(r2.bottom, r.bottom);
            }
            // 返回
            return this.rect_count_tlbr(r2);
        },
        //.............................................
        // 相交面积
        rect_overlap_area: function (rectA, rectB) {
            var rects = this.toArgs(arguments, true);
            var r2 = this.rect_overlap(rects);
            return this.rect_area(r2);
        },
        //.............................................
        // A 是否全部包含 B
        rect_contains: function (rectA, rectB) {
            return rectA.top <= rectB.top
                && rectA.bottom >= rectB.bottom
                && rectA.left <= rectB.left
                && rectA.right >= rectB.right;
        },
        //.............................................
        // 一个点是否在矩形之中，是否算上边
        rect_in: function (rect, pos, countBorder) {
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
        rect_is_overlap: function (rectA, rectB) {
            return this.rect_overlap_area(rectA, rectB) > 0;
        },
        //.............................................
        // 生成一个新的矩形
        // 用 B 限制 A，会保证 A 完全在 B 中，实在放不下了，就剪裁
        rect_clip_boundary: function (rectA, rectB) {
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

            // 最后取一下重叠部分
            return this.rect_overlap(re, rectB);
        },
        //.............................................
        // 修改 A ，将其中点移动到某个位置
        // 第二个参数对象只要有 x,y 就好了，因此也可以是另外一个 Rect
        rect_move_xy: function (rect, pos) {
            rect.x = pos.x;
            rect.y = pos.y;
            return this.rect_count_xywh(rect);
        },
        //.............................................
        // 修改 ，将其左上顶点移动到某个位置
        // 第二个参数对象只要有 x,y 就好了，因此也可以是另外一个 Rect
        // offset 表示一个偏移量，可选。通用用来计算移动时，鼠标与左上顶点的偏移
        rect_move_tl: function (rect, pos, offset) {
            rect.top = pos.y - (offset ? offset.y : 0);
            rect.left = pos.x - (offset ? offset.x : 0);
            return this.rect_count_tlwh(rect);
        },
        /*.............................................
        将一个元素停靠再另外一个元素上，根据目标元素在文档的位置来自动决定最佳的停靠方案
        !前提: 浮动元素将作为 position:fixed 来停靠
        ele: 被停靠元素
        ta:  浮动元素
        opt: {
            // H | V 表示是停靠在水平边还是垂直边，默认 H
            // 或者可以通过 "VA|VB..." 来直接指定停靠的区域
            // 可以的值为 ABCDNSWE 八个字符表示停靠元素所在页面的八个区域
            // 或者你可以直接指定 left|right|up|down 来指定浮动元素的位置
            // 不指定则表示根据形状和位置自动计算
            mode : "H", 
            ignoreSetCss ：true,  // 表示不强制设置 position
            viewport : Rect       // 获取矩形的时候，是否要对视口做补偿
        }
        @return {
            mode : "H",
            area : "A",
            direction : "up"  // 浮动元素位于停靠元素的四个方向 up|down|left|right
        };
        */
        dock: function (ele, ta, opt, ignoreSetCss, vp) {
            opt = opt || {};
            // 处理一下参数
            if(_.isString(opt)) {
                opt = {
                    mode : opt,
                    ignoreSetCss : ignoreSetCss,
                    viewport : vp
                };
            }
            // 处理节点
            var jq = $(ele);
            var jTa = $(ta);
            if (!opt.ignoreSetCss) {
                jTa.css("position", "fixed");
            }
            // 得到浮动元素大小
            var sub = {
                width: jTa.outerWidth(true),
                height: jTa.outerHeight(true)
            };
            // 得到被停靠元素的矩形信息
            var rect = $D.rect.gen(jq, opt.viewport);
            //console.log($D.rect.dumpValues(rect))
            // 得到页面的矩形信息
            var viewport = $D.dom.winsz(jq[0].ownerDocument.defaultView);
            //console.log("viewport:", viewport);
            /*
             看看这个位置在页面的那个区域
             +----+----+
             | A [N] B |
             +[W]-+-[E]+
             | C [S] D |
             +----+----+
             */
            var wx = viewport.width / 3;
            var wy = viewport.height / 3;
            var xx = [viewport.left + wx, viewport.left + wx*2];
            var yy = [viewport.top  + wy, viewport.top  + wy*2];
            var mode, area,direction;
            // A/W/C
            if(rect.x < xx[0]) {
                area = rect.y<yy[0]
                        ? "A"
                        : (rect.y>yy[1] ? "C" : "W");
            }
            // N/S
            else if(rect.x >= xx[0] && rect.x <= xx[1]) {
                area = rect.y<viewport.y ? "N" : "S";
            }
            // B/E/D
            else {
                area = rect.y<yy[0]
                        ? "B"
                        : (rect.y>yy[1] ? "D" : "E");
            }

            // 看看是否指定了停靠模式
            var m = /^([VH])?([ABCDWENS])?$/.exec((opt.mode || "H").toUpperCase());
            if(m) {
                mode = m[1] || mode;
                area = m[2] || area;
            }
            // left
            else if("left" == opt.mode) {
                mode = "V";
                area = "E";
            }
            // right
            else if("right" == opt.mode) {
                mode = "V";
                area = "W";
            }
            // up
            else if("up" == opt.mode) {
                mode = "H";
                area = "S";
            }
            // down
            else if("down" == opt.mode) {
                mode = "H";
                area = "N";
            }
            // 默认，根据自己的形状来决定
            else {
                // 得到自己矩形的比例
                var scale = rect.width / rect.height;
                // 扁的，默认停靠在水平边
                // 窄的，默认停靠在垂直边
                mode = scale < 1 ? "V" : "H";
            }
            
            // 准备 css 偏移的原型
            var off = {
                "top"    : "unset",
                "left"   : "unset",
                "right"  : "unset",
                "bottom" : "unset"
            };

            // 停靠在垂直边
            /*
             +----+----+
             | A [N] B |
             +[W]-+-[E]+
             | C [S] D |
             +----+----+
             */
            if ("V" == mode) {
                // A : 右上角对齐
                if ("A" == area) {
                    _.extend(off, {
                        "left": rect.right,
                        "top": rect.top,
                    });
                    direction = "right";
                }
                // B : 左上角对齐
                else if ("B" == area) {
                    _.extend(off, {
                        "right": viewport.width - rect.left,
                        "top": rect.top
                    });
                    direction = "left";
                }
                // C : 右下角对齐
                else if ("C" == area) {
                    _.extend(off, {
                        "left": rect.right,
                        "bottom": viewport.height - rect.bottom
                    });
                    direction = "right";
                }
                // D : 左下角对齐
                else if ("D" == area) {
                    _.extend(off, {
                        "right": viewport.width - rect.left,
                        "bottom": viewport.height - rect.bottom
                    });
                    direction = "left";
                }
                // W : 右边中对齐
                else if ("N" == area || "W" == area || "S" == area) {
                    _.extend(off, {
                        "left": rect.right,
                        "top": rect.top - (sub.height - rect.height)/2
                    });
                    direction = "right";
                }
                // E : 左边中对齐
                else {
                    _.extend(off, {
                        "right": viewport.width - rect.left,
                        "top": rect.top - (sub.height - rect.height)/2
                    });
                    direction = "left";
                }
            }
            // 停靠在上水平边
            /*
             +----+----+
             | A [N] B |
             +[W]-+-[E]+
             | C [S] D |
             +----+----+
             */
            else {
                // A : 左下角对齐
                if ("A" == area) {
                    _.extend(off, {
                        "left": rect.left,
                        "top": rect.bottom
                    });
                    direction = "down";
                }
                // B : 右下角对齐
                else if ("B" == area) {
                    _.extend(off, {
                        "left": rect.right - sub.width,
                        "top": rect.bottom
                    });
                    direction = "down";
                }
                // C : 左上角对齐
                else if ("C" == area) {
                    _.extend(off, {
                        "left": rect.left,
                        "bottom": viewport.height - rect.top
                    });
                    direction = "up";
                }
                // D : 右上角对齐
                else if ("D" == area) {
                    _.extend(off, {
                        "left": rect.right - sub.width,
                        "bottom": viewport.height - rect.top
                    });
                    direction = "up";
                }
                // N : 下边中对齐
                else if ("N" == area || "W" == area || "E" == area) {
                    _.extend(off, {
                        "left": rect.left - (sub.width-rect.width)/2,
                        "top": rect.bottom
                    });
                    direction = "down";
                }
                // S : 上边中对齐
                else {
                    _.extend(off, {
                        "left": rect.left - (sub.width-rect.width)/2,
                        "bottom": viewport.height - rect.top
                    });
                    direction = "up";
                }
            }
            /*
             +----+----+
             | A [N] B |
             +[W]-+-[E]+
             | C [S] D |
             +----+----+
             */
            // 调整上下边缘
            if (_.isNumber(off.top) && off.top < viewport.top) {
                off.top = viewport.top;
            }
            else if (_.isNumber(off.bottom) && off.bottom > viewport.bottom) {
                off.top = viewport.bottom - sub.height;
            }
            // 设置属性
            jTa.css(off);
            // 返回
            return {
                mode : mode,
                area : area,
                direction :direction
            };
        },
        //.............................................
        // 将一个元素停靠再另外一个元素上，根据目标元素在文档的位置来自动决定最佳的停靠方案
        // 
        // !前提: 浮动元素将作为 position:absolute 来停靠。并且假设被停靠元素包含浮动元素
        //        且其会被设置成 posoition:relative
        //
        // @ele  - 被停靠元素
        // @ta   - 浮动元素
        // @mode - H | V 表示是停靠在水平边还是垂直边，默认 H
        //         或者可以通过 "VA|VB|VC|VD|HA|HB|HC|HD" 来直接指定停靠的区域
        // @ignoreSetCss - true 表示不强制设置 position
        dockIn: function (ele, ta, mode, ignoreSetCss) {
            var jq = $(ele);
            var jTa = $(ta);
            if (!ignoreSetCss) {
                jq.css("position", "relative");
                jTa.css("position", "absolute");
            }
            // 得到浮动元素大小
            var sub = {
                width: jTa.outerWidth(true),
                height: jTa.outerHeight(true)
            };
            // 得到被停靠元素的矩形信息
            var rect = $z.rect(jq);
            //console.log(" rect  :", rect);
            // 计算页面的中点
            var viewport = $z.rect(jq.closest("body"));
            //console.log("viewport:", viewport);
            /*
             看看这个位置在页面的那个区域
             +----+----+
             | A [N] B |
             +----+----+
             | C [S] D |
             +----+----+
             */
            var off = {
                "top": "",
                "left": "",
                "right": "",
                "bottom": ""
            };

            // 分析模式
            var m = /^([VH])([ABCDNS])?$/.exec((mode || "H").toUpperCase());
            var mode = m ? m[1] : "H";
            // 分析一下视口所在网页的区域
            var area = (m ? m[2] : null) || (
                viewport.x >= rect.x && viewport.y >= rect.y ? "A" : (
                    viewport.x <= rect.x && viewport.y >= rect.y ? "B" : (
                        viewport.x >= rect.x && viewport.y <= rect.y ? "C"
                            : "D"
                    )
                )
            );
            // 停靠在垂直边
            if ("V" == mode) {
                // A : 右上角对齐
                if ("A" == area) {
                    _.extend(off, {
                        "left": rect.width,
                        "top": 0,
                    });
                }
                // B : 左上角对齐
                else if ("B" == area) {
                    _.extend(off, {
                        "right": 0 - rect.width,
                        "top": 0
                    });
                }
                // C : 右下角对齐
                else if ("C" == area) {
                    _.extend(off, {
                        "left": rect.width,
                        "bottom": 0
                    });
                }
                // D : 左下角对齐
                else {
                    _.extend(off, {
                        "left": 0 - rect.width,
                        "bottom": 0
                    });
                }
            }
            // 停靠在上水平边
            /*
             +----+----+
             | A [N] B |
             +----+----+
             | C [S] D |
             +----+----+
             */
            else {
                // A : 左下角对齐
                if ("A" == area) {
                    _.extend(off, {
                        "left": 0,
                        "top": rect.height
                    });
                }
                // B : 右下角对齐
                else if ("B" == area) {
                    _.extend(off, {
                        "right": 0,
                        "top": rect.height
                    });
                }
                // C : 左上角对齐
                else if ("C" == area) {
                    _.extend(off, {
                        "left": 0,
                        "bottom": 0 - rect.height
                    });
                }
                // D : 右上角对齐
                else if ("D" == area) {
                    _.extend(off, {
                        "right": 0,
                        "bottom": 0 - rect.height
                    });
                }
                // N : 下边中对齐
                else if ("N" == area) {
                    _.extend(off, {
                        "left": (rect.width - sub.width)/2,
                        "top": rect.height,
                    });
                }
                // S : 上边中对齐
                else {
                    _.extend(off, {
                        "left": (rect.width - sub.width)/2,
                        "bottom": 0 - rect.height
                    });
                }
            }
            // TODO 暂时先不调整
            // 调整上下边缘
            // if (_.isNumber(off.top) && off.top < viewport.top) {
            //     off.top = viewport.top;
            // }
            // else if (_.isNumber(off.bottom) && off.bottom > viewport.bottom) {
            //     off.top = viewport.bottom - sub.height;
            // }
            // 设置属性
            jTa.css(off);
        },
        //.............................................
        // 获得视口的矩形信息
        winsz: function (win) {
            win = win || window;
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
            ;
            // 继续剩余的值
            rect.top = 0;
            rect.left = 0;
            rect.right = rect.left + rect.width;
            rect.bottom = rect.top + rect.height;
            rect.x = rect.left + rect.width / 2;
            rect.y = rect.top + rect.height / 2;

            return rect;
        },
        /*.............................................
        改变指定窗口的根元素 fontSize 属性，这个主要用来
        配合 rem 来进行浏览器适配
         - context : {
            doc    : Document,
            win    : Window,
            root   : RootElement,
         }
         - designWidth : 本窗口的设计宽度，通常为  640
         - maxNb : 最大值，通常为 100
         - minNb : 最小值，通常为 70
        */
        do_change_root_fontSize: function (context, designWidth, maxNb, minNb) {
            context = context || {
                doc    : document,
                win    : window,
                root   : document.documentElement,
            };
            designWidth = designWidth || 640;
            maxNb = maxNb || 100;
            minNb = minNb || 70;
            var size = (context.win.innerWidth / designWidth) * maxNb;
            //console.log(size, Math.max(size, minNb), Math.min(Math.max(size, minNb), maxNb))
            var px = Math.min(Math.max(size, minNb), maxNb);
            context.root.style.fontSize = px + 'px';
        },
        //.............................................
        // 得到一个元素的外边距
        margin: function ($ele) {
            return {
                x: $ele.outerWidth(true) - $ele.outerWidth(),
                y: $ele.outerHeight(true) - $ele.outerHeight()
            };
        },
        //.............................................
        // 得到一个元素的内边距(包括 border)
        padding: function ($ele) {
            return {
                x: $ele.outerWidth() - $ele.width(),
                y: $ele.outerHeight() - $ele.height()
            };
        },
        //.............................................
        // 获得当前系统当前浏览器中滚动条的宽度
        // TODO 代码实现的太恶心，要重构!
        scrollBarWidth: function () {
            if (!window.SCROLL_BAR_WIDTH) {
                var newDivOut = "<div id='div_out' style='position:relative;width:100px;height:100px;overflow-y:scroll;overflow-x:scroll'></div>";
                var newDivIn = "<div id='div_in' style='position:absolute;width:100%;height:100%;'></div>";
                var scrollWidth = 0;
                $('body').append(newDivOut);
                $('#div_out').append(newDivIn);
                var divOutS = $('#div_out');
                var divInS = $('#div_in');
                scrollWidth = divOutS.width() - divInS.width();
                $('#div_out').remove();
                $('#div_in').remove();
                window.SCROLL_BAR_WIDTH = scrollWidth;
            }
            return window.SCROLL_BAR_WIDTH;
        },
        //.............................................
        // 获得当前系统当前浏览器中滚动条的宽度
        // TODO 代码实现的太恶心，要重构!
        scrollBarHeight: function () {
            if (!window.SCROLL_BAR_HEIGHT) {
                var newDivOut = "<div id='div_out' style='position:relative;width:100px;height:100px;overflow-y:scroll;overflow-x:scroll'></div>";
                var newDivIn = "<div id='div_in' style='position:absolute;width:100%;height:100%;'></div>";
                var scrollWidth = 0;
                $('body').append(newDivOut);
                $('#div_out').append(newDivIn);
                var divOutS = $('#div_out');
                var divInS = $('#div_in');
                scrollHeight = divOutS.height() - divInS.height();
                $('#div_out').remove();
                $('#div_in').remove();
                window.SCROLL_BAR_HEIGHT = scrollHeight;
            }
            return window.SCROLL_BAR_HEIGHT;
        },
        //.............................................
        // 查询给定元素是否有滚动条
        // 返回 {x:false, y:true}
        scrollbar: function (jq) {
            var el = $(jq)[0];
            var cs = window.getComputedStyle(el);
            var re = {x: false, y: false};
            var ofX = cs.getPropertyValue("overflow-x");
            var ofY = cs.getPropertyValue("overflow-y");
            // 有横向滚动条
            if ("scroll" == ofX
                || ("auto" == ofX && el.clientWidth < el.scrollWidth)) {
                re.x = true;
            }
            // 有纵向滚动条
            if ("scroll" == ofY
                || ("auto" == ofY && el.clientHeight < el.scrollHeight)) {
                re.y = true;
            }
            // 返回
            return re;
        },
        // 滚动到底部
        scroll2bottom: function (jq) {
            var h = jq[0].scrollHeight;
            jq.scrollTop(h);
        },
        //...............................................................
        // 跳转到顶部
        // - el: 文档对象，可以是 document|window|element|jQuery 总之会找到一个对应文档
        // - du: 滚动动画的时间，默认 500（毫秒），0 表示不显示动画
        // - scrollTop: 滚动到哪里， 默认为 0
        doAnimatDocumentScrollTop: function(el, du, scrollTop) {
            // 首先找到文档
            var doc;
            if(_.isElement(el)){
                doc = el.ownerDocument;
            }
            // jQuery
            else if(this.isjQuery(el)){
                doc = el[0].ownerDocument;
            }
            // 就是文档
            else if(el.defaultView) {
                doc = el;
            }
            // 窗口
            else if(el.document) {
                doc = el.document;
            }
            // 其他的不支持
            else {
                throw "Unsupport target for doAnimatDocumentScrollTop";
            }

            // 确定滚动动画时间
            if(!_.isNumber(du) || du<0){
                du = 500;
            }

            // 确定滚动位置
            if(!_.isNumber(scrollTop)){
                scrollTop = 0;
            }

            // 动画
            if(du>0) {
                $([doc.documentElement, doc.body]).animate({
                    scrollTop : scrollTop
                }, du);
            }
            // 直接设置
            else {
                doc.documentElement.scrollTop = scrollTop;
                doc.body.scrollTop = scrollTop;
            }
        },
        //.............................................
        // 从数组里获取值
        //   arr   : 数组
        //   index : 下标
        //   dft   : 如果下标越界，返回的东东
        getItem: function (arr, index, dft) {
            // 从后面取重新计算一下下标
            if (index < 0) {
                index = arr.length + index;
            }
            if (index < 0 || index >= arr.length)
                return dft;
            return arr[index];
        },
        // 生成一个新数组，移除指定下标元素
        //   arr   : 数组
        //   index : 要移除元素下标，如果是个数组，表示多个
        removeItemAt: function (arr, index) {
            // 一个下标
            if (_.isNumber(index)) {
                var list = [];
                for (var i = 0; i < arr.length; i++) {
                    if (i != index)
                        list.push(arr[i]);
                }
                return list;
            }
            // 多个下标
            else if (_.isArray(index)) {
                var list = [];
                for (var i = 0; i < arr.length; i++) {
                    if (index.indexOf(i) < 0)
                        list.push(arr[i]);
                }
                return list;
            }
        },
        //.............................................
        // 从普通对象里获取值
        // 根据键获取某对象的值，如果键是  "." 分隔的，则依次一层层进入对象获取值
        //   obj : 对象
        //   key : 键值，支持 "."
        //   dft : 如果木找到，返回的东东
        getValue: function (obj, key, dft) {
            // 首先硬取试试
            var re = obj[key];
            if (!_.isUndefined(re))
                return re;

            // 嗯，按照路径来白
            var ks = _.isArray(key)
                ? key
                : _.isString(key)
                    ? key.split(".")
                    : ["" + key];
            var o = obj;
            if (ks.length > 1) {
                var lastIndex = ks.length - 1;
                for (var i = 0; i < lastIndex; i++) {
                    key = ks[i];
                    o = o[key];
                    if (!o) {
                        return dft;
                    }
                }
                key = ks[lastIndex];
                re = o[key];
            }
            return _.isUndefined(re) ? dft : re;
        },
        //.............................................
        // 向普通对象里设置值
        // 根据键获取某对象的值，如果键是  "." 分隔的，则依次一层层进入对象设值
        //   obj : 对象
        //   key : 键值，支持 "."
        //   val : 值
        setValue: function (obj, key, val) {
            var ks = _.isArray(key)
                ? key
                : _.isString(key)
                    ? key.split(".")
                    : ["" + key];
            var o = obj;
            if (ks.length > 1) {
                var lastIndex = ks.length - 1;
                for (var i = 0; i < lastIndex; i++) {
                    key = ks[i];
                    o = o[key];
                    if (!o) {
                        o = {};
                        obj[key] = o;
                    }
                }
                key = ks[lastIndex];
            }
            o[key] = val;
            // 返回以便链式赋值
            return obj;
        },
        //.............................................
        // 向普通对象里设置值，如果值是无效的，那么无视
        setMeaningful: function (obj, key, val) {
            if (_.isObject(obj) && $z.isMeaningful(val)) {
                obj[key] = val;
            }
        },
        //.............................................
        // 判断一个值是否是有意义的
        // undefined, null, NaN, 空串 都是没意义的
        isMeaningful: function (v) {
            if (_.isUndefined(v) || _.isNull(v))
                return false;
            if (_.isNumber(v) && isNaN(v))
                return false;
            if (_.isString(v) && v.length == 0)
                return false;
            return true;
        },
        //.............................................
        // 将一个值加入对象的某个键，如果已经存在，则变数组
        //   obj : 对象
        //   key : 键值，支持 "."
        //   val : 值
        //   forceArray : 经过处理的值是否一定是个数组，默认true
        pushValue: function (obj, key, val, forceArray) {
            if(_.isUndefined(forceArray))
                forceArray = true;

            var ks = _.isArray(key) ? key : key.split(".");
            var o = obj;
            if (ks.length > 1) {
                var lastIndex = ks.length - 1;
                for (var i = 0; i < lastIndex; i++) {
                    key = ks[i];
                    o = o[key];
                    if (!o) {
                        o = {};
                        obj[key] = o;
                    }
                }
                key = ks[lastIndex];
            }

            var ov = o[key];
            // 加入
            if(_.isUndefined(ov)) {
                o[key] = forceArray ? [].concat(val) : val;
            }
            // 本来就是数组
            else if(_.isArray(ov) && ov.length > 0){
                o[key] = ov.concat(val);
            }
            // 创建新数组
            else {
                o[key] = [].concat(ov, val);
            }
        },
        //.............................................
        // 将一个值加入对象的某个键，如果已经存在，则变数组
        //   obj : 对象
        //   key : 键值
        //   val : 值
        //   forceArray : 经过处理的值是否一定是个数组，默认false
        addValue: function(obj, key, val, forceArray) {
            var ov = obj[key];
            // 加入
            if(_.isUndefined(ov)) {
                obj[key] = forceArray ? [].concat(val) : val;
            }
            // 本来就是数组
            else if(_.isArray(ov) && ov.length > 0){
                obj[key] = ov.concat(val);
            }
            // 创建新数组
            else {
                obj[key] = [].concat(ov, val);
            }
        },
        //.............................................
        // 根据一个映射对象，从源对象中生成一个新对象
        // 映射对象值的格式有两种:
        //  =key  表示从源对象中取对应键值
        //  xxx   直接就是静态值
        // 如果传入的 obj 为假，则直接返回 null
        mappingObj: function (mapping, obj) {
            if (!obj)
                return null;
            var re = {};
            for (var key in mapping) {
                var val = mapping[key];
                var m = /^=(.+)$/.exec(val);
                // 直接等于某个值
                if (m) {
                    re[key] = obj[m[1]];
                }
                // 进行模板替换
                else if(val.indexOf('{{') >= 0) {
                    re[key] = zUtil.tmpl(val)(obj);
                }
                // 静态值
                else {
                    re[key] = val;
                }
            }
            return re;
        },
        // 使用canvas对图片进行压缩
        // file: <input type='file'> 中获取到的file对象
        // afterCompress: 压缩后回调 afterCompress(newFile){...}
        // iosfix：是否修正ios设备上传导致的旋转问题
        // size: 压缩的大小，默认不填则为50%  可以填写单个数字表示百分比 比如30 或者填写指定宽高 300x400
        compressImageFile: function (file, afterCompress, iosfix, size) {
            // 旋转修正
            if (iosfix) {
                console.log('isofix: ' + navigator.userAgent);
                EXIF.getData(file, function () {
                    EXIF.getAllTags(this);
                    var orientation = EXIF.getTag(this, 'Orientation');
                    console.log('isofix-na: ' + navigator.userAgent);
                    console.log('isofix-or: ' + orientation);
                    if (orientation == '' || typeof orientation == 'undefined') {
                        orientation = 1;
                    }
                    $z._compressAndFixImageFile(file, afterCompress, orientation, size);
                });
            }
            // 直接压缩
            else {
                $z._compressAndFixImageFile(file, afterCompress, 1, size);
            }
        },
        _compressAndFixImageFile: function (file, afterCompress, orientation, size) {
            // 压缩图片需要的一些元素和对象
            var reader = new FileReader();
            var img = new Image();
            // 缩放图片需要的canvas
            var canvas = document.createElement('canvas');
            var context = canvas.getContext('2d');

            // 1,3不需要交换宽高
            var needWHChange = (orientation == 6 || orientation == 8);

            // base64地址图片加载完毕后
            img.onload = function () {
                // 图片原始尺寸
                var originWidth = this.width;
                var originHeight = this.height;

                console.log("origin: w" + originWidth + " h" + originHeight);

                // 最大可接受尺寸
                var maxWidth = 0;
                var maxHeight = 0;

                // 无配置则默认大小一半
                if (!size) {
                    maxWidth = Math.round(originWidth * 0.5);
                    maxHeight = Math.round(originHeight * 0.5);
                } else {
                    size = ("" + size).toLowerCase();
                    // 指定宽高
                    if (size.indexOf("x") != -1) {
                        var swh = size.split("x");
                        maxWidth = parseInt(swh[0]);
                        maxHeight = parseInt(swh[1]);
                        // 交换下，因为这是在旋转前的
                        if (needWHChange) {
                            var tmp = maxWidth;
                            maxWidth = maxHeight;
                            maxHeight = tmp;
                        }
                    }
                    // 单数字百分比
                    else {
                        var per = parseInt(size);
                        if (per == 100) {
                            maxWidth = originWidth;
                            maxHeight = originHeight;
                        } else {
                            maxWidth = Math.round(originWidth * (per / 100));
                            maxHeight = Math.round(originHeight * (per / 100));
                        }
                    }
                }

                // 目标尺寸
                var targetWidth = originWidth, targetHeight = originHeight;
                // 图片尺寸超过最大图片限制
                if (originWidth > maxWidth || originHeight > maxHeight) {
                    if (originWidth / originHeight > maxWidth / maxHeight) {
                        // 更宽，按照宽度限定尺寸
                        targetWidth = maxWidth;
                        targetHeight = Math.round(maxWidth * (originHeight / originWidth));
                    } else {
                        targetHeight = maxHeight;
                        targetWidth = Math.round(maxHeight * (originWidth / originHeight));
                    }
                }

                // canvas对图片进行缩放
                canvas.width = targetWidth;
                canvas.height = targetHeight;
                // 清除画布
                context.clearRect(0, 0, targetWidth, targetHeight);
                // 图片压缩
                context.drawImage(img, 0, 0, targetWidth, targetHeight);

                // 是否需要选择
                if (orientation != 1) {
                    switch (orientation) {
                        case 6://需要顺时针（向左）90度旋转
                            canvas.width = targetHeight;
                            canvas.height = targetWidth;
                            context.rotate(Math.PI / 2);
                            // (0,-imgHeight) 从旋转原理图那里获得的起始点
                            context.drawImage(img, 0, -targetHeight, targetWidth, targetHeight);
                            break;
                        case 8://需要逆时针（向右）90度旋转
                            canvas.width = imgHeight;
                            canvas.height = imgWidth;
                            context.rotate(3 * Math.PI / 2);
                            context.drawImage(img, -targetWidth, 0, targetWidth, targetHeight);
                            break;
                        case 3://需要180度旋转
                            context.rotate(Math.PI);
                            context.drawImage(img, -targetWidth, -targetHeight, targetWidth, targetHeight);
                            break;
                    }
                }

                // canvas转为blob并上传
                canvas.toBlob(function (blob) {
                    afterCompress(blob);
                }, file.type || 'image/png');
            };
            // 文件base64化，以便获知图片原始尺寸
            reader.onload = function (e) {
                img.src = e.target.result;
            };

            // 真正加载图片文件
            if (file.type.indexOf("image") == 0) {
                reader.readAsDataURL(file);
            } else {
                throw "file is not image";
            }
        },
        //.............................................
        // 执行一个 HTML5 的文件上传操作，函数接受一个配置对象：
        //
        // {
        //     file : {..},     // 要上传的文件对象
        //     // 上传的进度回调，即接受 XMLHttpRequest 的 "progress" 事件监听函数
        //     progress : function(e){
        //         // 比较有用的是 e.loaded 和 e.total
        //     },
        //     // 下面的回调函数会在上传完成后，根据条件不同分别被调用
        //     beforeSend : function(xhr){..},      // 执行 xhr.send() 前调用
        //     done : function(re){..},             // 上传成功后调用
        //     fail : function(re){..},             // 上传失败后被调用
        //     complete : function(re, status){..}, // 无论成功还是失败都会被调用
        //     // 服务器返回后，可以通过下面的函数预先处理一下返回
        //     // 如果没有声明这个参数，则返回原生的 xhr 对象
        //     // 如果是值 "ajax"，则用 Nutz 标准的  AjaxReturn 来处理返回值
        //     evalReturn : function(xhr){return {
        //          re     : {..},         // 会被当做参数传入 done|faile|complete
        //          status : "done|fail"   // 表示成功还是失败
        //     }},
        //     // 下面是上传到服务器的目标设置
        //     // 上传的目标url是一个字符串模板，会用本对象自身的键值来填充
        //     url  : "/o/upload/?nm=<%=file.name%>&sz=<%=file.size%>"
        // }
        uploadFile: function (opt) {
            // 没必要上传
            if (!opt.file)
                throw "!!! without field : 'file' ";
            // 开始上传
            var xhr = new XMLHttpRequest();
            // 检查
            if (!xhr.upload) {
                throw "XMLHttpRequest object don't support upload for your browser!!!";
            }
            // 用 ajax 方式处理返回值
            if ("ajax" == opt.evalReturn) {
                opt.evalReturn = function (xhr) {
                    // 如果是 200 那么具体看 AjaxReturn 的内容
                    if (xhr.status == 200) {
                        var ajaxRe = $z.fromJson(xhr.responseText);
                        if (ajaxRe.ok)
                            return {re: ajaxRe.data, status: "done"};
                        return {re: ajaxRe, status: "fail"};
                    }
                    // 否则一定是错误
                    return {re:xhr.responseText, status: "fail"};
                };
            }
            // 默认的处理方式
            else if (!(typeof opt.evalReturn == "function")) {
                opt.evalReturn = function (xhr) {
                    return {re: xhr, status: xhr.status == 200 ? "done" : "fail"};
                };
            }
            // 进度回调
            if (typeof opt.progress == "function")
                xhr.upload.addEventListener("progress", opt.progress, false);
            // 完成的处理
            xhr.onreadystatechange = function (e) {
                if (xhr.readyState == 4) {
                    var r = opt.evalReturn(xhr);
                    $z.invoke(opt, r.status, [r.re]);
                    // 统一处理完成
                    $z.invoke(opt, "complete", [r.re, r.status]);
                }
            };
            // 准备请求对象头部信息
            var url = ($z.tmpl(opt.url))(opt);
            //console.log("upload to:", url);
            xhr.open("POST", url, true);
            xhr.setRequestHeader('Content-type', "application/x-www-form-urlencoded; charset=utf-8");
            // 修改提示图标的标签
            $z.invoke(opt, "beforeSend", [xhr]);
            // 执行上传
            xhr.send(opt.file);
        },
        // 对于 AJAX 请求的返回对象，进行检查，如果发现是过期 session 报的错，直接踢回登录页面
        checkSessionNoExists: function (re, loginUrl) {
            if (re && !re.ok && "e.se.noexists" == re.errCode) {
                window.location = loginUrl || "/";
            }
        },
        // 应用加载的静态资源
        _app_rs: {},
        // 读取静态资源并且缓存
        // 资源描述符如果不可识别将原样返回，现在支持下列资源种类
        //  - json:///path/to/json
        //  - text:///path/to/text
        //  - jso:///path/to/js
        loadResource: function (rs, callback, context) {
            var ME = this;
            // 对结果的处理函数
            var _eval_re = function (type, re) {
                // 根据特定的类型处理数据
                if ("json" == type) {
                    return $z.fromJson(re);
                }
                if ("jso" == type) {
                    return eval('(' + re + ')');
                }
                // 那就是字符串咯
                return re;
            };
            // 请求资源必须是个字符串
            if (_.isString(rs)) {
                // 分析一下
                var m = /^(jso|json|text):\/\/(.+)$/.exec(rs);
                if (m) {
                    var reObj;
                    var type = m[1];
                    var url = m[2];
                    // 看看缓冲里有木有
                    context = context || this;
                    var str = ME._app_rs[rs];
                    // 缓冲里有，那么就不用请求
                    if (str) {
                        reObj = _eval_re(type, str);
                        return ME.doCallback(callback, [reObj], context);
                    }
                    // 看来要发起个请求喔
                    var async = _.isFunction(callback);
                    // console.log("async:", async)
                    var ajaxConf = {
                        method: "GET",
                        async: async,
                        data: {
                            auto_unwrap: /^json?$/.test(type)
                        },
                        dataType: "text",
                        success: function (re) {
                            // 计入缓存
                            ME._app_rs[rs] = re;
                            //console.log("success:", re);
                            // 根据特定的类型处理数据
                            reObj = _eval_re(type, re);

                            // 判断是否是过期
                            ME.checkSessionNoExists(reObj);

                            // 解开所有嵌套资源
                            reObj = ME.unwrapObjResource(reObj);

                            // 调用回调
                            if (_.isFunction(callback)) {
                                callback.call(context, ME.extend({}, reObj));
                            }
                        },
                        error: function (re, reason) {
                            alert("fail to load resource: " + rs + " : because " + reason);
                        }
                    };
                    // 发送请求
                    $.ajax(url, ajaxConf);

                    // 返回
                    return reObj;
                }
            }
            return ME.doCallback(callback, [rs], context);
        },
        // 深层检查一个对象，如果有字段引用了资源，则解析它
        unwrapObjResource: function (obj) {
            var ME = this;
            // 数组
            if (_.isArray(obj)) {
                for (var i = 0; i < obj.length; i++) {
                    obj[i] = ME.unwrapObjResource(obj[i]);
                }
            }
            // 对象
            else if (ME.isPlainObj(obj)) {
                for (var key in obj) {
                    var v = obj[key];
                    // 字符串
                    if (_.isString(v)) {
                        if (/^(jso|json|text):\/\/(.+)$/.test(v)) {
                            var rs = ME.loadResource(v, null);
                            obj[key] = rs;
                        }
                    }
                    // 其他统统来一下
                    else {
                        obj[key] = ME.unwrapObjResource(v);
                    }
                }
            }
            // 返回自身
            return obj;
        },
        // 评估一个字段配置项里面的 icon|text|display 函数，为这个配置项生成
        // __dis_obj 方法
        evalFldDisplay: function (fld) {
            var func;
            // 自定义的 display 方法
            if (fld.display) {
                // 本身就是函数
                if(_.isFunction(fld.display)){
                    func = fld.display;
                }
                // 一个函数调用
                else if(fld.display.method) {
                    func = function(o) {
                        //console.log(o, fld);
                        var context = this;
                        if(fld.display.context) {
                            context = zUtil.getValue(context, fld.display.context, this);
                        }
                        var func = context[fld.display.method];
                        if(_.isFunction(func)){
                            return func.call(context, o);
                        }
                        else {
                            console.warn('can not find method "'+fld.display.method+'"!');
                            return "::" + fld.display.method + '(obj)';
                        }
                    }
                }
                // 本身是个模板
                else if(_.isString(fld.display)) {
                    func = $z.tmpl(fld.display);
                }
                // 靠那是什么鬼！！！
                else {
                    throw "Unsupport fld.display:: " + $z.toJson(fld);
                }
            }
            // 同时有 text && icon
            else {
                // 预编译 icon
                if (fld.icon)
                    fld.__dis_icon = _.isFunction(fld.icon) ? fld.icon : $z.tmpl(fld.icon);

                // 预编译 text
                if (fld.text)
                    fld.__dis_text = _.isFunction(fld.text) ? fld.text : $z.tmpl(fld.text);

                // 同时有 icon 和 text
                if (fld.__dis_icon && fld.__dis_text) {
                    func = function (o, jso) {
                        var fld = _.isFunction(jso.type) ? jso.type() : jso;
                        return fld.__dis_icon.call(this, o, jso)
                            + fld.__dis_text.call(this, o, jso);
                    };
                }
                // 只有 icon
                else if (fld.__dis_icon) {
                    func = fld.__dis_icon;
                }
                // 只有 text
                else if (fld.__dis_text) {
                    func = fld.__dis_text;
                }
                // 啥都木有，直接显示吧
                else {
                    func = function (o, jso, UI) {
                        // 指定了类型转换，以及 UI
                        if(jso && jso.__jso_type && UI)
                            return UI.text(jso.parseByObj(o).toText());
                        // 否则直接变字符串
                        return zUtil.toJson(o);
                    }
                }
            }

            // 添加到配置信息里
            fld.__dis_obj = func;
        },
        // // 深层遍历一个给定的 Object，如果对象的字段有类似 "function(...}" 的字符串，将其变成函数对象
        // evalFunctionField : function(obj, memo){
        //     if(!memo)
        //         memo = [];
        //     for(var key in obj){
        //         var v = obj[key];
        //         // 字符串
        //         if(_.isString(v)){
        //             // 函数
        //             if(/^[ \t]*function[ \t]*\(.+\}[ \t]*/.test(v)){
        //                 obj[key] = eval('(' + v + ')');
        //             }
        //         }
        //         // 数组针对每个对象都来一下
        //         else if(_.isArray(v)){
        //             v.forEach(function(ele){
        //                 $z.evalFunctionField(ele, memo);
        //             });
        //         }
        //         // 如果是对象，但是应该无视
        //         else if(v instanceof jQuery || _.isElement(v)){
        //         }
        //         // 如果是普通对象，那么递归
        //         else if(_.isObject(v)){
        //             // 如果是特别指明 UI 调用的，变函数
        //             if(window.ZUI && v.callUI && v.method){
        //                 var UI = window.ZUI(v.callUI);
        //                 var func = UI[v.method] || UI.options[v.method];
        //                 if(_.isFunction(func)){
        //                     obj[key] = func;
        //                 }else{
        //                     throw "ZUI: " + v.callUI + "." + v.method + " not a function!!!";
        //                 }
        //             }
        //             // 否则递归
        //             else if(memo.indexOf(v)==-1){
        //                 memo.push(v);
        //                 $z.evalFunctionField(v, memo);
        //             }
        //         }
        //     }
        //     return obj;
        // },
        /*
         获取数据的方法，它的值可能性比较多:
         - 数组为静态数据，每个数据都必须是你希望的对象，那么这个数据会被直接使用
         [..]
         - 异步获取数据: 函数
         那么你的函数必须接收一个回调，当你处理完数据，调用这个回调，把你获得数组传回来
         function(callback){
         // TODO 不管怎样，获得一个对象数组
         // 假设你的对象数组是 objList，那么你必须这样调用回调
         callback(objList);
         }
         - 异步获取数据: ajaxReturn 或者是简单的 JSON 数组
         假设你给的 URL 的返回，根据鸭子法则(ok,data) 来判断是否是 AjaxReturn
         还是普通的 JSON 对象(数组)
         {
         url    : "/path/to/url",  // 请求的地址
         data   : {..},            // 请求的参数
         method : "GET"            // 请求方法，默认为 GET
         // 总之就是一个 jQuery 的 ajax 对象，但是 sucess 和 error 被定制了
         }
         */
        // data     - 待评估的数据源
        // params   - 【选】输入的参数,根据不同种类的数据源，会有不同的处理，
        //             不想输入参数，请输入 null
        // callback - 解析完数据调用的回调
        // context  - 指明特殊的回调的 this 参数，如果未定义，则采用本函数的 this
        evalData: function (data, params, callback, context) {
            if (!data) {
                $z.doCallback(callback, [], context);
                return;
            }

            // 异步的时候，返回值一定是 undefined
            var eval_re = undefined;
            var async = true;

            // 如果回调不是函数，那么将其视为 context，同时这必定是一个同步调用
            // 那么这里会设置返回值
            if (!_.isFunction(callback)) {
                context = context || callback;
                async = false;
                callback = null;
                // callback = function (objs) {
                //     eval_re = objs;
                // };
            }

            // 确保有 context
            context = context || this;
            // 数组
            if (_.isArray(data)) {
                callback.apply(context, [data]);
            }
            // 函数
            else if (_.isFunction(data)) {
                eval_re = data.call(context, params, function (objs) {
                    //callback.apply(context, [objs]);
                    zUtil.doCallback(callback, [objs], context);
                });
                // 如果有了有效的返回，那么说明函数是同步函数，不会处理 callback
                if (!_.isUndefined(eval_re)) {
                    zUtil.doCallback(callback, [eval_re], context);
                }
            }
            // 字符串，试图看看 context 里有没有 exec 方法
            else if (_.isString(data)) {
                //console.log(data, params);
                var str = ($z.tmpl(data))(params || {});
                //console.log(">> exec: ", str)
                var execFunc = context.exec || (context.options || {}).exec;
                if (_.isFunction(execFunc)) {
                    execFunc.call(context, str, {
                        async: async,
                        dataType: "json",
                        processData: true,
                        complete: function (re) {
                            //callback.apply(context, [re]);
                            zUtil.doCallback(callback, [re], context);
                        }
                    });
                } else {
                    throw "context DO NOT support exec : " + context;
                }
            }
            // 执行 ajax 请求
            else if (data.url) {
                $.ajax(_.extend({
                    method: "GET",
                    data: params || {},
                    dataType: "json",
                    async: async,
                    sucess: function (re) {
                        // 成功
                        if (_.isBoolean(re.ok) && re.data) {
                            // callback.apply(context, [re.data]);
                            zUtil.doCallback(callback, [re.data], context);
                        }
                        // 失败
                        else {
                            // callback.apply(context, re);
                            zUtil.doCallback(callback, [re], context);
                        }
                    },
                    error: function (xhr, textStatus, e) {
                        alert("OMG wnApi.evalData: " + textStatus + " : " + e);
                    }
                }, data));
            }
            // 厄，弱弱的直接返回一下吧
            else {
                // callback.apply(context, [data]);
                eval_re = data;
                zUtil.doCallback(callback, [data], context);
            }
            // 返回
            return eval_re;
        },
        //.............................................
        // 传入的如果是一个函数，执行它，否则直接返回
        evalObjValue: function (val, params, context) {
            if (_.isFunction(val)) {
                return val.apply(context || this, params || []);
            }
            return val;
        },
        //.............................................
        // d - 可被 parseDate 接受的日期(MS,str,Date)
        // off - 一个整数表示要偏移几个月，负数是向过去偏移， 0 是不偏移
        // @return 偏移后的日期对象，如果你传入的是日期对象会是同一份实例
        offsetMonthly: function(d, off) {
            var d2 = this.parseDate(d);
            d2.setMonth(d2.getMonth() + off);
            return d2;
        },
        //.............................................
        // d - 可被 parseDate 接受的日期(MS,str,Date)
        // off - 一个整数表示要偏移几周，负数是向过去偏移， 0 是不偏移
        // @return 偏移后的日期对象，如果你传入的是日期对象会是同一份实例
        offsetWeekly: function(d, off) {
            var d2 = this.parseDate(d);
            var ms = d2.getTime();
            d2.setTime(ms + 86400000*7*off);
            return d2;
        },
        getWeekOfYear: function(d){
            var Jan1 = new Date(d.getFullYear(),0, 1);
            var ms = d.getTime() - Jan1.getTime();
            var days = Math.round(ms / 86400000);
            return Math.ceil(days/7);
        },
        //.............................................
        // 解析日期字符串为一个日期对象
        /*
         - str : 日期字符串，当然你也可以传一个绝对毫秒数或者另外一个日期对象
         - regex :  如果参数是字符串，会用这个正则式来解析，
         匹配的组 1,2,3,4,5,6 分别年月日，时分秒
         你可以匹配到 3，也可以匹配到 5 还可以说匹配到 6
         @return 标准的 Date 对象
         !!! 诡异啊
         采用 Javascript 的 Date 对象， 对 1980-04-30 取值:
         JS: new Date("1980-04-30").toString() 结果是
         Wed Apr 30 1980 00:00:00 GMT+0800 (CST)
         对应毫秒数为 : 325872000000L

         我把毫秒数弄到 Java 里 -> Date d2 = new Date(325872000000L);
         结果竟然是  "Wed Apr 30 00:30:00 CST 1980"
         从 1980-05-01 开始，两边就完全一致了。之前都差这 30 分钟
         这是为啥，为啥，为啥？！！ 太坑了吧，靠，困了，不研究了，以后再说吧 -_-!
         */
        parseDate: function (str, regex) {
            if (!str) {
                return str;
            }
            // 日期对象
            if (_.isDate(str)) {
                return str;
            }
            // 数字则表示绝对毫秒数
            if (_.isNumber(str)) {
                d = new Date();
                d.setTime(str);
                return d;
            }
            // 否则当做字符串
            var REG;
            // 自动根据长度判断应该选取的表达式
            if (!regex) {
                REG = /^(\d{4})[-/](\d{1,2})([-/](\d{1,2}))?([T ](\d{1,2})(:(\d{1,2}))?(:(\d{1,2}))?)?$/;
            }
            // 构建个新正则
            else {
                REG = new RegExp(regex);
            }

            // 执行匹配
            str = $.trim(str);
            var re = REG.exec(str);

            // 分析结果，将会成为一个数组比如 [2015,9,24,12,23,11]
            // 数组元素至少要到 3 个才有效
            var m = null;
            if (re) {
                m = [];
                for (var i = 0; i < re.length; i++) {
                    var s = re[i];
                    if (/^\d{1,4}$/.test(s)) {
                        m.push(parseInt(s));
                    }
                }
            }

            // 格式正确
            if (m && m.length >= 2) {
                var d;
                // 仅仅是月
                if (m.length == 2) {
                    d = new Date(m[0], m[1] - 1, 1);
                }
                // 仅仅是日期
                else if (m.length == 3) {
                    d = new Date(m[0], m[1] - 1, m[2]);
                }
                // 精确到分
                else if (m.length == 5) {
                    d = new Date(m[0], m[1] - 1, m[2], m[3], m[4]);
                }
                // 精确到秒
                else if (m.length > 5) {
                    d = new Date(m[0], m[1] - 1, m[2], m[3], m[4], m[5]);
                }
                return d;
            }
            throw "invalid date '" + str + "' can not match : " + REG;
        },
        //.............................................
        // 解析日期字符串为一个日期对象
        /*
         - str : 日期字符串，当然你也可以传一个当天绝对秒数或者一个日期对象
         - regex :  如果参数是字符串，会用这个正则式来解析，
         匹配的组 1,2,3,4,5,6 分别年月日，时分秒
         你可以匹配到 3，也可以匹配到 5 还可以说匹配到 6
         @return {
         HH : 23,
         mm : 09,
         ss : 45
         }
         */
        parseTime: function (str, regex) {
            // 本身就是时间对象
            if (str.key_min && str.key && str.HH && str.mm && str.ss) {
                return _.extend({}, str);
            }

            // 会解析成这个时间对象
            var _t = {};
            // 日期对象
            if (_.isDate(str)) {
                _t.H = str.getHours();
                _t.m = str.getMinutes();
                _t.s = str.getSeconds();
            }
            // 数字则表示绝对秒数
            else if (_.isNumber(str)) {
                var n = parseInt(str);
                _t.H = parseInt(n / 3600);
                n -= _t.H * 3600;
                _t.m = parseInt(n / 60);
                _t.s = n - _t.m * 60;
            }
            // 否则当做字符串
            else {
                var REG;
                // 自动根据长度判断应该选取的表达式
                if (!regex) {
                    REG = /^(\d{1,2})(:(\d{1,2}))?(:(\d{1,2}))?$/;
                }
                // 构建个新正则
                else {
                    REG = new RegExp(regex);
                }

                // 执行匹配
                var re = REG.exec(str);

                // 分析结果，将会成为一个数组比如 [2015,9,24,12,23,11]
                // 数组元素至少要到 3 个才有效
                var m = null;
                if (re) {
                    m = [];
                    for (var i = 0; i < re.length; i++) {
                        var s = re[i];
                        if (/^\d{1,2}$/.test(s)) {
                            m.push(parseInt(s));
                        }
                    }
                }

                // 格式正确
                if (m) {
                    var d;
                    // 仅仅是到分
                    if (m.length == 2) {
                        _t.H = m[0];
                        _t.m = m[1];
                        _t.s = 0;
                    }
                    // 精确到秒
                    else if (m.length > 2) {
                        _t.H = m[0];
                        _t.m = m[1];
                        _t.s = m[2];
                    }
                }
                // 未通过校验，抛错
                else {
                    throw "invalid time '" + str + "' can not match : " + regex;
                }
            }
            _t.sec = _t.H * 3600 + _t.m * 60 + _t.s;
            _t.HH = (_t.H > 9 ? "" : "0") + _t.H;
            _t.mm = (_t.m > 9 ? "" : "0") + _t.m;
            _t.ss = (_t.s > 9 ? "" : "0") + _t.s;
            _t.key_min = _t.HH + ":" + _t.mm;
            _t.key = _t.key_min + ":" + _t.ss;

            // 自动显示
            _t.T = _t.H + (_t.m == 0 ? "" : ":" + _t.m);
            _t.TT = _t.HH + (_t.m == 0 ? "" : ":" + _t.mm);

            // 12小时制支持
            _t.H12 = _t.H > 12 ? _t.H % 12 : _t.H;
            _t.HH12 = (_t.H12 > 9 ? "" : "0") + _t.H12;
            _t.XM = (_t.H == _t.H12 ? "A" : "P");
            _t.xm = (_t.H == _t.H12 ? "a" : "p");
            _t.PM = (_t.H == _t.H12 ? "" : "P");
            _t.pm = (_t.H == _t.H12 ? "" : "p");

            // 12小时制自动显示
            _t.T12 = _t.H12 + (_t.m == 0 ? "" : ":" + _t.m);
            _t.TT12 = _t.HH12 + (_t.m == 0 ? "" : ":" + _t.mm);

            // 返回
            return _t;
        },
        //.............................................
        // 解析时间字符串（替代 parseTime)
        parseTimeInfo: function (input, dft) {
            if(_.isNull(input) || _.isUndefined(input))
                return null;
            // 接受日期对象
            if(_.isDate(input)) {
                var str = input.format('HH:MM:ss');
                input = str;
            }
            // 准备对齐方法
            var _pad = function (v, width) {
                width = width || 2;
                if (3 == width) {
                    return v > 99 ? v : (v > 9 ? "0" + v : "00" + v);
                }
                return v > 9 ? v : "0" + v;
            };
            input = (typeof input) == "number" ? input : input || dft;
            var inType = (typeof input);
            var ms = 0;
            var ti = {};
            // 字符串
            if ("string" == inType) {
                var m = /^([0-9]{1,2}):([0-9]{1,2})(:([0-9]{1,2})([.,]([0-9]{1,3}))?)?$/
                    .exec(input);
                if (!m)
                    throw "Not a Time: '" + input + "'!!";
                // 仅仅到分钟
                if (!m[3]) {
                    ti.hour = parseInt(m[1]);
                    ti.minute = parseInt(m[2]);
                    ti.second = 0;
                    ti.millisecond = 0;
                }
                // 到秒
                else if (!m[5]) {
                    ti.hour = parseInt(m[1]);
                    ti.minute = parseInt(m[2]);
                    ti.second = parseInt(m[4]);
                    ti.millisecond = 0;
                }
                // 到毫秒
                else {
                    ti.hour = parseInt(m[1]);
                    ti.minute = parseInt(m[2]);
                    ti.second = parseInt(m[4]);
                    ti.millisecond = parseInt(m[6]);
                }
            }
            // 数字
            else if ("number" == inType) {
                var sec;
                if ("ms" == dft) {
                    sec = parseInt(input / 1000);
                    ms = Math.round(input - sec * 1000);
                } else {
                    sec = parseInt(input);
                    ms = Math.round(input * 1000 - sec * 1000);
                }
                ti.hour = Math.min(23, parseInt(sec / 3600));
                ti.minute = Math.min(59, parseInt((sec - ti.hour * 3600) / 60));
                ti.second = Math.min(59, sec - ti.hour * 3600 - ti.minute * 60);
                ti.millisecond = ms;
            }
            // 其他
            else {
                throw "Not a Time: " + input;
            }
            // 计算其他的值
            ti.value = ti.hour * 3600 + ti.minute * 60 + ti.second;
            ti.valueInMillisecond = ti.value * 1000 + ti.millisecond;
            // 增加一个函数
            ti.toString = function (fmt) {
                // 默认的格式化方式
                if (!fmt) {
                    fmt = "HH:mm";
                    // 到毫秒
                    if (0 != this.millisecond) {
                        fmt += ":ss.SSS";
                    }
                    // 到秒
                    else if (0 != this.second) {
                        fmt += ":ss";
                    }
                }
                // 自动格式化
                else if ("min" == fmt) {
                    // 精确到分
                    if (this.hour <= 0) {
                        fmt = "mm:ss";
                    }
                    // 否则精确到小时
                    else {
                        fmt = "HH:mm:ss";
                    }
                }

                // 进行格式化
                var sb = "";
                var reg = /a|[HhKkms]{1,2}|S(SS)?/g;
                var pos = 0;
                var m;
                while (m = reg.exec(fmt)) {
                    //console.log(reg.lastIndex, m.index, m.input)
                    var l = m.index;
                    // 记录之前
                    if (l > pos) {
                        sb += fmt.substring(pos, l);
                    }
                    // 偏移
                    pos = reg.lastIndex;

                    // 替换
                    var s = m[0];
                    if ("a" == s) {
                        sb += this.value > 43200 ? "PM" : "AM";
                    }
                    // H Hour in day (0-23)
                    else if ("H" == s) {
                        sb += this.hour;
                    }
                    // k Hour in day (1-24)
                    else if ("k" == s) {
                        sb += (this.hour + 1);
                    }
                    // K Hour in am/pm (0-11)
                    else if ("K" == s) {
                        sb += (this.hour % 12);
                    }
                    // h Hour in am/pm (1-12)
                    else if ("h" == s) {
                        sb += ((this.hour % 12) + 1);
                    }
                    // m Minute in hour
                    else if ("m" == s) {
                        sb += this.minute;
                    }
                    // s Second in minute
                    else if ("s" == s) {
                        sb += this.second;
                    }
                    // S Millisecond Number
                    else if ("S" == s) {
                        sb += this.millisecond;
                    }
                    // HH 补零的小时(0-23)
                    else if ("HH" == s) {
                        sb += _pad(this.hour);
                    }
                    // kk 补零的小时(1-24)
                    else if ("kk" == s) {
                        sb += _pad(this.hour + 1);
                    }
                    // KK 补零的半天小时(0-11)
                    else if ("KK" == s) {
                        sb += _pad(this.hour % 12);
                    }
                    // hh 补零的半天小时(1-12)
                    else if ("hh" == s) {
                        sb += _pad((this.hour % 12) + 1);
                    }
                    // mm 补零的分钟
                    else if ("mm" == s) {
                        sb += _pad(this.minute);
                    }
                    // ss 补零的秒
                    else if ("ss" == s) {
                        sb += _pad(this.second);
                    }
                    // SSS 补零的毫秒
                    else if ("SSS" == s) {
                        sb += _pad(this.millisecond, 3);
                    }
                    // 不认识
                    else {
                        sb.append(s);
                    }
                }
                // 结尾
                if (pos < fmt.length) {
                    sb.append(fmt.substring(pos));
                }

                // 返回
                return sb.toString();
            };
            ti.valueOf = ti.toString;
            // 嗯，返回吧
            return ti;
        },
        //.............................................
        // 得到一个时间对象的显示字符串
        // mode : 显示格式，是一个以 _t 为上下文的字符串模板 (@see $z.tmpl)
        //        也可是一个下面的快捷字符串，表示的模板
        // 
        // 可用的快捷模式包括:
        //  24H  - 24 小时制自动（默认）
        //  24HH - 24 小时制自动且强制补 0 
        //  12H  - 12 小时制自动
        //  12HH - 12 小时制自动且强制补 0 
        //  24Hm - 24小时制，精确到分钟
        //  24Hs - 24小时制，精确到秒
        //  12Hm - 12小时制，精确到分钟
        //  12Hs - 12小时制，精确到秒
        timeText: function (t, mode) {
            var str = mode || "24H";
            switch (mode) {
                case "24H" :
                    str = "{{T}}";
                    break;
                case "24HH" :
                    str = "{{TT}}";
                    break;
                case "12H" :
                    str = "{{T12}}{{pm}}";
                    break;
                case "12HH" :
                    str = "{{TT12}}{{pm}}";
                    break;
                case "24Hm" :
                    str = "{{key_min}}";
                    break;
                case "24Hs" :
                    str = "{{key}}";
                    break;
                case "12Hm" :
                    str = "{{HH12}}:{{mm}} {{XM}}M";
                    break;
                case "12Hs" :
                    str = "{{HH12}}:{{mm}}:{{ss}} {{XM}}M";
                    break;
            }
            return $z.tmpl(str)(t);
        },
        /*
        返回数组:
            len==4 as Region:        [开/闭, 左值, 右值, 开/闭]
            len==3 as Single value:  [开/闭, 值, 开/闭]
            - true  : 开
            - false : 闭
        */
        region: function (str, formatFunc) {
            // 处理格式化值的方式
            if (formatFunc) {
                var fft = (typeof formatFunc);
                if ("string" == fft) {
                    // 日期
                    if ("date" == formatFunc) {
                        formatFunc = function (v) {
                            return AsDate(v);
                        };
                    }
                    // 时间
                    else if ("time" == formatFunc) {
                        formatFunc = function (v) {
                            return AsTimeInSec(v);
                        };
                    }
                    // 数字
                    else if ("number" == formatFunc) {
                        formatFunc = function (v) {
                            return parseInt(v);
                        };
                    }
                    // 不支持
                    else {
                        throw "Region formatFunc can not be a '" + fftp + "'";
                    }
                }
                // 那么必须是函数了
                else if ("function" != fft) {
                    throw "Region(.., formatFunc) can not be a " + fft;
                }
            }

            // 整理字符串
            var s = str.replace(/[ \t]/g, "");
            // eval:  |   1  ||  2  || 3 || 4  ||  5   |
            var m = /^([\[\(])([^,]*)(,)?([^,]*)([\)\]])$/.exec(str);
            if (!m) {
                throw "invalid region: " + str;
            }
            // 范围
            var re;
            if (m[3]) {
                re = [
                    m[1] == '(' ? true : false,  // [0]
                    m[2] || null,                // [1]
                    m[4] || null,                // [2] 
                    m[5] == ')' ? true : false,  // [3]
                ];
                if (formatFunc) {
                    if (re[1] != null) re[1] = formatFunc(re[1]);
                    if (re[2] != null) re[2] = formatFunc(re[2]);
                }
            }
            // 单值
            else {
                re = [
                    m[1] == '(' ? true : false,  // [0]
                    m[2] || null,                // [1]
                    m[5] == ')' ? true : false,  // [2]
                ];
                if (formatFunc) {
                    if (re[1] != null) re[1] = formatFunc(re[1]);
                }
            }
            // 添加帮助函数
            re.left = function () {
                return this[1];
            };
            re.right = function () {
                return this[this.length - 2];
            };
            re.leftAsStr = function (fmt) {
                var v = this[1];
                return v ? formatDate(fmt || "yyyy-MM-dd", v) : "";
            };
            re.rightAsStr = function (fmt) {
                var v = this[this.length - 2];
                return v ? formatDate(fmt || "yyyy-MM-dd", v) : "";
            };
            re.isLeftOpen = function () {
                return this[0];
            };
            re.isRightOpen = function () {
                return this[this.length - 1];
            };
            re.isRegion = function () {
                return this.length == 4;
            };
            re.match = function (v) {
                // 区间
                if (this.length == 4) {
                    if (null != this[1]) {
                        if ((this[0] && this[1] >= v) || (!this[0] && this[1] > v))
                            return false;
                    }
                    if (null != this[2]) {
                        if ((this[3] && this[2] <= v) || (!this[3] && this[2] < v))
                            return false;
                    }
                    return true;
                }
                // 不等于
                if (this[0] && this[2])
                    return this[1] != v;
                // 等于
                return this[1] == v;
            };
            re.valueOf = function () {
                var s = this.isLeftOpen() ? "(" : "[";
                if (this.isRegion()) {
                    s += this.leftAsStr() || "";
                    s += ",";
                    s += this.rightAsStr() || "";
                } else {
                    s += this.leftAsStr();
                }
                s += this.isRightOpen() ? ")" : "]";
                return s;
            };
            re.toString = re.valueOf;
            // 返回
            return re;
        },
        //.............................................
        // 根据颜色对象的 red,green,blue,alpha ，更新其他字段的值
        updateColor: function (color) {
            if (!_.isNumber(color.alpha))
                color.alpha = 1;
            color.AA = parseInt(color.alpha * 255).toString(16).toUpperCase();
            color.RR = color.red.toString(16).toUpperCase();
            color.GG = color.green.toString(16).toUpperCase();
            color.BB = color.blue.toString(16).toUpperCase();
            color.AA = color.AA.length == 1 ? '0' + color.AA : color.AA;
            color.RR = color.RR.length == 1 ? '0' + color.RR : color.RR;
            color.GG = color.GG.length == 1 ? '0' + color.GG : color.GG;
            color.BB = color.BB.length == 1 ? '0' + color.BB : color.BB;
            color.HEX = "#" + color.RR + color.GG + color.BB;
            color.RGB = "rgb(" + color.red + "," + color.green + "," + color.blue + ")";
            color.RGBA = "rgba(" + color.red + "," + color.green + "," + color.blue + "," + color.alpha + ")";
            color.AARRGGBB = "0x" + color.AA + color.RR + color.GG + color.BB;
            return color;
        },
        /*.............................................
         将任何颜色字符串，解析成标准颜色对象
         {
         red   : 255,
         green : 255,
         blue  : 255,
         AA    : "FF",
         RR    : "FF",
         GG    : "FF",
         BB    : "FF",
         alpha : 1.0
         HEX   : "#FFFFFF",
         RGB   : rgb(255,255,255),
         RGBA  : rgb(255,255,255, 1)
         AARRGGBB : "0xFF00AA99"
         }
         */
        parseColor: function (str, alpha) {
            // 初始颜色是黑色
            var color = {
                red: 0,
                green: 0,
                blue: 0,
                alpha: _.isNumber(alpha) ? alpha : 1.0
            };
            // 空的话用黑色
            if (!str)
                return this.updateColor(color);

            // 本来就是一个颜色对象，创建个新的
            if (_.isNumber(str.red) && _.isNumber(str.green) && _.isNumber(str.blue) && _.isNumber(str.alpha)) {
                return this.updateColor(_.extend({}, str));
            }

            // 初始化处理字符串
            str = str.replace(/[ \t\r\n]+/g, "").toUpperCase();

            // 解析吧
            if("UNSET" == str) {
                return null;
            }
            // RGB: #FFF
            else if (m = /^#?([0-9A-F])([0-9A-F])([0-9A-F]);?$/.exec(str)) {
                color.red = parseInt(m[1] + m[1], 16);
                color.green = parseInt(m[2] + m[2], 16);
                color.blue = parseInt(m[3] + m[3], 16);
            }
            // RRGGBB: #F0F0F0
            else if (m = /^#?([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2});?$/.exec(str)) {
                color.red = parseInt(m[1], 16);
                color.green = parseInt(m[2], 16);
                color.blue = parseInt(m[3], 16);
            }
            // RGB值: rgb(255,33,89)
            else if (m = /^RGB\((\d+),(\d+),(\d+)\)$/.exec(str)) {
                color.red = parseInt(m[1], 10);
                color.green = parseInt(m[2], 10);
                color.blue = parseInt(m[3], 10);
            }
            // RGBA值: rgba(6,6,6,0.9)
            else if (m = /^RGBA\((\d+),(\d+),(\d+),([\d.]+)\)$/.exec(str)) {
                color.red = parseInt(m[1], 10);
                color.green = parseInt(m[2], 10);
                color.blue = parseInt(m[3], 10);
                color.alpha = m[4] * 1;
            }
            // AARRGGBB : 0xFF000000
            else if (m = /^0[xX]([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2});?$/.exec(str)) {
                color.alpha = parseInt(m[1], 16) / 255;
                color.red = parseInt(m[2], 16);
                color.green = parseInt(m[3], 16);
                color.blue = parseInt(m[4], 16);
            }
            // 不支持的颜色值格式
            else {
                throw "unknown color format: " + str;
            }

            // 最后返回颜色
            return this.updateColor(color);
        },
        //.............................................
        // 设置一个 input 的值，如果值与 placeholder 相同，则清除值
        setInputVal: function (jInput, val) {
            var dft = jInput.attr("placeholder");
            if (dft == val)
                jInput.val("");
            else
                jInput.val(val);
        },
        //.............................................
        // 获得页面的锚值，即在 href 后面的
        pageAnchor: function () {
            var href = window.location.href;
            var pos = href.lastIndexOf("#");
            if (pos > 0)
                return href.substring(pos + 1);
            return null;
        },
        //.............................................
        /* 解析一个链接，返回对象:
        - href : 就是一个超链接
        - decode : 是否要对参数值解码，默认false
        @return 可能两种形式
            a. 超链接:
                {
                    primer : "/xxx?xx#xx",
                    value  : "/xxx?xx#xx",
                    href   : "/xxx",
                    params : {..},
                    anchor : "xxx",
                }
            b. 函数调用
                {
                    primer : "javascript:$z.xxx(true,200)",
                    value  : "$z.xxx(true,200)",
                    invoke : "$z.xxx",
                    args   : [..],
                }
        */
        parseHref : function(href, decode) {
            href = $.trim(href);
            if(!href)
                return null;
            // 首先按照 javascript 来理解
            var m = /^javascript:([^(]+)\(([^)]*)\);?$/.exec(href);
            if(m) {
                // 如果是 xx(this, true, 200) 形式的函数，this 就不要显示了
                var args = m[2];
                var m2 = /^([ \t]*this[ \t]*,?[ \t]*)(.*)$/.exec(m[2]);
                if(m2) {
                    args = m2[2];
                }
                return {
                    primer : href,
                    value  : href.substring('javascript:'.length),
                    invoke : m[1],
                    args   : $z.fromJson('[' + m[2] + ']')
                };
            }
            // 尝试按超链接理解
            m = /^([^#?]+)(\?([^#]*)*)?(#(.*))?$/.exec(href);
            // 有锚点或者链接
            if(m) {
                // 解析一下 QueryString
                var qs = m[3];
                var params = {};
                if(qs) {
                    var pp = qs.split(/&/);
                    for(var i=0; i<pp.length; i++) {
                        var mp = /^([^=]+)(=(.+)?)?$/.exec(pp[i]);
                        if(mp) {
                            var pv = mp[3] || "";
                            if(decode)
                                pv = decodeURIComponent(pv);
                            params[mp[1]] = pv;
                        }
                    }
                }
                // 得到数据
                return {
                    primer : href,
                    value  : decodeURI(href),
                    href   : decodeURI(m[1]),
                    params : params,
                    anchor : m[5] ? decodeURIComponent(m[5]) : null,
                };
            }
            // 只有锚点咯
            if(/^#/.test(href)) {
                return {
                    primer : href,
                    value  : decodeURI(href),
                    anchor : href.substring(1)
                };
            }
            // 只有链接咯
            return {
                primer : href,
                value  : decodeURI(href),
                href   : decodeURI(href)
            };
        },
        //.............................................
        // 将 parseHref 函数出来的结果，渲染成一个超链接
        // - ho     : 是 parseHref 函数出来的结果
        // - encode : 是否将返回的 URI 编码
        // @return 一个 URI 字符串
        renderHref : function(ho, encode) {
            // 返回拼合的字符串
            var re = [];
            if(ho.href)
                re.push(ho.href);

            // 参数
            if(ho.params && !_.isEmpty(ho.params)) {
                var plist = [];
                for(var key in  ho.params) {
                    var val = ho.params[key];
                    if(val)
                        plist.push(key + "=" + val);
                }
                if(plist.length > 0) {
                    re.push("?");
                    re.push(plist.join("&"));
                }
            }

            // 锚点
            if(ho.anchor){
                if(/^#/.test(ho.anchor))
                    re.push(ho.anchor);
                else    
                    re.push("#" + ho.anchor);
            }

            return encode ? encodeURI(re.join(""))
                          : re.join("");
        },
        //.............................................
        // 得到函数体的代码
        // 因为这种方法通常用来获得内嵌 HTML 文本，
        // 参数 removeComment 如果声明了，将会移除文本前后的 '/*' 和 '*/'
        getFuncBodyAsStr: function (func, removeComment) {
            var str = func.toString();
            var posL = str.indexOf("{");
            var re = $.trim(str.substring(posL + 1, str.length - 1));
            // Safari 会自己加一个语句结尾，靠
            if (re[re.length - 1] == ";")
                re = re.substring(0, re.length - 1);

            // 移除前后的注释
            if (removeComment)
                re = re.substring(2, re.length - 2);

            // 嗯，搞好了
            return re;
        },
        //.............................................
        /*
        对一段行分隔的文本解析，形成一组数据
         - str:  输入的文本，一行表示一个对象
         - sep:  字符串分隔符，也可以是一个正则表达式
         - keys: 无论是字符串分隔符，还是正则，最后都会解析成一个数组，
                 这里说明每个下标表示的键
                 如果不指定，则直接返回数组
         - format: 一个函数 F(obj)，最后对解析出来的值进行格式化
        @return 对象数组
        */
        parseLine : function(str, sep, keys, format) {
            str = $.trim(str);
            // 空字符串
            if(!str)
                return [];

            // 准备行解析函数
            var _do_line;
            // 字符串分隔
            if(_.isString(sep)) {
                _do_line = function(line, sep, keys) {
                    var m = line.split(sep);
                    if(m) {
                        var obj = {};
                        var len = Math.min(m.length, keys.length);
                        for(var i=0; i<len; i++) {
                            var key = keys[i];
                            var val = m[i];
                            obj[key] = $.trim(val);
                        }
                        return obj;
                    }
                }
            }
            // 正则表达式分隔
            else if(_.isRegExp(sep)) {
                _do_line = function(line, sep, keys) {
                    var m = sep.exec(line);
                    if(m) {
                        var obj = {};
                        var len = Math.min(m.length-1, keys.length);
                        for(var i=0; i<len; i++) {
                            var key = keys[i];
                            var val = m[i+1];
                            obj[key] = $.trim(val);
                        }
                        return obj;
                    }
                }
            }
            // 简直无法分隔
            else {
                _do_line = function(line, sep, keys) {
                    return _.isArray(keys) && keys.length>0
                            ? zUtil.obj(keys[0], line)
                            : line;
                };
            }

            // 准备返回值
            var list = [];
            // 逐行解析
            var lines = (str || "").split(/\r?\n/);
            for(var i=0; i<lines.length; i++) {
                var line = $.trim(lines[i]);
                if(line) {
                    var obj = _do_line(line, sep, keys);
                    if(obj) {
                        obj = $z.doCallback(format, [obj]) || obj;
                        list.push(obj);
                    }
                }
            }

            // 返回
            return list;
        },
        //.............................................
        // 循环将参数拼合成一个数组
        concat: function () {
            if (arguments.length > 0) {
                var i = 0;
                var re;
                for (; i < arguments.length; i++) {
                    re = arguments[i];
                    if (re) {
                        break;
                    }
                }
                if (!re)
                    return [];
                if (!_.isArray(re))
                    re = [re];
                for (i++; i < arguments.length; i++) {
                    var arg = arguments[i];
                    if (arg)
                        re = re.concat(arg);
                }
                return re;
            }
        },
        //.............................................
        // 如果一个对象某字段是 undefined，那么为其赋值
        //  setUndefined(obj, key, dft0, dft1 ...);
        setUndefined: function (obj, key) {
            // 后面的的默认值挨个试
            if (_.isUndefined(obj[key]))
                for (var i = 2; i < arguments.length; i++) {
                    var val = arguments[i];
                    if (_.isUndefined(val)) {
                        continue;
                    }
                    obj[key] = val;
                    break;
                }
        },
        //.............................................
        // 返回参数中第一个不是 undefined 的值
        fallbackUndefined: function () {
            for (var i = 0; i < arguments.length; i++) {
                var val = arguments[i];
                if (!_.isUndefined(val))
                    return val;
            }
        },
        //.............................................
        // pass - 「数组」指定的数组的值如果匹配，则向后取值
        // args - 「数组」备选值
        // dft  - 备选都不行，返回这个默认值
        // 返回第一个不能被 pass 的值，如果都 pass 了，返回默认值
        fallback: function (pass, args, dft) {
            for (var i = 0; i < args.length; i++) {
                var val = args[i];
                // 看看是否 pass
                var isPass = false;
                for (var x = 0; x < pass.length; x++) {
                    if (pass[x] === val) {
                        isPass = true;
                        break;
                    }
                }
                // 是否继续通过
                if (!isPass)
                    return val;
            }
            // 返回默认值
            return dft;
        },
        //.............................................
        jq: function (jP, arg, selector) {
            // 没有参数，那么全部 children 都会被选中
            if (_.isUndefined(arg)) {
                return jP.children();
            }
            // DOM 元素
            if (arg instanceof jQuery || _.isElement(arg)) {
                return $(arg);
            }
            // 数字
            if (_.isNumber(arg)) {
                if (selector)
                    return jP.find(selector).eq(arg);
                return jP.children().eq(arg);
            }
            // selector
            if (_.isString(arg)) {
                return jP.find(arg);
            }
            // 查找匹配对象
            if (_.isObject(arg)) {
                return jP.find(selector).filter(function () {
                    var o = $(this).data("OBJ");
                    return _.isMatch(o, arg);
                });
            }
            // 靠，神马玩意
            throw "Fuck! unknown arg : " + arg;
        },
        //............................................
        // 递归迭代给定元素下面所有的文本节点
        // jq 给定元素的 jQuery 对象
        // callback 回调 F(TextNode)
        eachTextNode: function (jq, callback) {
            jq = $(jq);
            for (var i = 0; i < jq.size(); i++) {
                var ele = jq[i];
                var ndList = ele.childNodes;
                for (var x = 0; x < ndList.length; x++) {
                    var nd = ndList[x];
                    // 文本节点
                    if (3 == nd.nodeType) {
                        callback.call(nd);
                    }
                    // 元素的话，递归
                    else if (1 == nd.nodeType) {
                        this.eachTextNode(nd, callback);
                    }
                }
            }
        },
        //............................................
        // 对一个字符串进行转换，相当于 $(..).text(str) 的效果
        __escape_ele: $(document.createElement("b")),
        escapeText: function (str, trim) {
            return _.isString(str) ? str.replace("<", "&lt;") : str;
            //re = this.__escape_ele.text(str).text();
        },
        // 对 HTML 代码逃逸
        escapeHtml: function (str, trim) {
            // var re = _.isString(str) ? str.replace("<", "&lt;") : str;
            //re = this.__escape_ele.text(str).text();
            if(!str || !_.isString(str))
                return str;

            var re = "";
            var REG = /[<&>]/g;
            var pos = 0;
            var m = REG.exec(str);
            while(m) {
                var ms = m[0];
                var bg = m.index;
                var ed = m.index + ms.length;
                // 补充前面的
                if(pos < bg) {
                    re += str.substring(pos, bg);
                }
                // `<`
                if('<' == ms) {
                    re += '&lt;';
                }
                // `&`
                else if('&' == ms) {
                    re += '&amp;';
                }
                // `>`
                else if('>' == ms) {
                    re += '&gt;';
                }

                // 继续执行
                pos = ed;
                m = REG.exec(str);
            }
            // 补足最后一个
            if(pos < str.length) {
                re += str.substring(pos);
            }

            // 返回吧
            return trim ? $.trim(re) : re;
        },
        // 对 HTML 代码逃逸的结果，反逃逸
        unescapeHtml: function (str, trim) {
            // var re = _.isString(str) ? str.replace("<", "&lt;") : str;
            //re = this.__escape_ele.text(str).text();
            if(!str || !_.isString(str))
                return str;

            var re = "";
            var REG = /&lt;|&amp;|&gt;/g;
            var pos = 0;
            var m = REG.exec(str);
            while(m) {
                var ms = m[0];
                var bg = m.index;
                var ed = m.index + ms.length;
                // 补充前面的
                if(pos < bg) {
                    re += str.substring(pos, bg);
                }
                // `<`
                if('&lt;' == ms) {
                    re += '<';
                }
                // `&`
                else if('&amp;' == ms) {
                    re += '&';
                }
                // `>`
                else if('&gt;' == ms) {
                    re += '>';
                }

                // 继续执行
                pos = ed;
                m = REG.exec(str);
            }
            // 补足最后一个
            if(pos < str.length) {
                re += str.substring(pos);
            }

            // 返回吧
            return trim ? $.trim(re) : re;
        },
        //.............................................
        // SVG 相关
        svg: {
            createRoot: function (styles) {
                var jRoot = $('<svg version="1.1" xmlns="http://www.w3.org/2000/svg">');
                if (styles) {
                    jRoot.css(styles);
                }
                return jRoot;
            },
            create: function (type, attrs, styles) {
                var it = document.createElementNS("http://www.w3.org/2000/svg", type);
                var jIt = $(it);
                if (attrs) {
                    jIt.attr(attrs);
                }
                if (styles) {
                    jIt.css(styles);
                }
                return jIt;
            },
        },
        //.............................................
        // 调用某对象的方法，如果方法不存在或者不是函数，无视
        // 如果对象的键是一个数组，会依次调用，但是返回的是最后一个
        // 函数的返回
        invoke: function (obj, funcName, args, me) {
            if (obj) {
                var func = obj[funcName];
                if (_.isFunction(func)) {
                    return func.apply(me || obj, args || []);
                }
                // 如果是数组，那么遍历一下
                else if(_.isArray(func)) {
                    var re;
                    for(var i=0; i<func.length; i++) {
                        var f2 = func[i];
                        if(_.isFunction(f2)){
                            re = f2.apply(me || obj, args || []);
                        }
                    }
                    return re;
                }
            }
        },
        //.............................................
        // 声明模块，回调必须返回这个模块本身
        // deps 是依赖的模块数组
        // 函数会自动根据 CMD / AMD 等约定自行选择怎么定义模块
        declare: function (deps, callback) {
            if (typeof define === "function" && define.cmd) {
                if (_.isString(deps)) {
                    deps = [deps];
                }
                define(deps, function (require) {
                    var args = [];
                    require.async(deps, function () {
                        for (var i = 0; i < arguments.length; i++) {
                            args.push(arguments[i]);
                        }
                    });
                    return callback.apply(this, args);
                });
            }
            else {
                throw "Fuck!!!!"
            }
        },
        //.............................................
        // 打开一个新的窗口
        openUrl: function (url, target, method, params) {
            // 如果默认是 GET 方法
            if(!params && method && !_.isString(method)) {
                params = method;
                method = "GET";
            }
            // 创建一个模拟表单 
            var html = '<form target="' + (target || '_blank') + '" method="' + (method || "GET") + '"';
            html += ' action="' + url + '" style="display:none;">';
            html += '</form>';
            var jq = $(html).appendTo(document.body);
            if (params)
                for (var key in params) {
                    $('<input type="hidden">').appendTo(jq)
                        .prop("name", key)
                        .val(params[key]);
                }
            jq[0].submit();
            jq.remove();
        },
        // 模拟POST提交
        postForm: function (url, data) {
            var html = '';
            html += '<form action="' + url + '" method="POST" style="display:none;">';
            for (var nm in data) {
                html += '<input type="text" name="' + nm + '" value="' + data[nm] + '">';
            }
            html += '</form>';
            var jq = $(html).appendTo(document.body);
            jq[0].submit();
            jq.remove();
        },
        //.............................................
        // 返回一个时间戳，其它应用可以用来阻止浏览器缓存
        timestamp: function () {
            return Date.now();
        },
        //.............................................
        // 生成一个随机字符串
        randomString: function (length) {
            var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz'.split('');
            if (!length) {
                length = Math.floor(Math.random() * chars.length);
            }
            var str = '';
            for (var i = 0; i < length; i++) {
                str += chars[Math.floor(Math.random() * chars.length)];
            }
            return str;
        },
        // 返回一个介于min，max的数字 包含2头
        randomInt: function (min, max) {
            return Math.floor(Math.random() * (max - min + 1) + min);
        },
        // 返回列表中随机一个对象
        randomListObj: function (list) {
            if (_.isArray(list)) {
                if (list.length > 0) {
                    return list[zUtil.randomInt(0, list.length - 1)];
                }
            }
        },
        //.............................................
        // 将一个对象的原型，链接到指定的父对象上
        // 这个函数很暴力的，直接修改对象的 __proto__，对于只读的对象
        // 用这个函数应该别 extend 要快点
        // 函数返回 obj 本身
        inherit: function (obj, parent) {
            obj.__proto__ = parent;
            return obj;
        },
        //.............................................
        // 扩展第一个对象，深层的，如果遇到重名的对象，则递归
        // 调用方法 $z.extend(a,b,c..)
        extend: function () {
            var a = arguments[0];
            for (var i = 1; i < arguments.length; i++) {
                var b = arguments[i];
                if (this.isPlainObj(b)) {
                    for (var key in b) {
                        var vA = a[key];
                        var vB = b[key];
                        // 两个都是普通对象，融合
                        if (this.isPlainObj(vA) && this.isPlainObj(vB)) {
                            this.extend(vA, vB);
                        }
                        else if (this.isjQuery(vB) || _.isElement(vB)) {
                            a[key] = vB;
                        }
                        // 否则仅仅是对 B 克隆
                        else {
                            a[key] = this.clone(vB);
                        }
                    }
                }
            }
            return a;
            // 否则不能接受
            //throw "can not extend a:" + a + " by b:" + b;
        },
        //.............................................
        // 为一个对象制作一个深层的副本
        clone: function (obj) {
            // 函数，布尔，数字等可以直接返回的，直接返回
            if (_.isNull(obj)
                || _.isUndefined(obj)
                || _.isNumber(obj)
                || _.isBoolean(obj)
                || _.isString(obj)
                || _.isFunction(obj)) {
                return obj;
            }
            // 数组
            if (_.isArray(obj)) {
                var re = [];
                for (var i = 0; i < obj.length; i++) {
                    re.push(this.clone(obj[i]));
                }
                return re;
            }
            // jQuery 或者 Elemet
            if (this.isjQuery(obj) || _.isElement(obj)) {
                return obj;
            }
            // 日期对象
            if (_.isDate(obj)) {
                return new Date(obj);
            }
            // 正则表达式
            if (_.isRegExp(obj)) {
                return new RegExp(obj);
            }
            // 普通对象
            return this.extend({}, obj);
        },
        isjQuery: function (obj) {
            return obj instanceof jQuery;
        },
        //.............................................
        // 判断一个对象是否是简单的 POJO
        isPlainObj: function (obj) {
            if (_.isUndefined(obj))
                return false;
            if (_.isFunction(obj))
                return false;
            if (_.isNull(obj))
                return false;
            if (_.isDate(obj))
                return false;
            if (_.isRegExp(obj))
                return false;
            if (_.isArray(obj))
                return false;
            if (_.isElement(obj))
                return false;
            if (this.isjQuery(obj))
                return false;
            return _.isObject(obj);
        },
        //.............................................
        isEmptyString: function (str) {
            return "" === str;
        },
        isBlankString: function (str) {
            return "" === $.trim(str);
        },
        isEndsWith: function(str, sub) {
            if(!str || !sub)
                return false;
            if(sub.length> str.length)
                return false;
            return str.substring(str.length-sub.length) == sub;
        },
        isStartsWith: function(str, sub) {
            if(!str || !sub)
                return false;
            if(sub.length> str.length)
                return false;
            return str.substring(0, sub.length) == sub;
        },
        //.............................................
        // 对 HTML 去掉空格等多余内容，并进行多国语言替换
        compactHTML: function (html, msgMap, settings) {
            html = (html || "").replace(/[ ]*\r?\n[ ]*/g, "");
            if (_.isObject(msgMap)) {
                return zUtil.tmpl(html, settings)(msgMap);
            }
            return html;
        },
        //.............................................
        // 将属性设置到控件的 DOM 上
        setJsonToSubScriptEle: function (jq, className, prop, needFormatJson) {
            var jPropEle = jq.children("script." + className);
            if (jPropEle.length == 0) {
                jPropEle = $('<script type="text/x-template" class="' + className + '">').prependTo(jq);
            }

            // 这里有个危险，如果内容中含有 '<script>' 那么这个解析基本就 over 了
            // 所以要对所有的值进行编码，即 '<' 要变 '&lt;' 等
            var RP = function (key, val) {
                if (/^__/.test(key))
                    return undefined;
                return val;
                //return val;
            };


            var json = needFormatJson 
                            ? "\n" + $z.toJson(prop, RP, '    ') + "\n" 
                            : $z.toJson(prop, RP);
            // 逃逸一下 HTML
            json = this.escapeHtml(json);
            // 存储
            jPropEle.text(json);
            //console.log(jPropEle.text())
        },
        // 从控件的 DOM 上获取控件的属性
        getJsonFromSubScriptEle: function (jq, className, dft) {
            var jPropEle = jq.children("script." + className);
            if (jPropEle.length > 0) {
                var json = jPropEle.text();
                // 反逃逸 HTML
                json = this.unescapeHtml(json);
                return $z.fromJson(json);
            }
            // 返回默认或者空
            return dft || {};
        },
        //.............................................
        /**
         * 将两个路径比较，得出相对路径
         * 
         * @param base
         *            基础路径，以 '/' 结束，表示目录
         * @param path
         *            相对文件路径，以 '/' 结束，表示目录
         * @param equalPath
         *            如果两个路径相等，返回什么，通常为 "./"。 
         *            你也可以用 "" 或者 "." 或者随便什么字符串来表示
         * 
         * @return 相对于基础路径对象的相对路径
         */
        getRelativePath: function (base, path, equalPath) {
            if (base == path) {
                return equalPath;
            }

            // 开始判断
            var bc = zUtil.getCanonicalPath(base);
            var bp = zUtil.getCanonicalPath(path);

            var bb = zUtil.splitIgnoreEmpty(base, /[\\\\/]/);
            var ff = zUtil.splitIgnoreEmpty(path, /[\\\\/]/);
            var len = Math.min(bb.length, ff.length);
            var pos = 0;
            for (; pos < len; pos++)
                if (bb[pos] != ff[pos])
                    break;

            // 证明路径是相等的
            if (len == pos && bb.length == ff.length) {
                return equalPath;
            }

            // 开始查找不同
            var dir = 1;
            if (/\/$/.test(base))
                dir = 0;

            //console.log("the dir=", dir)

            var sb = zUtil.dupString("../", bb.length - pos - dir);
            var pss = ff.splice(pos);
            var rph = sb + pss.join("/");
            //console.log(base, path, "=", rph);
            return rph;
        },
        //.............................................
        /**
         * 得到一个路径的父路径
         *
         * @param ph
         *            某路径
         * @return 父路径，且一定以 / 结尾
         */
        //.............................................
        getParentPath: function (ph, regular) {
            var pos = ph.lastIndexOf('/');
            if (pos <= 0)
                return null;
            return ph.substring(0, pos + 1);
        },
        //.............................................
        /**
         * 整理路径。 将会合并路径中的 ".."
         *
         * @param path
         *            路径
         * @return 整理后的路径
         */
        getCanonicalPath: function (path) {
            if (!path)
                return path;
            var pa = this.splitIgnoreEmpty(path, /[\\\\/]/g);
            var paths = [];
            for (var i = 0; i < pa.length; i++) {
                var s = pa[i];
                if (".." == s) {
                    if (paths.length > 0)
                        paths.pop();
                    continue;
                }
                if ("." == s) {
                    // pass
                } else {
                    paths.push(s);
                }
            }
            var reph = paths.join("/");
            if (/^\//.test(path))
                reph = "/" + reph;
            if (/\/$/.test(path))
                reph = reph + "/";
            return reph;
        },
        /**
         * 判断某路径是否在给定基础路径之下
         *
         * @param base
         *            基础路径，以 '/' 结束，表示目录
         * @param path
         *            相对文件路径，以 '/' 结束，表示目录
         * @return 文件路径是否在基础路径之下
         */
        isInPath: function (base, path) {
            if (path.length <= base.length)
                return false;
            // 截取前面一段，必须完全相等
            var len = base.length;
            if (/\/$/.test(base)) {
                len--;
                base = base.substring(0, len);
            }
            if (base != path.substring(0, len))
                return false;

            // 剩下的部分， path 必须以 '/' 开头
            return '/' == path.charAt(len);
        },
        //..............................................
        // 将一堆字符串合并成一个路径
        appendPath : function() {
            var paths = Array.from(arguments);
            if (paths && paths.length > 0) {
                var str = paths.join("/").toString();
                var ss  = str.split(/\/+/);
                return ss.join("/");
            }
            return null;
        },
        //----------------------------------------------------
        /**
         * jq - 要闪烁的对象
         * opt.after - 当移除完成后的操作
         * opt.html - 占位符的 HTML，默认是 DIV.z_blink_light
         * opt.speed - 闪烁的速度，默认为  500
         */
        blinkIt: function (jq, opt) {
            // 格式化参数
            jq = $(jq);

            if (jq.size() == 0)
                return;

            opt = opt || {};
            if (typeof opt == "function") {
                opt = {
                    after: opt
                };
            } else if (typeof opt == "number") {
                opt = {
                    speed: opt
                };
            }
            // 得到文档中的
            var off = jq.offset();
            var owDoc = jq[0].ownerDocument;
            var jDoc = $(owDoc);
            // 样式
            var css = {
                "width": jq.outerWidth(),
                "height": jq.outerHeight(),
                "border-color": "#FF0",
                "background": "#FFA",
                "opacity": 0.8,
                "position": "fixed",
                "top": off.top - jDoc.scrollTop(),
                "left": off.left - jDoc.scrollLeft(),
                "z-index": 9999999
            };
            // 建立闪烁层
            var lg = $(opt.html || '<div class="z_blink_light">&nbsp;</div>');
            lg.css(css).appendTo(owDoc.body);
            lg.animate({
                opacity: 0.1
            }, opt.speed || 500, function () {
                $(this).remove();
                if (typeof opt.after == "function") opt.after.apply(jq);
            });
        },
        //----------------------------------------------------
        /**
         * jq - 要移除的对象
         * opt.before    - 当移除执行前的操作, this 为 jq 对象
         * opt.remove    - 当移除完成后的操作, this 为 jq 对象
         * opt.after     - 当移除动画完成后的操作, this 为 jq 对象
         * opt.holder    - 占位符的 HTML，默认是 DIV.z_remove_holder
         * opt.speed     - 移除的速度，默认为  300
         * opt.appendTo  - (优先)一个目标，如果声明，则不会 remove jq，而是 append 到这个地方
         * opt.prependTo - 一个目标，如果声明，则不会 remove jq，而是 preppend 到这个地方
         */
        removeIt: function (jq, opt) {
            // 格式化参数
            jq = $(jq);
            opt = opt || {};
            if (typeof opt == "function") {
                opt = {
                    after: opt
                };
            } else if (typeof opt == "number") {
                opt = {
                    speed: opt
                };
            }
            // 计算尺寸
            var w = jq.outerWidth(true);
            var h = jq.outerHeight(true);

            // 得到被删除目标的真实样式
            var eleStyle = window.getComputedStyle(jq[0]);

            // 增加占位对象，以及移动 me
            var html = opt.holder || '<div class="z_remove_holder">&nbsp;</div>';
            var holder = $(html).css({
                "display": eleStyle.display,
                "vertical-align": "middle",
                "padding": 0,
                "margin": 0,
                "width": w,
                "height": h,
            }).insertAfter(jq);

            // 移动执行前的操作
            if (_.isFunction(opt.before)) {
                opt.before.apply(jq);
            }

            // 附加元素
            if (opt.appendTo) {
                jq.appendTo(opt.appendTo);
            }
            // 附件元素
            else if (opt.prependTo) {
                jq.prependTo(opt.prependTo);
            }
            // 删除元素
            else {
                jq.remove();
            }

            // 移动删除/移动后的操作
            if (_.isFunction(opt.remove)) {
                opt.remove.apply(jq);
            }

            // 执行动画删除占位元素，动画执行完毕后，调用回调
            holder.animate({
                width: 0,
                height: 0
            }, opt.speed || 300, function () {
                $(this).remove();
                if (_.isFunction(opt.after)) {
                    opt.after.apply(jq);
                }
            });
        },
        //----------------------------------------------------
        /**
         编辑任何元素的内容
         ele - 为任何可以有子元素的 DOM 或者 jq，本函数在该元素的位置绘制一个 input 框，让用户输入新值
         opt - 配置项目
         {
            multi : false       // 是否是多行文本
            enterAsConfirm : false  // 多行文本下，回车是否表示确认
            newLineAsBr : false // 多行文本上，新行用 BR 替换。 默认 false
            text  : null   // 初始文字，如果没有给定，采用 ele 的文本
            width : 0      // 指定宽度，没有指定则默认采用宿主元素的宽度
            height: 0      // 指定高度，没有指定则默认采用宿主元素的高度
            extendWidth  : true   // 自动延伸宽度
            extendHeight : true   // 自动延伸高度
            takePlace    : false   // 是否代替宿主的位置，如果代替那么将不用绝对位置和遮罩
            selectOnFocus : true   // 当显示输入框，是否全选文字（仅当非 multi 模式有效）

            // 修改之后的回调
            // 如果不指定这个项，默认实现是修改元素的 innertText
            after : {c}F(newval, oldval, jEle){}

            // 回调的上下文，默认为 ele 的 jQuery 包裹对象
            context : jEle
         }
         * 如果 opt 为函数，相当于 {after:F()}
         */
        editIt: function (ele, opt) {
            // 处理参数
            var jEle = $(ele);
            if (jEle.size() == 0 || jEle.attr("z-edit-it-on"))
                return;

            // 标记已经被编辑
            jEle.attr("z-edit-it-on", "yes");

            var opt = opt || {};
            // 直接给了回调
            if (typeof opt == "function") {
                opt = {
                    after: opt
                };
            }
            // 多行
            else if (typeof opt == "boolean") {
                opt = {
                    multi: true
                };
            }
            // 定义默认的回调
            if (!_.isFunction(opt.after)) {
                opt.after = function (newval, oldval, jEle, opt) {
                    // 多行用 HTML
                    if (opt.multi) {
                        jEle.html(newval);
                    }
                    // 单行用文本
                    else {
                        jEle.text(newval);
                    }
                };
            }
            //...............................................
            // 定义一些默认值
            zUtil.setUndefined(opt, "extendWidth", true);
            zUtil.setUndefined(opt, "extendHeight", opt.multi);
            zUtil.setUndefined(opt, "selectOnFocus", true);
            zUtil.setUndefined(opt, "copyStyle", true);
            //...............................................
            // 定义键盘处理函数
            var __on_input = function (e) {
                var jInput = $(this);
                var jDiv = jInput.parent();
                var opt = jDiv.data("@OPT");
                // 如果是自动延伸 ...
                if (opt.extendWidth) {
                    var orgW = jDiv.attr("org-width") * 1;
                    var newW = jInput[0].scrollWidth + jInput[0].scrollLeft;
                    jDiv.css("width", Math.max(orgW, newW));
                }
                if (opt.extendHeight) {
                    var orgH = jDiv.attr("org-height") * 1;
                    var newH = jInput[0].scrollHeight + jInput[0].scrollTop;
                    jDiv.css("height", Math.max(orgH, newH));
                }

            };
            var __on_keydown = function (e) {
                e.stopPropagation();
                var jInput = $(this);
                var jDiv = jInput.parent();
                var opt = jDiv.data("@OPT");
                //console.log(e.which)
                // Esc
                if (27 == e.which) {
                    var old = jDiv.data("@OLD");
                    jInput.val(old).blur();
                    return;
                }
                // Del
                else if( 46 == e.which) {
                    //e.stopPropagation();
                }
                // Enter
                else if ( 13 == e.which) {
                    // 多行的话，必须加 ctrl 才算确认
                    if (opt.multi) {
                        if (($z.os.mac && e.metaKey) || e.ctrlKey || opt.enterAsConfirm) {
                            jInput.blur();
                            return;
                        }
                    }
                    // 单行的话，就确认
                    else {
                        jInput.blur();
                        return;
                    }
                }
            };
            //...............................................
            // 定义确认后的处理
            var __on_ok = function () {
                var jInput = $(this);
                var jDiv = jInput.parent();
                var jEle = jDiv.data("@ELE");
                var opt = jDiv.data("@OPT");
                var context = opt.context || jEle;

                // 处理值
                var old = jDiv.data("@OLD");
                var val = jInput.val();

                // 单行的默认要 trim 一下
                if (opt.trim || (!opt.trim && !opt.multi)) {
                    val = $.trim(val);
                }

                // 如果是多行的话，用 HTML 替换一下
                if (opt.multi && opt.newLineAsBr) {
                    val = val.replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace(/\r?\n/g, "\n<br>");
                }

                // 回来吧宿主
                if (opt.takePlace) {
                    jEle.insertBefore(jDiv).show();
                }
                // 移除遮罩
                else {
                    jDiv.prev().remove();
                }

                // 移除编辑控件
                jDiv.remove();

                // 移除宿主标识
                jEle.removeAttr("z-edit-it-on");

                // 调用回调
                opt.after.apply(jEle, [val, old, jEle, opt]);
            };
            //...............................................
            // 准备显示输入框
            var val = opt.text || jEle.text();
            var html = '<div class="z-edit-it">' + (opt.multi ? '<textarea></textarea>' : '<input>') + '</div>';
            //...............................................
            // 计算宿主尺寸
            var rect = $D.rect.gen(jEle, true);
            var el = jEle[0];

            //...............................................
            // 显示输入框
            var boxW = opt.width || rect.width;
            var boxH = opt.height || rect.height;
            var jDiv = $(html)
                .data("@OPT", opt)
                .data("@ELE", jEle)
                .data("@OLD", val)
                .attr({
                    "org-width": boxW,
                    "org-height": boxH
                })
                .css({
                    "width": boxW,
                    "height": boxH,
                    "padding": 0,
                    "margin": 0
                });
            // 给输入框设值
            var jInput = jDiv.children();
            jInput.val(val).attr("spellcheck", "false").css({
                "width": "100%",
                "height": "100%",
                "outline": "none",
            });
            // 单行输入框，设一下行高
            if (!opt.multi) {
                jInput.css("line-height", boxH);
                if (opt.selectOnFocus)
                    jInput[0].select();
            }

            //...............................................
            // 取得宿主的显示模式
            if (opt.copyStyle) {
                var eleStyle = window.getComputedStyle(jEle[0]);

                var rKeys = ["display", "letter-spacing", "margin", "padding"
                    , "font-size", "font-family", "border"
                    , "line-height", "text-align"];
                // 如果占位模式，才 copy 背景色和前景色
                if (opt.takePlace) {
                    rKeys.push("background");
                    rKeys.push("color");
                }

                var css = {};
                for (var i = 0; i < rKeys.length; i++) {
                    var rKey = rKeys[i];
                    var pKey = $z.upperWord(rKey);
                    css[rKey] = eleStyle[pKey];
                    //console.log(rKey, " : ", eleStyle[pKey]);
                }
                //console.log(css);

                // 将自身设置成和宿主一样的显示模式
                jInput.css(_.extend(css, {
                    "overflow": "hidden",
                    "outline": "none",
                    "resize": "none"
                }));
            }
            //...............................................
            // 替代宿主的位置
            if (opt.takePlace) {
                // 占宿主的位置
                jDiv.insertBefore(jEle);

                // 然后，嗯，宿主死开
                jEle.appendTo(jEle[0].ownerDocument.body).hide();

                // 模拟宿主点击
                jDiv.click();
            }
            //...............................................
            // 绝对定位
            else {
                // 绝对定位，将自身插入到宿主里面
                jDiv.appendTo(jEle);
                // 显示绝对定位
                jDiv.css({
                    "position": "fixed",
                    "top": rect.top - document.body.scrollTop,
                    "left": rect.left - document.body.scrollLeft,
                    "z-index": 999999
                });
                // 显示遮罩
                $('<div>').insertBefore(jDiv)
                    .css({
                        "background": "#000",
                        "opacity": 0,
                        "position": "fixed",
                        "top": 0,
                        "left": 0,
                        "bottom": 0,
                        "right": 0,
                        "z-index": 999998,
                    });
            }
            //...............................................
            // 绑定事件
            jInput.one("blur", __on_ok);
            jInput.one("change", __on_ok);
            jInput.on("input", __on_input);
            jInput.on("keydown", __on_keydown);
            // jInput.on("click", function(e){
            //     console.log(e.isPropagationStopped());
            // })
            jInput.focus();

            // 返回最新的 DIV
            return jDiv;
        },
        /*----------------------------------------------------
         对指定元素进行高亮标注，主要用于界面的初始使用提示
         opt - 配置项目
         {
         items : [{
            target : jQuery   // 指定一组要标注的文字
            text   : "xxxx"   // 标注文字的内容
         }, {
            // 同时进行的下一组标注
         }],
         done : F()    // 所有标注显示完毕后的回调
         }
         */
        markIt: function (opt, callback) {
            // 支持快捷方式 ({..}, callback)
            if (opt && opt.target && opt.text) {
                opt = {items: [opt]};
            }

            // 必须得有绘制项目
            if (!_.isArray(opt.items) || opt.items.length == 0)
                return;

            // 支持设置回调
            this.setUndefined(opt, "done", callback);

            // 绘制遮罩层
            var jMark = $('<div>').appendTo(document.body).css({
                "position": "fixed",
                "top": 0,
                "left": 0,
                "right": 0,
                "bottom": 0,
                "zIndex": 99999,
                "user-select": "none",
            });

            // 得到遮罩层的大小并生成画布
            var R_VP = $D.rect.gen(jMark);
            //console.log(R_VP)
            var jCanv = $('<canvas>').appendTo(jMark).attr({
                "width": R_VP.width,
                "height": R_VP.height,
            });
            var canvas = jCanv[0];

            // 准备文字层
            var jText = $('<div>').appendTo(jMark).css({
                "position": "fixed",
                "top": 0,
                "left": 0,
                "font-size": "14px",
                "font-family": "Arial",
                "color": "#F80",
                "text-shadow": "1px 1px 2px rgba(0,0,0,0.6)",
            });

            // 准备绘制项目的方法
            var __draw_item = function (it) {
                if (!it.target)
                    return;
                var jTa = $(it.target);
                if (jTa.length == 0)
                    return;
                // 得到画笔，并绘制初始背景色
                var g2d = canvas.getContext("2d");
                g2d.save();
                g2d.clearRect(0, 0, canvas.width, canvas.height);
                g2d.restore();
                g2d.fillStyle = "rgba(0,0,0,0.8)";
                g2d.fillRect(0, 0, canvas.width, canvas.height);
                // 重置画笔
                g2d.fillStyle = "#F80";
                g2d.strokeStyle = "#F80";
                g2d.lineWidth = 2;
                // 计算矩形
                var rect = $D.rect.gen(jTa);
                console.log(rect)
                var args = zUtil.pick(rect, "left,top,width,height", true);
                // 绘制提示区域高亮矩形
                g2d.clearRect.apply(g2d, args);
                g2d.strokeRect.apply(g2d, args);

                // 绘制文字
                // 看看左右两个距离哪个大
                var css = $D.rect.asCss(rect, $D.dom.winsz(true));
                if (css.left > css.right) {
                    jText.text(it.text).css({
                        "left": "",
                        "right": css.right + css.width + 40,
                    });
                    // 绘制指示线
                    g2d.beginPath();
                    g2d.moveTo(rect.left - 4, rect.y);
                    g2d.lineTo(rect.left - 36, rect.y);
                    g2d.stroke();
                }
                // 默认绘制在右侧
                else {
                    jText.text(it.text).css({
                        "left": rect.right + 40,
                        "right": "",
                    });
                    // 绘制指示线
                    g2d.beginPath();
                    g2d.moveTo(rect.right + 4, rect.y);
                    g2d.lineTo(rect.right + 36, rect.y);
                    g2d.stroke();
                }
                // 设置文字 Y 轴位置
                jText.css("top", rect.y - (jText.outerHeight() / 2));
            }

            // 记录绘制的项目
            var index = 0;

            // 绘制第一个对象
            __draw_item(opt.items[index++]);

            //  监控鼠标
            jMark.on("click", "canvas", function () {
                // 结束
                if (index >= opt.items.length) {
                    jMark.remove();
                    zUtil.invoke(opt, "done", []);
                }
                // 继续绘制
                else {
                    __draw_item(opt.items[index++]);
                }
            });

        },
        /*.............................................
         归纳 Markdown 内部的标题，将其作为标签
          - jAr : 文章信息
          - isTab : F(jq, index):int 
                判断指定下标是否是标签。返回新下标
                @return null 表示不是标签
                否则返回 {
                    nextIndex : 0,  // 下一个迭代下标（以便跳过）
                    title : "xxx",  // 标题
                    html  : "<..>"  // 原始HTML，恢复用
                }
        */
        tabArticle : function(jAr, isTab) {
            if(!jAr || jAr.length == 0)
                return;
            var is_first_level = false;
            // 标记过的，就不要标记了
            if(jAr.attr("z-article-tab-already"))
                return;
            // 默认用 H1 判断
            if(!isTab) {
                is_first_level = true;
                isTab = function(jq, index) {
                    if(index>= (jq.length - 1))
                        return null;
                    var ele = jq[index];
                    var el2 = jq[index+1];
                    if('HR' == ele.tagName && 'H1' == el2.tagName){
                        return {
                            nextIndex : index + 1,
                            title : $(el2).text(),
                            html  : ele.outerHTML + el2.outerHTML,
                            eles  : $([ele, el2]),
                        };
                    }
                };
            }
            // 来吧，搞一个格式化，把所有 hr 后面有 H1 的都缩起来
            var jq = jAr.children();
            var block;
            var blist = [];
            var lastIndex = jq.length - 1;
            for(var i=0; i<jq.length; i++) {
                var ele = jq[i];
                var blo = isTab(jq, i);
                // 当前是 Hr 且前面是一个 H1，准备收缩的块
                if(blo) {
                    // 先看看之前的块要不要推入
                    if(block) {
                        blist.push(block);
                    }
                    // 开始一个新块
                    block = blo;
                    block.children = [];
                    i = block.nextIndex;
                    // 嗯，标识一下
                    block.eles.attr("z-article-tab-source", "yes");
                }
                // 增加到当前块里
                else if(block) {
                    block.children.push(ele);
                }
            }
            // 推入最后一个块
            if(block) {
                blist.push(block);
            }
            // 将所有有标题的块生成一下索引
            if(blist.length > 0) {
                var jStub = blist[0].eles.first();
                var jTabs = $('<div class="z-article-tabs">')
                                .insertBefore(jStub);
                var jUl = $('<ul>').appendTo(jTabs);
                for(var i=0; i<blist.length; i++) {
                    var b   = blist[i];
                    var str = b.title;
                    var m = /^([^:：]+)[:：]/.exec(str);
                    if(m)
                        str = m[1];
                    $('<li>').text(str).attr({
                        "b": i,
                        "title" : b.title
                    }).appendTo(jUl);
                }

                // 如果是二级，所有的块外面再包裹一层
                var jBlockCon;
                if(!is_first_level) {
                    jBlockCon = $('<div class="z-article-blcon">').appendTo(jAr);
                }

                // 依次将块缩入一个 DIV
                for(var i=0; i<blist.length; i++) {
                    var b = blist[i];
                    var jStub = b.eles.first();
                    if(b.children.length > 0) {
                        var jBlock = $('<div class="z-article-block">').attr("b", i);
                        if(jBlockCon){
                            jBlockCon.append(jBlock);
                        } else {
                            jBlock.insertAfter(jStub);
                        }
                        jBlock.append(b.eles);
                        jBlock.append($(b.children));
                        // 针对每个块进行第二级缩进
                        if(is_first_level) {
                            //console.log("in level 2")
                            $z.tabArticle(jBlock, function(jq, index){
                                if(index>= (jq.length - 1))
                                    return null;
                                var ele = jq[index];
                                if('H2' == ele.tagName){
                                    var jCode = $(ele).find('code');
                                    if(jCode.length == 0)
                                        return null;
                                    return {
                                        nextIndex : index,
                                        title : jCode.text(),
                                        html  : ele.outerHTML,
                                        eles  : $(ele),
                                    };
                                }
                            });  // ~ $z.tabArticle
                        }
                    } // ~ if(b.children.length > 0) {
                }
            } // ~ if(blist.length > 0) 
            // 最后标识一下，以防止重复标识
            jAr.attr("z-article-tab-already", "yes");
        },
        //.............................................
        untabArticle : function(jAr) {
            if(!jAr || jAr.length == 0)
                return;
            // 未标识过，无需释放
            if(!jAr.attr("z-article-tab-already"))
                return;

            // 首先移除所有标签
            jAr.find('.z-article-tabs').remove();

            // 循环块
            jAr.find('>.z-article-block').each(function(){
                var jBlock = $(this);
                
                // 解开二级包裹标签
                var jBlcon = jBlock.find(".z-article-blcon");
                jBlcon.find('>.z-article-block').each(function(){
                    $(this).children().insertBefore(jBlcon);
                });
                jBlcon.remove();

                // 解开当前块
                jBlock.children().insertAfter(jBlock);
                jBlock.remove();
            });

            // 移除标记
            jAr.find('[z-article-tab-source]').removeAttr('z-article-tab-source');
            jAr.removeAttr("z-article-tab-already")
        },
        //.............................................
        // 寻找所有的符合给定选择器的对象，
        // 根据与窗口相交的面积判断应该高亮那个标签
        tabArticleMarkCurrent : function(jAr, selector){
            if(!jAr || jAr.length == 0)
                return;
            var bIndex = 0;
            var bArea  = 0;
            var viewport = $D.dom.winsz(jAr[0].ownerDocument.defaultView);
            //console.log("haha")
            jAr.find(selector).each(function(){
                var jBlock = $(this);
                var rect = $D.rect.gen(jBlock);
                var area = $D.rect.overlap_area(viewport, rect);
                if(area > bArea) {
                    bArea  = area;
                    bIndex = jBlock.attr("b") * 1;
                }
            });
            // 得到标签
            jAr.find('> .z-article-tabs li[current]').removeAttr("current");
            jAr.find('> .z-article-tabs li[b="'+bIndex+'"]').attr("current", "yes");
        },
        //.............................................
        // 处理页面自动停靠相关的功能
        pageDock : {
            unmark : function(ele) {
                // 确保是个 jQuery
                var jq = $(ele);

                // 删除属性和标识
                jq.removeData("z-dock-info")
                    .removeAttr("z-dock-ele")
                        .css({
                            "position" : "",
                            "top"   : "",
                            "left"  : "",
                            "right" : "",
                            "z-index" : "",
                        });
            },
            /*
            标识指定元素，以备 scroll 时调整之用。
             - ele : Element      // 元素
             - opt : {
                fitpage : true     // 停靠时自适应页面宽度，默认 true
                zIndex  : 10       // 停靠时设置的 zIndex，默认 10
                pos : "top|bottom" // 停靠位置，默认top
                // 当滚动到停靠元素消失了多少个自身高度时，才发生停靠
                // 默认 3
                factor : 3,
                // 自己是否停靠，要依靠自己的父元素是否与视口有足够的相交面积
                // 默认 false
                detectParent : false,
                // 停靠的时候，是否取消掉前面的停靠
                // 默认 false
                reset : false,
            }
            */
            mark : function(ele, opt){
                // 确保是个 jQuery
                ele = $(ele);

                // 处理参数形式
                if(_.isBoolean(opt)){
                    opt = {fitpage : opt};
                }
                // 数字默认当做 zIndex
                else if(_.isNumber(opt)){
                    opt = {zIndex : opt};
                }

                // 确保参数
                opt = opt || {};
                $z.setUndefined(opt, "pos", "top");
                $z.setUndefined(opt, "fitpage", true);
                $z.setUndefined(opt, "zIndex", 10);
                $z.setUndefined(opt, "factor", 3);
                $z.setUndefined(opt, "detectParent", false);
                $z.setUndefined(opt, "reset", false);

                // 没内容无视
                if(ele.length == 0)
                    return;
                // 多个元素，逐个标记
                if(ele.length > 1) {
                    for(var i=0; i<ele.length; i++) {
                        this.mark(ele[i], opt);
                    }
                    return;
                }
                // 得到元素和对应列表
                ele = ele[0];
                doc = ele.ownerDocument;
                if(!_.isArray(doc.__dock_list_top)) {
                    doc.__dock_list_top = [];
                }
                var list = doc.__dock_list_top;                    

                // 查找一下
                var theEle;
                // 如果存在就无视吧
                for(var i=0; i<list.length; i++) {
                    if(list[i] === ele){
                        theEle = list[i];
                        break;
                    }
                }
                // 计入堆栈
                if(!theEle) {
                    list.push(ele);
                    theEle= ele;
                }
                // 补充一下值
                var jq = $(theEle);
                var t = jq.offset().top;
                var h = jq.outerHeight();
                jq.data("z-dock-info", {
                    fitpage      : opt.fitpage,
                    zIndex       : opt.zIndex,
                    factor       : opt.factor,
                    detectParent : opt.detectParent,
                    reset        : opt.reset,
                    primerTop    : t,
                    primerHeight : h,
                }).attr({
                    "z-dock-ele" : "yes"
                });
            },
            /*
            如果发生了页面滚动，调用这个函数，会自动处理排列
            */
            adjust : function(doc) {
                // 堆叠顶部
                var docScrollTop = $(doc).scrollTop();
                var viewport = $D.dom.winsz(doc);
                var topH  = 0;   // 已堆叠的控件占据的高度
                
                // 准备一个停靠列表
                var dockList = [];
                $(doc.body).find('[z-dock-ele]').each(function(index){
                    var jq = $(this);
                    var dt = jq.data("z-dock-info");
                    if(dt.reset) {
                        $(dockList).css({
                            "position" : "",
                            "top"   : "",
                            "left"  : "",
                            "right" : "",
                            "z-index" : "",
                        }).removeAttr("z-dock-enabled");
                        dockList = [];
                    }
                    dockList.push(this);
                });

                // 处理这个停靠列表
                for(var i=0; i<dockList.length; i++) {
                    var jq = $(dockList[i]);
                    var dt = jq.data("z-dock-info");
                    var threshold = dt.primerTop + dt.primerHeight * dt.factor - topH;
                    //console.log("scroll",index, docScrollTop, threshold);

                    // 超过了就停靠，或者自己所在的块并未移出屏幕
                    var is_should_dock = false;
                    if(docScrollTop > threshold) {
                        // 要看看自己所在块是否有足够的空间 doc
                        if(dt.detectParent) {
                            // 计算自己所在的块
                            var jMyPaOut = jq.closest('[hm-scroll-outview]');
                            if(jMyPaOut.length == 0) {
                                var jMyPaIn = jq.closest('[hm-scroll-inview]');
                                var inRect  = $D.rect.gen(jMyPaIn);
                                var ovRect  = $D.rect.overlap(viewport, inRect);
                                // 自己所在块与视口重叠的面积必须要能放置自身的高度
                                if(ovRect.height > jq.outerHeight()){
                                    is_should_dock = true;
                                }
                            }
                        }
                        // 嗯一定要 dock 咯
                        else {
                            is_should_dock = true;
                        }
                    }

                    if(is_should_dock) {
                        var css = {
                            "position" : "fixed",
                            "top" : topH,
                            "z-index" : dt.zIndex,
                        };
                        if(dt.fitpage) {
                            css.left = 0;
                            css.right = 0;
                        }
                        jq.css(css).attr("z-dock-enabled", "yes");
                        // 累计下一个堆叠起点
                        topH += dt.primerHeight;
                    }
                    // 否则取消停靠
                    else {
                        jq.css({
                            "position" : "",
                            "top"   : "",
                            "left"  : "",
                            "right" : "",
                            "z-index" : "",
                        }).removeAttr("z-dock-enabled");
                    }
                };
            }
        },
        //.............................................
        copyToClipboard : function(str) {
          var jEle = $("<input>").css({
                    "position" : "fixed",
                    "top"  : "-100000px",
                    "left" : "-100000px",
                }).appendTo(document.body);
          jEle.val(str).select();
          document.execCommand("copy");
          jEle.remove();
        },
        //.............................................
        // json : function(obj, fltFunc, tab){
        //     // toJson
        //     if(typeof obj == "object"){
        //         return JSON.stringify(obj, fltFunc, tab);
        //     }
        //     // fromJson
        //     if (!obj) {
        //         return null;
        //     }
        //     return JSON.parse(obj, fltFunc);
        // },
        //.............................................
        toJson: function (obj, fltFunc, tab) {
            return JSON.stringify(obj, fltFunc, tab);
        },
        //.............................................
        fromJson: function (str, fltFunc) {
            str = $.trim(str);
            if (!str)
                return null;
            try {
                return JSON.parse(str, fltFunc);
            } catch (e1) {
                try {
                    return eval('(' + str + ')');
                } catch (e2) {
                    throw e2 + " \n" + str;
                }
            }
        },
        //............................................
        map : function(str, fltFunc) {
            str = $.trim(str);
            if(!/^\{/.test(str))
                str = "{" + str;
            if(!/\}$/.test(str))
                str = str + "}";
            try {
                return JSON.parse(str, fltFunc);
            } catch (e1) {
                try {
                    return eval('(' + str + ')');
                } catch (e2) {
                    throw e2 + " \n" + str;
                }
            }
        },
        //............................................
        // [{id:"a",n:9}, {id:"b",n:10}]
        //  => {a: {id:"a",n:9}, b: {id:"b",n:10}}
        arrayToMap : function(ary, key) {
            var map = {};
            if(_.isArray(ary) && ary.length > 0) {
                for(var i=0; i<ary.length; i++) {
                    var ele = ary[i];
                    var kv  = ele[key];
                    if(kv) {
                        map[kv] = ele;
                    }
                }
            }
            return map;
        },
        //............................................
        // 计算一个字符串开头有几个缩进，
        // str : 给定字符串
        // tabWidth : 一个 \t 相当于几个空格，默认 4
        countStrHeadIndent: function (str, tabWidth) {
            var n = 0;
            tabWidth = tabWidth || 4;
            if (str) {
                for (var i = 0; i < str.length; i++) {
                    var c = str.charAt(i);
                    if (' ' == c)
                        n++;
                    else if ('\t' == c)
                        n += tabWidth;
                    else
                        break;
                }
            }
            return Math.floor(n / tabWidth);
        },
        //............................................
        // 计算一个字符串开头有几个重复的字符
        // str : 给定字符串
        // c : 指定重复字符
        countStrHeadChar: function (str, c) {
            var re = 0;
            if (str)
                for (; re < str.length; re++) {
                    if (str.charAt(re) != c)
                        return re;
                }
            return re;
        },
        //............................................
        // 计算一个子字符串在给定字符串出现的次数
        //  - str : 给定字符串
        //  - sub : 要计算重复的子字符串
        // 返回出现的次数
        countSubStr: function (str, sub) {
            var re = 0;
            if (str && sub) {
                var fromIndex = 0;
                var pos = str.indexOf(sub, fromIndex);
                while (pos >= 0) {
                    re++;
                    fromIndex = pos + sub.length;
                    pos = str.indexOf(sub, fromIndex);
                }
            }
            return re;
        },
        //............................................
        // 对字符串反向缩进
        // str : 给定字符串
        // indent : 反向 indent 几次，默认 1
        // tabWidth : 一个 \t 相当于几个空格，默认 4
        shiftIndent: function (str, indent, tabWidth) {
            if (!str)
                return str;
            indent = indent > 0 ? indent : 1;
            tabWidth = tabWidth || 4;
            var n = 0;
            var i = 0;
            for (; i < str.length; i++) {
                if (n > 0 && Math.floor(n / tabWidth) >= indent)
                    break;
                var c = str.charAt(i);
                if (' ' == c)
                    n++;
                else if ('\t' == c)
                    n += tabWidth;
                else
                    break;
            }
            if (i > 0)
                return str.substring(i);
            return str;
        },
        /*............................................
         将给定的 Markdown 文本，转换成 HTML 代码
         opt : {
            media   : {c}F(src)   // 计算媒体文件加载的真实 URL
            context : undefined  // 所有回调的上下文
         }
         */
        markdownToHtml: function (str, opt) {
            // 确保有选项对象
            opt = opt || {};
            context = opt.context || this;

            // 设置默认值
            zUtil.setUndefined(opt, "media", function (src) {
                return src;
            });

            /* 首先将文本拆分成段落： 
             {
             type  : "H|P|code|OL|UL|hr|Th|Tr|quote|empty",
             indent : 1,
             content:["line1","line2"],
             codeType : null,
             cellAligns : ["left", "center", "right"]
             }
             */
            var blocks = [];
            var lines = str.split(/\r?\n/g);
            // 增加一个帮助函数
            blocks.tryPush = function (B) {
                if (B.type) {
                    this.push(B);
                    return {type: null, level: 0, content: []};
                }
                return B;
            };
            // 准备第一段
            var B = {type: null, level: 0, content: []};
            var lastLineIsBlankLink = false;
            for (var i = 0; i < lines.length; i++) {
                var line = lines[i];
                var trim = $.trim(line);
                var indent = zUtil.countStrHeadIndent(line, 2);

                // 来吧，判断类型
                // 空段落
                if (trim.length == 0) {
                    // 之前如果是 code，那么增加进去
                    if (/^(code|UL|OL)$/.test(B.type)) {
                        B.content.push("");
                    }
                    // 否则如果有段落，就结束它
                    else {
                        B = blocks.tryPush(B);
                    }
                }
                // 标题: H
                else if (/^#+ .+/.test(line)) {
                    B = blocks.tryPush(B);
                    B.type = "H";
                    B.level = zUtil.countStrHeadChar(line, '#');
                    B.content.push($.trim(line.substring(B.level)));
                }
                // 代码: code
                else if (/^```/.test(line)) {
                    B = blocks.tryPush(B);
                    B.type = "code";
                    B.codeType = $.trim(trim.substring(3)) || null;
                    for (i++; i < lines.length; i++) {
                        line = lines[i];
                        if (/^```/.test(line)) {
                            break;
                        }
                        B.content.push(line);
                    }
                    B = blocks.tryPush(B);
                }
                // 水平线: hr
                else if (/^ *[=-]{3,} *$/.test(line)) {
                    B = blocks.tryPush(B);
                    B.type = "hr";
                    B = blocks.tryPush(B);
                }
                // 表格分隔符: T
                else if ("P" == B.type
                    && B.content.length == 1
                    && B.content[0].indexOf("|") > 0
                    && /^[ |:-]{6,}$/.test(line)) {
                    // 修改之前段落的属性
                    B.type = "Th";
                    B.content = zUtil.splitIgnoreEmpty(B.content[0], "|");

                    // 解析自己，分析单元格的 align
                    B.cellAligns = zUtil.splitIgnoreEmpty(trim, "|");
                    for (var x = 0; x < B.cellAligns.length; x++) {
                        var align = B.cellAligns[x].replace(/ +/g, "");
                        var m = /^(:)?([-]+)(:)?$/.exec(align);
                        if (m[1] && m[3]) {
                            B.cellAligns[x] = "center";
                        } else {
                            B.cellAligns[x] = m[3] ? "right" : "left";
                        }
                    }

                    // 推入
                    B = blocks.tryPush(B);

                    // 标识后续类型为 Tr
                    B.type = "Tr";
                }
                // 有序列表: OL
                else if ((!B.type || /^(OL|UL)$/.test(B.type))
                    && /^[0-9a-z][.]/.test(trim)) {
                    B = blocks.tryPush(B);
                    B.type = "OL";
                    B.level = indent;
                    B.content.push($.trim(trim.substring(trim.indexOf('.') + 1)));
                }
                // 无序列表: UL
                else if ((!B.type || /^(OL|UL)$/.test(B.type))
                    && /^[*+-][ ]/.test(trim)) {
                    B = blocks.tryPush(B);
                    B.type = "UL";
                    B.level = indent;
                    B.content.push($.trim(trim.substring(1)));
                }
                // 缩进表示的代码 
                else if (indent > 0) {
                    // 只有空段落，才表示开始 code
                    if (!B.type) {
                        B.type = "code";
                        B.content.push(zUtil.shiftIndent(line));
                    }
                    // 否则就要加入进去
                    else {
                        B.content.push(trim);
                    }
                }
                // 引用: quote
                else if (/^>/.test(trim)) {
                    B = blocks.tryPush(B);
                    B.type = "quote";
                    B.level = zUtil.countStrHeadChar(trim, ">");
                    B.content.push($.trim(trim.substring(B.level)));
                }
                // 普通段落融合到之前的块
                else if (/^(OL|UL|quote|P)$/.test(B.type)
                    && (!lastLineIsBlankLink || indent > B.level)) {
                    B.content.push(trim);
                }
                // 将自己作为表格行
                else if ("Tr" == B.type) {
                    B.content = zUtil.splitIgnoreEmpty(trim, "|");
                    B = blocks.tryPush(B);
                    B.type = "Tr";
                }
                // 默认是普通段落 : P
                else {
                    B = blocks.tryPush(B);
                    B.type = "P";
                    B.content.push(trim);
                }
                // 记录上一行
                lastLineIsBlankLink = (trim.length == 0);
            }

            // 处理最后一段
            B = blocks.tryPush(B);

            //console.log(blocks)

            // 定义内容输出函数
            var __B_to_html = function (tagName, B) {
                // 开始渲染
                var html = "";
                var tagNames = {};
                for (var i = 0; i < B.content.length; i++) {
                    var line = B.content[i];
                    // 忽略空行
                    if (!line)
                        continue;
                    // 首行以后每行前都加个换行符
                    if (i > 0) {
                        html += '<br>\n';
                    }
                    // 写入本行内容
                    html += __line_to_html(line, tagNames);
                }
                // 准备拼装
                var h2 = '\n<' + tagName;

                // 如果段落里只有 IMG，做一下特殊标识
                if (_.keys(tagNames).length == 1 && tagNames["img"]) {
                    h2 += ' md-img-only="yes"';
                }

                // 标识一下任务列表
                if (B.isTask) {
                    h2 += ' class="md-task-list-item">';
                    if (B.isChecked) {
                        h2 += '<input disabled type="checkbox" checked>';
                    } else {
                        h2 += '<input disabled type="checkbox">';
                    }
                }
                // 普通列表
                else {
                    h2 += '>';
                }

                // 返回
                return h2 + html;
            };

            var __line_to_html = function (str, tagNames) {
                var reg = '(\\*([^*]+)\\*)'
                    + '|(\\*\\*([^*]+)\\*\\*)'
                    + '|(__([^_]+)__)'
                    + '|(~~([^~]+)~~)'
                    + '|(`([^`]+)`)'
                    + '|(!\\[([^\\]]*)\\]\\(([^\\)]+)\\))'
                    + '|(\\[('
                    + '(!\\[([^\\]]*)\\]\\(([^\\)]+)\\))|([^\\]]*)'
                    + ')\\]\\(([^\\)]*)\\))'
                    + '|(https?:\\/\\/[^ ]+)';
                var REG = new RegExp(reg, "g");
                var m;
                var pos = 0;
                var html = "";
                while (m = REG.exec(str)) {
                    //console.log(m)
                    if (pos < m.index) {
                        var txt = str.substring(pos, m.index);
                        html += txt;
                        // 记录标签
                        if (tagNames && $.trim(txt)) {
                            tagNames["!TEXT"] = true;
                        }
                    }
                    // EM: *xxx*
                    if (m[1]) {
                        html += '<em>' + __line_to_html(m[2]) + '</em>';
                        // 记录标签
                        if (tagNames)
                            tagNames["em"] = true;
                    }
                    // B: **xxx**
                    else if (m[3]) {
                        html += '<b>' + __line_to_html(m[4]) + '</b>';
                        // 记录标签
                        if (tagNames)
                            tagNames["b"] = true;
                    }
                    // U: __xxx__
                    else if (m[5]) {
                        html += '<u>' + __line_to_html(m[6]) + '</u>';
                        // 记录标签
                        if (tagNames)
                            tagNames["b"] = true;
                    }
                    // DEL: ~~xxx~~
                    else if (m[7]) {
                        html += '<del>' + __line_to_html(m[8]) + '</del>';
                        // 记录标签
                        if (tagNames)
                            tagNames["del"] = true;
                    }
                    // CODE: `xxx`
                    else if (m[9]) {
                        var s2 = m[10];
                        // 特殊文字
                        if ("?" == s2) {
                            html += '<span><i class="fa fa-question-circle-o"></i></span>';
                        }
                        else {
                            var mfa = /^(fa[rs]?)-(.+)$/.exec(s2);
                            // fa
                            if (mfa) {
                                html += '<span><i class="' + mfa[1] + " fa-" + mfa[2] + '"></i></span>';
                            }
                            // zmdi
                            else if (/^zmdi-.+$/.test(s2)) {
                                html += '<span><i class="zmdi ' + s2 + '"></i></span>';
                            }
                            // 默认
                            else {
                                html += '<code>' + m[10] + '</code>';
                            }
                        }
                        // 记录标签
                        if (tagNames)
                            tagNames["code"] = true;
                    }
                    // IMG: ![](xxxx)
                    else if (m[11]) {
                        //console.log("haha", m[13])
                        var src  = m[13];
                        var src2 = opt.media.apply(context, [src]);

                        // 处理一下 alt
                        var alt = $.trim(m[12]);
                        var m2 = /^([0-9.]*(px|%|rem|em)?)[|\/]?([0-9.]*(px|%|rem|em)?)$/.exec(alt);
                        var iW, iH;
                        if(m2){
                            iW = m2[1];
                            iH = m2[3];
                            alt = "";
                        }
                        // 宽高
                        var cssImg = [];
                        if(iW){
                            cssImg.push("width:"+iW + (/[0-9]$/.test(iW)?"px;":";"));
                        }
                        if(iH)
                            cssImg.push("height:"+iH + (/[0-9]$/.test(iW)?"px;":";"));

                        // 判断是否是视频
                        var isVideo = /[.](mp4|avi|mov)$/.test(src);

                        // 准备输出头
                        html += isVideo
                                    ? '<video controls '
                                    : '<img ';
                        // 源
                        html += ' src="' + src2 + '" ';
                        // 宽高
                        if(cssImg.length > 0)
                            html += ' style="' + cssImg.join("") + '"';
                        
                        // 提示信息
                        if(alt)
                            html += ' title="' + alt + '"';
                        // 结束
                        html += '>';

                        // 视频还要加结束标记
                        if(isVideo) {
                            html += '</video>';
                        }

                        // 记录标签
                        if (tagNames)
                            tagNames["img"] = true;
                    }
                    // A: [](xxxx)
                    else if (m[14]) {
                        // 得到超链
                        var href = m[20];
                        if (href && href.endsWith(".md")) {
                            href = href.substring(0, href.lastIndexOf('.md')) + ".html";
                        }
                        // 如果内部是一个图片
                        if (m[16]) {
                            var src = opt.media.apply(context, [m[18]]);
                            html += '<a href="' + href + '">';
                            html += '<img alt="' + (m[17] || "") + '" src="' + src + '">';
                            html += '</a>';
                            // 记录标签
                            if (tagNames)
                                tagNames["img"] = true;
                        }
                        // 得到文字
                        else {
                            var text = m[19] || zUtil.getMajorName(href || "");
                            //console.log("A", href, text)
                            // 锚点
                            if (!href && /^#.+$/.test(text)) {
                                html += '<a name="' + text.substring(1) + '"></a>';
                            }
                            // 链接
                            else {
                                // 判断一下是否是新窗口打开
                                var newtab = false;
                                if(/^[+]/.test(text)) {
                                    text = $.trim(text.substring(1));
                                    newtab = true;
                                }
                                // 生成 DOM
                                html += '<a '
                                    + (newtab ? 'target="_blank' : "")
                                    + ' href="' + href + '">'
                                    + __line_to_html(text)
                                    + '</a>';
                            }
                        }
                        // 记录标签
                        if (tagNames)
                            tagNames["a"] = true;
                    }
                    // A: http://xxxx
                    else if (m[21]) {
                        html += '<a href="' + m[21] + '">' + m[21] + '</a>';
                        // 记录标签
                        if (tagNames)
                            tagNames["a"] = true;
                    }

                    // 唯一下标
                    pos = m.index + m[0].length;
                }
                if (pos < str.length) {
                    html += str.substring(pos);
                }
                return html;
            };

            var __B_to_blockquote = function (B, c) {
                c.html += __B_to_html("blockquote", B);
                // 循环查找后续的嵌套块
                for (c.index++; c.index < c.blocks.length; c.index++) {
                    var B2 = c.blocks[c.index];
                    if ("quote" == B2.type && B2.level > B.level) {
                        __B_to_blockquote(B2, c);
                    } else {
                        break;
                    }
                }
                c.html += '\n</blockquote>';
                c.index--;
            };

            // 处理一下第一行，判断支持一下 task list
            var __B_test_task = function (B) {
                if (B.content && B.content.length > 0) {
                    var line0 = B.content[0];
                    var m2 = /^[ \t]*(\[[xX ]\])[ ](.+)$/.exec(line0);
                    if (m2) {
                        B.isTask = true;
                        B.isChecked = m2[1] != '[ ]';
                        B.content[0] = m2[2];
                    }
                }
                return B;
            };

            var __B_to_list = function (B, c) {
                __B_test_task(B);
                // 开始渲染
                c.html += '\n<' + B.type
                if (B.isTask) {
                    c.html += ' class="md-task-list"';
                }
                c.html += '>';
                c.html += __B_to_html("li", B);
                // 循环查找后续的列表项，或者是嵌套
                for (c.index++; c.index < c.blocks.length; c.index++) {
                    var B2 = c.blocks[c.index];
                    // 继续增加
                    if (B.type == B2.type && B2.level == B.level) {
                        __B_test_task(B2);
                        c.html += '</li>' + __B_to_html("li", B2);
                    }
                    // 嵌套
                    else if (B2.level > B.level && /^(OL|UL)$/.test(B2.type)) {
                        __B_to_list(B2, c);
                    }
                    // 不属于本列表，退出吧
                    else {
                        break;
                    }
                }
                c.html += '</li>';
                c.html += '\n</' + B.type + '>';
                c.index--;
            };

            // 准备上下文
            var re = {
                html: "",
                index: 0,
                blocks: blocks,
            };

            // 逐个输出段落
            for (; re.index < re.blocks.length; re.index++) {
                B = re.blocks[re.index];

                // 标题: H
                if ("H" == B.type) {
                    re.html += '\n<h' + B.level + '>' + __line_to_html(B.content[0]) + '</h' + B.level + '>\n';
                }
                // 代码: code
                else if ("code" == B.type) {
                    re.html += '\n<pre' + (B.codeType ? ' code-type="' + B.codeType + '">' : '>');
                    re.html += B.content.join("\n").replace(/</g, "&lt;");
                    re.html += '</pre>';
                }
                // 列表: OL | UL
                else if ("OL" == B.type || "UL" == B.type) {
                    __B_to_list(B, re);
                }
                // 水平线: hr
                else if ("hr" == B.type) {
                    re.html += '\n<hr>';
                }
                // 表格
                else if ("Th" == B.type) {
                    re.html += '\n<table>';

                    // 记录表头
                    var THead = B;
                    var aligns = THead.cellAligns || [];

                    // 输出表头
                    re.html += '\n<thead>\n<tr>';
                    for (var x = 0; x < B.content.length; x++) {
                        var align = "left";
                        if (x < aligns.length)
                            align = aligns[x];
                        re.html += '\n<th' + ("left" != align ? ' align="' + align + '">' : '>');
                        re.html += __line_to_html(B.content[x]);
                        re.html += '</th>';
                    }
                    re.html += '\n</tr>\n</thead>';

                    // 输出表体
                    re.html += '\n<tbody>';
                    for (re.index++; re.index < re.blocks.length; re.index++) {
                        B = re.blocks[re.index];
                        if ("Tr" == B.type) {
                            re.html += '\n<tr>';
                            for (var x = 0; x < B.content.length; x++) {
                                var align = "left";
                                if (x < aligns.length)
                                    align = aligns[x];
                                re.html += '\n<td' + ("left" != align ? ' align="' + align + '">' : '>');
                                re.html += __line_to_html(B.content[x]);
                                re.html += '</td>';
                            }
                            re.html += '\n</tr>';
                        }
                        // 否则退出表格模式
                        else {
                            break;
                        }
                    }
                    re.html += '\n</tbody>';

                    // 退回一个块
                    re.index--;

                    // 结束表格
                    re.html += '\n</table>';
                }
                // 引用: quote
                else if ("quote" == B.type) {
                    __B_to_blockquote(B, re);
                }
                // 默认是普通段落 : P
                else {
                    re.html += __B_to_html("p", B);
                    re.html += '\n</p>';
                }
            }

            // 最后返回
            return re.html;
        },
        /*........................................................
        语法说明参见 doc/ext/hmaker/hm_markdown.md
        参数：{
            media   : {c}F(src)   // 计算媒体文件加载的真实 URL
            context : undefined   // 所有回调的上下文
        }
        */
        parsePoster : function(str, opt){
            str = str || "";
            opt = opt || {};
            // 设置默认值
            zUtil.setUndefined(opt, "media", function (src) {
                return src;
            });
            var C = opt.context || this;
            //console.log(C)

            // 准备解析结果
            var poster = {
                items : []
            };

            // 准备追加函数
            var __join_poster_item = function(it, poItem, m_attr, m_css) {
                // 必须有 poItem
                if(!poItem) {
                    throw "__join_poster_item no poItem!!!";
                }
                // 设置通用属性
                poItem.cssText = m_css;
                poItem.attr    = m_attr ? zUtil.fromJson(m_attr) : null;

                // 如果 it 是组，且给定项目的 depth 深
                if(it && 'group' == it.type && poItem.depth > 0) {
                    // 寻找最接近自己 depth 的组
                    var grp = it;
                    while(grp.items.length>0) {
                        var g2 = grp.items[grp.items.length-1];
                        if('group'!=g2.type || g2.depth >= poItem.depth) {
                            break;
                        }
                        grp = g2;
                    }
                    grp.items.push(poItem);
                    // 继续返回自己
                    return it;
                }
                
                // 加入到全局
                poster.items.push(poItem);
                // 如果是组，那么就返回
                if('group' == poItem.type) {
                    return poItem;
                }
                // 不是组
                return null;
            };
            
            // 按行解析
            var it = null;
            var lines  = str.split(/\r?\n/g);
            var m;
            for(var i=0; i<lines.length; i++) {
                var line = lines[i];
                var trim = $.trim(line);
                //console.log(line)

                // 计算缩进级别
                var depth = zUtil.countStrHeadIndent(line, 2);

                // 忽略注释和空行
                if(!trim || /^#/.test(trim))
                    continue;

                //....................................
                // 全局属性
                m = /^([@%]) *(bg|bgcolor|color|layout|height|width)( *: *(.+)?)?$/.exec(trim);
                if(m) {
                    // 分析正则表达式
                    var m_ta  = m[1];
                    var m_tp  = m[2];
                    var m_str = m[4];

                    // 如果是 >bg 或者 >bgcolor ...
                    var key = m_tp;
                    if('%' == m_ta)
                        key = "con_" + m_tp;

                    if('bg' == m_tp) {
                        poster[key] = opt.media.apply(C, [m_str]);
                    }else{
                        poster[key] = m_str;
                    }
                    continue;
                }
                //....................................
                // 分析组
                m = /^- *(group)([.]([^\[\{ :]+))? *(\{[^}]*\})? *(\[([^\]]*)\])?$/.exec(trim);
                if(m) {
                    // 分析正则表达式
                    var m_tp   = m[1];
                    var m_sel  = m[3];
                    var m_attr = m[4];
                    var m_css  = m[6];

                    // 加入项目
                    it = __join_poster_item(it, {
                        type  : "group",
                        depth : depth,
                        selector : m_sel,
                        items : []
                    }, m_attr, m_css);
                    continue;
                }
                //....................................
                // 判断海报的元素
                m = /^\+ *(text|picture|video|spec|list)([.]([^\[\{ :]+))? *(\{[^}]*\})? *(\[([^\]]*)\])?( *: *(.+)?)?$/.exec(trim);
                if(!m) 
                    continue

                // 分析正则表达式
                var m_tp   = m[1];
                var m_sel  = m[3];
                var m_attr = m[4];
                var m_css  = m[6];
                var m_str  = m[8];

                // 根据类型判断
                var poItem = {
                    type     : m_tp,
                    selector : m_sel,
                    depth    : depth
                };

                // 试图向后面读取
                if(/^(text|spec|list)$/.test(poItem.type)) {
                    var ss = [];
                    // 首行不要忘记
                    if(m_str)
                        ss.push(m_str)
                    // 读取内容，一直读到结束标记
                    for(i++; i<lines.length; i++) {
                        line = $.trim(lines[i]);
                        // 遇到组，或者其他元素结束
                        if(/^((- *group)|(\+ *(text|picture|video|spec|list)))/.test(line))
                            break;
                        ss.push(line);
                    }
                    // 不要忘记退一格
                    i--;
                    // 来吧根据类型搞一下
                    // 产品说明表格
                    if("spec" == poItem.type) {
                        var spec = {};
                        for(var x=0; x<ss.length; x++) {
                            var s = ss[x];
                            if(!s)
                                continue;
                            // 普通行
                            else if(s.indexOf('|') > 0){
                                spec.rows = spec.rows || [];
                                spec.rows.push(s.split(/[ \t]*\|[ \t]*/));
                            }
                            // 剩下的就追加到上一行
                            else {
                                // 追加到上一行最后一个单元格
                                if(spec.rows && spec.rows.length>0){
                                    var cells = spec.rows[spec.rows.length-1];
                                    if(cells.length>0){
                                        cells[cells.length-1] += "\n"+s;
                                    }
                                }
                                // 无法处理，干！
                                else {
                                    break;
                                }
                            }
                        }
                        poItem.spec = spec;
                    }
                    // 图文列表
                    else if("list" == poItem.type){
                        poItem.list = [];
                        for(var x=0; x<ss.length; x++) {
                            var s = ss[x];
                            if(!s)
                                continue;
                            var m2 = /^[ \t]*-[ \t]*([^:]+)?([ \t]*:[ \t]*(.+))?$/.exec(s);
                            if(m2) {
                                poItem.list.push({
                                    src  : opt.media.apply(C, [m2[1]]),
                                    text : m2[3]
                                });
                            }
                            // 否则追加
                            else if(poItem.list.length>0){
                                var li = poItem.list[poItem.list.length-1];
                                li.text = (li.text||"") + "\n" + s;
                            }
                        }
                    }
                    // 文字
                    else if('text' == poItem.type) {
                        poItem.text = ss;
                    }
                    // 没可能
                    else {
                        throw "Unknown poItem: " + poItem.type;
                    }
                }
                // 视频
                else if('video' == poItem.type) {
                    poItem.video = opt.media.apply(C, [m_str]);
                }
                // 图片
                else if('picture' == poItem.type) {
                    poItem.picture = opt.media.apply(C, [m_str]);
                }
                // 没可能
                else {
                    throw "Unknown poItem: " + poItem.type;
                }

                // 加入到项目
                it = __join_poster_item(it, poItem, m_attr, m_css);
            }

            //console.log(poster)

            // 返回
            return poster;
        },
        //.............................................
        // 根据 parsePoster 的解析结果，渲染一个 <div>
        // - poster : parsePoster 的解析结果
        // - jDiv   : 表示要渲染的目标，如果没有指定，则创建一个
        // @return jDiv
        renderPoster : function(poster, jDiv) {
            jDiv = jDiv || $("<div>");
            //----------------------------
            // 样式
            if(poster.cssText)
                jDiv[0].style.cssText = poster.cssText;
            // 全局属性
            if(poster.attr)
                jDiv.attr(poster.attr);
            //----------------------------
            // 宽度属性
            if(poster.width) {
                var lw = $z.parseLayoutSize(poster.width);
                jDiv.attr({
                    "layout-desktop-width" : lw.desktop,
                    "layout-mobile-width"  : lw.mobile,
                });
            }
            //----------------------------
            // 高度属性
            if(poster.height) {
                var lh = $z.parseLayoutSize(poster.height);
                jDiv.attr({
                    "layout-desktop-height" : lh.desktop,
                    "layout-mobile-height"  : lh.mobile,
                });
            }
            //----------------------------
            // 布局属性
            if(poster.layout)
                jDiv.attr("poster-layout", poster.layout);
            //----------------------------
            // 优先 CSS
            var css = {};
            // 背景图
            if(poster.bg)
                css.backgroundImage = 'url("' + poster.bg + '")';
            // 背景颜色
            if(poster.bgcolor)
                css.backgroundColor = poster.bgcolor;
            // 前景颜色
            if(poster.color)
                css.color = poster.color;
            // 设置 CSS
            jDiv.css(css);
            //----------------------------
            // 设置内胆
            var jInner = $('<div class="md-code-poster-con">').appendTo(jDiv);
            css = {};
            // 背景图
            if(poster.con_bg)
                css.backgroundImage = 'url("' + poster.con_bg + '")';
            // 背景颜色
            if(poster.con_bgcolor)
                css.backgroundColor = poster.con_bgcolor;
            // 设置 CSS
            jInner.css(css);
            //----------------------------
            // 声明处理函数
            var __do_item = function(jP, it) {
                var jIt;
                // 组
                if('group' == it.type) {
                    jIt = $('<div it="group">');
                    for(var i=0; i<it.items.length; i++) {
                        __do_item(jIt, it.items[i]);
                    }
                }
                // 文字
                else if(it.text) {
                    var ts = [];
                    for(var x=0; x<it.text.length; x++) {
                        ts.push(zUtil.escapeText(it.text[x]));
                    }
                    var txt = ts.join('<br>');
                    jIt = $('<span>').html(txt.replace(/\r?\n/g, '<br>') || "&nbsp;");
                }
                // 图片
                else if(it.picture) {
                    jIt = $('<img>').attr("src", it.picture);
                }
                // 视频
                else if(it.video) {
                    jIt = $('<video>').attr("src", it.video);
                }
                // 产品说明表格
                else if(it.spec) {
                    jIt = $('<div it="spec">');
                    var jT = $('<div class="spec-body">').appendTo(jIt);
                    // 表格内容
                    if(_.isArray(it.spec.rows))
                        for(var y=0; y<it.spec.rows.length; y++) {
                            var row = it.spec.rows[y];
                            var jTr = $('<ul>').appendTo(jT);
                            for(var x=0; x<row.length; x++) {
                                var cell = row[x];
                                var jTd = $('<li>').appendTo(jTr);
                                jTd.html(cell.replace(/\r?\n/g, '<br>'));
                            }
                        }
                }
                // 图文列表
                else if(it.list) {
                    jIt = $('<div it="list">');
                    var jUl = $('<ul>').appendTo(jIt);
                    for(var i=0; i<it.list.length; i++) {
                        var li = it.list[i];
                        var jLi = $('<li>').appendTo(jUl);
                        var jLiCon = $('<div class="li-con">').appendTo(jLi);
                        if(li.src) {
                            $('<img>').attr("src",li.src)
                                .appendTo(jLiCon);
                        }
                        if(li.text) {
                            $('<span>').html(li.text.replace(/\r?\n/g, '<br>'))
                                .appendTo(jLiCon);
                        }
                    }
                }
                // 不是合法的项目，无视
                if(!jIt)
                    return;
                // 增加类选择器
                if(it.selector)
                    jIt.addClass(it.selector);
                // 增加样式
                if(it.cssText)
                    jIt[0].style.cssText = it.cssText;
                // 增加属性
                if(it.attr)
                    jIt.attr(it.attr);
                // 加入 DOM
                jIt.appendTo(jP);
            };
            //----------------------------
            // 循环处理 Item
            if(_.isArray(poster.items)) {
                for(var i=0; i<poster.items.length; i++) {
                    var it = poster.items[i];
                    __do_item(jInner, it);
                } 
            }

            // 返回渲染结果
            return jDiv;
        },
        /*.............................................
        将给定的jq对象包含的元素，每个内容都解析一下 poster
        并将其替换
        - jq : 给定的一组 jq 对象
        - opt : {
            className : 生成 DIV 的类选择器名，默认为 md-code-poster
            media   : {c}F(src)   // 计算媒体文件加载的真实 URL
            context : undefined   // 所有回调的上下文
        }
        */
        explainPoster : function(jq, opt) {
            // 格式化输入
            jq = $(jq);
            opt = opt || {};
            opt.className = opt.className || "md-code-poster";
            if(jq.length == 0)
                return;
            // 循环处理
            jq.each(function(){
                var str    = $(this).html();
                var poster = zUtil.parsePoster(str, opt);
                var jDiv   = zUtil.renderPoster(poster);
                jDiv.addClass(opt.className).insertBefore(this);
            });
            // 清除原始内容
            jq.remove();
        },
        /*.............................................
        解析一种尺寸格式
        
         `1rem/.4rem`   表示 `PC/手机` 的宽度
    
        如果只有一种值，譬如 `1rem` 则相当于 `1rem/1rem`
        尺寸的值:
         - `50`  : 整数表示像素，与 50px 等效
         - `50%` : 可以支持各种 CSS 尺寸，譬如 50rem, 50vw 等  
         - `?`   : 返回的对象会是 undefined，表示不设置
         - `-`   : 返回的对象会是 "hidden" 表示隐藏

        返回对象 {desktop:"1rem", mobile:".04rem"}
        */ 
        parseLayoutSize : function(str, dft) {
            s = $.trim(str);
            if(!s) {
                if(_.isUndefined(dft))
                    return {};
                return dft;
            }

            // 准备解析函数
            var ___layout_size = function(sz) {
                if(_.isNumber(sz))
                    return sz + "px";
                
                // 确保去掉空白
                sz = $.trim(sz);

                if("?" == sz || !sz)
                    return undefined;
                if("-" == sz)
                    return "hidden";

                if(/^[0-9]+$/.test(sz))
                    return parseInt(sz);
                return sz;
            };

            var pos = s.indexOf('/');
            var desktop, mobile;
            if(pos>=0) {
                desktop = ___layout_size(s.substring(0,pos));
                mobile  = ___layout_size(s.substring(pos+1));
            }
            // 否则用一样的
            else {
                desktop = ___layout_size(s);
                mobile  = desktop;
            }
            // 搞定
            return {desktop:desktop, mobile:mobile};
        },
        //.............................................
        /* 将一个视频包裹成一个简单控制的播放
         - opt : {
            icons : {
                "pause"   : '<i class="zmdi zmdi-pause">',
                "play"    : '<i class="zmdi zmdi-play">',
                "loading" : '<i class="zmdi zmdi-spinner zmdi-hc-spin">',
                "replay"  : '<i class="zmdi zmdi-replay">',
            },
            autoFitHeight : false,
            watchClick    : true,
            copyAttrs     : ["pos","itfree","ithide","itfull","itshadow"]
         }
        */
        wrapVideoSimplePlayCtrl : function(jVideo, opt) {
            jVideo = $(jVideo);
            // 防空
            if(!jVideo || jVideo.length == 0)
                return;
            // 已经设置过了
            if(jVideo.parent().hasClass(".z-video-con"))
                return;

            // 多个视频
            if(jVideo.length > 1) {
                for(var i=0; i<jVideo.length; i++) {
                    this.wrapVideoSimplePlayCtrl(jVideo.eq(i), opt);
                }
                return;
            }

            // iPhone 手机版过于二逼，所以直接显示原生的 control 好了
            var userAgent = window.navigator.userAgent;
            // alert(userAgent);
            var is_iPhone = userAgent.indexOf("iPhone OS") >= 0;
            if(is_iPhone) {
                // alert("fuck iphone!!!");
                jVideo.prop("controls", true);
                jVideo.attr("type", "video/mp4");
                jVideo.css({
                   "width"  : "100%",
                   "margin" : "0 auto"
                });  
                return;
            }

            // 来吧准备初始化配置
            opt = opt || {};
            this.setUndefined(opt, "autoFitHeight", false);
            this.setUndefined(opt, "watchClick", true);
            this.setUndefined(opt, "copyAttrs", ["pos","itfree","ithide","itfull","itshadow"]);

            // 创建包裹
            var jCon = $('<div class="z-video-con">').insertBefore(jVideo);
            jCon.append(jVideo);
            if(opt.autoFitHeight)
                jCon.css("height", "100%");
            if(opt.copyAttrs) {
                for(var i=0; i<opt.copyAttrs.length;i++) {
                    var key = opt.copyAttrs[i];
                    var val = jVideo.attr(key) || null;
                    jCon.attr(key, val);
                }
            }
            //................................................
            // 包裹一下控制层
            var jCtrl = $('<div class="z-video-ctrl">').appendTo(jCon);
            var jB    = $('<b>').appendTo(jCtrl);
            //................................................
            // 设置默认 Icon
            var icons = _.extend({
                    "pause"   : '<i class="zmdi zmdi-pause">',
                    "play"    : '<i class="zmdi zmdi-play">',
                    "loading" : '<i class="zmdi zmdi-spinner zmdi-hc-spin">',
                    "replay"  : '<i class="zmdi zmdi-replay">',
                }, opt.icons);

            // 先显示 Loading
            jCtrl.attr("st", "loading");
            jB.html(icons.loading);
            //................................................
            // 设置事件
            jVideo.on("canplay", function(){
                jCtrl.attr("st", "pause");
                jB.html(icons.play);
            });

            // 事件:暂停时
            jVideo.on("pause", function(){
                jCtrl.attr("st", "pause");
                jB.html(icons.play);
            });

            // 事件:播放时
            jVideo.on("playing", function(){
                jCtrl.attr("st", "playing");
                jB.html(icons.pause);
                
            });
            // 事件:完成时
            jVideo.on("ended", function(){
                jCtrl.attr("st", "ended");
                jB.html(icons.replay);
            });
            //................................................
            // 事件:点击播放
            if(opt.watchClick) {
                jCon.on("click", ".z-video-ctrl", function(){
                    var st = $(this).attr("st");

                    // 暂停 -> 播放
                    if("pause" == st) {
                        jVideo[0].play();
                    }
                    // 播放 -> 暂停
                    else if("playing" == st) {
                        jVideo[0].pause();
                    }
                    // 结束 -> 重播
                    else if("ended" == st) {
                        jVideo[0].currentTime = 0;
                        jVideo[0].play();
                    }
                });
            }
        },
        //.............................................
        // 返回当前时间
        currentTime: function (date) {
            if (typeof date == "number") {
                date = new Date(date);
            }
            date = date || new Date();
            return zUtil.dateToYYMMDD(date) + " " + zUtil.dateToHHMMSS(date, ":");
        },
        // 返回当前时分秒
        dateToYYMMDD: function (date, split) {
            date = date ? this.parseDate(date) : new Date();
            split = (split == null || split == undefined) ? "-" : split;
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var day = date.getDate();
            return year + split + zUtil.alignRight(month, 2, '0') + split + zUtil.alignRight(day, 2, '0');
        },
        // 返回当前年月日
        dateToHHMMSS: function (date, split) {
            date = date ? this.parseDate(date) : new Date();
            split = (split == null || split == undefined) ? "-" : split;
            var hours = date.getHours()
            var minutes = date.getMinutes();
            var seconds = date.getSeconds();
            return zUtil.alignRight(hours, 2, '0') + split + zUtil.alignRight(minutes, 2, '0') + split + zUtil.alignRight(seconds, 2, '0');
        },
        // 任何东西转换为字符串
        anyToString: function (obj) {
            if (_.isUndefined(obj) || _.isNull(obj)) {
                return "";
            }
            if (_.isString(obj)) {
                return obj;
            }
            if (_.isNumber(obj)) {
                return "" + obj;
            }
            if (_.isObject(obj)) {
                return zUtil.toJson(obj);
            }
            // TODO 补全其他类型
            zUtil.noImplement();
        },
        // 补全右边
        alignLeft: function (str, length, char) {
            str = zUtil.anyToString(str);
            if (str.length >= length) {
                return str;
            }
            return str + zUtil.dupString(char || ' ', length - str.length);
        },
        // 补全左边
        alignRight: function (str, length, char) {
            str = zUtil.anyToString(str);
            if (str.length >= length) {
                return str;
            }
            return zUtil.dupString(char || ' ', length - str.length) + str;
        },
        // 重复字符
        dupString: function (char, num) {
            if (!char || num < 1) {
                return "";
            }
            var str = "";
            for (var i = 0; i < num; i++) {
                str += char;
            }
            return str;
        },
        /**
         * 将一个字符串由驼峰式命名变成分割符分隔单词
         *
         * <pre>
         *  lowerWord("helloWorld", '-') => "hello-world"
         * </pre>
         *
         * @param cs
         *            字符串
         * @param c
         *            分隔符
         *
         * @return 转换后字符串
         */
        lowerWord: function (cs, c) {
            var c = c || "-";
            var sb = "";
            for (var i = 0; i < cs.length; i++) {
                var ch = cs.charAt(i);
                if (/^[A-Z]$/.test(ch)) {
                    if (i > 0)
                        sb += c;
                    sb += ch.toLowerCase();
                }
                else {
                    sb += ch;
                }
            }
            return sb;
        },
        /**
         * 将一个字符串某一个字符后面的字母变成大写，比如
         *
         * <pre>
         *  upperWord("hello-world", '-') => "helloWorld"
         * </pre>
         *
         * @param cs
         *            字符串
         * @param c
         *            分隔符
         *
         * @return 转换后字符串
         */
        upperWord: function (cs) {
            var c = c || "-";
            var sb = "";
            var len = cs.length;
            for (var i = 0; i < len; i++) {
                var ch = cs.charAt(i);
                if (ch == c) {
                    do {
                        i++;
                        if (i >= len)
                            return sb;
                        ch = cs.charAt(i);
                    } while (ch == c);
                    sb += ch.toUpperCase();
                }
                else {
                    sb += ch;
                }
            }
            return sb;
        },
        /**
         * 将字符串首字母大写
         *
         * @param s
         *            字符串
         * @return 首字母大写后的新字符串
         */
        upperFirst: function (s) {
            if (!s)
                return s;
            var c = s.charAt(0);
            if (/[a-z]/.test(c))
                return c.toUpperCase() + s.substring(1);
            return s;
        },
        /**
         * 将字符串首字母小写
         *
         * @param s
         *            字符串
         * @return 首字母小写后的新字符串
         */
        lowerFirst: function (s) {
            if (!s)
                return s;
            var c = s.charAt(0);
            if (/[A-Z]/.test(c))
                return c.toLowerCase() + s.substring(1);
            return s;
        },
        // 显示一个元素的尺寸，调试用
        _dumpSize: function (ele) {
            var jq = $(ele);
            console.log("height:", jq.height(), " out:", jq.outerHeight(), " inner:", jq.innerHeight());
            console.log("width:", jq.width(), " out:", jq.outerWidth(), " inner:", jq.innerWidth());
        },
        // 未实现
        noImplement: function () {
            throw new Error("Not implement yet!");
        },
        // 将字符串拆分，并无视空字符串
        splitIgnoreEmpty: function (str, separator) {
            var ss = str.split(separator || ",");
            var re = [];
            for (var i = 0; i < ss.length; i++) {
                var s = ss[i];
                if (s)
                    re.push(s);
            }
            return re;
        },
        // 将字符串拆分，并无视空字符串
        splitIgnoreBlank: function (str, separator) {
            var ss = str.split(separator || ",");
            var re = [];
            for (var i = 0; i < ss.length; i++) {
                var s = $.trim(ss[i]);
                if (s)
                    re.push(s);
            }
            return re;
        },
        /**
         * 获取文件主名。 即去掉后缀的名称
         *
         * @param path
         *            文件路径
         * @return 文件主名
         */
        getMajorName: function (path) {
            if (!path)
                return "";
            var len = path.length;
            var l = 0;
            var r = len;
            for (var i = r - 1; i > 0; i--) {
                if (r == len)
                    if (path[i] == '.') {
                        r = i;
                    }
                if (path[i] == '/' || path[i] == '\\') {
                    l = i + 1;
                    break;
                }
            }
            return path.substring(l, r);
        },
        /**
         * 获取文件后缀名，不包括 '.'，如 'abc.gif','，则返回 'gif'
         *
         * @param path
         *            文件路径
         * @return 文件后缀名
         */
        getSuffixName: function (path, forceLower) {
            if (!path)
                return "";
            var p0 = path.lastIndexOf('.');
            var p1 = path.lastIndexOf('/');
            if (-1 == p0 || p0 < p1)
                return "";
            var sfnm = path.substring(p0 + 1);
            return forceLower ? sfnm.toLowerCase() : sfnm;
        },
        /**
         * 获取文件后缀名，包括 '.'，如 'abc.gif','，则返回 '.gif'
         *
         * @param path
         *            文件路径
         * @return 文件后缀
         */
        getSuffix: function (path, forceLower) {
            if (!path)
                return "";
            var p0 = path.lastIndexOf('.');
            var p1 = path.lastIndexOf('/');
            if (-1 == p0 || p0 < p1)
                return "";
            var sfnm = path.substring(p0 + 1);
            return forceLower ? sfnm.toLowerCase() : sfnm;
        },
        //============== 计算文件大小
        sizeText: function (sz, fix) {
            sz  = parseInt(sz) || 0;
            fix = fix || 2;
            // KB
            var ckb = sz / 1024;
            if (ckb > 1024) {
                // MB
                var cmb = ckb / 1024;
                if (cmb > 1024) {
                    // GB
                    var cgb = cmb / 1024;
                    return (cgb == parseInt(cgb) ? cgb : cgb.toFixed(fix)) + " GB";
                }
                return (cmb == parseInt(cmb) ? cmb : cmb.toFixed(fix)) + " MB";
            }

            return (ckb == parseInt(ckb) ? ckb : ckb.toFixed(fix)) + " KB";
        },
        //.............................................
        // 评估一个密码，看看其强度如何，强度得分: 1-5
        // 空密码返回 0 
        // 长度不足，返回 -1
        // 错误密码，比如空白字符，或者不可打印字符，则返回 -2
        // 依次计算 charCode:
        // 33: [!]
        // 34: ["]
        // 35: [#]
        // 36: [$]
        // 37: [%]
        // 38: [&]
        // 39: [']
        // 40: [(]
        // 41: [)]
        // 42: [*]
        // 43: [+]
        // 44: [,]
        // 45: [-]
        // 46: [.]
        // 47: [/]
        // 48-57:  [0-9]
        // 58: [:]
        // 59: [;]
        // 60: [<]
        // 61: [=]
        // 62: [>]
        // 63: [?]
        // 64: [@]
        // 65-90:  [A-Z]
        // 91: [[]
        // 92: [\]
        // 93: []]
        // 94: [^]
        // 95: [_]
        // 96: [`]
        // 97-122: [a-z]
        // 123: [{]
        // 124: [|]
        // 125: [}]
        // 126: [~]
        evalPassword: function (str, minLen) {
            // 空密码
            if (!str)
                return 0;

            // 密码长度不足
            minLen = minLen || 6;
            if (str.length < minLen)
                return -1;

            var ss = [];
            ss[0] = 0;   // 数字
            ss[1] = 0;   // 大写字母
            ss[2] = 0;   // 小写字母
            ss[3] = 0;   // 特殊字符

            for (var i = 0; i < str.length; i++) {
                var c = str.charCodeAt(i);
                // 非法
                if (c < 33 || c > 126)
                    return -2;
                // 数字
                if (c >= 48 && c <= 57) {
                    ss[0] = 1;
                }
                // 大写字母
                else if (c >= 65 && c <= 90) {
                    ss[1] = 1;
                }
                // 小写字母
                else if (c >= 97 && c <= 122) {
                    ss[2] = 1;
                }
                // 特殊字符
                else {
                    ss[3] = 1;
                }
            }

            // 开始计分
            var re = str.length >= 8 ? 1 : 0;

            for (var i = 0; i < ss.length; i++) {
                re += ss[i];
            }

            // 返回
            return re;
        },
        //.............................................
        // 如果字符串溢出，把中间的内容表示为省略号，以便显示头尾
        ellipsisCenter: function (str, len) {
            if (str && str.length > len) {
                var n0 = parseInt(len / 2);
                var n1 = str.length - (len - n0);
                return str.substring(0, n0) + "..." + str.substring(n1);
            }
            return str;
        },
        formatJson: function (obj, depth) {
            var type = typeof obj;
            // 空对象
            if (null == obj && ("object" == type || 'undefined' == type || "unknown" == type)) return 'null';
            // 字符串
            if ("string" == type) return '"' + obj.replace(/(\\|\")/g, "\\$1").replace(/\n|\r|\t/g, function () {
                var a = arguments[0];
                return (a == '\n') ? '\\n' : (a == '\r') ? '\\r' : (a == '\t') ? '\\t' : "";
            }) + '"';
            // 布尔
            if ("boolean" == type) return obj ? "true" : "false";
            // 数字
            if ("number" == type) return obj;
            // 是否需要格式化
            var format = false;
            if (typeof depth == "number") {
                depth++;
                format = true;
            } else if (depth == true) {
                depth = 1;
                format = true;
            } else {
                depth = false;
            }
            // 数组
            if ($.isArray(obj)) {
                var results = [];
                for (var i = 0; i < obj.length; i++) {
                    var value = obj[i];
                    results.push(zUtil.formatJson(obj[i], depth));
                }
                return '[' + results.join(', ') + ']';
            }
            // 函数
            if ('function' == type) return '"function(){...}"';
            // 普通 JS 对象
            var results = [];
            // 需要格式化
            if (format) {
                // 判断一下，如果key少于3个，就不格式化了，并且，之内的所有元素都为 boolean, string,number
                var i = 0;
                for (var key in obj) {
                    if (++i > 2) {
                        format = true;
                        break;
                    }
                    var type = typeof obj[key];
                    if (type == "object") {
                        format = true;
                        break;
                    }
                }
                // 确定要格式化
                if (format) {
                    var prefix = "\n" + zUtil.dupString(INDENT_BY, depth);
                    for (key in obj) {
                        var value = obj[key];
                        if (value !== undefined) results.push(prefix + '"' + key + '" : ' + zUtil.formatJson(value, depth));
                    }
                    return '{' + results.join(',') + '\n' + zUtil.dupString(INDENT_BY, depth - 1) + '}';
                }
            } // 紧凑格式
            for (var key in obj) {
                var value = obj[key];
                if (value !== undefined) results.push('"' + key + '":' + zUtil.formatJson(value, depth));
            }
            return '{' + results.join(',') + '}';
        },
        //
        guid: function (sep) {
            function s4() {
                return Math.floor((1 + Math.random()) * 0x10000)
                    .toString(16)
                    .substring(1);
            }

            // return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            //     s4() + '-' + s4() + s4() + s4();
            sep = _.isString(sep) ? sep : "-";
            return [s4() + s4(), s4(), s4(), s4(), s4() + s4() + s4()].join(sep);
        }
    };  // ~ End Of zUtil

    // 感知平台
    var platform = navigator.platform.toLowerCase();
    zUtil.os = {
        mac: /^mac/.test(platform)
    };

    // 浏览器版本
    var ua = window.navigator.userAgent;
    zUtil.browser = {
        trident: ua.indexOf('Trident') > -1, //IE内核
        presto: ua.indexOf('Presto') > -1, //opera内核
        webKit: ua.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
        gecko: ua.indexOf('Gecko') > -1 && ua.indexOf('KHTML') == -1, //火狐内核
        mobile: !!ua.match(/AppleWebKit.*Mobile.*/), //是否为移动终端
        ios: !!ua.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
        android: ua.indexOf('Android') > -1 || ua.indexOf('Linux') > -1, //android终端或者uc浏览器
        iPhone: ua.indexOf('iPhone') > -1 || ua.indexOf('Mac') > -1, //是否为iPhone或者安卓QQ浏览器
        iPad: ua.indexOf('iPad') > -1, //是否为iPad
        webApp: ua.indexOf('Safari') == -1 ,//是否为web应用程序，没有头部与底部
        weixin: ua.match(/MicroMessenger/i) == "micromessenger" //是否为微信浏览器
    };

//..................................................
// 挂载到 window 对象
    window.NutzUtil = zUtil;
    window.$z = zUtil;

// TODO 支持 AMD | CMD
//===============================================================
    // if (typeof define === "function") {
    //     // CMD
    //     if (define.cmd) {
    //         define(function (require, exports, module) {
    //             module.exports = zUtil;
    //         });
    //     }
    //     // AMD
    //     else {
    //         define("zutil", [], function () {
    //             return zUtil;
    //         });
    //     }
    // }
    zUtil.defineModule("zutil", zUtil);
//===================================================================
})();