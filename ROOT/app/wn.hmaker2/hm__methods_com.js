define(function (require, exports, module) {
var methods = {
    __gen_base_data : function() {
        return {
            "_seq"  : this.$el.attr("com-seq") * 1,
            "_type" : this.$el.attr("ctype"),
        };
    },
    getData : function() {
        return _.extend({}, $z.invoke(this,"getProp"), this.__gen_base_data());
    },
    // 通常由 hm_page::doChangeCom 调用
    setData : function(com) {
        // 检查是否合法
        // if(this.$el.attr("ctype") != com._type)
        //     throw "Unmatched com type:" + com._type;

        // if(this.$el.attr("com-seq") != com._seq)
        //     throw "Unmatched com seq:" + com._seq;

        // 得到完整的属性
        var com2 = _.extend(this.getData(), com);

        // 子类的绘制方法
        $z.invoke(this, "paint", [com2]);
    },
    notifyChange : function(key, val) {
        this.fire("change:com", _.extend($z.obj(key,val), this.__gen_base_data()));
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