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
        
        // 确保自己的配置都填充上了
        for(var key in  UI.__ui_map) {
            var uiDef = UI.__ui_map[key];
            if(!uiDef || !uiDef.uiType)
                throw "gasket ["+key+"] without setup!";
        }
        console.log(UI.__ui_map)

        // 返回自己的 keySet 以便后续可以延迟加载
        return _.keys(UI.__ui_map);
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
        $L.renderDom(UI, layout.boxes, UI.arena, true);

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
    }
    //..............................................
});
//==================================================
});
})(window.NutzUtil);