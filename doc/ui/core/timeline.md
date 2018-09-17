---
title: 时间线控件
author:zozoh
---

# 控件概述

*timeline* 控件会在给定选区绘制一个 24 小时的时间线。就像各个日历应用一样。并提供创建和修改项目的功能。

控件在时间区域，可以允许用户创建一个新的块。块对应的数据可以是任何 JS Object。

# 如何创建实例

```js
new TimelineUI({
    // 建立多少个绘制层
    layers : {
        // 层名称作为 Key
        AAA : {
            // 层对象的样式
            css : {
                "color" : "#AAF",
                "background-color" : "rgba(88,88,255,0.8)",
                "border-color" : "rgba(66,66,200,0.8)",
            }
            /*
            当绘制一个时间块后，会调用这个函数来重绘内部的显示内容
            如果掉到这个函数，那么一定是这个区块的位置大小什么都已经给你设置好了
            你就直接在里面绘制就好了.
            
            context 参数是一个 JS 对象，包括字段:
              - $block : jQuery: 你要绘制的区域，DOM 结构参加下节
              - $info  : jQuery: 绘制区内的标题区
              - $main  : jQuery: 绘制区内的内容区
              - ui     : UI : 指向当前的 timeline UI
                 
            tlo  参数是一个这个区域你在 on_create 时候生成的 JS Object
            */ 
            on_draw_block : {context}F(tlo)
            
            /*
            当层发生变化时的回调
            context 参数是一个 JS 对象，包括字段：
             - layerName  : "xxxx",     // 改变的层
             - currentObj : {...},      // 改变的 tlo
             - layerData  : [{..},{..}] // 当前层的数据
             - data       : {...},      // 整个时间线的数据
            */
            on_layer_change : {context}F(layerData)
        }
    }
    
    /*
    事件: 当点击空白区域，会调用
    
     from - 这个区域的开始时间 _t (@see $z.parseTime)
     to   - 这个区域的结束时间 _t
     callback - 回调函数，当你函数处理完毕后，调用
     
        callback({
            layer : "AAA"   // 层的名称
            from  : _t,
            to    : _t,
            ... 你其他的数据 
        })
    
    控件会在对应位置绘制相关区块。当然，如果你给出的 obj 是假，那么什么也不会绘制
    */
    on_create : {UI}F(from, to， callback)
    
    /*
    当任何层发生变化时的回调。这个会在对应层的 on_layer_change 后面被调用
    context 参数是一个 JS 对象，包括字段：
    - layerName  : "xxxx",     // 改变的层
    - currentObj : {...},      // 改变的 tlo
    - layerData  : [{..},{..}] // 当前层的数据
    - data       : {...},      // 整个时间线的数据
    */
    on_change : {context}F(data)
    
})
```

# jBlock 的 DOM 结构

```html
<div class="tmln-obj">
    <div class="tmln-objw">
        <!--
        头部区
        -->
        <header>
            <dt>11 - 1p</dt>
            <em><!--$info:标题区--></em>
        </header>
        <section><!--$main:内容区--></section>
        <!--
        控制柄
        -->
        <footer>
            <span>==</span>
        </footer>
    </div>
</div>
```

# 支持的操作

## setData : 设置数据

```js
uiTimeline.setData([{
    layer : "AAA"   // 层的名称
    from : '10:20:12',
    to   : '12:32:15',
    ...
}]
```

* 设置数据，会清除所有当前的数据显示
* 输入必须是一个数组，如果是普通对象，将用数组包裹
* 数据元素必须有 `from` 和 `to` 两个字段表示时间段的开始和结束
* 其他字段您随意

## getData : 获取数据

```js
var list = uiTimeline.getData();
```

## add : 添加数据

```js
uiTimeline.add({
    layer : "AAA"   // 层的名称
    from : '10:20:12',
    to   : '12:32:15',
    ...
})
```

* 当然您也能添加一个数组

## clear : 全部清除

```js
uiTimeline.clear()
```


