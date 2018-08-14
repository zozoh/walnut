/**
 * 本函数集提供 Layout 需要的帮助函数支持
 *
 * @author  zozoh(zozohtnt@gmail.com)
 * 2012-10 First created
 */
(function () {
//..................................................
var WnLayout = {
//..................................................
// 从 xml 节点设置属性的帮助方法
setPropByEle : function(obj, propName, $el, attrName) {
    attrName = attrName || $z.lowerWord(propName);
    var m = /^(>)?(CDATA)(:(json))?$/.exec(attrName);

    // 走子节点
    if(m && '>' == m[1])
        $el = $el.children(propName).first();

    // 木有就算了
    if($el.length == 0)
        return;

    var str = $.trim(m ? $el.text() : $el.attr(attrName));

    // 木有值
    if(!str)
        return;

    // 准备返回值
    var val;

    // 走 CDATA:json
    if(m && 'json' == m[4]) {
        val = $z.fromJson(str);
    }
    // 否则走属性
    else {
        if("0" == str) {
            val = 0;
        }
        // 自动格式化整数为像素值
        else if(/^[0-9.]+$/.test(str)) {
            val = str * 1 + "px";
        }
        // 布尔值
        else if(/^(yes|no)$/.test(str)) {
            val = ("yes" == str);
        }
        // 其他值
        else {
            val = str;
        }
    }
        
    // 设置值
    obj[propName] = val;
},
//..................................................
// 迭代：树
eachLayoutItem : function(layout, callback, context) {
    var list = layout[layout.type];
    if(list && _.isArray(list) && list.length > 0) {
        for(var i=0; i<list.length; i++) {
            var it = list[i];
            var re = callback.apply(context||this, [it]);
            if(false !== re) {
                this.eachLayoutItem(it, callback, context);
            }
        }
    }
    // 迭代一下 boxes
    if(layout.boxes && _.isArray(layout.boxes) && layout.boxes.length > 0) {
        for(var i=0; i<layout.boxes.length; i++) {
            var it = layout.boxes[i];
            callback.apply(context||this, [it]);
        }
    }
    // 返回上下文
    return context;
},
//..................................................
// 迭代：子
eachLayoutChildren : function(layout, callback, context) {
    var list = _.isArray(layout) ? layout : layout[layout.type];
    if(list && _.isArray(list) && list.length > 0) {
        for(var i=0; i<list.length; i++) {
            var it = list[i];
            callback.apply(context||this, [it]);
        }
    }
},
//..................................................
// 解析 layout.xml 的布局语法
// 将给入的 XML 节点，转化成 json 对象
//  - xml : 如果是字符串，表示 XML 代码，或者是 xml 节点对象
// @return JSON 格式的 layout 语法
parseXml : function(xml) {
    if(!xml)
        return null;
    // 确保为 XML 节点
    if(_.isString(xml))
        xml = $.parseXML(xml);
    // console.log(new XMLSerializer().serializeToString(xml))
    // 准备递归解析函数  boxes|rows|cols|tabs
    var __do_it = function(la, $el) {
        // 元素的 tagName 就是类型
        var tp  = $el[0].tagName.toLowerCase();
        if('boxes'!=tp)
            la.type = tp;
        // 通用的 actionMenu
        $L.setPropByEle(la, "action", $el, ">CDATA:json");
        // 根据类型获取  box|row|col|tab
        var subType = tp.substring(0,3);
        //console.log(subType)
        var list = [];
        $el.children().each(function(){
            var $it  = $(this);
            var ittp = this.tagName.toLowerCase();
            // bar
            if('bar' == ittp) {
                list.push({type : 'bar'});
            }
            // box|row|col|tab
            else if(subType == ittp) {
                var obj  = {};
                $L.setPropByEle(obj, "name", $it);
                $L.setPropByEle(obj, "size", $it);
                $L.setPropByEle(obj, "collapse", $it);
                $L.setPropByEle(obj, "collapseSize", $it);
                $L.setPropByEle(obj, "icon",  $it, ">CDATA");
                $L.setPropByEle(obj, "text",  $it, ">CDATA");
                list.push(obj);

                // 如果是 box 也声明上自己的 type 
                if('box' == ittp) {
                    obj.type = ittp;
                    // 处理 title
                    var $tt = $it.children('title').first();
                    if($tt.length > 0) {
                        obj.title = {};
                        $L.setPropByEle(obj.title, "icon",  $tt, ">CDATA");
                        $L.setPropByEle(obj.title, "text",  $tt, ">CDATA");
                    }
                    // 处理 action
                    $L.setPropByEle(obj, "action", $it, ">CDATA:json");
                    // 处理 pos
                    obj.pos = {
                        dockAt : "P",
                        width  : "60%",  height : "60%",
                        left:0, right:0, bottom:0, top:0
                    };
                    var $pos = $it.children('pos').first();
                    if($pos.length > 0) {
                        $L.setPropByEle(obj.pos, "dockAt", $pos);
                        $L.setPropByEle(obj.pos, "width",  $pos, ">CDATA");
                        $L.setPropByEle(obj.pos, "height", $pos, ">CDATA");
                        $L.setPropByEle(obj.pos, "left",   $pos, ">CDATA");
                        $L.setPropByEle(obj.pos, "top",    $pos, ">CDATA");
                        $L.setPropByEle(obj.pos, "bottom", $pos, ">CDATA");
                        $L.setPropByEle(obj.pos, "right",  $pos, ">CDATA");
                    }
                }

                // 看看子是否声明了ui
                var $ui = $it.children('ui').first();
                if($ui.length > 0) {
                    $L.setPropByEle(obj, "uiType", $ui, "type");
                    $L.setPropByEle(obj, "uiConf", $ui, "CDATA:json");
                }
                // 否则循环递归自己的 chilren
                else if('box' != ittp) {
                    $it.children('rows,cols,tabs').each(function(){
                        __do_it(obj, $(this));
                    });
                }
            }
        });
        // 计入全局列表
        if(list.length > 0)
            la[tp] = list;
    };
    // 准备解析结果
    var la = {};
    // 开始解析
    $(xml.documentElement).children('rows,cols,tabs,boxes').each(function(){
        __do_it(la, $(this));
    });
    // 返回解析结果
    return la;
},
//..................................................
// 将 layout 项目渲染到一个 DOM 节点
renderDom : function(UI, laItem, $p, isArena) {
    var $con = $p;
    // 构建
    if(laItem.type) {
        var $div = isArena 
                    ? $('<div>').appendTo($p)
                    : $p;
        $div.attr('wl-type',laItem.type);
        $con = $('<div class="wn-layout-con">').appendTo($div);
        // 对于标签
        if('tabs' == laItem.type) {
            var $tabs = $('<div class="wlt-tabs">').prependTo($div);
            // 标签
            var $ul = $('<ul>').appendTo($tabs);
            for(var i=0; i<laItem.tabs.length; i++) {
                var tab = laItem.tabs[i];
                console.log(tab)
                var $li = $('<li>').attr({
                             "wl-tab-name" : tab.name
                          }).appendTo($ul);
                if(tab.icon) {
                    $(tab.icon).appendTo($li);
                }
                if(tab.text) {
                    $('<span>').text(UI.text(tab.text)).appendTo($li);
                }
            }
            // 命令
            if(laItem._action_menu_name) {
                $('<div class="wl-action">').attr({
                    "ui-gasket" : laItem._action_menu_name
                }).appendTo($tabs);
            }
        }
        // 对于 box
        else if('box' == laItem.type 
                && (laItem.title || laItem._action_menu_name)) {
            var $info = $('<div class="wlt-tabs">').prependTo($div);
            // 标题
            var btt = laItem.title;
            if(btt) {
                var $btt = $('<div class="wlbt-title">').appendTo($info);
                if(btt.icon) {
                    $(btt.icon).appendTo($btt);
                }
                if(btt.text) {
                    $('<span>').text(UI.text(btt.text)).appendTo($btt);
                }
            }
            // 命令
            if(laItem._action_menu_name) {
                $('<div class="wl-action">').attr({
                    "ui-gasket" : laItem._action_menu_name
                }).appendTo($info);
            }
        }
    }
    // 迭代自己的子
    $L.eachLayoutChildren(laItem, function(it){
        var $it = $('<div>').attr({
            "ui-gasket" : it.name || null,
            "wl-size"   : it.size || null,
            "wl-collapse-size" : it.collapseSize || null,
            "wl-collapse" : it.collapse ? "yes" : null
        });
        // 递归
        $L.renderDom(UI, it, $it);
        // 加入 DOM
        $it.appendTo(this);
    }, $con);
}
//..................................................
};  // ~ End Of WnLayout
//..................................................
// 挂载到 window 对象
window.WnLayout = WnLayout;
window.$L = WnLayout;

// AMD | CMD
$z.defineModule("walnutLayoutUtil", WnLayout);
//===================================================================
})();