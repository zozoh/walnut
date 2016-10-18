define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    //...............................................................
    __setup_dft_display_func : function(opt) {
        $z.setUndefined(opt, "icon", function(o){
            if(_.isObject(o)) 
                return o.icon;
        });
        $z.setUndefined(opt, "text", function(o){
            if(_.isString(o))
                return o;
            return o.text;
        });
        $z.setUndefined(opt, "value", function(o, index){
            if(_.isString(o))
                return index;
            if(!_.isUndefined(o.value)) {
                return o.value;
            }
            if(!_.isUndefined(o.val)) {
                return o.val;
            }
            return index;
        });
    },
    //...............................................................
    redraw : function(){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI.parent;

        // 列表绘制前附加逻辑
        $z.invoke(UI, "_before_load", []);

        // 读取数据
        var re = ["loading"];
        UI.setItems(UI.options.items, function(){
            re.pop();
            UI.defer_report(0, "loading");
        });

        // 返回，以便异步的时候延迟加载
        return re;
    },
    //...............................................................
    setItems : function(items, callback){
        var UI  = this;
        var opt = UI.options;
        var context = _.extend({}, opt.context || UI.parent, {
            app  : UI.app,
            exec : UI.exec
        });

        $z.evalData(items, null, function(items){
            UI._draw_items(items);
            UI.setData();
            $z.doCallback(callback, [items], UI);
        }, context);
    },
    //...............................................................
    refresh : function(callback) {
        var UI  = this;
        var opt = UI.options;
        
        UI.setItems(opt.items, callback);
    },
    //...............................................................
}; // ~End methods
//====================================================================
// 得到枚举列表的顶级方法 
var ParentMethods = require("ui/form/support/form_c_methods");

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(ParentMethods(uiSub), methods);
};
//=======================================================================
});
