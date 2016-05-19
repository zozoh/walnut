---
title: 列表控件
author:zozoh
---

# 控件概述

*list* 控件假想输入是一组 JS对象数组，它会将对象显示成一个表格。
同时它也提供对这个列表的数据操作方法。

*list* 控件仅仅负责数据的显示，要想触发它的显示，就调用 `setData` 方法即可

# 如何创建实例

```
new ListUI({
    //............................................................
    // 数据方面
    // 获取一个对象的唯一标识，这个标识会记录到 DOM 元素的 `oid` 属性
    // 快速索引到这个 DOM 元素，每个 DOM 元素都会记录对应的对象
    // 默认的，会取这个对象的 "id" 字段的值作为 ID
    idKey : "xxx" | {c}F(obj)
    
    // 与 idKey 相仿，会记录 DOM 元素的 `onm` 属性。
    // 不是必须的，没声明就没这个属性
    nmKey : "xxx" | {c}F(obj)
    
    // 【选】同步预先解析设置进来的数据
    parseData : {c}F(Array) : Array

    // 【选】异步预先解析设置进来的数据    
    asyncParseData : {c}F(Array, callback:F(Array));
    
    // 【选】同步处理自己的数据
    formatData : {c}F(callback:{UI}F(opt):Array) : Array
    
    //............................................................
    // 行为
    checkable : Boolean       // 是否显示 checkbox
    multi     : Boolean       // 是否多选，如果 checkable 默认可以多选
    activable : Boolean       // 可以激活行
    blurable  : Boolean       // 可以取消高亮行

    //............................................................
    // 显示
    icon : Tmpl|{c}F(o):HTML  // 自定义对象图标
    text : Tmpl|{c}F(o):Str   // 自定义对象文本
    
    // 自定义显示内容，一般配合 escapeHtml:true 来自定义内容
    // 如果声明了这个，icon 和 text 会被无视
    display : Tmpl|{c}F(o):HTML 
    
    // 是否将输出逃逸 HTML，默认 true，输出的内容将会不会被认为是 HTML
    escapeHtml : Boolean

    //............................................................
    // 绘制完一行后，你可以补充对这行的后续处理
    on_draw_item : {c}F(jItem, obj)

    //............................................................
    // 在激活某个对象前的回调，如果函数返回的是 false
    // 那么会禁止这个对象的激活
    on_before_actived : {c}F(o,jq):Boolean

    //............................................................
    // 事件
    on_change  : {c}F()           // "list:change"!首次加载一定会被触发
    on_add     : {c}F(objs)       // "list:add"   !setData 时不会触发
    on_checked : {c}F([objs,jq])  // "list:checked"
    on_blur    : {c}F([objs,jq])  // "list:blur"
    on_actived : {c}F(o,index,jq) // "list:actived"

    //............................................................
    // 上下文，可以之际指定 {c} 对应的上下，默认 UI 自身
    context : UI

}).render();
```

# 控件方法

## getObjId

```
var id = uiList.getObjId(obj);
```

* 实际上会返回对象的 *id* 字段的值
*  *id* 字段由 *idKey* 来配置，默认为 "id"

## getObjName

```
var id = uiList.getObjName(obj);
```

* 实际上会返回对象的 *name* 字段的值
*  *name* 字段由 *nmKey* 来配置，如果不声明，本函数总是会返回 *undefined*

## getActived

```
// 获取当前被激活的对象
var o = uiList.getActived();
```

## getActivedId

```
// 获取当前被激活的对象的 ID
var id = uiList.getActivedId();
```

## setActived

```
// 激活第3个对象 (0 base)
uiList.setActived(2);

// 激活某 DOM 元素所在的对象
uiList.setActived($(..));

// 激活最后一个对象
uiList.setActived(-1);

// 激活某个 ID 的对象
uiList.setActived("45cfad78a3ec99ade12");

// 相当于调用 blur
uiList.setActived();
```

## blur

```
uiList.blur();
```

## getChecked

```
var objList = uiList.getChecked();
```

## check

```
// 选中第3个对象 (0 base)
uiList.check(2);

// 选中某 DOM 元素所在的对象
uiList.check($(..));

// 选中最后一个对象
uiList.check(-1);

// 选中某个 ID 的对象
uiList.check("45cfad78a3ec99ade12");

// 全部选中
uiList.check();
```

## uncheck

```
// 取消选中第3个对象 (0 base)
uiList.uncheck(2);

// 取消选中某 DOM 元素所在的对象
uiList.uncheck($(..));

// 取消选中最后一个对象
uiList.uncheck(-1);

// 取消选中某个 ID 的对象
uiList.uncheck("45cfad78a3ec99ade12");

// 取消全部选中
uiList.uncheck();
```

## toggle

```
// 将给定的东东，全部反选，并触发相应的消息
// 如果没有参数，则表示全部反选
uiList.toggle(Element | jQuery | Number | "selector" | {..})
```

## has

```
// 是否存在某个 ID 项目
uiList.has("45cfad78a3ec99ade12");
```

## getData

```
// 获取第3个对象 (0 base)
var obj = uiList.getData(2);

// 获取某 DOM 元素所在的对象
uiList.getData($(..));

// 获取最后一个对象
uiList.getData(-1);

// 获取某个 ID 的对象
uiList.getData("45cfad78a3ec99ade12");

// 获取全部数据
var objs = uiList.getData();
```

## setData

```
uiList.setData(objs)
```

## add

```
// 在第3个对象 (0 base) 后插入
var obj = uiList.add(objs, 3);

// 在第3个对象 (0 base) 前插入
var obj = uiList.add(objs, 3, true);

// 在某 DOM 元素所在的对象后插入
uiList.add(objs, $(..));

// 在某 DOM 元素所在的对象前插入
uiList.add(objs, $(..), true);

// 在最后一个对象后插入
uiList.add(objs, -1);

// 在最后一个对象前插入
uiList.add(objs, -1, true);

// 在某个 ID 的对象后插入
uiList.add(objs, "45cfad78a3ec99ade12");

// 在某个 ID 的对象前插入
uiList.add(objs, "45cfad78a3ec99ade12", true);

// 在当前对象后插入
uiList.add(objs);

// 在当前对象前插入
uiList.add(objs, null, true);
```

* 参数 *objs* 可以是单个对象，函数判断不是数组，会用数组包裹

## update 

```
// 更新第3个对象 (0 base)
var obj = uiList.update(obj, 2);

// 更新某 DOM 元素所在的对象
uiList.update(obj, $(..));

// 更新最后一个对象
uiList.update(obj, -1);

// 更新某个 ID 的对象
uiList.update(obj, "45cfad78a3ec99ade12");

// 更新持有相同 ID 的对象
uiList.update(obj);
```

## showLoading

```
uiList.showLoading();
```





