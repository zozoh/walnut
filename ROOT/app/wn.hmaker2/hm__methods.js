define(function (require, exports, module) {
// ....................................
// 块的 CSS 属性基础对象
var _css_base = {
    position:"",top:"",left:"",width:"",height:"",right:"",bottom:"",
    margin:"",padding:"",border:"",borderRadius:"",
    color:"",background:"",
    overflow:"",boxShadow:"",
};
// ....................................
// 方法表
var methods = {
    hmaker : function(){
        var UI = this;
        while(!UI.__hmaker__ && UI) {
            UI = UI.parent;
        }
        return UI;
    },
    // 得到站点主目录
    getHomeObj : function() {
        var uiHMaker = this.hmaker();
        return Wn.getById(uiHMaker.__home_id);
    },
    // 监听消息
    listenBus : function(event, handler){
        var uiHMaker = this.hmaker();
        this.listenUI(uiHMaker, event, handler);
    },
    // 发送消息
    fire : function() {
        var args = Array.from(arguments);
        var uiHMaker = this.hmaker();
        // console.log("fire", args)
        uiHMaker.trigger.apply(uiHMaker, args);
    },
    // 得到 HmPageUI，如果不是，则抛错
    pageUI : function(quiet) {
        var UI = this;
        var uiHMaker = this.hmaker();
        var re = uiHMaker.gasket.main;
        // 严格模式
        if(!quiet){
            if(!re){
                throw 'PageUI Not Loadded!';
            }
            if(re.uiName != "app.wn.hmaker_page"){
                throw 'Not A PageUI: ' + re.uiName;
            }
        }
        if(re && re.uiName != "app.wn.hmaker_page")
            return null;
        return re;
    },
    // 获取 prop UI 下面的子 UI，如果类型未定义，则返回 prop UI 本身
    propUI : function(uiPath) {
        var uiHMaker = this.hmaker();
        var uiProp   = uiHMaker.gasket.prop;
        if(uiPath)
            return uiProp.subUI(uiPath);
        return uiProp;
    },
    // 将 rect 按照 posBy 转换成 posVal 字符串
    transRectToPosVal : function(rect, posBy) {
        var re = _.mapObject($z.rectObj(rect, posBy), function(val){
            return Math.round(val) + "px";
        });
        return _.values(re).join(",");
    },
    // 将 CSS 对象与 base 合并，并将内部所有的 undefined 和 null 都变成空串
    formatCss : function(css, mergeWithBase) {
        // 传入了 base 对象
        if(_.isObject(mergeWithBase)){
            css = _.extend({}, mergeWithBase, css);
        }
        // 与默认 base 对象合并
        else if(mergeWithBase) {
            css = _.extend({}, _css_base, css);   
        }

        // 将所有的 undefined 和 null 都变成空串，表示去掉
        // 如果 key 以 _ 开头，则会被删除掉
        var re = {};
        for(var key in  css) {
            if(/^_/.test(key))
                continue;
            var val = css[key];
            if(_.isUndefined(val) || _.isNull(val))
                re[key] = "";
            else
                re[key] = val;
        }

        // 返回新创建的对象 
        return re;
    },
    // 返回 base_css 的一个新实例
    getBaseCss : function() {
        return _.extend({}, _css_base);
    }

}; // ~End methods
//====================================================================

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});

/* TODO 应该可以删了 ...
// 将属性设置到控件的 DOM 上
    setPropToElement : function(ele, prefix, prop) {
        ele = $(ele)[0];

        // 准备属性键解析的正则表达式
        var regex = new RegExp("^"+prefix+"(.+)$");

        // 检查所有的控件属性
        var attrs = {};
        for(var i=0;i<ele.attributes.length;i++){
            var attr = ele.attributes[i];
            if(regex.test(attr.name))
                attrs[attr.name] = null;   // 设成 null 表示删除
        }

        // 分析
        for(var key in prop) {
            if(!/^_/.test(key)){
                var attrName = "com-" + $z.lowerWord(key);
                var val = prop[key];
                if(_.isUndefined(val) || $z.isEmptyString(val)) {
                    val = null;
                }
                attrs[attrName] = val;
            }
        }

        // 记录
        UI.$el.attr(attrs);

    },
    // 从控件的 DOM 上获取控件的属性
    getPropFromElement : function(ele, prefix){
        ele = $(ele)[0];

        // 准备属性键解析的正则表达式
        var regex = new RegExp("^"+prefix+"(.+)$");

        // 分析
        var prop = {};
        for(var i=0;i<ele.attributes.length;i++){
            var attr = ele.attributes[i];
            var m = regex.exec(attr.name);
            if(m) {
                prop[$z.upperWord(m[1])] = attr.value;
            }
        }
        
        // 返回
        return prop;
    },
*/