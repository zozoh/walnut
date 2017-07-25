/*
专门处理数组型数据
new ArrayUI({
    items : [],     // 备选值数组
    groupSize : 0,  // 数组会分组显示，多少个值为一组，小于等于0表示不分组,即只有一组
    multi : true,   // 默认true，getData() 只返回一个值，否则一定返回一个数组,
    text : F(val):String  // 如何显示值，默认直接显示值
}).render();
*/
(function($z){
$z.declare([
    'zui',
    'ui/form/support/form_ctrl',
], function(ZUI, FormMethods){
//==============================================
var html = function(){/*
<div class="ui-arena com-array"></div>
*/};
//===================================================================
return ZUI.def("ui.form_com_array", {
    //...............................................................
    dom  : $z.getFuncBodyAsStr(html.toString()),
    //...............................................................
    init : function(opt){
        FormMethods(this);

        // 设置默认值
        $z.setUndefined(opt, "items", []);
        $z.setUndefined(opt, "groupSize", 0);
        $z.setUndefined(opt, "multi", true);
        $z.setUndefined(opt, "text", function(val){
            return val;
        });
    },
    //...............................................................
    events : {
        // 鼠标按下
        'mousedown li' : function(e) {
            // 只对鼠标左键有效
            if(1!=e.which)
                return;

            var UI  = this;
            var opt = UI.options;
            var jLi = $(e.currentTarget);
            var old_val = UI._get_data();

            // 如果单选，去掉之前的高亮
            if(!opt.multi)
                UI.arena.find('li[current]').removeAttr("current");

            // 首先 toggle 当前的值
            $z.toggleAttr(jLi, "current", "yes");

            // 标识一下，当 mouseover 时会改变当前
            if(opt.multi) {
                UI.__mark_current_yes = jLi.attr("current") ? true : false;
            }

            // 绑定 mouseup 事件
            var win = jLi[0].ownerDocument.defaultView;
            $(win).one("mouseup", function(){
                // 去掉标识
                UI.__mark_current_yes = undefined;
                // 看看有没有改动，有的话，做通知
                var new_val = UI._get_data();
                if($z.toJson(new_val) != $z.toJson(old_val)){
                    UI.__on_change();
                }
            });
        },
        // 鼠标移动时，对于多选，批量标识属性
        'mouseenter li' : function(e) {
            var UI  = this;
            var opt = UI.options;
            var jLi = $(e.currentTarget);
            
            if(_.isBoolean(UI.__mark_current_yes)) {
                jLi.attr("current", UI.__mark_current_yes ? "yes" : null);
            }
        }
    },
    //...............................................................
    redraw : function(){
        this.setItems();
    },
    //...............................................................
    setItems : function(items) {
        var UI  = this;
        var opt = UI.options;

        // 清空选区
        UI.arena.empty();

        // 得到侯选值数组，绘制侯选值
        items = $z.evalObjValue(items || opt.items) || [];

        // 循环绘制
        var jUl;
        for(var i=0; i<items.length; i++) {
            // 创建分组: 第一次 || 组大小为1 || 下标到了组大小
            if(i == 0 
               || opt.groupSize == 1 
               || (opt.groupSize > 0 && (i%opt.groupSize) == 0)) {
                jUl = $('<div class="com-array-grp"><ul></ul></div>')
                        .appendTo(UI.arena).find("ul");
            }
            // 创建项目 
            var itv = items[i];
            var txt = opt.text.call(UI, itv);
            $('<li>').attr("val", itv).text(txt).appendTo(jUl);
        }

        // 记录
        UI.__items = items;
    },
    //...............................................................
    setMulti : function(mu) {
        this.options.multi = mu ? true : false;
    },
    //...............................................................
    getItems : function(){
        return this.__items;
    },
    //...............................................................
    _get_data : function(){
        var UI  = this;
        var opt = UI.options;
        var lst = [];
        UI.arena.find('li').each(function(){
            var jLi = $(this);
            if(jLi.attr("current")){
                var v = jLi.attr("val");
                lst.push(/^[\d.]+$/.test(v) ? v * 1 : v);
            }
        });
        // 单个值 
        if(!opt.multi && !opt.keepArray) {
            return lst.length > 0 ? lst[0] : null;
        }
        // 多个值
        return lst;
    },
    //...............................................................
    _set_data : function(val, jso){
        var UI  = this;
        var opt = UI.options;
        var lst = [];
        // 处理单选
        if(!opt.multi) {
            // 数组只取第一个元素
            if(_.isArray(val)){
                if(val.length>0)
                    lst.push(val[0]);
            }
            // 否则认为传入的是值
            else{
                lst.push(val);
            }
        }
        // 处理多选
        else {
            // 合并数组
            if(_.isArray(val)){
                if(val.length>0)
                    lst = lst.concat(val);
            }
            // 否则认为传入的是值
            else{
                lst.push(val);
            }
        }
        //console.log("in array:", lst)
        // 处理选择
        UI.arena.find('li').each(function(){
            var jLi = $(this);
            var itv = jLi.attr("val");
            // 查找值，为了考虑字符串的情况，就笨笨的找一下吧
            for(var i=0; i<lst.length; i++){
                if(lst[i] == itv){
                    jLi.attr("current", "yes");
                    return;
                }
            }
            // 木有找到，取消高亮
            jLi.removeAttr("current");
        });
    }
    //...............................................................
});
//===================================================================
});
})(window.NutzUtil);