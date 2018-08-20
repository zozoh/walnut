(function($z){
    $z.declare([
        'zui',
        'ui/layout/support/layout_methods',
        'ui/layout/support/layout_util'
    ], function(ZUI, LayoutMethods, $L){
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
    },
    //..............................................
    events : {
        // 关闭 box
        'click [wl-type="box"] > .wlb-con > .wlb-closer > b' : function(e){
            this.hideArea(e.currentTarget);
        },
        'dblclick [wl-type="box"] > .wlb-con > .wlb-info' : function(e){
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
                // 标记自己的结束状态
                $ar.attr({
                    'animat-on'    : null,
                    'wl-area-hide' : $ar.attr('wl-collapse')||null
                });
                // 重新调整子区域布局
                UI.__resize_area($ar);
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
        //console.log("before load")
        seajs.use(layoutPath, function(xml){
            //console.log("after load")
            var layout = $L.parseXml(xml);
            UI.__do_redraw(layout);
            console.log($z.toJson(layout, null, '  '));
            // 如果有 UI，那么 __do_redraw 已经把他们塞入延时了
            // 这里可以放心的释放一个延时的 key 先
            UI.defer_report("load");
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
        UI.__ui_map = $L.eachLayoutItem(layout, function(it){
            // 声明了名称，那么就要记录一下映射
            if(it.name) {
                // 重名！ 不能忍啊
                if(this[it.name])
                    throw "duplicate gasketName ["+it.name+"] in layout " + opt.layout;
                this[it.name] = $z.pick(it, ["uiType", "uiConf"]);
            }
            // 声明了 action 的话，需要定制一下名称
            if(it.action) {
                it._action_menu_name = "action_menu_" + (menu_seq++);
                this[it._action_menu_name] = {
                    uiType : 'ui/menu/menu',
                    uiConf : {
                        setup : it.action
                    }
                };
            }
        }, {});

        // 用自己配置填充一下缺失的 UI 配置（最优先嘛）
        for(var key in UI.__ui_map) {
            var val = opt.setup[key];
            if(!val)
                continue;
            if(_.isString(val)) {
                UI.__ui_map[key] = {uiType : val}
            }
            else if(val.uiType) {
                UI.__ui_map[key] = _.extend({
                    uiType : val.uiType,
                    uiConf : val.uiConf
                });
            }
        }

        // 未定义的 UI 从映射表里删掉，留着也是祸害 
        var map = {};
        for(var key in  UI.__ui_map) {
            var uiDef = UI.__ui_map[key];
            if(uiDef && uiDef.uiType)
                map[key] = uiDef;
        }
        UI.__ui_map = map;
    },
    //..............................................
    // 通常用来寻找需要加载的 gaskeName 以便调用者注册延迟加载
    getSubUINames : function() {
        return _.keys(this.__ui_map);
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
        console.log(UI.__area_map)

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
        var keys = UI.getSubUINames();
        console.log(keys)
        UI.defer(keys, function(){
            console.log("all UI loaded!");
        });

        // 下面依次绘制 DOM 节点
        UI.__build_dom(layout);

        // 下面依次加载子界面
        for(var key in UI.__ui_map) {
            var uiDef = UI.__ui_map[key];
            UI.__do_redraw_subUI(uiDef.uiType, uiDef.uiConf, key);
        }
    },
    //..............................................
    __do_redraw_subUI : function(uiType, uiConf, key) {
        var UI = this;
        var theConf = _.extend({}, uiConf, {
            parent : UI,
            gasketName : key,
            on_init : function(){
                this._bus = UI;
            }
        });
        seajs.use(uiType, function(SubUI){
            new SubUI(theConf).render(function(){
                UI.defer_report(key);
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
    //..............................................
    switchTab : function($li) {
        var UI = this;
        var ix = $li.attr('wl-tab-index') * 1;
        $li.parent().find('>[current]').removeAttr('current');
        $li.attr('current', 'yes');
        $li.closest('[wl-type]').find('>.wn-layout-con>*')
            .removeAttr('current')
                .eq(ix)
                    .attr('current', 'yes');
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
        // 标识
        $ar.removeAttr('wl-collapse');
        // 对于 Box 单独设置
        UI.__resize_area($ar, true);
    },
    //..............................................
    toggleArea : function(arg) {
        var UI  = this;
        var $ar = UI.$area(arg);
        // 无视
        if(!$ar || $ar.length == 0)
            return;
        // 标识
        $z.toggleAttr($ar, 'wl-collapse', 'yes');
        // 对于 Box 单独设置
        UI.__resize_area($ar, true);
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
        }
        // 对于其他区域，整体 resize
        else {
            UI.resize();
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
        var $info = $bxc.find('>.wlb-info');
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
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);