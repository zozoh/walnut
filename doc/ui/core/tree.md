---
title:树控件
author:zozoh
---



# 控件概述

```
{
    // 获取树的顶层元素列表，如果是普通对象会用数组包裹
    // 如果是函数，可以返回普通对象或者数组
    // callback 的格式是 {c}F([]);
    tops : {..}|[..]|{c}F(callback)
    
    // 根据一个对象获取该对象的子节点
    // callback 的格式是 F([]);
    // 如果给 callback 传递了 null，则表示这个对象永远也不会有子节点了
    // 控件会将其设成 leaf 节点
    children : {c}F(obj, callback)
    
    // 获取一个对象的唯一标识，这个标识会记录到 DOM 元素的 `oid` 属性
    // 快速索引到这个 DOM 元素，每个 DOM 元素都会记录对应的对象
    // 默认的，会取这个对象的 "id" 字段的值作为 ID
    idKey : "xxx" | {c}F(obj)
    
    // 与 idKey 相仿，会记录 DOM 元素的 `onm` 属性。
    // 不是必须的，没声明就没这个属性
    nmKey : "xxx" | {c}F(obj)
    
    // 返回树节点的图标 HTML，可以是多个 icon，总之一段 HTML 拉
    icon : {c}F(obj) : HTML
    
    // 返回树节点的文本内容，会被当做一个字符串 
    text : {c}F(obj} : String
    
    // 判断当前节点是不是叶子节点，标识属性 "ndtp=leaf|node"
    // 如果没有这个属性，那么默认会认为当前节点是 "leaf"
    isLeaf : {c}F(obj) : Boolan

    // 手柄的样子，如果不声明，会有默认的样子
    // 必须是一段 HTML 给出两个inline-block节点，而且必须是 <i>
    // 第一个<i>是节点收起的状态，第二个<i>是节点展开的状态
    handle : '<i class="fa fa-caret-right"></i>
              <i class="fa fa-caret-down"></i>'
    
    // 是否显示多选框，默认为 false
    checkable : false
    
    // 多选框的样子，如果不声明，会有默认的样子
    // 必须是一段 HTML 给出两个inline-block节点，而且必须是 <i>
    // 第一个<i>是没选中的状态，第二个<i>是选中的状态
    checkbox : '<i class="fa fa-square-o"></i>
                <i class="fa fa-check-square-o"></i>'
    
    // 如果节点被激活，是否主动展开子节点，默认 false
    openWhenActived : false,
    
    // 如果激活的节点文本被点击
    on_click_actived_text : {c}F(obj, jText, jNode);
    
    // 树节点自定义的 contextmenu，函数需要返回 menu 控件 setup 段的内容
    on_contextmenu : {c}F(obj);
    
    // 绘制完一个树节点后，你可以补充对这个节点的后续处理
    on_draw_node : {c}F(jNode, obj)
    
    // 所有回调的上下文，默认是树控件本身
    context : null
    
    //................................. 事件
    on_checked : {c}F([o..])    // "tree:checked" 参数为本次被选的对象
    on_uncheck : {c}F([o..])    // "tree:uncheck" 参数为本次被选的对象
    on_actived : {c}F(o,jq)     // "tree:actived" 参数为本次激活的对象
    on_blur    : {c}F(o,jq)     // "tree:blur"    参数为列表中已经激活的项目
}
```

# 支持的操作


## getActived

```
// 获取当前被激活的对象
var o = tree.getActived();
```

## setActived

```
// 将某个对象变成激活状态，会触发对应的回调和消息
tree.setActived(Element | jQuery | "ID" | {..})
```

* 对于 Element 和 jQuery，则表示 dom 节点
* 如果是普通对象，代表查找，则依次匹配列表的数据，发现完全匹配的，就激活
* 如果是字符串，则表示数据的 ID

## blur

```
// 如果列表有激活的项目，取消它，会触发对应的回调和消息
tree.blur()
```

## getChecked

```
// 获取当前被选中的对象，当然如果多选开关没开或者没有项目被选中，永远返回空数组
var objList = tree.getChecked();
```

## check

```
// 将给定的东东，全部选中，并触发相应的消息
// 如果没有参数，则表示全选
tree.check(Element | jQuery | ID | {..})
```

## uncheck

```
// 将给定的东东，全部选中，并触发相应的消息
// 如果没有参数，则表示全部取消
tree.uncheck(Element | jQuery | ID | {..})
```

## toggle

```
// 将给定的东东，全部反选，并触发相应的消息
// 如果没有参数，则表示全部反选
tree.toggle(Element | jQuery | ID | {..})
```

## addNode

```
// 有活动节点插前面，否则插第一个
tree.addNode({..}, "before");

// 有活动节点插后面，否则插最后一个
tree.addNode({..}, "after");

// 插第一个
tree.addNode({..}, "first");

// 插最后一个
tree.addNode({..}, "last");
```

