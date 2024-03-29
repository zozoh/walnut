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
            if(_.isString(o) || _.isNumber(o))
                return o;
            return o.text;
        });
        $z.setUndefined(opt, "value", function(o, index, UI){
            if(_.isString(o)) {
                if(opt.textAsValue)
                    return o;
                return index;
            }
            if(_.isNumber(o))
                return o;
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
        //console.log("redraw enum_list")

        if(opt.drawOnSetData) {
            return;
        }

        // 读取数据
        var re = ["loading"];
        UI.setItems(UI.options.items, function(){
            UI.defer_report("loading",true);
        });

        // 返回，以便异步的时候延迟加载
        return re;
    },
    //...............................................................
    setData : function(val, jso, obj){
        var UI  = this;
        var opt = UI.options;
        //console.log("I am enum setData");
        // 存储旧值
        UI.__old_val = val;

        // 设置到控件
        this.ui_parse_data(val, function(v){
            // 动态的话，首先重新设置一下项目
            if(opt.dynamic) {
                UI.__load_items(opt.items, function(){
                    UI._set_data(v);    
                }, obj);
            }
            // 静态的话，直接设置就好
            else {
                UI._set_data(v);
            }
        });
    },
    //...............................................................
    __load_items : function(items, callback, params) {
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;
        
        $z.evalData(items, params || opt.itemArgs, function(list){
            var list2;
            // 应用过滤器
            if(_.isFunction(opt.filter)) {
                list2 = [];
                for(var i=0;i<list.length;i++) {
                    var o = list[i];
                    if(opt.filter.apply(context, [o]))
                        list2.push(o);
                }
            }
            // 直接使用
            else {
                list2 = list;
            }
            // 绘制
            UI._draw_items(list2);
            // 调用回调
            $z.doCallback(callback, [list2], UI);
        }, UI);
    },
    //...............................................................
    setItems : function(items, callback, params){
        var UI  = this;
        var opt = UI.options;

        // 动态的话，只有 setData 的时候才会调用
        if(opt.dynamic) {
            $z.doCallback(callback, [], UI);
        }
        // 静态的话，先初始化
        else {
            UI.__load_items(items, callback, params);
        }
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
var ParentMethods = require("ui/form/support/form_ctrl");

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(ParentMethods(uiSub), methods);
};
//=======================================================================
});
