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
    // 获取组件的属性
    getData : function() {
        return _.extend({}, this.getPropFromDom(), this.__gen_base_data());
    },
    // 通常由 hm_page::doChangeCom 调用
    setData : function(com, shallow) {
        // 直接无视吧
        if(com && com.__com_ignore_setData)
            return;

        // 执行更新
        var com2 = this.updateProp(com, shallow);

        // 更新皮肤
        if(com2._skin_old)
            this.$el.removeClass(com2._skin_old);
        if(com2._skin)
            this.$el.addClass(com2._skin);

        // 子类的绘制方法
        $z.invoke(this, "paint", [com2]);
    },
    // 合并修改组件的属性，并保存
    updateProp : function(com, shallow) {
        // 得到完整的属性
        var com2 = shallow ? _.extend(this.getData(), com)
                           : $z.extend(this.getData(), com);

        // // 删除不必要保存东东
        // delete com2.__com_ignore_setData;
        // delete com2.__prop_ignore_update;

        // 保存属性
        this.setPropToDom(com2);

        return com2;
    },
    // 发出属性修改通知，本函数自动合并其余未改动过的属性
    notifyChange : function(key, val) {
        // 如果 key 都未定义，那么表示发出这个消息的组件不希望引起自己的属性的保存和界面的绘制
        // 通常，这种 com 都是通过自身的 DOM 来存放某些数据的，它修改了自身的 DOM 
        // 剩下的就是通知 prop 那边刷新显示而已
        if(!key) {
            this.fire("change:com", _.extend(this.getData(), {__com_ignore_setData:true}), this.$el);
        } 
        // 如果 key 不是字符串，或者里面包含了 "." 那么可能是多层的 key
        // $z.setValue 提供了这个逻辑的封装 
        else {
            var prop = $z.extend(this.getData(), $z.setValue({}, key, val));
            this.fire("change:com", _.extend(prop, this.__gen_base_data()), this.$el);
        }
    },
    // 根据 Block 的属性设置，得到它的应该的矩形对象
    getBlockRectByProp : function(prop) {
        var keys = prop.posBy.split(",");
        var vals = prop.posVal.split(",");
        var rect = {};
        for(var i in keys) {
            rect[keys[i]] = $z.toPixel(vals[i]);
        }
        return $z.rect_count_auto(rect, true);
    },
    // 将属性设置到控件的 DOM 上
    setPropToDom : function(com) {
        $z.setJsonToSubScriptEle(this.$el, "hmc-prop-ele", com, true);
    },
    // 从控件的 DOM 上获取控件的属性
    getPropFromDom : function(){
        return $z.getJsonFromSubScriptEle(this.$el, "hmc-prop-ele");
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
        return $(jq).closest(".hm-block").attr("hm-actived") == "yes";
    },
    // 判断控件自身是否是 Actived
    isActived : function() {
        return this.$el.closest(".hm-block").attr("hm-actived") == "yes";
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