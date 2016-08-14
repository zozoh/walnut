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
        return _.extend({}, this.getPropFromDom(), this.__gen_base_data());
    },
    // 通常由 hm_page::doChangeCom 调用
    setData : function(com) {
        // 得到完整的属性
        var com2 = _.extend(this.getData(), com);

        // 保存属性
        this.setPropToDom(com2);

        // 子类的绘制方法
        $z.invoke(this, "paint", [com2]);
    },
    // 发出属性修改通知，本函数自动合并其余未改动过的属性
    notifyChange : function(key, val) {
        this.fire("change:com", _.extend($z.obj(key,val), this.__gen_base_data()));
    },
    // 将属性设置到控件的 DOM 上
    setPropToDom : function(prop) {
        var jPropEle = this.$el.children("script.hmc-th-prop-ele");
        if(jPropEle.size() == 0) {
            jPropEle = $('<script type="text/x-template" class="hmc-th-prop-ele">').prependTo(this.$el);
        }
        jPropEle.html("\n"+$z.toJson(prop,null,'    ')+"\n");
    },
    // 从控件的 DOM 上获取控件的属性
    getPropFromDom : function(){
        var jPropEle = this.$el.children("script.hmc-th-prop-ele");
        if(jPropEle.size() > 0){
            var json = jPropEle.html();
            return $z.fromJson(json);
        }
        // 返回空
        return {};
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
            re += "\n" +$z.lowerWord(key) + ":" + val + ";";
        }
        re += '\n}';
        return re;
    },
    // 判断一个 DOM 元素是否在一个激活的块中
    isInActivedBlock : function(jq) {
        return $(jq).closest(".hm-block[hm-actived]").size() > 0;
    },
    // 在属性面板的扩展元素接口，绘制自定义 UI
    drawComEleInProp : function(uiDef, callback) {
        this.propUI("edit").drawComEle(uiDef, callback);
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