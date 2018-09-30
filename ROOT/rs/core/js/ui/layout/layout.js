(function($z){
    $z.declare([
        'zui',
        'ui/menu/menu',
        'ui/layout/support/layout_methods',
        'ui/layout/support/layout_util'
    ], function(ZUI, MenuUI, LayoutMethods, $L){
//==============================================
// 获取区域内尺寸数组
// @return ["auto", "30%", "-bar-", "24px"]
var formatChildrenSizing = function($con, base) {
    $con.children().each(function(){
        var jq = $(this);
        // 分隔条就无视吧
        if('bar' == jq.attr('wl-type')) 
            return;
        
        var sz;
        // 收缩
        if('yes' == jq.attr('wl-collapse')) {
            sz = jq.attr('wl-collapse-size') || "24px";
        }
        // 正常尺寸
        else {
            sz = jq.attr('wl-size') || "auto";
        }
        // 如果是百分比
        var m = /^([\d.]+)%$/.exec(sz);
        if(m) {
            sz = m[1] * 0.01 * base + "px";
        }

        // 标识一下真实尺寸
        jq.attr({'wl-real-size' : sz});
    });
};
//----------------------------------------------
// 根据数组进行分配的函数
//  - keyGet 一般为 outerWidth | outerHeight
//  - keyOff 一般为 top | left
//  - keySiz 一般为 width | height
var applyChildrenSizing = function($con, base, keyGet, keyOff, keySiz) {
    var n_auto = 0;
    var n_remain = base;
    // 先来一轮固定的分配自身尺寸
    $con.children('[wl-real-size]').each(function(){
        var jq = $(this);
        var sz = jq.attr('wl-real-size');
        //console.log(sz)
        // 稍后分配自动的
        if('auto' == sz) {
            n_auto ++;
            return;
        }
        // 设置一下 css
        jq.css(keySiz, sz);
        // 计算剩余
        n_remain -= jq[keyGet]();
    });
    // 自动分配剩余
    if(n_remain>0 && n_auto>0) {
        var n = n_remain / n_auto;
        $con.children('[wl-real-size="auto"]').each(function(){
            $(this).css(keySiz, n);
        });
    }
    // 逐个累加设置偏移
    var off = 0;
    $con.children().each(function(){
        var jq = $(this);
        // 分隔条，不参与累加
        if('bar' == jq.attr('wl-type')) {
            jq.css(keyOff, off - jq[keyGet]()/2);
        }
        // 累加
        else {
            jq.css(keyOff, off);
            off += jq[keyGet]();
        }
    });
};
//----------------------------------------------
// 根据类型修改当前层
var funcMap = {
    "rows" : function($div, $con) {
        // 得到尺寸
        var base = $div.height();

        // 预先计算尺寸
        formatChildrenSizing($con, base);

        // 分配
        applyChildrenSizing($con, base, "outerHeight", "top", "height");
    },
    "cols" : function($div, $con) {
        // 得到尺寸
        var base = $div.width();

        // 预先计算尺寸
        formatChildrenSizing($con, base);

        // 分配
        applyChildrenSizing($con, base, "outerWidth", "left", "width");
    },
    "tabs" : function($div, $con) {
        var $tab = $div.find('>.wlt-tabs');
        $con.css('height', $div.height() - $tab.outerHeight());
    }
};
//----------------------------------------------
// 修改布局的函数
var __resize_items = function($div) {
    // 根据类型修改区域尺寸的函数
    var tp = $div.attr('wl-type');
    var func = funcMap[tp];

    // 得到子容器
    var $con = $div.children('.wn-layout-con');

    // 执行修改逻辑
    if(_.isFunction(func) && $con.length > 0) {
        funcMap[tp]($div, $con);
    }

    // 找到下属 DIV 递归处理
    var $subDivs =  $con.find('>[wl-type]').not('[wl-type="bar"]');
    for(var i=0; i<$subDivs.length; i++) {
        var $subDiv = $subDivs.eq(i);
        __resize_items($subDiv);
    }
};
//==============================================
var html = function(){/*
    <div class="ui-arena wn-layout" ui-fitparent="yes">
        I am layout
    </div>
    */};
//==============================================
return ZUI.def("ui.layout", {
    dom  : $z.getFuncBodyAsStr(html.toString()),
    css  : "ui/layout/theme/layout-{{theme}}.css",
    //..............................................
    init : function(opt) {
        $z.setUndefined(opt, "setup", {});
        $z.setUndefined(opt, "eventRouter", {});

        // 确保 eventRouter 值为数组
        for(var ekey in opt.eventRouter) {
            var edst = opt.eventRouter[ekey];
            if(_.isString(edst) && '..' != edst) {
                opt.eventRouter[ekey] = [edst];
            }
        }
    },
    //..............................................
    events : {
        // 关闭 box
        'click [wl-type="box"] > .wlb-con > .wlb-closer > b' : function(e){
            this.hideArea(e.currentTarget);
        },
        'dblclick [wl-type="box"] > .wlb-con > .wl-info' : function(e){
            this.hideArea(e.currentTarget);
        },
        // 切换标签
        'click [wl-type="tabs"] > .wlt-tabs > ul > li' : function(e) {
            this.switchTab($(e.currentTarget));
        },
        // 区域动画结束
        "transitionend [wl-area]" : function(e) {
            var UI = this;
            var oe = e.originalEvent;
            var $ar = $(e.target);
            if($ar.attr('wl-area') && 'opacity' == oe.propertyName) {
                //console.log("area trans end")
                // 标记自己的结束状态
                var isHide = $ar.attr('wl-collapse') == "yes";
                $ar.attr({
                    'animat-on'    : null,
                    'wl-area-hide' : isHide ? "yes" : null
                });
                // 重新调整子区域布局
                UI.__resize_area($ar);

                // // 最后 resize 自己所在控件
                // var uis = this.getAreaUIList($ar);
                // for(var i=0; i<uis.length; i++) {
                //     //console.log(uis[i])
                //     uis[i].resize(true);
                // }
                // 需要显示区域，如果是需要显示区域，那么确保这个区域内的 UI 是加载的
                // 没有的话，试图重新加载
                if(!isHide) {
                    UI.on_area_ready($ar);
                }
            }
        }
    },
    //..............................................
    redraw : function() {
        var UI  = this;
        var opt = UI.options;

        // 必须有一个 layout.xml
        if(!opt.layout){
            throw "layout without defined!";
        }

        // seajs 的加载器需要强制声明文本
        var layoutPath = opt.layout;
        if(!/^text!/.test(opt.layoutPath)){
            layoutPath = "text!" + layoutPath;
        }

        // 读取 layout
        if(opt.debug)console.log(UI._TK(2), "before load");
        seajs.use(layoutPath, function(xml){
            if(opt.debug)console.log(UI._TK(2), "after load");
            // 解析
            var layout = $L.parseXml(xml);

            // 首先重置一下所有的 tabs 的 collapse
            var tabStatus = {};
            if(opt.localKey) {
                tabStatus = UI.local(opt.localKey) || tabStatus;
            }
            if(opt.debug)console.log(UI._TK(2), "tabStatus", tabStatus);
            $L.normalizeTabCollapse(layout, tabStatus);
            
            if(opt.debug)console.log(UI._TK(2), $z.toJson(layout, null, '  '));
            UI.__do_redraw(layout);
            
            // 如果有 UI，那么 __do_redraw 已经把他们塞入延时了
            // 这里可以放心的释放一个延时的 key 先
            UI.defer_report("load");
        });

        // 监听通用事件
        UI.listenSelf("all", function(eventType, eo){
            // 如果是  ui:xxx 事件，无视
            if(/^ui:/.test(eventType))
                return;
            if(opt.debug)console.log(UI._TK(2), "heard:", eventType, eo)
            // 在路由表里有记录，那么就路由
            var edst = opt.eventRouter[eventType]
                        || opt.eventRouter.all;
            // 路由到父
            if('..' == edst) {
                var pbus = $z.invoke(UI.parent, 'bus');
                if(pbus) {
                    pbus.trigger(eventType, eo);
                }
            }
            // 有映射值
            else if(edst) {
                // 看看是否映射到子控件
                var m = /^(->)([^=]+)(=(.+))?$/.exec(edst);
                // 映射到子控件
                if(m) {
                    var subUI = UI.subUI(m[2]);
                    var evTp2 = $.trim(m[4]);
                    if(subUI) {
                        // 触发新事件
                        if(evTp2) {
                            var eo2 = this.__gen_event_obj(evTp2, eo.area, subUI, eo.data);
                            subUI.trigger(eo2.type, eo2);
                        }
                        // 直接转发旧事件
                        else {
                            subUI.trigger(eo.type, eo);
                        }
                    }
                }
                // 重新发起事件
                for(var i=0; i<edst.length; i++) {
                    var evTp2 = edst[i];
                    var eo2 = this.__gen_event_obj(evTp2, eo.area, subUI, eo.data);
                    UI.trigger(eo2.type, eo2);
                }
            }
            // 默认的处理
            else {
                var ss = eventType.split(":");
                if(ss.length == 2) {
                    var ac = ss[0];
                    var ar = ss[1];
                    // 通用事件 show:xxx
                    if('show' == ac) {
                        UI.showArea(ar);
                    }
                    // 通用事件 hide:xxx
                    else if('hide' == ac) {
                        UI.hideArea(ar);
                    }
                }
            }
        });

        // 返回延迟加载
        return ['load'];
    },
    //..............................................
    __build_ui_map : function(layout) {
        var UI  = this;
        var opt = UI.options;       

        // 准备一个 menu 的序列
        var menu_seq = 0;

        // 首先编制一下自己的 gasketName 映射表
        UI.__ui_map = $L.eachLayoutItem(layout, function(it, p){
            if(p)
                it.__is_in_hidden_area = p.collapse || p.__is_in_hidden_area;
            // 声明了名称，那么就要记录一下映射
            if(it.name) {
                // 重名！ 不能忍啊
                if(this[it.name])
                    throw "duplicate gasketName ["+it.name+"] in layout " + opt.layout;
                this[it.name] = {
                    name     : it.name,
                    collapse : it.collapse,
                    uiType   : it.uiType,
                    uiConf   : it.uiConf,
                    __is_in_hidden_area : it.__is_in_hidden_area
                };
            }
            // 如果有 Action，那么格式化一下
            if(it.action) {
                it.action.setup = UI.__normalize_action_menu(it.action.setup);
            }
        }, {});

        // 用自己配置填充一下缺失的 UI 配置（最优先嘛）
        for(var name in UI.__ui_map) {
            var val = opt.setup[name];
            if(!val)
                continue;
            if(_.isString(val)) {
                UI.__ui_map[name] = {
                    name   : name,
                    uiType : val
                };
            }
            else if(val.uiType) {
                UI.__ui_map[name] = _.extend({
                    name   : name,
                    uiType : val.uiType,
                    uiConf : val.uiConf
                });
            }
        }

        // 未定义的 UI 从映射表里删掉，留着也是祸害 
        var map = {};
        for(var name in  UI.__ui_map) {
            var uiDef = UI.__ui_map[name];
            if(uiDef && uiDef.uiType)
                map[name] = uiDef;
        }
        UI.__ui_map = map;
        if(opt.debug)console.log(UI._TK(2), "UI.__ui_map", UI.__ui_map);
    },
    //..............................................
    // 返回现在所有要显示的 UI key （即非 collapse:true）
    // 通常用来寻找需要加载的 gaskeName 以便调用者注册延迟加载
    getDisplayUIKeys : function() {
        var UI = this;
        var keys = [];
        for(var key in  UI.__ui_map) {
            var uiDef = UI.__ui_map[key];
            if(!uiDef.collapse && !uiDef.__is_in_hidden_area)
                keys.push(uiDef.name);
        }
        return keys;
    },
    //..............................................
    __build_dom : function(layout) {
        var UI  = this;
        var opt = UI.options;

        // 清空自身
        UI.releaseAllChildren(true);
        UI.arena.empty();

        // 处理普cols/rows/tabs
        $L.renderDom(UI, layout, UI.arena, true);

        // 处理box
        if(_.isArray(layout.boxes)) {
            for(var i=0; i<layout.boxes.length; i++) {
                var box = layout.boxes[i];
                $L.renderDom(UI, box, UI.arena, true);
            }
        }

        // 编译一份区域键值索引·以便用来show/hide区域
        UI.__area_map = {};
        UI.arena.find('[wl-area][wl-key]').each(function(){
            var $ar = $(this);
            UI.__area_map[$ar.attr('wl-key')] = $ar;
        });
        if(opt.debug)console.log(UI._TK(2)," UI.__area_map", UI.__area_map);

        // 编译 gasket
        UI.rebuildGaskets();
    },
    //..............................................
    __do_redraw : function(layout, callback) {
        var UI  = this;
        var opt = UI.options;

        // 首先编制一下自己的 gasketName 映射表
        UI.__build_ui_map(layout);
        
        // 注册延迟加载
        var keys = UI.getDisplayUIKeys();
        if(opt.debug)console.log(UI._TK(2), "deferKeys", keys);
        UI.defer(keys, function(){
            $z.doCallback(callback, [], UI);
            UI.trigger("layout:ready", {
                type : "layout:ready",
                uis  : _.extend({}, UI.gasket)
            });
        });

        // 下面依次绘制 DOM 节点
        UI.__build_dom(layout);

        // 同步所有的 tabs 菜单
        UI.arena.find('.wlt-tabs > ul >li[current]').each(function(){
            UI.__sync_tab_action($(this));
        });

        // 下面依次加载子界面
        for(var i=0; i<keys.length; i++) {
            (function(key){
                var uiDef = UI.__ui_map[key];
                UI.__do_redraw_subUI(uiDef, function(key){
                    UI.defer_report(key);
                    this.fireBus("area:ready");
                });
            })(keys[i]);
        }
    },
    //..............................................
    __do_redraw_subUI : function(uiDef, callback) {
        var UI = this;
        var theConf = _.extend({}, uiDef.uiConf, {
            parent : UI,
            gasketName : uiDef.name,
            on_before_init : function(){
                this.__layout_bus = UI;
                LayoutMethods(this);
            }
        });
        seajs.use(uiDef.uiType, function(SubUI){
            new SubUI(theConf).render(function(){
                callback.apply(this, [uiDef.name]);
            });
        });
    },
    //..............................................
    // 获取 area 的 jquery 对象
    $area : function(arg) {
        if(_.isElement(arg) || $z.isjQuery(arg)){
            return $(arg).closest('[wl-area]');
        }
        // 如果是字符串，表示区域的键
        if(_.isString(arg)){
            return this.__area_map[arg];
        }
    },
    //....................................................
    getAreaUIMap : function($ar, showAll) {
        var cid = this.cid;
        var map = {};
        $ar.find('[ui-gasket-cid="'+cid+'"]').andSelf().each(function(){
            var gasName = $(this).attr('ui-gasket-raw');
            var childUI = ZUI($(this).children('[ui-id]'));
            if(gasName && (childUI || showAll)) {
                map[gasName] = childUI || null;
            }
        });
        return map;
    },
    //....................................................
    getAreaUIList : function($ar, showAll) {
        var cid = this.cid;
        var list = [];
        $ar.find('[ui-gasket-cid="'+cid+'"]').andSelf().each(function(){
            var childUI = ZUI($(this).children('[ui-id]'));
            if(childUI || showAll) {
                list.push(childUI);
            }
        });
        return list;
    },
    //....................................................
    __gen_event_obj : function(eventType, $ar, UI, data) {
        var bus = this;

        // 格式化参数
        if(!_.isUndefined($ar) && !$z.isjQuery($ar) && !_.isElement($ar)){
            data = $ar;
            $ar = undefined;
        }

        var eo = {
            UI    : UI || this,
            area  : $ar,
            key   : $ar ? $ar.attr('wl-key') : null,
            type  : eventType,
            data  : data,
        };
        // 如果是区域
        if($ar) {
            eo.uis = bus.getAreaUIMap($ar);
        }
        // 否则就全部
        else {
            eo.uis = _.extend({}, bus.gasket);
        }
        // 返回
        return eo;
    },
    //....................................................
    // 发送消息
    fire : function(eventType, $ar, UI, data) {
        // 事件对象
        var eo = this.__gen_event_obj(eventType, $ar, UI, data);

        // 触发吧
        this.trigger(eventType, eo);
    },
    /*..............................................
    修改指定区域标题
     - arg  : 区域
     - info : {
         text : 'i18n:xxx',
         icon : '<i..>'
     }
    */
    changeAreaTitle : function(arg, info) {
        var UI = this;
        var $ar = UI.$area(arg);
        var $tt = $ar.find('>div>.wl-info>.wl-title');
        info = info || {};
        // 图标
        if(info.icon)
            $tt.find('>.wlt-icon').html(info.icon);
        else
            $tt.find('>.wlt-icon').empty();
        // 文字
        if(info.text)
            $tt.find('>.wlt-text').text(UI.text(info.text));
        else
            $tt.find('>.wlt-text').empty();
    },
    //..............................................
    on_area_ready : function($ar){
        var UI = this;
        var uis = UI.getAreaUIMap($ar, true);
        var dks = [];
        for(var key in uis) {
            var ui = uis[key];
            if(!ui) {
                var uiDef = UI.__ui_map[key];
                if(uiDef) {
                    // 诡异
                    if(uiDef.name != key) 
                        throw "Weird uiDef:" + uiDef.name
                                + " != ["+key+'] uiType:'+uiDef.uiType;
                    dks.push(key);
                }
            }
        }
        // 看来需要加载完再通知
        if(dks.length > 0) {
            UI.defer(dks, function(){
                UI.fire('area:ready', $ar, UI);
            });
            for(var i=0; i<dks.length; i++) {
                (function(key){
                    var uiDef = UI.__ui_map[key];
                    UI.__do_redraw_subUI(uiDef, function(){
                        UI.defer_report(key);
                    });
                })(dks[i]);
            }
        }
        // 不需要，直接通知吧
        else {
            UI.fire('area:ready', $ar);
        }
    },
    //..............................................
    __normalize_action_menu : function(items) {
        // 防守
        if(!_.isArray(items) || items.length <= 0)
            return [];
    
        // 迭代
        var list = [];
        for(var i=0; i<items.length; i++) {
            var mi = items[i];
        
            if(!mi)
                continue;
    
            // 组的话·递归
            if(_.isArray(mi.items)) {
                mi.items = this.__normalize_action_menu(mi.items);
                list.push(mi);
                continue;
            }
            // 开始展开吧
            // <i..>::i18n:xxx::-@do:create    # 触发消息
            if(mi.fireEvent) {
                list.push(_.extend({}, mi, {
                    handler : function(jq, mi) {
                        this.fire(mi.fireEvent);
                    }
                }));
            }
            // <i..>::i18n:xxx::~@do:create    # 触发异步消息
            else if(mi.asyncFireEvent) {
                list.push(_.extend({}, mi, {
                    asyncHandler : function(jq, mi, callback) {
                        this.fire(mi.asyncFireEvent, [callback]);
                    }
                }));
            }
            // 默认项目
            else {
                list.push(_.extend({}, mi));
            }
        }
        // 返回
        return list;
    },
    //..............................................
    __sync_tab_action : function($li) {
        var UI = this;

        var jAction = $li.closest('.wlt-tabs').find('>.wl-action');
        if(jAction.length > 0) {
            var acGasName = jAction.attr('ui-gasket-raw');
            // 看看有没有菜单可以加载
            var action = $li.data('@ACTION');
            //console.log("======= ACTION", action)
            // 木有的话，注销菜单
            if(!action) {
                if(UI.gasket[acGasName]){
                    UI.gasket[acGasName].destroy();
                }
            }
            // 装载一个新菜单
            else {
                new MenuUI(_.extend({}, action, {
                    parent : UI,
                    gasketName : acGasName
                })).render(function(){
                    $z.blinkIt(jAction);
                });
            }
        }
    },
    //..............................................
    switchTab : function($li) {
        var UI = this;
        var opt = UI.options;

        // 已经高亮了
        if($li.attr('current'))
            return;
        
        // 切换标签状态
        var ix = $li.attr('wl-tab-index') * 1;
        $li.parent().find('>[current]').removeAttr('current');
        $li.attr('current', 'yes');

        // 找到所在的菜单
        UI.__sync_tab_action($li);

        // 看看这个标签是否可以被本地存储状态
        if(opt.localKey) {
            var $tabs = $li.closest('[wl-type="tabs"]');
            var lsKey = $tabs.attr('wl-key');
            if(lsKey) {
                var tabStatus = UI.local(opt.localKey) || {};
                tabStatus[lsKey] = $li.attr('wl-tab-key');
                UI.local(opt.localKey, tabStatus);
            }
        }

        var $ar = $li.closest('[wl-type]').find('>.wn-layout-con>*')
            .attr('wl-collapse', 'yes')
                .eq(ix);
        // 显示区域
        // console.log($ar)
        UI.showArea($ar);
    },
    //..............................................
    hideArea : function(arg) {
        var UI  = this;
        var $ar = UI.$area(arg);
        // 无视
        if(!$ar || $ar.length == 0)
            return;
        // 标识
        $ar.attr('wl-collapse', "yes");
        // 对于 Box 单独设置
        UI.__resize_area($ar, true);
    },
    //..............................................
    showArea : function(arg) {
        var UI  = this;
        var $ar = UI.$area(arg);
        // 无视
        if(!$ar || $ar.length == 0)
            return;
        // 原本是隐藏的
        if($ar.attr('wl-collapse')) {
            // 标识
            $ar.removeAttr('wl-collapse');

            // 通知
            UI.fire('area:show', $ar, UI);

            // 修改尺寸
            UI.__resize_area($ar, true);
        }
        // 原本就是现实的，那么直接就报 ready
        else {
            UI.on_area_ready($ar);
        }
    },
    //..............................................
    toggleArea : function(arg) {
        var UI  = this;
        var $ar = UI.$area(arg);
        // 无视
        if(!$ar || $ar.length == 0)
            return;
        
        if($ar.attr('wl-collapse')) {
            UI.showArea($ar);
        } else {
            UI.hideArea($ar);
        }
    },
    //..............................................
    __resize_area : function($ar, enableAnimat) {
        var UI  = this;
        // 确保同步 wl-area-hide
        if('yes' != $ar.attr('wl-collapse'))
            $ar.removeAttr('wl-area-hide');
        // 对于 Box 单独设置
        if('box' == $ar.attr('wl-area')) {
            UI.__resize_box($ar, enableAnimat);
            // 动画结束后，会导致 area ready, 所以这里先不用管了
        }
        // 对于其他区域，整体 resize
        else {
            UI.resize(true);
            // 之后确保 area ready
            UI.on_area_ready($ar);
        }
    },
    //..............................................
    __resize_box : function($box, isAnimatOn) {
        var UI = this;
        //................................................
        // 开关动画
        $box.attr('animat-on', isAnimatOn ? "yes" : null);
        //................................................
        // 准备基础 css
        var base = {
            width:"",height:"",top:"",left:"",right:"",bottom:""
        };
        //................................................
        var pos = $L.getPropFromEle($box, "wlb-");
        //................................................
        // 编制计算方法
        var mW  = 40;
        var mH  = 40;
        var AW  = UI.arena.width();
        var AH  = UI.arena.height()
        var bW  = $box.outerWidth();
        var bH  = $box.outerHeight();
        var _AW = AW * -1;
        var _AH = AH * -1;
        var _bW = bW * -1;
        var _bH = bH * -1;
        // r - 自动计算的比率
        // c - 收缩时尺寸
        // auto - 自动计算的键
        // keys - 显示时设定的键
        var dockingMap = {
            "NW" : {r:1,  c:[0,0,mW,mH], auto:"",  keys:"ltwh"},
            "N"  : {r:.5, c:[null,_bH],  auto:"l", keys:"twh"},
            "NE" : {r:1,  c:[0,0,mW,mH], auto:"",  keys:"rtwh"},
            "W"  : {r:.5, c:[null,_bW],  auto:"t", keys:"lwh"},
            "P"  : {r:.5, c:[(AW-mW)/2,(AH-mH)/2,mW,mH], auto:"lt",keys:"wh"},
            "E"  : {r:.5, c:[null,_bW],  auto:"t", keys:"rwh"},
            "SW" : {r:1,  c:[0,0,mW,mH], auto:"",  keys:"lbwh"},
            "S"  : {r:.5, c:[null,_bH],  auto:"l", keys:"bwh"},
            "SE" : {r:1,  c:[0,0,mW,mH], auto:"",  keys:"rbwh"}
        };
        var dk  = dockingMap[pos.dockAt];
        // 隐藏 Box
        if('yes' == $box.attr('wl-collapse')) {
            var css = $D.rect.quick(dk.c, dk.auto+dk.keys);
            $box.css(css);
            return;
        }
        // 计算固定值
        var css = $D.rect.pick(pos, dk.keys);
        bW = $z.dimension(css.width, AW);
        bH = $z.dimension(css.height, AH);

        // 自动计算
        for(var i=0; i<dk.auto.length; i++) {
            var ak = dk.auto[i];
            // 垂直
            if("t" == ak) {
                css["top"] = (AH - bH)*dk.r;
            }
            // 水平
            else if('l' == ak){
                css["left"] = (AW - bW)*dk.r;
            }
        }
        $box.css(css);

        // 设置标题和内部主区域高度
        var $bxc  = $box.find('>.wlb-con');
        var $info = $bxc.find('>.wl-info');
        var $main = $bxc.find('>.wlb-main');
        $main.css('height', $bxc.height() - $info.outerHeight());

        // 递归内部区域
        if($main.attr('wl-type')){
            __resize_items($main);
        }
    },
    //..............................................
    resize : function() {
        var UI = this;

        // 将布局充满区域
        var jDiv = UI.arena.find('>[wl-type]').not('[wl-type="box"]').first();
        jDiv.css({
            "width"  : UI.arena.width(),
            "height" : UI.arena.height()
        })

        // 重置布局
        __resize_items(jDiv);

        // 重置 box
        UI.arena.find('>[wl-type="box"]').each(function(){
            UI.__resize_box($(this));
        });
    },
    //..............................................
    _D : function() {
        return [this.uiName+"@"+this.cid].concat(Array.from(arguments));
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);