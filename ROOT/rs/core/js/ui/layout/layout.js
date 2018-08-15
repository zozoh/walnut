(function($z){
    $z.declare([
        'zui',
        'ui/layout/support/layout_methods',
        'ui/layout/support/layout_util'
    ], function(ZUI, LayoutMethods, $L){
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
        LayoutMethods(this);
    },
    //..............................................
    events : {
        
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

        // 用自己配置填充一下（最优先嘛）
        _.extend(UI.__ui_map, opt.setup);
        
        // 寻找需要加载的 gaskeName 以便调用者注册延迟加载
        var keys = [];
        for(var key in  UI.__ui_map) {
            var uiDef = UI.__ui_map[key];
            if(uiDef && uiDef.uiType)
                keys.push(key);
        }

        // 返回以便延迟加载
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

        // 编译 gasket
        //UI.rebuildGaskets();
    },
    //..............................................
    __do_redraw : function(layout, callback) {
        var UI  = this;
        var opt = UI.options;

        // 首先编制一下自己的 gasketName 映射表
        var keys = UI.__build_ui_map(layout);
        //UI.defer(keys);

        // 下面依次绘制 DOM 节点
        UI.__build_dom(layout);
    },
    //..............................................
    resize : function() {
        var UI = this;

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
            var $box = $(this);

            // 整体宽高
            var pos = $z.fromJson($box.attr('wl-box-pos'));

            // 准备基础 css
            var base = {
                width:"",height:"",top:"",left:"",right:"",bottom:""
            };

            // 编制计算方法
            var dockingMap = {
                "NW" : {auto:[],       keys:["left","top","width","height"]   ,},
                "N"  : {auto:["left"], keys:["top","width","height"]},
                "NE" : {auto:[],       keys:["right","top","width","height"]},
                "W"  : {auto:["top"],  keys:["left","width","height"]},
                "P"  : {auto:["left","top"], keys:["width","height"]},
                "E"  : {auto:["top"],  keys:["right","width","height"]},
                "SW" : {auto:[],       keys:["left","bottom","width","height"]},
                "S"  : {auto:["left"], keys:["bottom","width","height"]},
                "SE" : {auto:[],       keys:["right","bottom","width","height"]}
            };
            // 计算
            var dk  = dockingMap[pos.dockAt];
            var css = $z.pick(pos, dk.keys);
            $box.css(_.extend({},base,css));

            // 自动计算
            css = {};
            for(var i=0; i<dk.auto.length; i++) {
                var ak = dk.auto[i];
                // 垂直
                if("top" == ak) {
                    css[ak] = (UI.arena.height() - $box.outerHeight())/2;
                }
                // 水平
                else if('left' == ak){
                    css[ak] = (UI.arena.width() - $box.outerWidth())/2;
                }
            }
            $box.css(css);

            // 设置标题和内部主区域高度
            var $info  = $box.find('>.wlt-tabs');
            var $main = $box.find('>.wlb-main');
            $main.css('height', $box.height() - $info.outerHeight());
        });
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);