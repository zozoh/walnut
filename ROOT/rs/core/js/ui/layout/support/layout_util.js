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
getPropFromEle : function($el, prefix) {
    var obj = {};
    $.each($el[0].attributes, function(){
        if(prefix) {
            if(this.name.substring(0,prefix.length) == prefix) {
                var anm = this.name.substring(prefix.length);
                var key = $z.upperWord(anm);
                var val = $z.strToJsObj(this.value);
                obj[key] = val;
            }
        }else{
            obj[this.name] = $z.strToJsObj(this.value);
        }
    });
    return obj;
},
setPropToEle : function(obj, $el, prefix) {
    for(var key in obj) {
        var anm = (prefix||"") + $z.lowerWord(key);
        var val = obj[key];
        $el.attr(anm, val);
    }
},
//..................................................
// 从 xml 节点设置属性的帮助方法
setPropByXmlNode : function(obj, propName, $el, attrName) {
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
        $L.setPropByXmlNode(la, "key", $el);
        $L.setPropByXmlNode(la, "action", $el, ">CDATA:json");
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
            else if('box' == ittp) {
                var obj  = {type:'box'};
                $L.setPropByXmlNode(obj, "key", $it);
                $L.setPropByXmlNode(obj, "name", $it);
                $L.setPropByXmlNode(obj, "collapse", $it);
                list.push(obj);

                // 处理 title
                var $tt = $it.children('title').first();
                if($tt.length > 0) {
                    obj.title = {};
                    $L.setPropByXmlNode(obj.title, "icon",  $tt, ">CDATA");
                    $L.setPropByXmlNode(obj.title, "text",  $tt, ">CDATA");
                }
                // 处理 action
                $L.setPropByXmlNode(obj, "action", $it, ">CDATA:json");
                // 处理 pos
                obj.pos = {
                    dockAt : "P",
                    width  : "60%",  height : "60%",
                    left:0, right:0, bottom:0, top:0
                };
                var $pos = $it.children('pos').first();
                if($pos.length > 0) {
                    $L.setPropByXmlNode(obj.pos, "dockAt", $pos);
                    $L.setPropByXmlNode(obj.pos, "width",  $pos, ">CDATA");
                    $L.setPropByXmlNode(obj.pos, "height", $pos, ">CDATA");
                    $L.setPropByXmlNode(obj.pos, "left",   $pos, ">CDATA");
                    $L.setPropByXmlNode(obj.pos, "top",    $pos, ">CDATA");
                    $L.setPropByXmlNode(obj.pos, "bottom", $pos, ">CDATA");
                    $L.setPropByXmlNode(obj.pos, "right",  $pos, ">CDATA");
                }

                // 看看子是否声明了ui
                var $ui = $it.children('ui').first();
                if($ui.length > 0) {
                    $L.setPropByXmlNode(obj, "uiType", $ui, "type");
                    $L.setPropByXmlNode(obj, "uiConf", $ui, "CDATA:json");
                }
                // 否则循环递归自己的 chilren
                else {
                    var $sub = $it.children('rows,cols,tabs').first();
                    if($sub.length > 0) {
                        obj.box = {};
                        __do_it(obj.box, $sub);
                    }
                }
            }
            // row|col|tab
            else if(/^(row|col|tab)$/.test(ittp)) {
                var obj  = {};
                $L.setPropByXmlNode(obj, "key", $it);
                $L.setPropByXmlNode(obj, "name", $it);
                $L.setPropByXmlNode(obj, "size", $it);
                $L.setPropByXmlNode(obj, "collapse", $it);
                $L.setPropByXmlNode(obj, "collapseSize", $it);
                $L.setPropByXmlNode(obj, "icon",  $it, ">CDATA");
                $L.setPropByXmlNode(obj, "text",  $it, ">CDATA");
                list.push(obj);

                // 看看子是否声明了ui
                var $ui = $it.children('ui').first();
                if($ui.length > 0) {
                    $L.setPropByXmlNode(obj, "uiType", $ui, "type");
                    $L.setPropByXmlNode(obj, "uiConf", $ui, "CDATA:json");
                }
                // 否则循环递归自己的 chilren
                else {
                    var $sub = $it.children('rows,cols,tabs').first();
                    if($sub.length > 0) {
                        __do_it(obj, $sub);
                    }
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
    var $con;
    // 构建
    if(laItem.type) {
        var $div = isArena 
                    ? $('<div>').appendTo($p)
                    : $p;
        var areaType = null;   // 准备迭代 row/col 标识区域
        $div.attr({
            'wl-type'     : laItem.type || null,
            'wl-key'      : laItem.key  || laItem.name || null,
            "wl-collapse" : laItem.collapse ? "yes" : null
        });
        // 对于 box
        if('box' == laItem.type) {
            // 标识自己是区域
            $div.attr('wl-area', 'box');
            // 记录一下位置
            var pos = laItem.pos;
            if(pos) {
                this.setPropToEle(pos, $div, "wlb-");
            }
            //.....................................
            // 创建包裹器
            var $bxc = $('<div class="wlb-con">').appendTo($div);
            var $info = $('<div class="wl-info">').prependTo($bxc);
            //.....................................
            // 标题
            var btt = laItem.title || {};
            var $btt = $('<div class="wl-title">').appendTo($info);
            $('<span class="wlt-icon">').appendTo($btt);
            $('<span class="wlt-text">').appendTo($btt);
            if(!btt.icon && !btt.text)
                btt.icon = '<i class="fas fa-info-circle"></i>';
            UI.changeAreaTitle($div, btt);
            //.....................................
            // 命令
            if(laItem._action_menu_name) {
                $('<div class="wl-action">').attr({
                    "ui-gasket" : laItem._action_menu_name
                }).appendTo($info);
            }
            //.....................................
            // 内容
            $main = $('<div class="wlb-main">').appendTo($bxc);
            //.....................................
            // 关闭器
            var colserMode = /^[N]$/.test(pos.dockAt)
                                    ? pos.dockAt
                                    : "RT";
            $('<div class="wlb-closer"><b></b></div>').appendTo($bxc);
            $bxc.attr({'wlb-cm': colserMode});
            //.....................................
            // 有子内容...递归
            //console.log(laItem)
            if(laItem.box) {
                $L.renderDom(UI, laItem.box, $main);
            }
            // 否则作为插入点
            else if(laItem.name) {
                $main.attr('ui-gasket', laItem.name);
            }
        }
        // 对于 tab
        else if('tabs' == laItem.type) {
            // 标识自己是区域
            $div.attr('wl-area', 'tabs');
            // 标签栏
            var $tt = $('<div class="wlt-tabs">').prependTo($div);
            var $ul = $('<ul>').appendTo($tt);
            for(var i=0; i<laItem.tabs.length; i++) {
                var tab = laItem.tabs[i];
                var $li = $('<li>').attr({
                             "wl-tab-index" : i,
                             "wl-tab-key" : tab.key || null
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
                }).appendTo($tt);
            }
            // 内容
            $con = $('<div class="wn-layout-con">').appendTo($div);
        }
        // 对于 rows
        else if('rows' == laItem.type){
            areaType = 'row';
            $con = $('<div class="wn-layout-con">').appendTo($div);
        }
        // 对于 cols
        else if('cols' == laItem.type){
            areaType = 'col';
            $con = $('<div class="wn-layout-con">').appendTo($div);
        }
    }
    // 迭代自己的子
    if($con) {
        $L.eachLayoutChildren(laItem, function(it){
            var $it = $('<div>').attr({
                "ui-gasket" : it.name || null,
                "wl-key"    : it.key  || it.name || null,
                'wl-area'   : areaType,
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