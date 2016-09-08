/**
使用了 options 的配置项

 - idKey
 - nmKey
 - activable
 - on_before_actived : {UI}F(jItem, obj)
 - on_draw_item      : {UI}F(jItem, obj)
 
 - getItemData : {UI}F(jItem)       // 根据一个 DOM 返回对应的数据对象
 - setItemData : {UI}F(jItem, obj)  // 设置，默认用 jQuery.data
 
子类可以覆盖的方法
 
 - $item      // 根据 arg 返回 DOM， 否则返回激活的行
 - $checked   // 返回所有被多选的项目
 - __after_actived(o, jItem, prevObj, jPreItem)
 - __after_blur(jItems)
 - __after_checked(jItems)
 - __after_toggle(jItems)
 - __before_draw_data(objs)
 - __after_draw_data(objs)
 - __after_draw_item(jItem, obj)
 - __after_add()
 - __after_remove()
 
! 子类必须实现

 - $listBody()             // 返回承载列表项的 jQuery 对象
 - $crateItem()            // 创建一个列表项的 jQuery 对象（未加入DOM)
 - _draw_item(jItem, obj)  // 根据数据和列表项修改列表项的显示方式

提供给子类强烈推荐调用的方法

- __setup_options(opt)     // 当 init 时
 
在 jItem 上的通用属性:

.list-item
oid="xxx"
onm="xxxx"
li-actived="yes"
li-checked="yes"

消息

"item:actived"   : {UI}on_actived(o, jItem, prevObj, jPreItem)
"item:blur"      : {UI}on_blur(jItems, nextObj, nextItem)
"item:checked"   : {UI}on_checked(jItems)
"item:unchecked" : {UI}on_unchecked(jItems)
"item:add"

*/
define(function (require, exports, module) {
// ....................................
// 方法表
var methods = {
    //...............................................................
    __setup_options : function(opt) {
        var UI = this;

        $z.setUndefined(opt, "idKey", "id");
        $z.setUndefined(opt, "nmKey", "nm");
        $z.setUndefined(opt, "activable", true);
        $z.setUndefined(opt, "blurable", true);
        $z.setUndefined(opt, "checkable", true);
        $z.setUndefined(opt, "multi",  opt.checkable);

        // options.get/setItemData
        $z.setUndefined(opt, "getItemData", function(jItem){
            return jItem.data("@OBJ");
        });
        $z.setUndefined(opt, "setItemData", function(jItem, obj){
            jItem.attr({
                "oid" : obj[opt.idKey],
                "onm" : obj[opt.nmKey],
            }).data("@OBJ", obj);
        });

        // $item
        $z.setUndefined(UI, "$item", function(arg) {
            var UI = this;
            // 默认用激活项
            if(_.isUndefined(arg)){
                return UI.arena.find('.list-item[li-actived]');
            }
            // 如果是字符串表示 ID
            else if(_.isString(arg)){
                return UI.arena.find(".list-item[oid="+arg+"]");
            }
            // 本身就是 dom
            else if(_.isElement(arg) || $z.isjQuery(arg)){
                return $(arg).closest(".list-item");
            }
            // 数字
            else if(_.isNumber(arg)){
                return UI.arena.find(".list-item:eq("+arg+")");
            }
            // 靠不晓得了
            else {
                throw "unknowns $item selector: " + arg;
            }
        });
        // $checked
        $z.setUndefined(UI, "$checked", function() {
            return this.arena.find('.list-item[li-checked]');
        });
    },
    //...............................................................
    getObjId : function(obj){
        return obj[this.options.idKey];
    },
    getObjName : function(obj){
        return obj[this.options.nmKey];
    },
    //...............................................................
    // 从某项目取出数据
    getObj : function(arg) {
        var jItem = this.$item(arg);
        return this.options.getItemData.call(this, jItem);
    },
    // 向某项目设置数据
    setObj : function(arg, obj) {
        var jItem = this.$item(arg);
        this.options.setItemData.call(this, jItem, obj);
    },
    //...............................................................
    getActived : function(){
        return this.getObj(this.$item());
    },
    getActivedId : function(){
        return this.$item().attr("oid");
    },
    isActived : function(arg){
        var jItem = this.$item(arg);
        return jItem.attr("li-actived") ? true : false;
    },
    //...............................................................
    setActived : function(arg){
        var UI  = this;
        var opt = UI.options;
        var context = UI;

        // 不许激活项目 ...
        if(!opt.activable)
            return;

        // 未给入参数，相当于 blur
        if(_.isUndefined(arg) || _.isNull(arg)){
            UI.setAllBure();
            return;
        }

        // 执行查找
        var jItem = UI.$item(arg);
        if(jItem.length>0 && !UI.isActived(jItem)){
            // 得到数据
            var o = UI.getData(jItem);

            // 看看是不是调用者要禁止激活
            var cancelIt = $z.doCallback(opt.on_before_actived, [o, jItem], context);
            if(false === cancelIt){
                return;
            }

            // 得到之前的激活项
            var jPreItem = UI.$item();
            var prevObj  = UI.getData(jItem);

            // 取消其他的激活
            UI.setAllBure(o, jItem);
            jItem.attr("li-actived", "yes");
            jItem.attr("li-checked", "yes");

            // 触发消息 
            UI.trigger("item:actived", o, jItem);
            $z.invoke(opt, "on_actived", [o, jItem, prevObj, jPreItem], context);
        }
        // 调用子类方法
        $z.invoke(UI, "__after_actived", [o, jItem, prevObj, jPreItem]);
    },
    //...............................................................
    setAllBure : function(nextObj, nextItem){
        var UI  = this;
        var opt = UI.options;
        var jItems  = UI.$checked();
        var context = UI;

        // 如果有下一个高亮对象，或者 blurable 为 true 则可以取消
        if((opt.blurable || nextObj) && jItems.length > 0){
            // 移除标记
            jItems.attr({
                "li-checked" : null,
                "li-actived" : null,
            });

            // 同步选择器 
            $z.invoke(UI, "__after_blur", [jItems, nextObj, nextItem]);

            // 触发消息 
            UI.trigger("item:blur", jItems, nextObj, nextItem);
            $z.invoke(opt, "on_blur", [jItems, nextObj, nextItem], context);
        }
    },
    //...............................................................
    getChecked : function(){
        var UI = this;
        var objs = [];
        UI.$checked().each(function(){
            var obj = UI.getObj(this);
            objs.push(obj);
        });
        return objs;
    },
    isChecked : function(arg){
        var jItem = this.$item(arg);
        return jItem.attr("li-checked") ? true : false;
    },
    //...............................................................
    check : function(arg){
        var UI  = this;
        var opt = UI.options;

        // 找到未选中项目
        var jItems = (_.isUndefined(arg)
                        ? UI.arena.find(".list-item")
                        : UI.$item(arg))
                        .not('[li-checked]');

        // 执行
        if(jItems.length>0){
            jItems.attr("li-checked", "yes");

            // 调用子类方法
            $z.invoke(UI, "__after_checked", [jItems]);

            // 触发消息 
            UI.trigger("item:checked", jItems);
            $z.invoke(opt, "on_checked", [jItems], UI);
        }
    },
    //...............................................................
    uncheck : function(arg){
        var UI  = this;
        var opt = UI.options;

        // 找到选中项目
        var jItems = (_.isUndefined(arg)
                        ? UI.arena.find(".list-item")
                        : UI.$item(arg))
                        .filter('[li-checked]');

        // 执行
        if(jItems.length>0){
            jItems.attr("li-checked", null);

            // 调用子类方法
            $z.invoke(UI, "__after_checked", [jItems]);

            // 触发消息 
            UI.trigger("item:unchecked", jItems);
            $z.invoke(opt, "on_unchecked", [jItems], UI);
        }
    },
    //...............................................................
    toggle : function(arg){
        var UI  = this;
        var opt = UI.options;
        
        var jItems = _.isUndefined(arg)
                        ? UI.arena.find(".list-item")
                        : UI.$item(arg);

        if(jItems.length>0){
            var jCheckeds = $([]);
            var jUnchecks = $([]);
            jItems.each(function(){
                var jItem = $(this);

                // 取消选中
                if(UI.isChecked(jItem)){
                    jItem.attr("li-checked", null);
                    jCheckeds.add(jItem);
                }
                // 选中
                else{
                    jItem.attr("li-checked", "yes");
                    jUnchecks.add(jItem);
                }
            });

            // 调用子类方法
            $z.invoke(UI, "__after_toggle", [jItems]);

            // 触发消息 : checked
            if(checkeds.length > 0) {
                UI.trigger("item:checked", jCheckeds);
                $z.invoke(opt, "on_checked", [jCheckeds], UI);
            }
            // 触发消息 : uncheck
            if(unchecks.length > 0) {
                UI.trigger("item:unchecked", jUnchecks);
                $z.invoke(opt, "on_unchecked", [jUnchecks], UI);
            }
        }
    },
    //...............................................................
    has: function(arg) {
        return this.$item(arg).length > 0;
    },
    //...............................................................
    getData : function(arg){
        var UI = this;
        // 特指某个项目
        if(!_.isUndefined(arg)){
            return UI.getObj(arg);
        }
        // 获取完整的列表
        return UI.ui_format_data(function(opt){
            var objs = [];
            UI.arena.find('.list-item').each(function(){
                var obj = UI.getObj(this);
                objs.push(obj);
            });
            return objs;
        });
    },
    //...............................................................
    setData : function(objs){
        this.ui_parse_data(objs, function(objs){
            this._draw_data(objs);
        });
    },
    //...............................................................
    _draw_data : function(objs){
        var UI  = this;
        var opt = UI.options;

        // 检查要输出的数据
        if(!_.isArray(objs))
            return;

        // 绘制前子类准备
        $z.invoke(UI, "__before_draw_data", [objs]);

        // 输出表格内容 
        var jListBody = UI.$listBody().empty();
        objs.forEach(function(o, index){
            UI._upsert_item(o, jListBody);
        });

        // 绘制后子类处理
        $z.invoke(UI, "__after_draw_data", [objs]);

        // 重新计算尺寸
        UI.resize();
    },
    //...............................................................
    add : function(objs, it, direction) {
        var UI = this;
        objs = _.isArray(objs) ? objs : [objs];

        var jListBody = UI.$listBody();
        for(var obj of objs) {
            UI._upsert_item(obj, jListBody, UI.$item(it), direction);
        }

        // 子类后续处理
        $z.invoke(UI, "__after_add", []);

        // 重新调整尺寸
        UI.resize(true);

        // 返回自身
        return this;
    },
    //...............................................................
    remove : function(it, keepAtLeastOne) {
        var UI   = this;
        var jItem = UI.$item(it);

        // 如果没有匹配的行，啥也不做
        if(jItem.length == 0)
            return;

        // 如果当前是高亮节点，则试图得到下一个高亮的节点，给调用者备选
        var jN2   = null;
        if(UI.isActived(jItem) || UI.isChecked(jItem)){
            jN2 = jItem.last().next();
            if(jN2.length == 0){
                jN2 = jItem.first().prev();
                // 返回 false 表示只剩下最后一个节点额
                if(jN2.length == 0 && keepAtLeastOne){
                    return false;
                }
            }
        }

        // 删除当前节点
        jItem.remove();

        // 子类后续处理
        $z.invoke(UI, "__after_remove", []);

        // 返回下一个要激活的节点，由调用者来决定是否激活它
        return jN2 && jN2.length > 0 ? jN2 : null;
    },
    //...............................................................
    update : function(obj, it) {
        this._upsert_item(obj, null, this.$item(it || this.getObjId(obj)), 0);
    },
    //...............................................................
    _upsert_item : function(obj, jListBody, jReferItem, direction){
        var UI  = this;
        var opt = UI.options;
        var context = UI;
        
        // 要修改的项目
        var jItem;

        // 没有参考项表示追加，插入到表格后头
        if(!jReferItem || jReferItem.length == 0){
            jListBody = jListBody || UI.$listBody();
            jItem = UI.$createItem().addClass("list-item").appendTo(jListBody);
        }
        // 有的话，看方向 0 表示替换
        else if(0 === direction){
            jItem = jReferItem.empty();
        }
        // 正数插到前面
        else if(direction>0){
            jItem = UI.$createItem().addClass("list-item").insertBefore(jReferItem);
        }
        // 负数插到后面
        else {
            jItem = UI.$createItem().addClass("list-item").insertAfter(jReferItem);
        }

        // 设置数据
        UI.setObj(jItem, obj);

        // 绘制项目
        UI._draw_item(jItem ,obj);

        // 调用配置项，自定义更多节点外观
        $z.invoke(opt, "on_draw_item", [jItem, obj], context);

        // 如果原来就已经是高亮的，那么还需要回调一下激活事件
        if(UI.isActived(jItem)){
            UI.trigger("item:actived", obj, jItem);
            $z.invoke(opt, "on_actived", [obj, jItem, obj, jItem], context);
        }

        // 返回
        return jItem;
    },
    //...............................................................
    // 点击项目的时候，需要考虑 ctrl/shift 等单选多选问题
    // 子 UI 截获事件后调用这个函数处理即可
    _do_click_list_item : function(e){
        var UI = this;
        var jItem = UI.$item(e.currentTarget);
        //console.log(".lst-item click");
        // 如果支持多选 ...
        if(UI.options.multi){
            // 仅仅表示单击选中
            if(($z.os.mac && e.metaKey) || (!$z.os.mac && e.ctrlKey)){
                UI.check(e.currentTarget);
                return;
            }
            // shift 键表示多选
            else if(e.shiftKey){
                // 准备计算需要选中的项目
                var jItems;

                // 找到激活项目的 ID
                var jA = UI.$item();

                // 没有项目被激活，那么从头选
                if(jA.size() == 0){
                    jA = UI.$item(0);
                }
                
                // 激活项目在自己以前
                var selector = "[oid=" + jA.attr("oid") + "]";
                if(jItem.prevAll(selector).size() > 0){
                    jItems = jItem.prevUntil(jA).addBack().add(jA);
                }
                // 激活项目在自己以后
                else if(jItem.nextAll(selector).size() > 0){
                    jItems = jItem.nextUntil(jA).addBack().add(jA);
                }
                // 那就是自己咯
                else{
                    jItems = jItem;
                }

                // 选中这些项目
                UI.check(jItems);

                // 防止冒泡
                e.stopPropagation();

                // 返回吧
                return;
            }
        }
        // 否则就是激活
        UI.setActived(jItem);
    },
    //...............................................................
}; // ~End methods
//====================================================================

//====================================================================
// 输出
module.exports = function(uiSub){
    return _.extend(uiSub, methods);
};
//=======================================================================
});
