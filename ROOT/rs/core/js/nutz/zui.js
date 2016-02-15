define(function (require, exports, module) {
    //console.log("***************** define the ZUI ***********************");
    //console.log(module);
    //console.log("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    // 采用统一的 ZUI.css
    var dt = require("ui/dateformat.js");
    seajs.use(["theme/ui/zui.css", "ui/support/data_editing.css"]);

    // 提供数据类型的支持
    var DTYPES = require('./data_types');
    var _type = function(fld){
        var hdl = DTYPES[fld.type];
        if(!hdl){
            throw "!! Field type [" + fld.type + "] invalid: " + $z.toJson(fld);
        }
        return hdl;
    };

    // 提供基本的数据显示控件
    var EDITS = require("./data_editing");
    var _edit = function(fld){
        var edit;
        // 完全自定义的编辑控件
        if($z.isPlainObj(fld.editAs)){
            edit = fld.editAs;
        }
        // 采用内置的编辑控件
        else{
            edit = EDITS[fld.editAs];
            if(edit && !edit.name)
                edit.name = fld.editAs;
        }

        // 检查定义
        if(!edit){
            throw "!! Field without editAs : " + $z.toJson(fld);
        }

        // 如果具备了名字，如果没有初始化过，需要初始化
        if(edit.name && !EDITS._init[edit.name]){
            // 执行初始化
            if(edit.events){
                var jBody = $(document.body);
                for(var eKey in edit.events){
                    var handler = edit.events[eKey];
                    var pos = eKey.indexOf(' ');
                    if(pos>0){
                        var eventType = eKey.substring(0, pos);
                        var selector  = eKey.substring(pos+1);
                        jBody.on(eventType, selector, handler);
                    }
                }
            }
            // 标记
            EDITS._init[edit.name] = true;
        }

        // 返回
        return edit;
    };


    // 这个逻辑用来解析 UI 的 DOM， 并根据约定，预先得到 gasket 和 layout
    // 当然 layout 不是必须有的
    var parse_dom = function (html) {
        var UI = this;
        // 解析代码模板
        var tmpl = _.template(html);
        html = tmpl(UI._msg_map);
        UI.$el[0].innerHTML = html;  // FIXME 这里有严重的bug, tr不能被加入到页面中

        // 分析 DOM 结构
        var map = this._code_templates;
        var jTmpl = this.$el.children('.ui-code-template').hide();
        var commonClass = jTmpl.attr("common-class");
        jTmpl.children('[code-id]').each(function () {
            var jq = $(this);
            if (commonClass && jq.attr("nocommon") != "true")
                jq.addClass(commonClass);
            var key = jq.attr('code-id');
            map[key] = jq;
        });
        UI.arena = UI.$el.children('.ui-arena');

        // 搜索所有的 DOM 扩展点
        UI.gasket = {};
        UI.$el.find("[ui-gasket]").each(function () {
            var me = $(this);
            var nm = $.trim(me.attr("ui-gasket"));
            UI.gasket[nm] = {
                ui    : [],
                jq    : $(this),
                multi : me.attr("ui-multi") || false
            };
        });

        // DOM 解析完成
    };

    var register = function(UI) {
        var options = UI.options;
        var gas;
        //....................................
        // 考虑将自己组合到自己的父插件
        if (UI.parent && UI.gasketName) {
            gas = UI.parent.gasket[UI.gasketName];
            if (!gas)
                throw "Fail to found gasket '" + UI.gasketName + "' in " + UI.uiName;
            // 如果已经存在了插件，是否需要释放
            if(!gas.multi && gas.ui.length>0) {
                gas.ui.forEach(function(ui){
                    ui.destroy();
                });
                gas.ui = [];
            }
            // 将自己添加到父插件
            gas.ui.push(UI);
        }
        //..................................... 确定选区
        // 采用给定的父选区: jQuery
        if($z.isjQuery(options.$pel)){
            UI.$pel = options.$pel;
            UI.pel  = UI.$pel[0]; 
        }
        // 采用给定的父选区: Element
        else if(_.isElement(options.pel)){
            UI.pel  = options.pel;
            UI.$pel = $(UI.pel);
        }
        // 将插入点作为父选区
        else if(gas){
            UI.$pel = gas.jq;
            UI.pel = gas.jq[0];
        }
        // 没办法了，只好附加到 body 上了
        else {
            UI.pel  = document.body;
            UI.$pel = $(UI.pel);
        }
        //.....................................
        // 如果指定了 $pel ， 但是没有声明 parent，则会自动判断
        // 是不是自己被组合到了某个 UI 里面， 即使没有声明 ui-gakset 属性
        // 只要自己有 gasketName，就试图为父 UI 创建这个 gasket
        if(!UI.parent && UI.gasketName){
            var PUI = ZUI.getInstance(UI.pel);
            if(PUI) {
                gas = PUI.gasket[UI.gasketName];
                // 没有就创建这个扩展点
                if(!gas) {
                    gas = {
                        jq : UI.$pel,
                        ui : []
                    };
                    PUI.gasket[UI.gasketName] = gas;
                }
                // 有的话,看看需不需要释放
                else if(!gas.multi && gas.ui.length>0) {
                    gas.ui.forEach(function(ui){
                        ui.destroy();
                    });
                    gas.ui = [];
                }
                // 将自己添加到父插件
                gas.ui.push(UI);
                UI.parent = PUI;
            }
        }
        //.....................................
        // 加入 DOM 树
        UI.$pel.append(UI.$el);
        //.....................................
        // 记录自身实例
        UI.$el.attr("ui-id", UI.cid);
        ZUI.instances[UI.cid] = UI;
        if (document.body == UI.pel || !UI.parent) {
            window.ZUI.tops.push(UI);
        }
        //.....................................
    };

    // 定义一个 UI 的原型
    var ZUIPrototype = {
        // Backbone.View 的初始化函数
        initialize: function (options) {
            var UI  = this;
            var opt = options || {};
            // 初始化必要的内部字段
            UI._nutz_ui = "1.a.0";
            UI.uiKey = opt.uiKey;
            UI._code_templates = {};  // 代码模板
            UI._watch_keys = {};
            UI.options = opt;
            UI.parent = opt.parent;
            UI.gasketName = opt.gasketName;
            UI.depth = UI.parent ? UI.parent.depth + 1 : 0;
            // 继承执行器
            UI.exec = opt.exec || (UI.parent||{}).exec;
            // 继承应用信息
            UI.app = opt.app || (UI.parent||{}).app;
            // 默认先用父类的多国语言字符串顶个先
            UI._msg_map = UI.parent ? UI.parent._msg_map : ZUI.g_msg_map;

            // 加载时保持隐藏
            UI.$el.attr("ui-loadding","yes").hide();

            // 调用子类自定义的 init，以及触发事件
            $z.invoke(UI.$ui, "init", [opt], UI);
            $z.invoke(UI.opt, "on_init", [opt], UI);
            UI.trigger("ui:init", UI);

            // 注册 UI 实例
            register(UI);

        },
        destroy: function () {
            var UI = this;
            //console.log("destroy: " + UI.uiName)
            // 触发事件
            $z.invoke(UI.optoptions, "on_depose", [], UI);
            UI.trigger("ui:depose", UI);

            // 释放掉自己所有的子
            for (var key in UI.gasket) {
                var sub = UI.gasket[key];
                sub.ui.forEach(function(ui){
                    ui.destroy();
                }); 
            }
            UI.gasket = {};

            // 移除自己的监听事件
            UI.unwatchKey();
            UI.unwatchMouse();

            // 释放掉自己
            $z.invoke(UI.$ui, "depose", [], UI);

            // 删除自己的 DOM 节点
            $z.invoke(UI.$el, "undelegate", []);
            UI.$el.off().remove();
            // 移除注册
            delete ZUI.instances[UI.cid];
            // 移除 uiKey
            if(UI.uiKey){
                delete ZUI._uis[UI.uiKey];
            }

            // 删除顶级节点记录
            var list = [];
            ZUI.tops.forEach(function(top){
                if (top != UI)
                    list.push(top)
            }, UI);
            ZUI.tops = list;
        },
        // 渲染自身
        render: function (afterRender) {
            var UI = this;
            var options = UI.options;
            
            // 确定语言
            UI.lang = (UI.parent||{}).lang || window.$zui_i18n || "zh-cn";

            //============================================== i18n读完后的回调
            // 看看是否需要异步加载多国语言字符串
            var callback = function () {
                // 合并成一个语言集合
                for(var i=0; i<arguments.length; i++){
                    _.extend(UI._msg_map, arguments[i]);
                }
                // 用户自定义 redraw 执行完毕的处理
                var do_after_redraw = function(){
                    // if("ui.mask" == UI.uiName)
                    //     console.log("!!!!! do_after_redraw:", UI.uiName, UI._defer_uiTypes)
                    // 回调，延迟处理，以便调用者拿到返回值之类的
                    window.setTimeout(function(){
                        if (typeof afterRender === "function") {
                            afterRender.call(UI);
                        }

                        // 触发事件
                        $z.invoke(UI.options, "on_redraw", [], UI);
                        UI.trigger("ui:redraw", UI);

                        // 确定 uiKey，并用其索引自身实例
                        if(UI.uiKey){
                            if(ZUI.getByKey(UI.uiKey)){
                                throw 'UI : uiKey="'+UI.uiKey+'" exists!!!';
                            }
                            ZUI._uis[UI.uiKey] = UI;
                            // 看看有没有要延迟监听的
                            var dl = ZUI._defer_listen[UI.uiKey];
                            if(dl){
                                for(var i in dl){
                                    var dlo = dl[0];
                                    dlo.context.listenUI(UI, dlo.event, dlo.handler);
                                }
                                delete ZUI._defer_listen[UI.uiKey];
                            }
                        }

                        // 触发显示事件
                        $z.invoke(UI.options, "on_display", [], UI);
                        UI.trigger("ui:display", UI);

                        // 让 UI 的内容显示出来
                        UI.$el.removeAttr("ui-loadding").show();

                        // 因为重绘了，看看有木有必要重新计算尺寸，这里用 setTimeout 确保 resize 是最后执行的指令
                        // TODO 这里可以想办法把 resize 的重复程度降低
                        window.setTimeout(function(){
                            UI.resize(true);
                        }, 0);
                    }, 0);
                };
                // 定义后续处理
                var do_render = function () {
                    // 首先看看是否有增加 class
                    if(options.arenaClass){
                        UI.arena.addClass(options.arenaClass);
                    }

                    // 调用UI的特殊重绘方法，如果方法返回了一组 ui 的类型，那么就表示
                    // 用户在方法里采用了异步还要加载这组 UI
                    // 加载完毕后，调用者需要主动在自己的 redraw 函数里，调用 
                    //  UI.defer_report(i, uiType)
                    // 标识加载完成，待全部异步加载的 UI 完成后，会调才会调用 do_after_redraw
                    var uiTypes = $z.invoke(UI.$ui, "redraw", [], UI);
                    if(!uiTypes || uiTypes.length == 0){
                        do_after_redraw();
                    }
                    // 哦？ 是异步的绘制，那么先存储一下回调，等待 UI 全部加载完再调用
                    else {
                        UI._defer_do_after_redraw = do_after_redraw;
                        UI._defer_uiTypes = uiTypes;
                        UI._check_defer_done("redraw");
                    }

                    // 调用自定义的 dom 监听
                    if(UI.options.dom_events) {
                        for(var key in UI.options.dom_events){
                            var m = /^([^ ]+)[ ]+(.+)$/.exec(key);
                            if(null==m){
                                throw "wrong dom_events key: " + key;
                            }
                            var handler = UI.options.dom_events[key];
                            if(!_.isFunction(handler)){
                                handler = UI[handler];
                            }
                            UI.$el.on(m[1], m[2], handler);
                        }
                    }

                    // 调用自定义的 UI 监听
                    if(UI.options.do_ui_listen){
                        for(var key in UI.options.do_ui_listen){
                            var m = /^([^ ]+)[ ]+(.+)$/.exec(key);
                            if(null==m){
                                throw "wrong do_ui_listen key: " + key;
                            }
                            var handler = UI.options.do_ui_listen[key];
                            if(!_.isFunction(handler)){
                                handler = UI[handler];
                            }
                            UI.listenUI(m[1], m[2], handler);
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
                    else if(/^[ \t\n\r]*<.+>[ \t\n\r]*$/m.test(uiDOM)){
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
                    do_render();
                }
            };
            //============================================== i18n
            var do_i18n = function(){
                // 采用父 UI 的字符串
                var uiI18N = $z.concat(UI.$ui.i18n, UI.options.i18n);
                // console.log(UI.uiName, "UI.$ui.i18n", UI.$ui.i18n);
                // console.log(UI.uiName, "UI.options.i18n", UI.$ui.i18n);

                // 存储多国语言字符串
                // 需要将自己的多国语言字符串与父 UI 的连接 
                UI._msg_map = $z.inherit({}, UI.parent ? UI.parent._msg_map : ZUI.g_msg_map);

                // 找到需要加载的消息字符串
                var i18nLoading = [];
                for(var i=0; i<uiI18N.length; i++){
                    var it = uiI18N[i];
                    // 字符串的话，转换后加入待加载列表
                    if(_.isString(it)){
                        i18nLoading.push(_.template(it)({lang: UI.lang}));
                    }
                    // 对象的话，直接融合进来
                    else if(_.isObject(it)){
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
            }
            //============================================== 从处理CSS开始
            // 加载 CSS
            var uiCSS = $z.concat(UI.$ui.css, UI.options.css);
            if (uiCSS.length > 0) {
                seajs.use(uiCSS, do_i18n);
            }else{
                do_i18n();
            }
            // 返回自身
            return UI;
        },
        // 汇报自己完成了哪个延迟加载的 UI
        defer_report : function(i, uiType){
            var UI = this;
            if(!UI._loaded_uiTypes)
                UI._loaded_uiTypes = [];

            // 如果仅仅给了个 uiType，那么自动寻找 i
            if(_.isString(i)){
                uiType = i;
                i = UI._defer_uiTypes.indexOf(uiType);
                if(i<0){
                    alert("defer uiType '" + uiType + "' without define!");
                    throw "defer uiType '" + uiType + "' without define!";
                }
            }

            UI._loaded_uiTypes[i] = uiType;
            UI._check_defer_done();
        },
        _check_defer_done : function(){
            var UI = this;
            if(UI._loaded_uiTypes && UI._defer_uiTypes && UI._defer_do_after_redraw){
                if(UI._loaded_uiTypes.length == UI._defer_uiTypes.length){
                    for(var i=0; i<UI._loaded_uiTypes.length; i++){
                        if(UI._loaded_uiTypes[i] != UI._defer_uiTypes[i]){
                            return;
                        }
                    }
                    // 延迟调用
                    window.setTimeout(function(){
                        if(UI._loaded_uiTypes && UI._defer_uiTypes && UI._defer_do_after_redraw){
                             UI._defer_do_after_redraw.call(UI);
                             delete UI._defer_uiTypes;
                            delete UI._loaded_uiTypes;
                            delete UI._defer_do_after_redraw;
                        }
                    }, 0);
                }
            }
        },
        // 修改 UI 的大小
        resize: function (deep) {
            var UI = this;
            // 如果是选择自适应
            if (UI.options.fitself) {
                return;
            }

            // 如果还在加载中，也什么都不做
            if(UI.$el.attr("ui-loadding")){
                return;
            }

            // 有时候，初始化的时候已经将自身加入父UI的gasket
            // 父 UI resize 的时候会同时 resize 子
            // 但是自己这时候还没有初始化完 DOM (异步加载)
            // 那么自己的 arena 就是未定义，因此不能继续执行 resize
            if (UI.arena) {
                // 需要调整自身，适应父大小
                if (UI.options.fitparent === true
                    || (UI.options.fitparent !== false && UI.arena.attr("ui-fitparent"))) {
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
                        "width" : w - margin.x, 
                        "height": h - margin.y
                    });
                    margin = $z.margin(UI.arena);
                    UI.arena.css({
                        "width" : UI.$el.width()  - margin.x, 
                        "height": UI.$el.height() - margin.y
                    });
                }

                // 调用自身的 resize
                $z.invoke(UI.$ui, "resize", [deep], UI);

                // 触发事件
                $z.invoke(UI.options, "on_resize", [], UI);
                UI.trigger("ui:resize", UI);

                // 调用自身所有的子 UI 的 resize
                if(deep){
                    for (var key in UI.gasket) {
                        var sub = UI.gasket[key];
                        sub.ui.forEach(function(ui){
                            ui.resize(deep);
                        });
                    }
                }
            }
        },
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
            // 没有特殊 key
            if (typeof keys == "function") {
                handler = keys;
                keys = undefined;
            }
            
            var key = this.__keyboard_watch_key(which, keys);
            //console.log("watchKey : '" + key + "' : " + this.cid);
            $z.pushValue(ZUI.keymap, [key, this.cid], handler);

            // 记录到自身以便可以快速索引
            this._watch_keys[key] = true;
        },
        __keyboard_watch_key : function(which, keys){
            // 直接就是字符串
            if(_.isString(which)){
                return which;
            }
            // 组合的key
            if (_.isArray(keys)) {
                return keys.sort().join("+") + "+" + which;
            }
            // 字符串
            if (_.isString(keys)) {
                return  keys + "+" + which;
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
            if(_.isUndefined(which)){
                for(var key in this._watch_keys){
                    var wkm = ZUI.keymap[key];
                    if(wkm && wkm[this.cid]){
                        delete wkm[this.cid];
                    }
                }
                this._watch_keys=[];
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
        watchMouse : function(eventType, handler){
            $z.pushValue(ZUI.mousemap, [eventType, this.cid], handler);
        },
        // 取消全局鼠标事件监听
        unwatchMouse : function(eventType){
            // 去掉所有的事件监听
            if(!eventType){
                for(var key in ZUI.mousemap){
                    this.unwatchMouse(key);
                }
            }
            // 去掉指定事件
            else{
                var wmm = ZUI.mousemap[eventType];
                if(wmm && wmm[this.cid])
                    delete wmm[this.cid];
            }
        },
        // 得到多国语言字符串
        msg: function (key, ctx, msgMap) {
            var re = $z.getValue(msgMap || this._msg_map, key);
            if(!re){
                return key;
            }
            // 需要解析
            if (re && ctx && _.isObject(ctx)) {
                re = (_.template(re))(ctx);
            }
            return re;
        },
        // 得到多国语言字符串，如果没有返回默认值，如果没指定默认值，返回空串 ("")
        str : function(key, dft){
            var re = $z.getValue(this._msg_map, key);
            return re || dft || "";
        },
        // 对一个字符串进行文本转移，如果是 i18n: 开头采用 i18n
        text: function(str, ctx, msgMap){
            // 多国语言
            if(/^i18n:.+$/g.test(str)){
                return this.msg(str.substring(5), ctx, msgMap);
            }
            // 字符串模板
            if(str && ctx && _.isObject(ctx)){
                return (_.template(str))(ctx);
            }
            // 普通字符串
            return str;
        },
        // 在某区域显示读取中，如果没有指定区域，则为整个 arena
        showLoading : function(selector){
            var html = '<div class="ui-loading">';
            html += '<i class="fa fa-spinner fa-pulse"></i> <span>'+this.msg("loading")+'</span>';
            html += '</div>';

            var jq = selector ? $(selector) : this.arena;
            jq.empty().html(html);
        },
        // 根据路径获取一个子 UI
        subUI : function(uiPath){
            var ss = uiPath.split(/[\/\\.]/);
            var UI = this;
            for(var i=0;i<ss.length;i++){
                var s = ss[i];
                var g = UI.gasket[s];
                if(!g || g.ui.length == 0)
                    return null;
                UI = g.ui[0];
            }
            return UI;
        },
        // 处理一个对象的字段，将其记录成特殊显示的值
        // !!! 这个方法要作废 >o< !!!
        eval_obj_display : function(obj, displayMap){
            if(!_.isObject(obj))
                return;
            if(!_.isObject(displayMap))
                return;
            obj._display = {};
            for(var key in displayMap){
                var val = obj[key];
                var str = displayMap[key][val];
                if(/^i18n:.+$/g.test(str)){
                    str = this.msg(str.substring(5));
                }
                obj._display[key] = str || val;
            }
        },
        get_obj_val_by : function(obj, ref){
            var val = obj[ref.key];
            return ref.map[val] || ref.dft;
        },
        // 快捷方法，帮助 UI 存储本地状态
        // 需要设置 "app" 段
        // 参数 appName 默认会用 app.name 来替代 
        local : function(key,val, appName){
            var UI = this;
            var app = UI.app;
            if(!app || !app.session || !app.session.me){
                throw "UI.local need 'app.session.me'";
            }
            return $z.local(appName||app.name, app.session.me, key, val);
        },
        // 字段显示方式可以是模板或者回调，这个函数统一成一个方法
        eval_tmpl_func : function(obj, nm){
            var F = obj ? obj[nm] : null;
            if(!F)
                return null;
            return _.isFunction(F) ? F : _.template(F);
        },
        //............................................
        ui_parse_data : function(obj, callback) {
            var UI   = this;
            var opt  = UI.options;
            var context = opt.context || UI;
            // 同步
            if(_.isFunction(opt.parseData)){
                var o = opt.parseData.call(context, obj);
                callback.call(UI, o);
            }
            // 异步 
            else if(_.isFunction(opt.asyncParseData)){
                opt.asyncParseData.call(context, obj, function(o){
                    callback.call(UI, o);
                });
            }
            // 直接使用
            else{
                callback.call(UI, obj);
            }
        },
        //............................................
        ui_format_data : function(callback){
            var UI  = this;
            var opt = UI.options;
            var obj = callback.call(UI, opt);
            if(_.isFunction(opt.formatData)){
                var context = opt.context || UI;
                return opt.formatData.call(context, obj);
            }
            return obj;
        },
        //............................................
        // 监听本 UI 的模块事件
        listenModel: function (event, handler) {
            this._listen_to(this.model, event, handler);
        },
        // 监听本 UI 的父UI事件
        listenParent : function(event, handler) {
            this._listen_to(this.parent, event, handler);
        },
        // 监听某个 UI 的事件
        listenUI : function(uiKey, event, handler) {
            // 给的就是一个 UI 实例，那么直接监听了
            if(uiKey._nutz_ui){
                this._listen_to(uiKey, event, handler);
            }
            // 监听自己的父
            else if("$parent" == uiKey){
                this._listen_to(this.parent, event, handler);
            }
            // 否则看看是不是需要推迟建立
            else{
                var taUI = ZUI.getByKey(uiKey);
                // 不需要推迟
                if(taUI) {
                    this._listen_to(taUI, event, handler);       
                }
                // 暂存
                else {
                    var dl = ZUI._defer_listen[uiKey];
                    if(!dl){
                        dl = [];
                        ZUI._defer_listen[uiKey] = dl;
                    }
                    dl.push({
                        event : event, 
                        handler : handler,
                        context : this
                    });
                }
            }
        },
        // 监听某个 backbone 的模块消息 
        _listen_to: function (target, event, handler) {
            if (target){
                // 如果给的是一个字符串，那么就表示当前对象的一个方法
                // 支持 . 访问子对象
                if(_.isString(handler)){
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
        val_check : function(fld, v){
            var UI = this;
            var hdl = _type(fld);
            var val = hdl.test.apply(UI, [fld, v]);
            return hdl.asValue.apply(UI, [fld, val]);
        },
        val_get : function(fld, o){
            var UI = this;
            var hdl = _type(fld);
            var v = $z.getValue(o, fld.key, fld.dft);
            var val = hdl.test.apply(UI, [fld, v]);
            return hdl.asValue.apply(UI, [fld, val]);
        },
        val_edit : function(fld, o){
            var UI = this;
            var hdl = _type(fld);
            var v = $z.getValue(o, fld.key, fld.dft);
            var val = hdl.test.apply(UI, [fld, v]);
            return hdl.asEdit.apply(UI, [fld, val]);
        },
        val_display : function(fld, o){
            var UI = this;
            // 如果自定义了显示方法
            if(_.isFunction(fld._disfunc)){
                return fld._disfunc(o, fld, UI);
            }
            // 否则采用标准的显示方式
            var hdl = _type(fld);
            var v = $z.getValue(o, fld.key, fld.dft);
            var val = hdl.test.apply(UI, [fld, v]);
            var obj = hdl.asEdit.apply(UI, [fld, val]);
            if(_.isNull(obj) || _.isUndefined(obj)){
                return obj;
            }
            else if(_.isArray(obj)){
                return obj.join(",");
            }
            return obj.toString();
        },
        val_test : function(fld, v){
            var UI = this;
            var hdl = _type(fld);
            return hdl.test.apply(UI, [fld, v]);
        },
        normalize_field : function(fld, dft){
            var UI = this;

            // 确保字段有类型 
            if(dft){
                fld.type = fld.type || dft.type || 'string';
            }else{
                fld.type = fld.type || 'string';
            }

            // 得到控制器
            var hdl = _type(fld);

            // 必须有 key
            if(!_.isString(fld.key)){
                throw "!!! ZUI : fld noKey : " + $z.toJson(fld);
            }
            // 将字段标题本地化
            if(fld.title)
                fld.title = UI.text(fld.title);
            else
                fld.title = fld.key;

            // 处理字段控件
            if(UI.options.idKey == fld.key){
                fld.editAs = "label";
            }
            // 处理字段控件
            if(!fld.editAs) {
                fld.editAs = hdl.defaultEditAs;
            }

            // 预先加载字段的配置
            if(fld.setup){
                fld.setup = $z.loadResource(fld.setup);
            }

            // 根据类型处理各个字段的配置信息 
            hdl.normalize.apply(UI, [fld]);

            // 显示方法
            fld._disfunc = UI.eval_tmpl_func(fld, "display");
            $z.setUndefined(fld, "escapeHtml",  true);
            
            // 返回自身以便链式赋值
            return UI;
        },
        //...............................................................
        edit_set : function(fld, o) {
            var UI = this;
            var edit = _edit(fld);
            edit.set.apply(UI, [fld, o]);
            return UI;
        },
        //...............................................................
        edit_get : function(fld) {
            var UI = this;
            var edit = _edit(fld);
            // 显示字段编辑控件
            return edit.get.apply(UI, [fld]);
        },
        //...................................................................
        ajaxReturn : function(re, option, context){
            var UI = this;
            context = context || UI;
            if(_.isString(re)){
                re = $z.fromJson(re);
            }
            // 格式化 callback
            if(_.isFunction(option)){
                option = {
                    success : option
                }
            }
            // 如果失败了
            if(!re.ok){
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
        }
    };

    // ZUI 就是一个处理方法 
    var ZUI = function(arg0, arg1){
        // 定义
        if(_.isString(arg0) && _.isObject(arg1)){
            return ZUI.def(arg0, arg1);
        }
        // 获取实例
        else if(_.isElement(arg0) || (arg0 instanceof jQuery)){
            return arg1 ? ZUI.checkInstance(arg0)
                        : ZUI.getInstance(arg0);
        }
        // 根据 uiKey
        else if(_.isString(arg0)){
            return arg1 ? ZUI.checkByKey(arg0) || ZUI.checkByCid(arg0)
                        : ZUI.getByKey(arg0) || ZUI.getByCid(arg0); 
        }
        // 未知处理
        throw "Unknown arg0 : " + arg0 + ", arg1" + arg1;
    };
    // 初始化 ZUI 工厂类对象
    /*这些字段用来存放 UI 相关的运行时数据*/
    ZUI.keymap = {/*
        "alt+shift+28" : {
            "c2" : [F(e), F(e)..]
        }
    */};
    ZUI.mousemap = {/*
        click: {
           "c2" : [F(e), F(e)..]
        }
    */};
    ZUI.tops = [];
    ZUI.definitions = {};
    ZUI.instances = {};  // 根据cid索引的 UI 实例
    ZUI._uis = {};       // 根据键值索引的 UI 实例，没声明 key 的 UI 会被忽略

    // 调试方法，打印当前 UI 的级联 tree
    ZUI.dump_tree = function(UI, depth){
        // 显示一个 UI 的树
        if(UI){
            var depth = _.isUndefined(depth) ? 0 : depth
            var prefix = $z.dupString("    ", depth);
            var str = (_.template("{{nm}}({{cid}}){{uiKey}}"))({
                nm     : UI.uiName,
                cid    : UI.cid,
                uiKey  : UI.uiKey ? "["+UI.uiKey+"]" : ""
            });
            for(var key in UI.gasket) {
                var G = UI.gasket[key];
                if(G.ui && G.ui.length == 1){
                    str += "\n" + prefix 
                           + "  @" + $z.alignLeft('"' + key + '"', 8, " ")
                           + "-> " + ZUI.dump_tree(G.ui[0], depth+1);
                }
                else if(G.ui && G.ui.length > 1){
                    str += "\n" + prefix + "  @'" + key + "'-> " + G.ui.length + "UIs: "
                    for(var i in G.ui){
                        str += "\n" + ZUI.dump_tree(G.ui[i], depth+1);
                    }
                }
            }
            return str;
        }
        // 显示全部顶层 UI
        var str = "";
        for(var i in ZUI.tops){
            str += "-> " + ZUI.dump_tree(ZUI.tops[i], 0) + "\n"; 
        }
        return str;
    };

    // 如果监听一个 UI 的键值，但是这个 UI 的实例因为异步还没有被加载
    // 那么，先暂存到这个属性里，当 UI 实例被加载完毕了，会自动应用这个监听的
    ZUI._defer_listen = {}

    // 这个函数用来定义一个 UI 模块，返回一个 Backbone.View 的类用来实例化
    ZUI.def = function (uiName, conf) {
        var UIDef = this.definitions[uiName];
        if (!UIDef) {
            // 这里准备将所有用户自定义事件包裹一下
            // 以便能够控制不让按钮被连续点击之类的
            // if("ui.opager" == uiName)
            // if(_.isObject(conf.events)){
            //     for(var eKey in conf.events){
            //         var func = conf.events[eKey];
            //         conf.events[eKey] = function(e){
            //             func.call(this, e);
            //         };
            //     }
            // }

            // 准备配置对象的默认属性
            var viewOptions = {
                uiName: uiName,
                tagName: 'div',
                className: uiName.replace(/[.]/g, '-'),
                $ui: {}
            };
            // 将 UI 的保留方法放入 $ui 中，其余 copy
            for (var key in conf) {
                if (/^(css|dom|i18n|init|redraw|depose|resize)$/g.test(key)) {
                    viewOptions.$ui[key] = conf[key];
                } 
                else {
                    viewOptions[key] = conf[key] || viewOptions[key];
                }
            }

            // 加入 UI 的原型方法
            _.extend(viewOptions, ZUIPrototype);
            // 注册
            UIDef = Backbone.View.extend(viewOptions);
            UIDef.uiName = uiName;
            this.definitions[uiName] = UIDef;
        }
        // 返回
        return UIDef;
    };

    // 根据任何一个 DOM 元素，获取其所在的 UI 对象
    ZUI.getInstance = function (el) {
        var jq = $(el);
        var jui = jq.closest("[ui-id]");
        if (jui.size() == 0) {
            console.warn(el);
            throw "Current DOMElement no belone to any UI!";
        }
        var cid = jui.attr("ui-id");
        return this.getByCid(cid);
    };
    ZUI.checkInstance = function (el) {
        var jq = $(el);
        var jui = jq.parents("[ui-id]");
        if (jui.size() == 0) {
            console.warn(el);
            throw "Current DOMElement no belone to any UI!";
        }
        var cid = jui.attr("ui-id");
        return this.checkByCid(cid);
    };

    // 根据 cid 获取 UI 的实例 
    ZUI.getByCid = function(cid){
        return this.instances[cid];
    };
    ZUI.checkByCid = function(cid){
        var UI = this.getByCid(cid);
        if(!UI)
            throw "UI instances 'cid="+cid+"' no exists!";
        return UI;
    };

    // 根据 uiKey 获取 UI 的实例 
    ZUI.getByKey = function(uiKey){
        return this._uis[uiKey];
    };
    ZUI.checkByKey = function(uiKey){
        var UI = this.getByKey(uiKey);
        if(!UI)
            throw "UI instances 'uiKey="+uiKey+"' no exists!";
        return UI;
    };

    // 异步读取全局的消息字符串
    ZUI.loadi18n = function (path, callback) {
        path = _.template(path)({lang: window.$zui_i18n || "zh-cn"});
        require.async(path, function (mm) {
            ZUI.g_msg_map = mm || {};
            callback();
        });
    };

    // 创建全局变量，以及模块导出
    window.ZUI = ZUI;
    module.exports = ZUI;
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
        $(document.body).keydown(function (e) {
            //console.log(e.which);
            var keys = [];
            // 顺序添加，所以不用再次排序了
            if (e.altKey)   keys.push("alt");
            if (e.ctrlKey)  keys.push("ctrl");
            if (e.metaKey)  keys.push("meta");
            if (e.shiftKey) keys.push("shift");
            var key;
            if (keys.length > 0) {
                key = keys.join("+") + "+" + e.which;
            } else {
                key = "" + e.which;
            }
            var wkm = ZUI.keymap[key];
            if (wkm) {
                for(var cid in wkm){
                    var ui = ZUI(cid);
                    if(!ui) continue;
                    var funcs = wkm[cid];
                    if(funcs){
                        for(var i=0;i<funcs.length;i++)
                            funcs[i].call(ui, e);
                    }
                }
            }
        });
        // 全局鼠标事件
        var on_g_mouse_event = function(e){
            var wmm = ZUI.mousemap[e.type];
            if(wmm)
                for(var cid in wmm){
                    var ui = ZUI(cid);
                    if(!ui) continue;
                    var funcs = wmm[cid];
                    for(var i=0;i<funcs.length;i++)
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

});