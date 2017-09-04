define(function (require, exports, module) {
//====================================================
// 采用统一的 ZUI.css
    require("ui/dateformat.js");
    seajs.use("nutz/theme/zui-" + window.$zui_theme + ".css");
//====================================================
    var parse_dom = function (html) {
        var UI = this;

        // 解析代码模板
        // html = html.replace(/[ ]*\r?\n[ ]*/g, "");
        // html = $z.tmpl(html)(UI._msg_map);
        html = UI.compactHTML(html);

        // 找到要加入 HTML 的位置并替换 HTML
        var jCon = UI.findDomParent();
        if (jCon && jCon.length > 0) {
            jCon.html(html);

            // 展开内嵌的 DOM 节点
            jCon.find('[hm-inner-html]').each(function () {
                var jq = $(this);
                var ph = jq.attr("hm-inner-html");
                seajs.use(ph, function (re) {
                    jq.html(re);
                });
            });
        }

        // 分析代码模板
        var map = UI._code_templates;
        var jTmpl = UI.findCodeTemplateDomNode.call(UI);
        if (jTmpl) {
            jTmpl.hide();
            var commonClass = jTmpl.attr("common-class");
            jTmpl.children('[code-id]').each(function () {
                var jq = $(this);
                if (commonClass && jq.attr("nocommon") != "true")
                    jq.addClass(commonClass);
                var key = jq.attr('code-id');
                map[key] = jq;
            });
        }

        // 分析 .ui-arena
        var jArena = UI.findArenaDomNode.call(UI);
        if (jArena && jArena.size() > 0) {
            UI.arena = jArena;
        } else {
            // 兼容旧版的查找方式
            // jArena = UI.$el.children('.ui-arena');
            // if (jArena.size() > 0)
            //     UI.arena = jArena;
        }


        // 标识所有的扩展点
        sign_gaskets(UI);

        // DOM 解析完成
    };
    var sign_gaskets = function (UI) {
        // 搜索所有的 DOM 扩展点, 为其增加前缀
        UI.$el.find("[ui-gasket]").each(function () {
            var jq = $(this);
            var ga = jq.attr("ui-gasket");
            // 已经被标识过了的话
            var m = /^([\w\d]+)@(.+)$/.exec(ga);
            if (m) {
                // 不是自己标识的，改成自己
                if (m[1] != UI.cid) {
                    jq.attr("ui-gasket", UI.cid + "@" + m[2]);
                }
            }
            // 没被标识过，标识一下
            else {
                jq.attr("ui-gasket", UI.cid + "@" + ga);
            }
        });
    };
//====================================================
    var register = function (UI) {
        var opt = UI.options;
        //....................................
        // 直接指明了 $el，那么需要进行替换
        if (UI.$el) {
            // 重新定义自己的父选区
            UI.$pel = UI.$el.parent();
            UI.pel = UI.$pel[0];
            UI.parent = UI.parent || ZUI.getInstance(UI.pel);
            // 这种模式下，默认是要 keepDom 的
            $z.setUndefined(UI, "keepDom", (_.isUndefined(opt.keepDom) || opt.keepDom));
            UI.keepDom = _.isUndefined(opt.keepDom)
                ? (UI.keepDom === false ? false : true)
                : opt.keepDom;
            // if(UI.keepDom)
            //     console.log("keepDom", UI.uiName)

            // 试图正确的获取 Arena
            // UI.arena = UI.$el.children('.ui-arena');
            // if(UI.arena.size() == 0)
            //     UI.arena = UI.$el;

            // 看看这个 $el 是不是已经是个 UI 了
            var cid = UI.$el.attr("ui-id");

            // 如果本身就是一个 UI，则试图注销它
            if (cid) {
                var oldUI = ZUI(cid);
                if (oldUI)
                    oldUI.destroy();
                UI.$el = null;
                UI.el = null;
            }
            // // 否则清空它
            // else if(!UI.keepDom){
            //     var jCon = UI.findDomParent();
            //     UI.$el.empty();
            // }
        }
        //....................................
        // 指定了 $pel 的话 ...
        else if (UI.$pel) {
            // 如果有 gasketName 那么就试图看看其所在 UI
            UI.parent = UI.parent || ZUI.getInstance(UI.$pel);
            // 如果这个元素带了 gasketName，则采用它
            var gnm = UI.$pel.attr("ui-gasket");
            var m = /^(\w+)@(\w+)$/.exec(gnm);
            opt.gasketName = m ? m[2] : gnm;
            // 如果配置里没指定父，那么就得手动来一下
            if (!opt.parent && UI.parent) {
                UI.parent.children.push(UI);
                UI.depth = UI.parent.depth + 1;
            }
        }
        //....................................
        // 没有 $pel 则需要寻找选区
        else if (opt.gasketName && UI.parent) {
            var selector = '[ui-gasket="' + UI.parent.cid + "@" + opt.gasketName + '"]';
            UI.$pel = UI.parent.$el.find(selector);
            // 从扩展的搜索范围里去寻找
            if (UI.$pel.length == 0 && UI.__elements.length > 0) {
                for (var i = 0; i < UI.__elements.length; i++) {
                    var $_ele = UI.__elements[i];
                    UI.$pel = $_ele.find(selector);
                    if (UI.$pel.size() > 0)
                        break;
                }
            }

            // 没找到是不能忍受的哦
            if (UI.$pel.length == 0) {
                throw $z.tmpl("fail to match gasket[{{gas}}] in {{pnm}}({{pid}})")({
                    gas: opt.gasketName,
                    pnm: UI.parent.uiName,
                    pid: UI.parent.cid
                });
            }
        }
        //....................................
        // 顶级的 UI 啦
        else {
            UI.parent = null;
            UI.$pel = $(document.body);
            UI.pel = document.body;
        }
        //....................................
        // 指明了扩展点的要记录哦，如果已经存在了 UI 就毁掉哦
        if (UI.parent && opt.gasketName) {
            var oldUI = UI.parent.gasket[opt.gasketName];
            if (oldUI)
                oldUI.destroy();
            UI.parent.gasket[opt.gasketName] = UI;
        }
        //....................................
        // 确保 $el 被创建
        if (!UI.$el) {
            UI.$el = $('<' + (opt.tagName || 'div') + '>');
            UI.el = UI.$el[0];
            //.....................................
            // 加载时保持隐藏
            UI.$el.attr("ui-loadding", "yes").css("visibility", "hidden");
            // 加入 DOM 树
            UI.$pel.append(UI.$el);
        }
        //.....................................
        // 记录自身实例
        UI.$el.attr("ui-id", UI.cid);
        ZUI.instances[UI.cid] = UI;

        // 记录顶级 UI
        if (!UI.parent) {
            window.ZUI.tops.push(UI);
        }
        //.....................................
    };
//====================================================
// 定义一个 UI 的原型
    var ZUIObj = function () {
    };
    ZUIObj.prototype = {
        nutz_ui_version: "1.3",
        //............................................
        __init__: function (options) {
            var UI = this;
            //............................... 保存配置项
            var opt = options || {};
            UI.options = opt;
            //............................... 必要的内部字段
            UI.uiKey = opt.uiKey;
            UI._code_templates = {};   // 代码模板
            UI._watch_keys = {};       // 键盘监听
            UI.gasket = {};            // 记录扩展点对应 UI
            //............................... 确定父子关系
            UI.parent = opt.parent;
            UI.children = [];
            if (UI.parent) {
                UI.parent.children.push(UI);
                UI.depth = UI.parent.depth + 1;
            }
            else {
                UI.depth = 0;
            }
            //............................... 确定 el
            if (opt.$el) {
                UI.el = opt.$el[0];
                UI.$el = opt.$el;
            }
            else if (opt.el) {
                UI.el = opt.el;
                UI.$el = $(opt.el);
            }
            //............................... 确定 pel
            if (opt.$pel) {
                UI.pel = opt.$pel[0];
                UI.$pel = opt.$pel;
            }
            else if (opt.pel) {
                UI.pel = opt.pel;
                UI.$pel = $(opt.pel);
            }

            //............................... 控件的通用方法
            $z.setUndefined(UI, "blur", function () {
                this.$el.removeAttr("actived");
                $z.invoke(this.$ui, "blur", [], this);
            });
            $z.setUndefined(UI, "active", function () {
                this.$el.attr("actived", "yes");
                $z.invoke(this.$ui, "active", [], this);
            });

            // 默认先用父类的多国语言字符串顶个先
            UI._msg_map = UI.parent ? UI.parent._msg_map : ZUI.g_msg_map;

            // 注册 UI 实例
            register(UI);

            //............................... 继承父UI的属性执行器
            UI.exec = opt.exec || (UI.parent || {}).exec;
            UI.app = opt.app || (UI.parent || {}).app;

            // 得到 UI 的事件
            var events = _.extend({}, UI.events, opt.events);

            // 监听事件
            if (UI.$el) {
                for (var key in events) {
                    var m = /^([^ ]+)[ ]+(.+)$/.exec(key);
                    if (null == m) {
                        throw "wrong events key: " + key;
                    }
                    // 将处理函数作为 data 传入事件， 以便调用的时候，让 UI 变成 this
                    UI.$el.on(m[1], m[2], events[key], function (e) {
                        e.data.apply(UI, [e]);
                    });
                }
            }

            // 确保自己设置了自定义的 className
            var isOptResetClassName = /^!/.test(opt.className);
            if (!isOptResetClassName && UI.className) {
                UI.$el.addClass(UI.className);
            }
            if (opt.className) {
                UI.$el.addClass(isOptResetClassName
                    ? opt.className.substring(1)
                    : opt.className);
            }

            // 调用子类自定义的 init
            $z.invoke(UI.$ui, "init", [opt], UI);

            // 触发初始化事件
            $z.invoke(opt, "on_init", [opt], UI);
            UI.trigger("ui:init", UI);
        },
        //............................................
        // 释放全部自己的子
        releaseAllChildren: function (forceRemoveDom) {
            var UI = this;
            // 释放掉自己所有的子
            var __children = UI.children ? [].concat(UI.children) : [];
            for (var i = 0; i < __children.length; i++) {
                __children[i].destroy(forceRemoveDom, true);
            }
            // 置空
            UI.children = [];
        },
        //............................................
        // 将自己加入另外一个 UI
        // target 可以是一个 DOM 节点，或者一个 string 表示 gasket
        appendTo: function (pui, target) {
            var UI = this;

            // 首先，将自己从父中去掉
            if (UI.parent != pui)
                UI.__remove_from_parent();

            // 然后加入到 pui 的子列表
            if (pui)
                pui.__append_child(UI);

            // 没有指定 target ...
            if (!target)
                return this;

            // 直接移动到某指定的元素下面
            var gasketName;
            if (_.isElement(target) || $z.isjQuery(target)) {
                UI.$pel = $(target);
                UI.pel = UI.$pel[0];
                var gnm = UI.$pel.attr("ui-gasket");
                var m = /^(\w+)@(\w+)$/.exec(gnm);
                gasketName = m ? m[2] : gnm;
            }
            // 指定了 gasketName
            else if (_.isString(target)) {
                gasketName = target;
                var selector = '[ui-gasket="' + pui.cid + "@" + gasketName + '"]';
                UI.$pel = pui.$el.find(selector);

                // 没找到是不能忍受的哦
                if (UI.$pel.length == 0) {
                    throw $z.tmpl("fail to match gasket[{{gas}}] in {{pnm}}({{pid}})")({
                        gas: gasketName,
                        pnm: UI.parent.uiName,
                        pid: UI.parent.cid
                    });
                }

                UI.pel = UI.$pel[0];
            }
            // 指定 target 非法
            else {
                throw "UI.appendTo() invalid target!!!";
            }

            // 确保自己移动到新的 pel 下面
            UI.$el.appendTo(UI.$pel);

            // 指定了 gasketName
            if (gasketName) {
                opt.gasketName = gasketName;
                var oldUI = pui.gasket[opt.gasketName];
                if (oldUI)
                    oldUI.destroy();
                pui.gasket[opt.gasketName] = UI;
            }

            // 返回自身以便链式赋值
            return this;
        },
        //............................................
        __append_child: function (childUI) {
            if (childUI.parent != this) {
                this.children.push(childUI);
                childUI.parent = this;
                childUI.depth = this.depth + 1;
            }
        },
        //............................................
        __remove_child: function (childUI) {
            var UI = this;
            UI.children = _.without(UI.children, childUI);
            for (var key in UI.gasket) {
                if (UI.gasket[key] === childUI) {
                    UI.gasket[key] = null;
                }
            }
            childUI.parent = null;
            UI.depth = 0;
        },
        //............................................
        __remove_from_parent: function () {
            if (this.parent) {
                this.parent.__remove_child(this);
            }
        },
        //............................................
        destroy: function (forceRemoveDom, dontNeedRemoveFromParent) {
            var UI = this;
            var opt = UI.options;

            // console.log("destroy UI:", UI.uiName, "::", UI.cid);

            // 调用更多的释放逻辑
            $z.invoke(UI.$ui, "depose", [], UI);
            $z.invoke(opt, "on_depose", [], UI);
            UI.trigger("ui:depose", UI);

            // 释放掉自己所有的子
            UI.releaseAllChildren(forceRemoveDom);

            // 移除自己在父节点的记录
            if (UI.parent && !dontNeedRemoveFromParent) {
                // UI.parent.children = UI.parent.children.filter(function(subUI){
                //     return subUI.cid != UI.cid;
                // });
                // UI.parent.children = _.without(UI.parent.children, UI);
                // if(opt.gasketName){
                //     delete UI.parent.gasket[opt.gasketName];
                // }
                UI.parent.__remove_child(UI);
            }

            // 注销 DOM 事件
            var events = _.extend({}, UI.events, opt.events);
            for (var key in events) {
                var m = /^([^ ]+)[ ]+(.+)$/.exec(key);
                if (m) {
                    UI.$el.off(m[1], m[2]);
                }
                ;
            }

            if (opt.dom_events) {
                for (var key in UI.options.dom_events) {
                    var m = /^([^ ]+)[ ]+(.+)$/.exec(key);
                    if (m) {
                        UI.$el.off(m[1], m[2]);
                    }
                }
            }

            // 停止监听其他 UI 的事件
            UI.stopListening();

            // 移除自己的监听的键盘和鼠标事件
            UI.unwatchKey();
            UI.unwatchMouse();

            // 移除 DOM 的事件监听
            $z.invoke(UI.$el, "undelegate", []);
            UI.$el.off();

            // 删除自己的 DOM 节点
            if (forceRemoveDom || !UI.keepDom) {
                UI.$el.remove();
            }

            // 移除注册
            delete ZUI.instances[UI.cid];
            // 移除 uiKey
            if (UI.uiKey) {
                delete ZUI._uis[UI.uiKey];
            }

            // 删除顶级节点记录
            var list = [];
            ZUI.tops.forEach(function (top) {
                if (top != UI)
                    list.push(top)
            }, UI);
            ZUI.tops = list;
        },
        //............................................
        // 渲染自身
        render: function (afterRender) {
            var UI = this;

            // 确定语言
            UI.lang = (UI.parent || {}).lang || window.$zui_i18n || "zh-cn";
            UI.theme = window.$zui_theme || "light";

            //============================================== i18n读完后的回调
            // 看看是否需要异步加载多国语言字符串
            var callback = function () {
                // 合并成一个语言集合
                for (var i = 0; i < arguments.length; i++) {
                    _.extend(UI._msg_map, arguments[i]);
                }
                // 用户自定义 redraw 执行完毕的处理
                var do_after_redraw = function () {
                    // if("ui.mask" == UI.uiName)
                    //     console.log("!!!!! do_after_redraw:", UI.uiName, UI._defer_uiTypes)
                    // 回调，延迟处理，以便调用者拿到返回值之类的
                    //window.setTimeout(function(){
                    // 触发事件
                    $z.invoke(UI.options, "on_redraw", [], UI);
                    UI.trigger("ui:redraw", UI);

                    // 确定 uiKey，并用其索引自身实例
                    if (UI.uiKey) {
                        if (ZUI.getByKey(UI.uiKey)) {
                            throw 'UI : uiKey="' + UI.uiKey + '" exists!!!';
                        }
                        ZUI._uis[UI.uiKey] = UI;
                        // 看看有没有要延迟监听的
                        var dl = ZUI._defer_listen[UI.uiKey];
                        if (dl) {
                            for (var i in dl) {
                                var dlo = dl[0];
                                dlo.context.listenUI(UI, dlo.event, dlo.handler);
                            }
                            delete ZUI._defer_listen[UI.uiKey];
                        }
                    }

                    // 让 UI 的内容显示出来
                    UI.$el.removeAttr("ui-loadding").css("visibility", "");

                    // 触发当前实例的绘制回调
                    if (typeof afterRender === "function") {
                        afterRender.call(UI);
                    }

                    // 准备绘制快速帮助
                    if (UI.options.quickTip && UI.options.quickTip.items.length > 0) {
                        var qtip = {items: []};
                        var alreadyPlayed = UI.local("quick_tip_played");
                        for (var i = 0; i < UI.options.quickTip.items.length; i++) {
                            var it = UI.options.quickTip.items[i];
                            qtip.items.push({
                                target: UI.arena.find(it.target),
                                text: UI.text(it.text),
                            });
                        }
                        // 默认回调
                        qtip.done = function () {
                            UI.confirm("quick_tip_already", {
                                ok: function () {
                                    UI.local("quick_tip_played", false)
                                },
                                cancel: function () {
                                    UI.local("quick_tip_played", true)
                                }
                            });
                        }
                        UI.__quick_tip = qtip;
                        // 从来没播放过，来一遍
                        if (!alreadyPlayed) {
                            UI.showQuickTip();
                        }
                    }

                    // 触发显示事件
                    $z.invoke(UI.options, "on_display", [], UI);
                    UI.trigger("ui:display", UI);

                    // 因为重绘了，看看有木有必要重新计算尺寸，这里用 setTimeout 确保 resize 是最后执行的指令
                    // TODO 这里可以想办法把 resize 的重复程度降低
                    window.setTimeout(function () {
                        UI.resize(true);
                    }, 0);
                    //}, 0);
                };
                // 定义后续处理
                var do_render = function () {
                    // 首先看看是否有增加 class
                    if (UI.options.arenaClass) {
                        UI.arena.addClass(UI.options.arenaClass);
                    }

                    // 调用UI的特殊重绘方法，如果方法返回了一组 ui 的类型，那么就表示
                    // 用户在方法里采用了异步还要加载这组 UI
                    // 加载完毕后，调用者需要主动在自己的 redraw 函数里，调用
                    //  UI.defer_report(i, uiType)
                    // 标识加载完成，待全部异步加载的 UI 完成后，会调才会调用 do_after_redraw
                    var uiTypes = $z.invoke(UI.$ui, "redraw", [], UI);
                    if (!uiTypes || uiTypes.length == 0) {
                        do_after_redraw();
                    }
                    // 哦？ 是异步的绘制，那么先存储一下回调，等待 UI 全部加载完再调用
                    else {
                        UI._defer_do_after_redraw = do_after_redraw;
                        UI._defer_uiTypes = [].concat(uiTypes);
                        UI._check_defer_done("redraw");
                    }

                    // 调用自定义的 dom 监听
                    if (UI.options.dom_events) {
                        for (var key in UI.options.dom_events) {
                            var m = /^([^ ]+)[ ]+(.+)$/.exec(key);
                            if (null == m) {
                                throw "wrong dom_events key: " + key;
                            }
                            var handler = UI.options.dom_events[key];
                            if (!_.isFunction(handler)) {
                                handler = UI[handler];
                            }
                            UI.$el.on(m[1], m[2], handler);
                        }
                    }

                    // 调用自定义的 UI 监听
                    if (UI.options.do_ui_listen) {
                        for (var key in UI.options.do_ui_listen) {
                            var m = /^([^ ]+)[ ]+(.+)$/.exec(key);
                            if (null == m) {
                                throw "wrong do_ui_listen key: " + key;
                            }
                            var handler = UI.options.do_ui_listen[key];
                            if (!_.isFunction(handler)) {
                                handler = UI[handler];
                            }
                            var taUI = "$parent" == m[1] ? UI.parent : m[1];
                            UI.listenUI(taUI, m[2], handler);
                        }
                    }
                    //console.log("do_render:", UI.uiName, UI._defer_uiTypes)
                };
                // 看看是否需要解析 DOM
                var uiDOM = UI.options.dom || UI.$ui.dom;
                if (uiDOM) {
                    // DOM 片段本身就是一段 HTML 代码  /*...*/ 包裹
                    if (/^(\/\*([^*]|[\r\n]|(\*+([^*\/]|[\r\n])))*\*+\/)$/.test(uiDOM)) {
                        parse_dom.call(UI, uiDOM.substring(2, uiDOM.length - 2));
                        do_render();
                    }
                    // DOM 片段就是一段 HTML 代码
                    else if (/^[ \t\n\r]*<.+>[ \t\n\r]*$/m.test(uiDOM)) {
                        parse_dom.call(UI, uiDOM);
                        do_render();
                    }
                    // DOM 片段存放在另外一个地址
                    else {
                        require.async(uiDOM, function (html) {
                            parse_dom.call(UI, html);
                            do_render();
                        });
                    }
                }
                // 否则直接渲染
                else {
                    sign_gaskets(UI);
                    do_render();
                }
            };
            //============================================== i18n
            var do_i18n = function () {
                // 采用父 UI 的字符串
                var uiI18N = $z.concat(UI.$ui.i18n, UI.options.i18n);
                // console.log(UI.uiName, "UI.$ui.i18n", UI.$ui.i18n);
                // console.log(UI.uiName, "UI.options.i18n", UI.$ui.i18n);

                // 存储多国语言字符串
                // 需要将自己的多国语言字符串与父 UI 的连接
                UI._msg_map = $z.inherit({}, UI.parent ? UI.parent._msg_map : ZUI.g_msg_map);

                // 找到需要加载的消息字符串
                var i18nLoading = [];
                for (var i = 0; i < uiI18N.length; i++) {
                    var it = uiI18N[i];
                    // 字符串的话，转换后加入待加载列表
                    if (_.isString(it)) {
                        i18nLoading.push($z.tmpl(it)({lang: UI.lang}));
                    }
                    // 对象的话，直接融合进来
                    else if (_.isObject(it)) {
                        _.extend(UI._msg_map, it);
                    }
                    // 其他的无视就好
                }

                // 不需要读取了
                if (i18nLoading.length == 0) {
                    callback();
                }
                // 异步加载 ...
                else {
                    //console.log(UI.uiName, "i18n", uiI18N);
                    require.async(i18nLoading, callback);
                }
            };
            //============================================== 从处理CSS开始
            // 加载 CSS
            var uiCSS = $z.concat(UI.$ui.css, UI.options.css);
            // 有 CSS 的话，需要对每个项目替换 theme
            if (uiCSS.length > 0) {
                for (var i = 0; i < uiCSS.length; i++) {
                    uiCSS[i] = UI.getCssTheme(uiCSS[i]);
                }
                seajs.use(uiCSS, do_i18n);
            }
            // 无 CSS
            else {
                do_i18n();
            }
            // 返回自身
            return UI;
        },
        //............................................
        // 汇报自己完成了哪个延迟加载的 UI
        defer_report: function (i, uiType) {
            var UI = this;
            if (!UI._loaded_uiTypes)
                UI._loaded_uiTypes = [];

            // 因为是延迟，所以放到执行队列最后执行
            window.setTimeout(function () {
                // console.log(" - defer", UI.uiName, i, uiType)
                if (UI._loaded_uiTypes
                    && UI._defer_uiTypes
                    && UI._defer_do_after_redraw) {
                    // 如果仅仅给了个 uiType，那么自动寻找 i
                    if (_.isString(i)) {
                        uiType = i;
                        i = UI._defer_uiTypes.indexOf(uiType);
                        if (i < 0) {
                            alert("defer uiType '" + uiType + "' without define!");
                            throw "defer uiType '" + uiType + "' without define!";
                        }
                    }
                    // 记录加载完的项目
                    UI._loaded_uiTypes[i] = uiType;
                    UI._check_defer_done();
                }
                // 已经被执行完了
                else {
                    // console.log(" - !!! ignore");
                }
            }, 0);
        },
        _check_defer_done: function () {
            var UI = this;
            if (UI._loaded_uiTypes
                && UI._defer_uiTypes
                && UI._defer_do_after_redraw) {
                if (UI._loaded_uiTypes.length == UI._defer_uiTypes.length) {
                    for (var i = 0; i < UI._loaded_uiTypes.length; i++) {
                        if (UI._loaded_uiTypes[i] != UI._defer_uiTypes[i]) {
                            return;
                        }
                    }
                    // 延迟调用
                    //window.setTimeout(function(){
                    if (UI._loaded_uiTypes
                        && UI._defer_uiTypes
                        && UI._defer_do_after_redraw) {
                        // console.log(" - defer done", UI.uiName)
                        UI._defer_do_after_redraw.call(UI);
                        delete UI._defer_uiTypes;
                        delete UI._loaded_uiTypes;
                        delete UI._defer_do_after_redraw;
                    }
                    //}, 0);
                }
            }
        },
        //............................................
        // 替换 css 路径中的 {{theme}} 占位符
        getCssTheme: function (cssPath) {
            return $z.tmpl(cssPath)({
                theme: this.theme
            });
        },
        //............................................
        isFitParent: function () {
            var UI = this;
            var opt = UI.options;
            return opt.fitparent === true
                || (opt.fitparent !== false && UI.arena.attr("ui-fitparent"));
        },
        //............................................
        // 修改 UI 的大小
        resize: function (deep) {
            var UI = this;
            var opt = UI.options;
            // 如果是选择自适应
            if (opt.fitself) {
                return;
            }

            // 如果还在加载中，也什么都不做
            if (UI.$el.attr("ui-loadding")) {
                return;
            }

            // 有时候，初始化的时候已经将自身加入父UI的gasket
            // 父 UI resize 的时候会同时 resize 子
            // 但是自己这时候还没有初始化完 DOM (异步加载)
            // 那么自己的 arena 就是未定义，因此不能继续执行 resize
            if (UI.arena) {
                // 需要调整自身，适应父大小
                // if (opt.fitparent === true
                //     || (opt.fitparent !== false && UI.arena.attr("ui-fitparent"))) {
                if (UI.isFitParent()) {
                    // 调整自身的顶级元素
                    var w, h;
                    if (this.pel === document.body) {
                        var winsz = $z.winsz();
                        w = winsz.width;
                        h = winsz.height;
                    } else {
                        w = UI.$pel.width();
                        h = UI.$pel.height();
                    }
                    // 得到自身顶级元素的左右边距
                    var margin = $z.margin(UI.$el);
                    UI.$el.css({
                        "width": w - margin.x,
                        "height": h - margin.y
                    });
                    margin = $z.margin(UI.arena);
                    UI.arena.css({
                        "width": UI.$el.width() - margin.x,
                        "height": UI.$el.height() - margin.y
                    });
                }

                // 调用自身的 resize
                $z.invoke(UI.$ui, "resize", [deep], UI);

                // 触发事件
                $z.invoke(opt, "on_resize", [], UI);
                UI.trigger("ui:resize", UI);

                // 调用自身所有的子 UI 的 resize
                if (deep) {
                    for (var i = 0; i < UI.children.length; i++) {
                        UI.children[i].resize(deep);
                    }
                }
            }
        },
        //............................................
        // 记录了更多的外部 DOM(jQuery包裹)，扩展点也会从这里面寻找
        // 仅仅是记录，控件销毁的时候并不会清空这些扩展点
        // 除非你在 depose 方法里编写逻辑
        __elements: [],
        addElement: function (jq) {
            this.__elements.push(jq);
        },
        //............................................
        // 监听一个事件
        // ui.watchKey(27, "ctrl", F(..))
        // ui.watchKey(27, ["ctrl","meta"], F(..))
        // ui.watchKey(27, F(..))
        /*
         对象格式
         ZUI.keymap = {
         "alt+shift+28" : {
         "c2" : [F(e), F(e)]
         }
         }
         */
        watchKey: function (which, keys, handler) {
            this.__watch_key_event(ZUI.keymap, which, keys, handler);
        },
        watchKeyUp: function (which, keys, handler) {
            this.__watch_key_event(ZUI.keyUpmap, which, keys, handler);
        },
        __watch_key_event: function (kmap, which, keys, handler) {
            // 没有特殊 key
            if (typeof keys == "function") {
                handler = keys;
                keys = undefined;
            }

            var key = this.__keyboard_watch_key(which, keys);
            //console.log("watchKey : '" + key + "' : " + this.cid);
            $z.pushValue(kmap, [key, this.cid], handler);

            // 记录到自身以便可以快速索引
            this._watch_keys[key] = true;
        },
        __keyboard_watch_key: function (which, keys) {
            // 直接就是字符串
            if (_.isString(which)) {
                return which;
            }
            // 组合的key
            if (_.isArray(keys)) {
                return keys.sort().join("+") + "+" + which;
            }
            // 字符串
            if (_.isString(keys)) {
                return keys + "+" + which;
            }
            // 没有特殊键
            return "" + which;
        },
        // 取消监听一个事件
        // ui.unwatchKey(27, "ctrl"})
        // ui.unwatchKey(27, ["ctrl","meta"])
        // ui.unwatchKey(27)
        unwatchKey: function (which, keys) {
            // 注销全部监听
            if (_.isUndefined(which)) {
                for (var key in this._watch_keys) {
                    var wkm = ZUI.keymap[key];
                    if (wkm && wkm[this.cid]) {
                        delete wkm[this.cid];
                    }
                }
                this._watch_keys = [];
                return;
            }
            // 注销指定事件监听
            var key = this.__keyboard_watch_key(which, key);
            // console.log("unwatchKey : '" + key + "' : " + this.cid);
            // 删除全局索引
            var wkm = ZUI.keymap[key];
            if (wkm && wkm[this.cid]) {
                delete wkm[this.cid];
            }
            // 删除自身的快速索引
            if (this._watch_keys[key])
                delete this._watch_keys[key];
        },
        // 监听全局鼠标事件
        watchMouse: function (eventType, handler) {
            $z.pushValue(ZUI.mousemap, [eventType, this.cid], handler);
        },
        // 取消全局鼠标事件监听
        unwatchMouse: function (eventType) {
            // 去掉所有的事件监听
            if (!eventType) {
                for (var key in ZUI.mousemap) {
                    this.unwatchMouse(key);
                }
            }
            // 去掉指定事件
            else {
                var wmm = ZUI.mousemap[eventType];
                if (wmm && wmm[this.cid])
                    delete wmm[this.cid];
            }
        },
        // 对 HTML 去掉空格等多余内容，并进行多国语言替换
        compactHTML: function (html, msgMap) {
            return $z.compactHTML(html, msgMap || this._msg_map, this.tmplSettings);
        },
        // 得到多国语言字符串
        msg: function (str, ctx, msgMap) {
            if (!str || !_.isString(str))
                return str;
            // 消息字符串是 "e.com.xxx : some reasone" 格式的
            var key = str;
            var reason = "";
            var pos = key.indexOf(':');
            if (pos > 0) {
                key = $.trim(str.substring(0, pos));
                reason = str.substring(pos);
            }
            var re = $z.getValue((msgMap || this._msg_map), key);
            if (!re) {
                return str;
            }
            // 得到了字符串
            if (_.isString(re)) {
                // 需要解析
                if (re && ctx && _.isObject(ctx)) {
                    re = ($z.tmpl(re))(ctx);
                }
                return re + reason;
            }
            // 否则可能是个对象，表示一个子 MsgMap
            return re;
        },
        // 得到多国语言字符串，如果没有返回默认值，如果没指定默认值，返回空串 ("")
        str: function (key, dft) {
            if (/^i18n:.+$/g.test(key)) {
                key = key.substring(5);
            }
            var re = $z.getValue(this._msg_map, key);
            return re || dft || "";
        },
        // 对一个字符串进行文本转移，如果是 i18n: 开头采用 i18n
        text: function (str, ctx, msgMap) {
            // // 多国语言
            // if (/^i18n:.+$/g.test(str)) {
            //     var key = str.substring(5);
            //     return this.msg(key, ctx, msgMap);
            // }
            // // 字符串模板
            // if (str && ctx && _.isObject(ctx)) {
            //     return ($z.tmpl(str))(ctx);
            // }
            // // 普通字符串
            // return str;
            var key = str;
            if (/^i18n:.+$/g.test(str)) {
                key = str.substring(5);
            }
            return this.msg(key, ctx, msgMap);
        },
        // 对于控件 DOM 中所有的元素应用 data-balloon 的设定
        // 查找属性 "balloon" 格式是 "方向:msgKey"
        // selector 如果不给，默认是 "*"
        balloon: function (selector, enabled) {
            var UI = this;

            if (false === selector) {
                selector = "*";
                enabled = false;
            }
            if (_.isUndefined(enabled))
                enabled = true;

            // 得到选区
            var jq = UI.$el;
            if (_.isString(selector))
                jq = jq.find(selector);
            else if (_.isElement(selector) || $z.isjQuery(selector))
                jq = $(selector);

            // 启用
            if (enabled) {
                jq.find('[balloon]').each(function () {
                    var me = $(this);
                    var m = /^((up|down|left|right)(:(small|medium|large|xlarge|fit))?:)?(.*)/.exec(me.attr("balloon"));
                    me.attr({
                        "data-balloon": UI.msg($.trim(m[5])),
                        "data-balloon-pos": $.trim(m[2]) || null,
                        "data-balloon-length": $.trim(m[4]) || null,
                    });
                });
            }
            // 取消
            else {
                jq.find('[balloon]').each(function () {
                    $(this).attr({
                        "data-balloon": null,
                        "data-balloon-pos": null,
                        "data-balloon-length": null,
                    });
                });
            }
        },
        // 在某区域显示读取中，如果没有指定区域，则为整个 arena
        showLoading: function (selector) {
            var html = '<div class="ui-loading">';
            html += '<i class="fa fa-spinner fa-pulse"></i> <span>' + this.msg("loading") + '</span>';
            html += '</div>';
            var rect = $z.rect(this.$el);
            $(html).appendTo(this.$el).css(_.extend({
                "position": "fixed",
            }, $z.rectObj(rect, "top,left,width,height")));
        },
        hideLoading: function () {
            this.$el.find(".ui-loading").remove();
        },
        // 根据路径获取一个子 UI
        subUI: function (uiPath) {
            var UI = this;
            var ss = uiPath.split(/[\/\\.]/);
            for (var i = 0; i < ss.length; i++) {
                var s = ss[i];
                UI = UI.gasket[s];
                if (!UI)
                    return null;
            }
            return UI;
        },
        // 释放某个子 UI
        releaseSubUI: function (uiPath) {
            var sub = this.subUI(uiPath);
            if (sub)
                sub.destroy();
        },
        // 快捷方法，帮助 UI 存储本地状态
        // 需要设置 "app" 段
        // 参数 appName 默认会用 app.name 来替代
        local: function (key, val, appName) {
            var UI = this;
            var app = UI.app;
            if (!app || !app.session || !app.session.me) {
                throw "UI.local need 'app.session.me'";
            }
            return $z.local(appName || app.name, app.session.me, key, val);
        },
        // 字段显示方式可以是模板或者回调，这个函数统一成一个方法
        eval_tmpl_func: function (obj, nm) {
            var F = obj ? obj[nm] : null;
            if (!F)
                return null;
            return _.isFunction(F) ? F : $z.tmpl(F);
        },
        //............................................
        ui_parse_data: function (obj, callback) {
            var UI = this;
            var opt = UI.options;
            var context = opt.context || UI;
            // 同步
            if (_.isFunction(opt.parseData)) {
                var o = opt.parseData.call(context, obj, UI);
                callback.call(UI, o, opt);
            }
            // 异步
            else if (_.isFunction(opt.asyncParseData)) {
                opt.asyncParseData.call(context, obj, function (o) {
                    callback.call(UI, o, opt);
                }, UI);
            }
            // 直接使用
            else {
                callback.call(UI, obj, opt);
            }
        },
        //............................................
        ui_format_data: function (callback) {
            var UI = this;
            var opt = UI.options;
            var obj = callback.call(UI, opt);
            if (_.isFunction(opt.formatData)) {
                var context = opt.context || UI;
                return opt.formatData.call(context, obj, UI);
            }
            return obj;
        },
        //............................................
        // 监听本 UI 的模块事件
        listenSelf: function (event, handler) {
            this._listen_to(this, event, handler);
        },
        // 监听本 UI 的父UI事件
        listenParent: function (event, handler) {
            this._listen_to(this.parent, event, handler);
        },
        // 监听某个 UI 的事件
        listenUI: function (uiKey, event, handler) {
            // 给的就是一个 UI 实例，那么直接监听了
            if (uiKey.nutz_ui_version) {
                this._listen_to(uiKey, event, handler);
            }
            // 监听自己的父
            else if ("$parent" == uiKey) {
                this._listen_to(this.parent, event, handler);
            }
            // 否则看看是不是需要推迟建立
            else {
                var taUI = ZUI.getByKey(uiKey);
                // 不需要推迟
                if (taUI) {
                    this._listen_to(taUI, event, handler);
                }
                // 暂存
                else {
                    var dl = ZUI._defer_listen[uiKey];
                    if (!dl) {
                        dl = [];
                        ZUI._defer_listen[uiKey] = dl;
                    }
                    dl.push({
                        event: event,
                        handler: handler,
                        context: this
                    });
                }
            }
        },
        // 监听某个 backbone 的模块消息
        _listen_to: function (target, event, handler) {
            if (target) {
                // 如果给的是一个字符串，那么就表示当前对象的一个方法
                // 支持 . 访问子对象
                if (_.isString(handler)) {
                    handler = $z.getValue(this, handler);
                }
                this.listenTo(target, event, handler);
            }
        },
        // 监听父UI的事件
        // 返回一个对应的 DOM 节点的克隆
        ccode: function (codeId) {
            var jq = this._code_templates[codeId];
            if (jq)
                return jq.clone().removeAttr("code-id");
            throw "Can not found code-template '" + codeId + "'";
        },
        //...................................................................
        ajaxReturn: function (re, option, context) {
            var UI = this;
            context = context || UI;
            if (_.isString(re)) {
                re = $z.fromJson(re);
            }
            // 格式化 callback
            if (_.isFunction(option)) {
                option = {
                    success: option
                }
            }
            // 如果失败了
            if (!re.ok) {
                console.warn(UI.msg(re.errCode) + "\n\n" + re.msg);
                return $z.doCallback(option.fail, [re], context);
            }
            // 如果成功了
            return $z.doCallback(option.success, [re.data], context);
        },
        //...................................................................
        // 提供一个通用的文件上传界面，任何 UI 可以通过
        //   this.listenModel("do:upload", this.on_do_upload);
        // 来启用这个方法
        // !!! 这个方法将被删除
        on_do_upload: function (options) {
            //var Mask     = require("ui/mask/mask");
            //var Uploader = require("ui/uploader/uploader");
            require.async(['ui/uploader/uploader', 'ui/mask/mask'], function (Uploader, Mask) {
                new Mask({
                    closer: true,
                    escape: true,
                    width: 460,
                    height: 500
                }).render(function () {
                    new Uploader({
                        parent: this,
                        gasketName: "main",
                        target: "id:" + options.target.id
                    }).render();
                });
            });
        },
        //...................................................................
        showQuickTip: function (showParentAsDefault) {
            if (this.__quick_tip) {
                $z.markIt(this.__quick_tip);
            }
            else if (showParentAsDefault && this.parent) {
                this.parent.showQuickTip(showParentAsDefault);
            }
        },
        // 使用HTML5中的Web Notification桌面通知
        notification: function (opt) {
            if (window.Notification) {
                var popNotice = function () {
                    if (Notification.permission == "granted") {
                        var notification = new Notification(opt.title, {
                            body: opt.body,
                            icon: opt.icon
                        });
                        // onclick
                        if (opt.onclick) {
                            notification.onclick = opt.onclick;
                        }
                    }
                };
                if (Notification.permission == "granted") {
                    popNotice();
                } else if (Notification.permission != "denied") {
                    Notification.requestPermission(function (permission) {
                        popNotice();
                    });
                }
            } else {
                alert('浏览器不支持Notification');
            }
        },
        //...................................................................
        // 提供一个通用的确认对话框
        // opt.ok     : {c}F(str)
        // opt.cancel : {c}F()
        confirm: function (msgKey, opt) {
            var UI = this;

            // 分析参数
            if (_.isFunction(opt)) {
                opt = {ok: opt};
            }
            opt = opt || {};
            $z.setUndefined(opt, "width", 400);
            $z.setUndefined(opt, "height", 240);
            $z.setUndefined(opt, "title", "confirm");
            $z.setUndefined(opt, "btnYes", "yes");
            $z.setUndefined(opt, "btnNo", "no");
            $z.setUndefined(opt, "icon", '<i class="zmdi zmdi-help-outline"></i>');

            // 显示遮罩层
            seajs.use("ui/mask/mask", function (MaskUI) {
                new MaskUI(_.extend({}, opt, {
                    css: 'ui/pop/theme/pop-{{theme}}.css',
                    dom: "ui/pop/pop.html",
                    arenaClass: "pop-msg pop-confirm",
                    events: {
                        "click .pm-btn-ok": function () {
                            this.close();
                            $z.invoke(opt, "ok", [], opt.context || this);
                        },
                        "click .pm-btn-cancel": function () {
                            this.close();
                            $z.invoke(opt, "cancel", [], opt.context || this);
                        }
                    }
                })).render(function () {
                    // 设置标题
                    if (opt.title)
                        this.$main.find(".pm-title").html(UI.msg(opt.title));
                    else
                        this.$main.find(".pm-title").remove();

                    // 设置按钮文字
                    this.$main.find(".pm-btn-ok").html(UI.msg(opt.btnYes));
                    this.$main.find(".pm-btn-cancel").html(UI.msg(opt.btnNo)).focus();

                    // 设置显示内容
                    var html = '';
                    if (opt.icon) {
                        html += '<div class="pop-msg-icon">' + opt.icon + '</div>';
                    }
                    html += '<div class="pop-msg-text">' + UI.msg(msgKey) + '</div>';
                    this.$main.find(".pm-body").html(html);
                })
            });
        },
        //...................................................................
        // 提供一个通用的确认对话框
        // opt.callback : {c}F(str)
        alert: function (msgKey, opt) {
            var UI = this;
            opt = opt || {};

            // 分析参数
            if (_.isFunction(opt)) {
                callback = opt;
                opt = {
                    callback: callback
                };
            }
            // 快捷图标
            else if (_.isString(opt)) {
                opt = {icon: opt};
            }
            // 设置图标
            if ("warn" == opt.icon)
                opt.icon = '<i class="zmdi zmdi-alert-triangle"></i>';
            else if ("error" == opt.icon)
                opt.icon = '<i class="zmdi zmdi-alert-polygon"></i>';
            else if ("notify" == opt.icon)
                opt.icon = '<i class="zmdi zmdi-notifications-active"></i>';
            
            $z.setUndefined(opt, "width", 400);
            $z.setUndefined(opt, "height", 240);
            $z.setUndefined(opt, "title", "info");
            $z.setUndefined(opt, "btnOk", "ok");
            $z.setUndefined(opt, "icon", '<i class="zmdi zmdi-info"></i>');

            // 显示遮罩层
            seajs.use("ui/mask/mask", function (MaskUI) {
                new MaskUI(_.extend({}, opt, {
                    css: 'ui/pop/theme/pop-{{theme}}.css',
                    dom: "ui/pop/pop.html",
                    arenaClass: "pop-msg pop-info",
                    events: {
                        "click .pm-btn-ok": function () {
                            this.close();
                            $z.invoke(opt, "callback", [], opt.context || this);
                        }
                    }
                })).render(function () {
                    // 设置标题
                    if (opt.title)
                        this.$main.find(".pm-title").html(UI.text(opt.title));
                    else
                        this.$main.find(".pm-title").remove();

                    // 设置按钮文字
                    this.$main.find(".pm-btn-ok").html(UI.text(opt.btnOk)).focus();
                    this.$main.find(".pm-btn-cancel").remove();

                    // 设置显示内容
                    var html = '';
                    if (opt.icon) {
                        html += '<div class="pop-msg-icon">' + opt.icon + '</div>';
                    }
                    html += '<div class="pop-msg-text">' + UI.text(msgKey) + '</div>';
                    this.$main.find(".pm-body").html(html);
                })
            });
        },
        //...................................................................
        // 短暂时间提示
        // opt.callback : {c}F(str)
        toast: function (msgKey, opt) {
            var UI = this;

            // 分析参数
            if (_.isFunction(opt)) {
                callback = opt;
                opt = {
                    callback: callback
                };
            }
            // 快捷图标
            else if (_.isString(opt)) {
                var opt2 = {};
                if ("warn" == opt)
                    opt2.icon = '<i class="zmdi zmdi-alert-triangle"></i>';
                else if ("error" == opt)
                    opt2.icon = '<i class="zmdi zmdi-alert-polygon"></i>';
                else if ("notify" == opt)
                    opt2.icon = '<i class="zmdi zmdi-notifications-active"></i>';
                opt = opt2;
            }

            opt = opt || {};
            $z.setUndefined(opt, "time", 1000);
            $z.setUndefined(opt, "width", 400);
            $z.setUndefined(opt, "height", 240);
            $z.setUndefined(opt, "title", "info");
            $z.setUndefined(opt, "btnOk", "ok");
            $z.setUndefined(opt, "icon", '<i class="zmdi zmdi-info"></i>');
            if (opt.time < 0) {
                opt.closer = false;
            }

            // 显示遮罩层
            seajs.use("ui/mask/mask", function (MaskUI) {
                new MaskUI(_.extend({}, opt, {
                    css: 'ui/pop/theme/pop-{{theme}}.css',
                    dom: "ui/pop/pop.html",
                    arenaClass: "pop-msg pop-info"
                })).render(function () {
                    var self = this;
                    // 设置标题
                    if (opt.title)
                        this.$main.find(".pm-title").html(UI.msg(opt.title));
                    else
                        this.$main.find(".pm-title").remove();

                    // 设置按钮文字
                    this.$main.find(".pm-btn-ok").remove();
                    this.$main.find(".pm-btn-cancel").remove();

                    // 设置显示内容
                    var html = '';
                    if (opt.icon) {
                        html += '<div class="pop-msg-icon">' + opt.icon + '</div>';
                    }
                    html += '<div class="pop-msg-text">' + UI.msg(msgKey) + '</div>';
                    this.$main.find(".pm-body").html(html);

                    // 不限时间
                    if (opt.time < 0) {
                        $z.invoke(opt, "callback", [], opt.context || self);
                    } else {
                        setTimeout(function () {
                            self.close();
                            $z.invoke(opt, "callback", [], opt.context || self);
                        }, opt.time);
                    }
                })
            });
        },
        //...................................................................
        // 一个提示输入框
        // opt.ok     : {c}F(str)
        // opt.cancel : {c}F()
        // opt.check  : {c}F(str, callback(ErrMsg))
        prompt: function (msgKey, opt) {
            var UI = this;

            // 支持直接给定 ok 的回调
            if (_.isFunction(opt)) {
                opt = {ok: opt};
            }

            // 分析参数
            opt = opt || {};
            $z.setUndefined(opt, "width", 400);
            $z.setUndefined(opt, "height", 240);
            $z.setUndefined(opt, "title", "prompt");
            $z.setUndefined(opt, "placeholder", "");
            $z.setUndefined(opt, "btnOk", "ok");
            $z.setUndefined(opt, "btnCancel", "cancel");
            $z.setUndefined(opt, "icon", '<i class="zmdi zmdi-keyboard"></i>');
            $z.setUndefined(opt, "iconWarn", '<i class="zmdi zmdi-alert-triangle"></i>');
            $z.setUndefined(opt, "iconOk", '<i class="zmdi zmdi-check-circle"></i>');
            $z.setUndefined(opt, "iconIng", '<i class="fa fa-spinner fa-pulse"></i>');
            $z.setUndefined(opt, "format", function (str) {
                return str;
            });

            // 准备确认的逻辑
            var on_ok = function () {
                var uiMask = this;
                if ("warn" != uiMask.$main.find(".pm-body").attr("mode")) {
                    var context = opt.context || this;
                    var str = $.trim(this.$main.find(".pmp-input input").val());
                    if (str)
                        str = opt.format(str);
                    // 关闭对话框
                    uiMask.close();
                    // 调用回调
                    window.setTimeout(function () {
                        $z.invoke(opt, "ok", [str], opt.context || this);
                    }, 100);
                }
                // 直接关闭
                else {
                    uiMask.close();
                }
            };

            // 显示遮罩层
            seajs.use("ui/mask/mask", function (MaskUI) {
                new MaskUI(_.extend({}, opt, {
                    css: 'ui/pop/theme/pop-{{theme}}.css',
                    dom: "ui/pop/pop.html",
                    arenaClass: "pop-msg pop-info",
                    events: {
                        // 确认
                        "keydown .pmp-input input": function (e) {
                            if (13 == e.which) {
                                on_ok.call(this);
                            }
                        },
                        "click .pm-btn-ok": on_ok,
                        // 取消
                        "click .pm-btn-cancel": function () {
                            this.close();
                            $z.invoke(opt, "cancel", [], opt.context || this);
                        },
                        // 执行检查
                        "input .pmp-input input": function () {
                            var uiMask = this;
                            var context = opt.context || this;
                            if (_.isFunction(opt.check)) {
                                var str = $.trim(this.$main.find(".pmp-input input").val());
                                if (str)
                                    str = opt.format(str);
                                this.$main.find(".pop-msg-icon").html(opt.iconIng);
                                opt.check.call(context, str, function (err) {
                                    // 显示错误信息
                                    if (err) {
                                        uiMask.$main.find(".pm-body").attr("mode", "warn")
                                            .find(".pop-msg-icon")
                                            .html(opt.iconWarn);
                                        uiMask.$main.find(".pmp-warn").text(UI.msg(err));
                                    }
                                    // 正常回复默认 ICON
                                    else {
                                        uiMask.$main.find(".pm-body").attr("mode", null)
                                            .find(".pop-msg-icon")
                                            .html(opt.icon);
                                        uiMask.$main.find(".pmp-warn").empty();
                                    }
                                });
                            }
                        }
                    }
                })).render(function () {
                    // 设置标题
                    if (opt.title)
                        this.$main.find(".pm-title").html(UI.msg(opt.title));
                    else
                        this.$main.find(".pm-title").remove();

                    // 设置按钮文字
                    this.$main.find(".pm-btn-ok").html(UI.msg(opt.btnOk));
                    this.$main.find(".pm-btn-cancel").html(UI.msg(opt.btnCancel));
                    ;

                    // 设置显示内容
                    var html = '';
                    if (opt.icon) {
                        html += '<div class="pop-msg-icon">' + opt.icon + '</div>';
                    }
                    html += '<div class="pop-msg-prompt">'
                    html += '<div class="pmp-text">' + UI.msg(msgKey) + '</div>';
                    html += '<div class="pmp-input"><input spellcheck="false"></div>';
                    html += '<div class="pmp-warn"></div>';
                    html += '</div>';
                    this.$main.find(".pm-body").html(html);

                    if (opt.placeholder) {
                        this.$main.find(".pmp-input input").attr({
                            "placeholder": UI.msg(opt.placeholder)
                        });
                    }

                    this.$main.find(".pmp-input input").focus();
                })
            });
        }
        //...................................................................
    };

// ZUI 就是一个处理方法 
    var ZUI = function (arg0, arg1) {
        // 定义
        if (_.isString(arg0) && _.isObject(arg1)) {
            return ZUI.def(arg0, arg1);
        }
        // 获取实例
        else if (_.isElement(arg0) || (arg0 instanceof jQuery)) {
            return arg1 ? ZUI.checkInstance(arg0)
                : ZUI.getInstance(arg0);
        }
        // 根据 uiKey
        else if (_.isString(arg0)) {
            return arg1 ? ZUI.checkByKey(arg0) || ZUI.checkByCid(arg0)
                : ZUI.getByKey(arg0) || ZUI.getByCid(arg0);
        }
        // 未知处理
        throw "Unknown arg0 : " + arg0 + ", arg1" + arg1;
    };
// 初始化 ZUI 工厂类对象
    /*这些字段用来存放 UI 相关的运行时数据*/
    ZUI.keymap = {
        /*
         "alt+shift+28" : {
         "c2" : [F(e), F(e)..]
         }
         */
    };
    ZUI.keyUpmap = {
        /*
         "alt+shift+28" : {
         "c2" : [F(e), F(e)..]
         }
         */
    };
    ZUI.mousemap = {
        /*
         click: {
         "c2" : [F(e), F(e)..]
         }
         */
    };
    ZUI.tops = [];
    ZUI.definitions = {};
    ZUI.instances = {};  // 根据cid索引的 UI 实例
    ZUI._uis = {};       // 根据键值索引的 UI 实例，没声明 key 的 UI 会被忽略
    ZUI.__CID = 0;       // 统一的 UI 计数器

// 如果监听一个 UI 的键值，但是这个 UI 的实例因为异步还没有被加载
// 那么，先暂存到这个属性里，当 UI 实例被加载完毕了，会自动应用这个监听的
    ZUI._defer_listen = {}

// 这个函数用来定义一个 UI 模块，返回一个 Backbone.View 的类用来实例化
    ZUI.def = function (uiName, conf) {
        var uiDef = this.definitions[uiName];
        if (!uiDef) {
            // 准备配置对象的默认属性
            var uiBaseObj = {
                uiName: uiName,
                tagName: 'div',
                className: uiName.replace(/[.]/g, '-'),
                $ui: {}
            };
            // TODO zozoh@161113 这个逻辑分钟木用了吧，应该删了
            // FIXME pw@161115 有用，删了的话wedit，wjson报错
            // pkg信息补全css，dom, i18n
            if (conf.pkg) {
                // i18n 加载一个即可
                conf.i18n = conf.i18n || _.template("ui/{{pkg}}/i18n/{{lang}}.js")({pkg: conf.pkg, lang: "{{lang}}"});
                // dom
                conf.dom = conf.dom || _.template("ui/{{pkg}}/{{pkg}}.html")({pkg: conf.pkg});
                // TODO 这里还需要再讨论下
                // if (conf.vue) { // 使用了vue的话，也就是直接加载html即可，无需经过转换
                //     conf.dom = [conf.dom, _.template("ui/{{pkg}}/{{pkg}}_vue.html")({pkg: conf.pkg})];
                // }
                // css 可以追加到当前的配置中
                var dftcss = _.template("ui/{{pkg}}/{{pkg}}.css")({pkg: conf.pkg});
                if (conf.css) {
                    if (_.isArray(conf.css)) {
                        conf.css.push(dftcss);
                    } else {
                        conf.css = [conf.css, dftcss];
                    }
                } else {
                    conf.css = dftcss;
                }
            }
            // 将 UI 的保留方法放入 $ui 中，其余 copy
            for (var key in conf) {
                if (/^(css|dom|i18n|init|redraw|depose|resize|active|blur)$/g.test(key)) {
                    uiBaseObj.$ui[key] = conf[key];
                }
                else if ("className" == key) {
                    // 重置
                    if (/^!/.test(conf.className)) {
                        uiBaseObj.className = conf.className.substring(1);
                    }
                    // 附加
                    else {
                        uiBaseObj.className += " " + conf.className;
                    }
                }
                else {
                    uiBaseObj[key] = uiBaseObj[key] || conf[key];
                }
            }
            // 定义了默认的获取 DOM 插入点的方法
            $z.setUndefined(uiBaseObj, "findDomParent", function () {
                if (!this.keepDom)
                    return this.$el;
            });
            // 定义了默认的获取 arena 方法
            $z.setUndefined(uiBaseObj, "findArenaDomNode", function () {
                return this.$el.children('.ui-arena');
            });
            // 定义了默认获取 code-template 的方法
            $z.setUndefined(uiBaseObj, "findCodeTemplateDomNode", function () {
                return this.$el.children('.ui-code-template');
            });

            // 定义新 UI
            uiDef = function (options) {
                // 建立自己的 ID
                this.cid = "view" + (ZUI.__CID++);
                // 加入 Backbone 的事件支持
                _.extend(this, Backbone.Events);
                // 调用初始化方法
                this.__init__(options);
            };
            uiDef.prototype = _.extend(uiBaseObj, new ZUIObj());
            uiDef.uiName = uiName;

            // 缓存上这个定义
            this.definitions[uiName] = uiDef;
        }
        // 返回
        return uiDef;
    };

// 根据任何一个 DOM 元素，获取其所在的 UI 对象
    ZUI.getInstance = function (el) {
        var jq = $(el);
        var jui = jq.closest("[ui-id]");
        if (jui.size() == 0) {
            return null;
        }
        var cid = jui.attr("ui-id");
        return this.getByCid(cid);
    };
    ZUI.checkInstance = function (el) {
        var jq = $(el);
        var jui = jq.closest("[ui-id]");
        if (jui.size() == 0) {
            if (console && console.warn)
                console.warn(el);
            throw "Current DOMElement no belone to any UI: " + jq[0].outerHTML;
        }
        var cid = jui.attr("ui-id");
        return this.checkByCid(cid);
    };

// 根据 cid 获取 UI 的实例 
    ZUI.getByCid = function (cid) {
        return this.instances[cid];
    };
    ZUI.checkByCid = function (cid) {
        var UI = this.getByCid(cid);
        if (!UI)
            throw "UI instances 'cid=" + cid + "' no exists!";
        return UI;
    };

// 根据 uiKey 获取 UI 的实例 
    ZUI.getByKey = function (uiKey) {
        return this._uis[uiKey];
    };
    ZUI.checkByKey = function (uiKey) {
        var UI = this.getByKey(uiKey);
        if (!UI)
            throw "UI instances 'uiKey=" + uiKey + "' no exists!";
        return UI;
    };

// 异步读取全局的消息字符串
    ZUI.loadi18n = function (path, callback) {
        var base = {lang: window.$zui_i18n || "zh-cn"};
        path = $z.tmpl(path)(base);
        require.async(path, function (mm) {
            ZUI.g_msg_map = _.extend(mm || {}, base);
            callback();
        });
    };

// 显示 ZUI 的调试界面
    ZUI.debug = function () {
        // 找到调试内容输出的 DOM，没有就创建
        var jDebugRoot = $(".ui-debug");
        var inMask = false;
        if (jDebugRoot.length == 0) {
            var jDebugMask = $('<div class="ui-debug-mask">').appendTo(document.body);
            jDebugRoot = $('<div class="ui-debug">').appendTo(jDebugMask);
            inMask = true;
        }

        // 确保调试样式 CSS 被加载
        //seajs.use("theme/ui/zui_debug.css");

        // 预先声明闭包要用到的变量
        var info = {count: 0};
        var _draw_sub, _draw_tree;

        var _show_mark = function (jSelf) {
            var cid = jSelf.children('b').text();
            var UI = ZUI(cid);

            if (!UI)
                return;

            var jMark = $('.ui-debug-mark', UI.el.ownerDocument);
            if (jMark.length == 0) {
                jMark = $('<div class="ui-debug-mark">').appendTo(UI.el.ownerDocument.body);
            }
            var rect = $z.rect(UI.$el);

            jMark.css(_.extend($z.rectObj(rect, "top,left,width,height"), {
                "position": "fixed",
                "z-index": 999,
                "border": "1px dashed #F0F",
                "background": "rgba(255,255,0,0.4)",
            }));
        };

        var _hide_mark = function (jSelf) {
            var cid = jSelf.children('b').text();
            var UI = ZUI(cid);
            if (UI)
                $('.ui-debug-mark', UI.el.ownerDocument).remove();
        };

        // 加载处理函数
        if (!jDebugRoot.attr("bind-debug-func")) {
            jDebugRoot.attr("bind-debug-func", "yes");
            if (inMask) {
                $(document.body).one("keydown", function () {
                    $('.ui-debug-mask').remove();
                });
            }
            jDebugRoot
                .on("click", ".uid-closer", function () {
                    $('.ui-debug-mask').remove();
                })
                .on("click", "[has-children] .uid-self > .tnd-handle", function (e) {
                    $z.toggleAttr($(this).closest(".uid-tnode"), "collapse");
                })
                .on("click", ".uid-self", function (e) {
                    if ($(e.target).closest('.tnd-handle, .tnd-actions').length > 0)
                        return;

                    var jSelf = $(this);
                    $z.toggleAttr(jSelf, "show-in-live");

                    if (jSelf.attr("show-in-live")) {
                        jDebugRoot.find('[show-in-live]').not(jSelf).removeAttr("show-in-live");
                        _show_mark(jSelf);
                    }
                    // 去掉标记
                    else {
                        _hide_mark(jSelf);
                    }
                })
                .on("mouseenter", ".uid-self", function (e) {
                    var jSelf = $(this).closest(".uid-self");
                    if (jDebugRoot.find("[show-in-live]").length == 0) {
                        _show_mark(jSelf);
                    }
                })
                .on("mouseleave", ".uid-self", function (e) {
                    var jSelf = $(this).closest(".uid-self");
                    if (jDebugRoot.find("[show-in-live]").length == 0) {
                        _hide_mark(jSelf);
                    }
                })
                .on("click", '.uid-self [a="reload"]', function (e) {
                    var jq = $(this);
                    var jSelf = jq.closest(".uid-self");
                    var cid = jSelf.children('b').text();
                    var UI = ZUI(cid);
                    var jNode = jSelf.parent();

                    _draw_sub(UI, jNode);

                    $z.blinkIt(jq);
                    $z.blinkIt(jNode.children(".uid-tsub"));
                });
        }

        // 重置 DOM
        jDebugRoot.html(`
        <section class="uid-detail"></section>
        <section class="uid-tree"></section>
    `);
        if (inMask)
            $('<div class="uid-closer">').appendTo(jDebugRoot);

        // 绘制子节点
        _draw_sub = function (UI, jNode) {
            // 有子
            if (_.isArray(UI.children) && UI.children.length > 0) {
                var jMySub = jNode.children(".uid-tsub").empty();
                if (jMySub.length == 0) {
                    jMySub = $('<div class="uid-tsub">').appendTo(jNode);
                }
                _draw_tree(UI, UI.children, jMySub);
                jNode.attr("has-children", "yes");
            }
            // 无子节点
            else {
                jNode.attr("no-children", "yes");
            }
        };

        // 递归输出界面的数
        _draw_tree = function (parentUI, uiList, jSub) {
            if (!_.isArray(uiList))
                return;

            // 循环输出
            for (var i = 0; i < uiList.length; i++) {
                // 计数
                info.count++;

                // 准备绘节点 DOM
                var UI = uiList[i];
                var jNode = $('<div class="uid-tnode">').appendTo(jSub);
                var jSelf = $('<div class="uid-self">').appendTo(jNode);

                // 查找自己的 gasket
                var gasketName = null;
                if (parentUI) {
                    for (var key in parentUI.gasket) {
                        if (parentUI.gasket[key] === UI) {
                            gasketName = key;
                            break;
                        }
                    }
                }

                // 计算自己的面积
                var rect = $z.rect(UI.$el);
                var area = rect.width * rect.height;
                jSelf.attr("no-area", area ? null : "yes");

                // 绘制自己的结构
                $('<span class="tnd-handle">').appendTo(jSelf);
                $('<em class="index">').appendTo(jSelf).text(i + ")");
                // 有扩展点名称
                if (gasketName) {
                    var jGasket = $('<span class="gasket">').appendTo(jSelf);
                    $('<i class="fa fa-puzzle-piece">').appendTo(jGasket);
                    $('<em class="gasket">').appendTo(jGasket).text(gasketName);
                    $('<i class="zmdi zmdi-arrow-right">').appendTo(jGasket);
                }
                $('<b>').appendTo(jSelf).text(UI.cid);
                $('<u>').appendTo(jSelf).text(UI.uiName);
                $('<div class="tnd-actions"><i class="zmdi zmdi-refresh" a="reload"></i></div>').appendTo(jSelf);

                // 对于 form UI，自动折叠子
                if ('ui.form' == UI.uiName)
                    jNode.attr("collapse", "yes");

                // 处理子节点
                _draw_sub(UI, jNode);
            }
        };

        // 从顶级树开始显示
        _draw_tree(null, ZUI.tops, jDebugRoot.children(".uid-tree"));

        // 汇总更多统计信息
        info.instances = Object.keys(ZUI.instances).length;
        info._uis = Object.keys(ZUI._uis).length;
        info.__CID = ZUI.__CID;

        // 显示信息
        var str = "";
        for (var key in info) {
            str += $z.alignLeft(key, 10) + " : " + info[key] + "\n";
        }
        jDebugRoot.children(".uid-detail").text($.trim(str));
    }

// 创建全局变量，以及模块导出
    window.ZUI = ZUI;
    module.exports = ZUI;
//===================================================================

    ZUI._on_keyevent = function (e, kmap) {
        var keys = [];
        // 顺序添加，所以不用再次排序了
        if (e.altKey && e.keyCode != 18) keys.push("alt");
        if (e.ctrlKey && e.keyCode != 17) keys.push("ctrl");
        if (e.metaKey && (e.keyCode != 91 || e.keyCode != 93)) keys.push("meta");
        if (e.shiftKey && e.keyCode != 16) keys.push("shift");
        var key;
        if (keys.length > 0) {
            key = keys.join("+") + "+" + e.which;
        } else {
            key = "" + e.which;
        }
        var wkm = kmap[key];
        if (wkm) {
            for (var cid in wkm) {
                var ui = ZUI(cid);
                if (!ui) continue;
                var funcs = wkm[cid];
                if (funcs) {
                    for (var i = 0; i < funcs.length; i++)
                        funcs[i].call(ui, e);
                }
            }
        }
    }

// 处理键盘事件的函数
    ZUI.on_keydown = function (e) {
        ZUI._on_keyevent(e, ZUI.keymap);
    };

// 处理键盘事件的函数
    ZUI.on_keyup = function (e) {
        ZUI._on_keyevent(e, ZUI.keyUpmap);
    };

//===================================================================
// 注册 window 的 resize 和键盘事件
// 以便统一处理所有 UI 的 resize 行为和快捷键行为 
    if (!window._zui_events_binding) {
        // 改变窗口大小
        $(window).resize(function () {
            for (var i = 0; i < window.ZUI.tops.length; i++) {
                var topUI = window.ZUI.tops[i];
                topUI.resize(true);
            }
        });
        // 键盘快捷键
        $(document.body).on("keydown", ZUI.on_keydown);
        $(document.body).on("keyup", ZUI.on_keyup);
        // 全局鼠标事件
        var on_g_mouse_event = function (e) {
            var wmm = ZUI.mousemap[e.type];
            if (wmm)
                for (var cid in wmm) {
                    var ui = ZUI(cid);
                    if (!ui) continue;
                    var funcs = wmm[cid];
                    for (var i = 0; i < funcs.length; i++)
                        funcs[i].call(ui, e);
                }
        };
        $(document).mousedown(on_g_mouse_event);
        $(document).mouseup(on_g_mouse_event);
        $(document).mousemove(on_g_mouse_event);
        $(document).click(on_g_mouse_event);
        $(document).dblclick(on_g_mouse_event);
        $(document).mouseover(on_g_mouse_event);
        $(document).mouseout(on_g_mouse_event);
        $(document).mouseenter(on_g_mouse_event);
        $(document).mouseleave(on_g_mouse_event);
        $(document).contextmenu(on_g_mouse_event);

        // TODO 捕获 ondrop，整个界面不能被拖拽改变
        // 标记以便能不要再次绑定了
        window._zui_events_binding = true;
    }

// 修改 underscore.js 的模板设置
    _.templateSettings.escape = /\{\{([\s\S]+?)\}\}/g;
//====================================================
});