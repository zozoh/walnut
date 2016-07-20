---
title: 时间线控件
author:zozoh
---

# 控件概述

*timeline* 控件会在给定选区绘制一个 24 小时的时间线。就像各个日历应用一样。并提供创建和修改项目的功能。

控件在时间区域，可以允许用户创建一个新的块。块对应的数据可以是任何 JS Object。

# 如何创建实例

```
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
            // 当绘制一个时间块后，会调用这个函数来重绘内部的显示内容
            // jBlock 参数是一个 jQuery 对象表示你要绘制的区域，DOM 结构参加下节
            // tlo  参数是一个这个区域你在 on_create 时候生成的 JS Object
            // 如果掉到这个函数，那么一定是这个区块的位置大小什么都已经给你设置好了
            // 你就直接在里面绘制就好了
            on_draw_block : {context}F(jBlock, tlo)
        }
    }

    // 各种回调的上下文，默认是 UI 自身
    context : null,
    
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
    on_create : {context}F(from, to， callback)
    
})
```

# jBlock 的 DOM 结构

```
<div class="tmln-obj">
    <div class="tmln-objw">
        <!--标题区-->
        <header><dt>11 - 1p</dt></header>
        <!--内容区-->
        <section></section>
        <!--控制柄-->
        <footer><span>==</span></footer>
    </div>
</div>
```

# 支持的操作

## setData : 设置数据

```
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

```
var list = uiTimeline.getData();
```

## add : 添加数据

```
uiTimeline.add({
    layer : "AAA"   // 层的名称
    from : '10:20:12',
    to   : '12:32:15',
    ...
})
```

* 当然您也能添加一个数组

## clear : 全部清除

```
uiTimeline.clear()
```


