define(function (require, exports, module) {
var methods = {
    __gen_base_data : function() {
        var seq = this.$el.attr("c_seq");
        return {
            "_id"   : "com"+seq,
            "_seq"  : seq * 1,
            "_type" : this.$el.attr("ctype"),
        };
    },
    getData : function() {
        return _.extend({}, $z.invoke(this,"getProp"), this.__gen_base_data());
    },
    // 通常由 hm_page::doChangeCom 调用
    setData : function(com) {
        // 得到完整的属性
        var com2 = _.extend(this.getData(), com);

        // 子类的绘制方法
        $z.invoke(this, "paint", [com2]);
    },
    // 发出属性修改通知，本函数自动合并其余未改动过的属性
    notifyChange : function(key, val) {
        this.fire("change:com", _.extend($z.obj(key,val), this.__gen_base_data()));
    },
    // 将属性设置到控件的 DOM 上
    setPropToDom : function(prop) {
        var UI = this;

        // 检查所有的控件属性
        var attrs = {};
        for(var i=0;i<UI.el.attributes.length;i++){
            var attr = UI.el.attributes[i];
            if(/^com-(.+)$/.test(attr.name))
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
    getPropFromDom : function(){
        var UI = this;

        // 分析
        var prop = {};
        for(var i=0;i<UI.el.attributes.length;i++){
            var attr = UI.el.attributes[i];
            var m = /^com-(.+)$/.exec(attr.name);
            if(m) {
                prop[$z.upperWord(m[1])] = attr.value;
            }
        }
        
        // 返回
        return prop;
    },
    // 将一个 json 描述的 CSS 对象变成 CSS 文本
    // css 对象，key 作为 selector，值是 JS 对象，代表 rule
    // prefix 为 selector 增加前缀，如果有的话，后面会附加空格
    genCssText : function(css, prefix) {
        prefix = prefix ? prefix + " " : "";
        var re = "";
        for(var selector in css) {
            re += prefix + selector;
            re += this.genCssRuleText(css[selector])
            re += "\n";
        }
        return re;
    },
    genCssRuleText : function(rule) {
        var re = "{";
        for(var key in rule) {
            var val = rule[key];
            if(_.isNumber(val)){
                val = val + "px";
            }
            re += "\n" +$z.lowerWord(key) + ":" + val + ";"
        }
        re += '\n}'
        return re;
    }
}; // ~End methods
//====================================================================
// 得到 HMaker 所有 UI 对象的方法
var HmMethods = require("app/wn.hmaker2/hm__methods");

//====================================================================
// 输出
module.exports = function(uiCom){
    return _.extend(HmMethods(uiCom), methods);
};
//=======================================================================
});