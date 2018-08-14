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
setPropByEleAttr : function(obj, propName, $el, attrName) {
    attrName = attrName || $z.upperWord(propName);
    var val = $el.attr(attrName);
    if(val) {
        // 自动格式化整数
        if(/^[0-9.]+$/.test(val))
            val = val * 1;
        // 布尔值
        else if(/^(yes|no)$/.test(val))
            val = ("yes" == val);
        // 设置值
        obj[propName] = val;
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
        xml = $(xml);
    // 准备递归解析函数
    var __do_it = function(la, ele) {
        var $el = $(this);
        var tp  = ele.tagName.toLowerCase();
        // UI
        if('ui' == tp) {

        }
        // box|rows|cols|tabs
        else if(/^(box|rows|cols|tabs)$/.test(tp)) {
            $L.setPropByEleAttr(la, "name", $el);
            $L.setPropByEleAttr(la, "size", $el);
            $L.setPropByEleAttr(la, "collapse", $el);
            // Box 特殊的设置
            if('box' == tp) {

            }
            // rows | cols 的特殊设置
            else {
                $L.setPropByEleAttr(la, "collapse-size", $el);
            }
        }
        // !靠不认识
        else {
            console.warn("Unknown ele when parseLayoutXML", ele, la);
            throw "Unknown ele when parseLayoutXML";
        }
    };
    // 准备解析结果
    var la = {};
    // 开始解析
    xml.children().each(functoin(){
        var jIt = $(this);
        if(this.tagName == 'rows') {

        }

    });
}
//..................................................
};  // ~ End Of zUtil
//..................................................
// 挂载到 window 对象
window.WnLayout = WnLayout;
window.$L = WnLayout;

// AMD | CMD
$z.defineModule("walnutLayoutUtil", WnLayout);
//===================================================================
})();