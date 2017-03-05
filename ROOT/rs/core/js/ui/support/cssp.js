/**
 * 提供了 CSS 的解析和渲染功能
 */
define(function (require, exports, module) {
// ....................................
// 方法表
var CssP = {
    //.........................................................
    // 根据一个 Map 描述的CSS规则集合，生成 CSS 文本，
    // cssObj 的键就是选择器，值是一个 Map 为规则表
    // prefix 表示是否为所有的选择器添加统一前缀
    genCss : function(cssObj, prefix){
        var re = "";
        for(var selector in cssObj) {
            if(!prefix && !selector)
                continue;
            re += (prefix||"") + " " + (selector||"") + "{\n";
            re += this.genCssRule(cssObj[selector]);
            re += "}\n";
        }
        return re;
    },
    //.........................................................
    // 根据一个 Map 描述的CSS规则，生成 CSS 文本，
    // cssRule 为一组规则的集合，键为css属性，比如 background,color 等
    genCssRule : function(cssRule) {
        var re = "";
        for(var key in cssRule) {
            var v = cssRule[key];
            if(v){
                re += key + ":" + v;
                if(_.isNumber(v))
                    re += "px";
                re += ";\n";
            }
        }
        return re;
    },
    /*.........................................................
    解析 background 属性
    输入字符串格式类似:
    
        padding-box? scroll? #FFF url("/ss/ss/ss/ss") no-repeat  center/cover 

    会被解析成:
    {
        backgroundColor    : "#FFF",
        backgroundImage    : "url(xxx)",
        backgroundPosition : "0% 0% | center center",
        backgroundSize     : "auto auto | 100% 100% | contain | cover",
        backgroundRepeat   : "repeat | no-repeat",
        backgroundOrigin   : "padding-box | border-box  | content-box | inherit",
        backgroundAttachment : "scroll | fixed"
    }
    */
    parseBackground : function(str) {
        // 如果本身就是背景，返回
        if(_.isObject(str)) {
            return _.extend({}, str);
        }

        // 首先整理字符串，去掉多余的空格，确保 backgroundPosition|backgroundSize 之间是没有空格的
        var s = (str||"")
                    .replace(/[ ]{2,}/g, " ")
                    .replace(/[ ]*([\/,])[ ]*/g, "$1")
                    .replace(/[ ]\)/g, ")")
                    .replace(/\([ ]/g, "(");

        // 正则表达式拼装
        // 1: backgroundColor
        var R = "(#[0-9a-f]{3,}|rgba?\\([\\d, .]+\\))";
        // 2: backgroundImage
        R += "|(url\\([^\\)]+\\))";
        // 3: 组合 backgroundPosition / backgroundSize 的组合
        R += "|(";
        // 4: backgroundPosition : 2 子表达式
        R += "(top|left|bottom|right|center|\\d+(%|em|px|cm|ch) \\d+(%|em|px|cm|ch))";
        // 7: backgroundSize : 3 子表达式
        R += "/(cover|contain|\\d+(%|em|px)( \\d+(%|em|px))?|auto( auto)?)";
        R += ")";
        // 12: backgroundRepeat
        R += "|(repeat|no-repeat)";  
        // 13: backgroundOrigin : 1 子表达式
        R += "|((padding|border|content)-box)";
        // 15: backgroundAttachment
        R += "|(scroll|fixed)";
        var regex = new RegExp(R, "gi");

        // 准备赋值
        var indexes = {
            backgroundColor      : 1,
            backgroundImage      : 2,
            backgroundPosition   : 4,
            backgroundSize       : 7,
            backgroundRepeat     : 12,
            backgroundOrigin     : 13,
            backgroundAttachment : 15
        };

        // 准备返回对象
        var bg = {};

        // 循环解析字符串
        var m;
        while ((m = regex.exec(s)) !== null) {
            for(var key in indexes){
                var index = indexes[key];
                if(m[index]){
                    bg[key] = m[index];
                }
            }
        }

        // 搞定收工
        return bg;
    },
    //.........................................................
    // 将背景字符串变成一行字符串表示
    strBackground : function(bg) {
        var re = [];
        for(var key in bg) {
            // 忽略这两东东，因为需要连写
            if(/^background(Position|Size)$/.test(key))
                continue;
            // 其他的加入
            re.push(bg[key]);
        }
        // 最后整合 backgroundPosition 和 backgroundSize
        var pos_sz = [];
        if(bg.backgroundPosition)
            pos_sz.push(bg.backgroundPosition);
        if(bg.backgroundSize){
            // 指定了 size 则必须指定一个背景位置
            if(pos_sz.length == 0)
                pos_sz.push("left");
            pos_sz.push(bg.backgroundSize);
        }
        if(pos_sz.length > 0)
            re.push(pos_sz.join("/"));

        // 返回
        return re.join(" ");
    },
    //.........................................................
    // 背景的解析和渲染的测试
    __test_background : function(){
        // 定义测试函数
        var _T = function(str, bgColor){
            var bg  = CssP.parseBackground(str);
            var s2  = CssP.strBackground(bg);
            var bg2 = CssP.parseBackground(s2);
            if(_.isEqual(bg, bg2)){
                if(_.isUndefined(bgColor)){
                    console.log("OK", str);
                }
                // 验证一下颜色
                else if(bg.backgroundColor != bgColor){
                    console.warn("!!", str);
                    console.warn("  -- s2:", s2);
                    console.warn("  -- bg:", bg);
                    console.warn("  -- bg2:", bg2);    
                }
            }
            else{
                console.warn("!!", str);
                console.warn("  -- s2:", s2);
                console.warn("  -- bg:", bg);
                console.warn("  -- bg2:", bg2);
            }
        }
        // 逐个执行测试用例
        _T("#FFF", "#FFF");
        _T("rgb( 255, 255,  32  )", "rgb(255,255,32)");
        _T("rgba( 255  , 255,  32, 0.5 )", "rgba(255,255,32,0.5)");
        _T("#FFF url(A.png) no-repeat");
        _T("rgb(255, 255, 32) url(a.png) no-repeat left/conver");
        _T("#FFF url(a.png) no-repeat 0% 0%/cover");
        _T("rgba(255,255,32, 0.5) url(a.png) repeat top/100% auto border-box fixed");
    },
    //.........................................................
}; // ~End methods
//====================================================================
// 输出
return _.extend(exports, CssP);
//=======================================================================
});
