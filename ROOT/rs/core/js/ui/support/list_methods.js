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
 - __after_add()
 - __after_remove()
 
! 子类必须实现

 - $listBody()             // 返回承载列表项的 jQuery 对象
 - $crateItem()            // 创建一个列表项的 jQuery 对象（未加入DOM)
 - _draw_item(jItem, obj)  // 根据数据和列表项修改列表项的显示方式
 - _draw_empty             // 没有数据时显示什么

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
        $z.setUndefined(opt, "stopPropagation", false);
        $z.setUndefined(opt, "rowToggle", true);

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
            // 直接返回激活的节点
            if(_.isUndefined(arg)){
                return this.arena.find('.list-item[li-actived]');
            }
            // 否则查找
            return this.findItem(arg);
        });
        // $checked
        $z.setUndefined(UI, "$checked", function() {
            return this.arena.find('.list-item[li-checked]');
        });
        // _draw_empty
        $z.setUndefined(UI, "_draw_empty", function() {
            var jBody = UI.$listBody().empty();
            jBody.html(UI.compactHTML(`<div class="list-empty">
                <i class="zmdi zmdi-alert-circle-o"></i>
                <span>{{empty}}</span>
            </div>`));
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
    // 根据参数，返回一个 jQuery 对象，表示一个或多个对象所在的 DOM 元素
    findItem : function(arg){
        var UI  = this;
        var opt = UI.options;

        // 没指定内容，为空
        if(_.isUndefined(arg)){
            // return $([]);
            return UI.arena.find(".list-item");
        }
        // 如果是字符串表示 ID
        if(_.isString(arg)){
            // 名字吗
            if(/^nm:/.test(arg)){
                var nm = arg.substring(3);
                return UI.arena.find('.list-item[onm="'+nm+'"]');    
            }
            // 那么就当做 ID 吧
            return UI.arena.find('.list-item[oid="'+arg+'"]');
        }
        // 如果是个对象数组，则分别查找这个对象的集合
        if(_.isArray(arg)){
            var eles = [];
            for(var i=0; i<arg.length; i++) {
                var jIt = UI.findItem(arg[i]);
                if(jIt.length > 0)
                    eles.push(jIt[0]);
            }
            return $(eles);
        }
        // 正则表达式，则表示匹配名称
        if(_.isRegExp(arg)){
            var re = [];
            UI.arena.find('.list-item').each(function(){
                var nm = $(this).attr("onm");
                if(arg.test(nm))
                    re.push(this);
            });
            return $(re);
        }
        // 是一个通用过滤函数
        if(_.isFunction(arg)){
            var re = [];
            UI.arena.find('.list-item').each(function(){
                var jItem = $(this);
                var obj = opt.getItemData.call(UI, jItem);
                if(arg.call(UI, obj, jItem))
                    re.push(this);
            });
            return $(re);
        }
        // 本身就是 dom
        if(_.isElement(arg) || $z.isjQuery(arg)){
            return $(arg).closest(".list-item");
        }
        // 数字
        if(_.isNumber(arg)){
            return UI.arena.find(".list-item:eq("+arg+")");
        }
        // 如果是对象，那么试图获取 IDKey
        if(_.isObject(arg)) {
            var id = arg[opt.idKey];
            return UI.arena.find(".list-item[oid="+id+"]");
        }
        // 靠不晓得了
        throw "unknowns $item selector: " + arg;
    },
    //...............................................................
    // 从某项目取出数据
    getObj : function(arg, forceArray) {
        var UI    = this;
        var opt   = UI.options;
        var jItem = UI.$item(arg);
        // 如果是单一对象
        if(!forceArray && jItem.length <= 1){
            return opt.getItemData.call(UI, jItem);
        }
        // 否则返回的是一个数组
        var re = [];
        jItem.each(function(){
            re.push(opt.getItemData.call(UI, $(this)));
        });
        return re;
    },
    // 向某项目设置数据
    setObj : function(arg, obj) {
        var jItem = this.$item(arg);
        this.options.setItemData.call(this, jItem, obj);
    },
    //...............................................................
    disableItem : function(arg) {
        var UI = this;

        // 木有的话，就搞全部
        var jItems = UI.findItem(arg);

        // 标识一下
        jItems.attr("it-disabled", "yes");
    },
    //...............................................................
    enableItem : function(arg) {
        var UI = this;

        // 木有的话，就搞全部
        var jItems = UI.findItem(arg);

        // 标识
        jItems.removeAttr("it-disabled");
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
    hasActived : function(){
        return this.getActivedId() ? true : false;
    },
    isEmpty : function() {
        return this.arena.find('.list-item').length <= 0;
    },
    //...............................................................
    setActived : function(arg){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 不许激活项目 ...
        if(!opt.activable)
            return;

        // 未给入参数，相当于 blur
        if(_.isUndefined(arg) || _.isNull(arg)){
            UI.setAllBlur();
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
            var prevObj  = UI.getData(jPreItem);

            // 取消其他的激活
            UI.setAllBlur(o, jItem);
            jItem.attr("li-actived", "yes");
            jItem.attr("li-checked", "yes");

            // 触发消息 
            UI.trigger("item:actived", o, jItem);
            $z.invoke(opt, "on_actived", [o, jItem, prevObj, jPreItem], context);
            
            UI.trigger("item:checked", jItem);
            $z.invoke(opt, "on_checked", [jItem], UI);
        }
        // 调用子类方法
        $z.invoke(UI, "__after_actived", [o, jItem, prevObj, jPreItem]);
    },
    //...............................................................
    setBlur : function(arg) {
        var UI  = this;
        var jItems  = UI.$item(arg);
        UI._do_blur(jItems);
    },
    //...............................................................
    setAllBlur : function(nextObj, nextItem){
        var UI  = this;
        var jItems  = UI.$checked();
        UI._do_blur(jItems, nextObj, nextItem);
    },
    //...............................................................
    _do_blur : function(jItems, nextObj, nextItem) {
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        if((opt.blurable || nextObj) && jItems){
            // 移除标记
            jItems.attr({
                "li-checked" : null,
                "li-actived" : null,
            });

            // 子类的特殊过程，比如 table 的同步选择器 
            $z.invoke(UI, "__after_blur", [jItems, nextObj, nextItem]);

            // 触发消息 
            UI.trigger("item:blur", jItems, nextObj, nextItem);
            $z.invoke(opt, "on_blur", [jItems, nextObj, nextItem], context);
            
            UI.trigger("item:unchecked", jItems);
            $z.invoke(opt, "on_unchecked", [jItems], UI);
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
    getCheckedIds : function(){
        var UI = this;
        var ids = [];
        UI.$checked().each(function(){
            ids.push($(this).attr("oid"));
        });
        return ids;
    },
    getCheckedMap : function(){
        var UI = this;
        var opt = UI.options;

        var objs = UI.getChecked();
        return $z.arrayToMap(objs, opt.idKey);
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

            // 取消激活
            var jA = jItems.filter('[li-actived]');
            if(jA.length > 0) {
                jA.removeAttr("li-actived");

                // 子类的特殊过程，比如 table 的同步选择器 
                $z.invoke(UI, "__after_blur", [jA]);

                // 触发消息 
                UI.trigger("item:blur", jA);
                $z.invoke(opt, "on_blur", [jA]);
            }

        }
    },
    //...............................................................
    toggle : function(arg){
        var UI  = this;
        var opt = UI.options;
        
        var jItems = _.isUndefined(arg)
                        ? UI.arena.find(".list-item")
                        : UI.$item(arg);
        //console.log("toggle", jItems.html())
        if(jItems.length>0){
            var checkeds = [];
            var unchecks = [];
            jItems.each(function(){
                var jItem = $(this);

                // 如果是激活的，就blur
                if(UI.isActived(jItem)){
                    UI.setBlur(jItem);
                    return;
                }

                // 取消选中
                if(UI.isChecked(jItem)){
                    jItem.removeAttr("li-checked");
                    unchecks.push(this);
                }
                // 选中
                else{
                    jItem.attr("li-checked", "yes");
                    checkeds.push(this);
                }
            });
            // console.log("jCheckeds", checkeds);
            // console.log("jUnchecks", unchecks);

            // 调用子类方法
            $z.invoke(UI, "__after_toggle", [jItems]);

            // 触发消息 : checked
            if(checkeds.length > 0) {
                var jCheckeds = $(checkeds);
                UI.trigger("item:checked", jCheckeds);
                $z.invoke(opt, "on_checked", [jCheckeds], UI);
            }
            // 触发消息 : uncheck
            if(unchecks.length > 0) {
                var jUnchecks = $(unchecks);
                UI.trigger("item:unchecked", jUnchecks);
                $z.invoke(opt, "on_unchecked", [jUnchecks], UI);
            }
        }
    },
    //...............................................................
    has: function(arg) {
        //console.log("has?", arg);
        return this.$item(arg).length > 0;
    },
    //...............................................................
    getData : function(arg, forceArray){
        var UI = this;
        
        return UI.ui_format_data(function(opt){
            // 特指某个项目
            if(!_.isUndefined(arg)){
                return UI.getObj(arg, forceArray);
            }
            // 获取完整的列表
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

        //console.log("_draw_data:", objs)

        // 如果数据不是数组，那么相当于空数组
        if(!_.isArray(objs))
            objs = [];

        // 绘制空数组
        if(objs.length == 0) {
            $z.invoke(UI, "_draw_empty");
            return;
        }

        // 得到上次激活对象的 ID
        var aiid = UI.getActivedId();

        // 得到上次已经选中的项目 ID
        var checkeds = UI.getChecked();
        var ckids = [];
        for(var i=0; i<checkeds.length; i++) {
            ckids.push(UI.getObjId(checkeds[i]));
        }

        // 绘制前子类准备
        $z.invoke(UI, "__before_draw_data", [objs]);

        // 输出表格内容 
        var jListBody = UI.$listBody().empty();
        objs.forEach(function(o, index){
            UI._upsert_item(o, jListBody, index);
        });

        // 绘制后子类处理
        $z.invoke(UI, "__after_draw_data", [objs]);

        // 如果之前有激活，那么继续激活
        if(aiid && objs.length > 0)
            UI.setActived(aiid);

        // 如果之前有选中，那么继续选中
        if(ckids && ckids.length > 0) {
            for(var i=0; i<ckids.length; i++){
                UI.check(ckids[i]);
            }
        }

        // 重新计算尺寸
        UI.resize();
    },
    //...............................................................
    moveUp : function(it) {
        var jItem = this.$item(it);

        // 如果没有匹配的行，啥也不做
        if(jItem.length == 0)
            return;

        // 寻找移动目标
        var jTa = jItem.prev(".list-item");
        if(jTa.length > 0) {
            jItem.insertBefore(jTa);
        }

        // 返回被移动项
        return jItem;
    },
    //...............................................................
    moveDown : function(it) {
        var jItem = this.$item(it);

        // 如果没有匹配的行，啥也不做
        if(jItem.length == 0)
            return;

        // 寻找移动目标
        var jTa = jItem.next(".list-item");
        if(jTa.length > 0) {
            jItem.insertAfter(jTa);
        }

        // 返回被移动项
        return jItem;
    },
    //...............................................................
    add : function(objs, it, direction) {
        var UI = this;
        objs = _.isArray(objs) ? objs : [objs];

        // 准备返回的元素数组
        var re = [];

        // 如果为空，则表示重绘数据
        if(UI.isEmpty()) {
            UI._draw_data(objs);
            UI.arena.find(".list-item").each(function(){
                re.push(this);
            });
        }
        // 否则依次追加数据
        else {
            var jListBody = UI.$listBody();
            var jItem = UI.$item(it);
            for(var obj of objs) {
                jItem = UI._upsert_item(obj, jListBody, jItem, direction);
                re.push(jItem[0]);
            }
        }

        // 子类后续处理
        $z.invoke(UI, "__after_add", []);

        // 重新调整尺寸
        UI.resize(true);

        // 返回新增加的 jQuery 集合
        return $(re);
    },
    //...............................................................
    remove : function(it, keepAtLeastOne) {
        var UI   = this;
        var jItem = UI.$item(it);

        // 如果没有匹配的行，啥也不做
        if(jItem.length == 0)
            return;

        // 如果当前是高亮节点，则试图得到下一个高亮的节点，给调用者备选
        var jN2 = UI.findNextItem(jItem);
        if(jN2.length == 0){
            if(keepAtLeastOne)
                return false;
            else
                jN2 = null;
        }
        // 如果即将被删除的节点集合包含激活节点，才建议改变激活节点
        // 否则不建议改变激活节点
        else if(!jItem.filter(UI.$item()).length>0)
            jN2 = null;

        // 删除当前节点
        jItem.remove();

        // 子类后续处理
        $z.invoke(UI, "__after_remove", []);

        // 如果都删光了，绘制空
        if(UI.arena.find(".list-item").length == 0) {
            $z.invoke(UI, "_draw_empty");
        }

        // 返回下一个要激活的节点，由调用者来决定是否激活它
        return jN2;
    },
    //...............................................................
    // 根据给定的:
    //  - 对象数组
    //  - 对象
    //  - 总之，任何可以被 $item 接受的对象就对了
    // 查找如果这些对象被删除后，下一个被高亮的元素应该是什么
    // 返回的是 jQuery
    findNextItem : function(it) {
        var jItems = this.$item(it);
        var jN2 = jItems.last().next();
        if(jN2.length == 0)
            jN2 = jItems.first().prev();

        // 还是木有？那么逐个查找吧
        // 逐个查找，找到第一个不是给定的元素
        var jList = this.arena.find(".list-item");
        for(var i=0;i<jList.length;i++){
            if(!jItems.filter(jList[i])){
                jN2 = jList.eq(i);
                break;
            }
        }

        // 返回
        return jN2;
    },
    //...............................................................
    update : function(obj, it) {
        return this._upsert_item(obj, null, this.$item(it || this.getObjId(obj)), 0);
    },
    //...............................................................
    _upsert_item : function(obj, jListBody, jReferItem, direction){
        var UI  = this;
        var opt = UI.options;
        var context = opt.context || UI;

        // 传入了 index
        var index = -1;
        if(_.isNumber(jReferItem) && jReferItem >= 0) {
            index = jReferItem;
            jReferItem = undefined;
        }
        
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
        UI._draw_item(jItem ,obj, index);

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
        var UI  = this;
        var opt = UI.options;
        var jItem = UI.$item(e.currentTarget);
        
        // 不要冒泡了
        if(opt.stopPropagation)
            e.stopPropagation();

        //console.log("haha", opt.stopPropagation)

        // 如果支持多选 ...
        if(opt.multi){
            // 仅仅表示单击选中
            if(($z.os.mac && e.metaKey) 
                || (!$z.os.mac && e.ctrlKey)){
                // 防止冒泡
                e.stopPropagation();
                // 切换状态
                UI.toggle(jItem);
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
                var selector = '[oid="' + jA.attr("oid") + '"]';
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
                // 防止冒泡
                e.stopPropagation();
                // 选中这些项目
                UI.check(jItems);
                // 返回吧
                return;
            }
        }
        // 激活
        if(opt.activable) {
            UI.setActived(jItem);
        }
        // 否则就是纯粹选中
        else if(opt.rowToggle){
            UI.toggle(jItem);
        }
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
