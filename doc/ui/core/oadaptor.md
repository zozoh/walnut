---
title: 对象适配器
author:zozoh
---

# 控件概述

`oadaptor` 假想输入是一个 `WnObj` 对象，它会根据`WnObj`的配置，决定用什么 UI 来呈现这个对象。 因此这个控件对 `WnObj` 对象有一个自己的假设:

```
{
    // .. 所有的 WnObj 的标准字段 ..
    // 特殊字段 "_zui_" 指明了希望对这个对象采用的特殊 UI
    // 对象的各个字段的意义与 shelf.js 的区域配置对象相同
    _zui_ : {
        uiType : "name/to/ui",
        uiConf : {..}
    }
}
```

除此以外，如果用户创建实例的时候，想为一系列对象指定默认 UI，可以参看下面一节，实例的创建

# 如何创建实例

```
new UIOAdaptor({
    $pel  : $(document.body),

    // 指明是否需要忽略特殊对象的 _zui_ 字段
    // 默认的，对象的 _zui_ 字段优先
    ignoreObjUI : {..}|F(o)|true,

    // 通过一个数组，指明对于任何类型的对象，应该如何适配
    // 如果没有任何 UI 被匹配，那么本控件将在选区内打印『没有匹配UI』字样
    setup : [{
        // 指明，如果一个对象与给定对象匹配，那么就会采用
        // 值如果是对象，那么就会和给定的对象匹配
        // 如果是一个函数，则调用看看，是不是会返回 true
        // 如果直接是 true 那么一定会匹配
        // 如果为 null，则表示在给定对象为空时的 UI
        match  : {..}|F(o)|true|null,
        // 这两个字段参照 shelf 的规范
        uiType : "name/to/ui",    
        uiConf : {..}
    },{
        // 下一个适配 UI
    }]

    // 本控件支持下列消息
    on_uiChange  : F(UI)    // "oadaptor:uichange"  首次加载一定会被触发

}).render();
```

# 控件方法

## getData

```
// 如果当前已经创建了一个 UI 实例，并且有 getData 方法，那么调用
// 否则返回 null
var d = oadaptor.getData();
```

## setData

```
// 如果当前已经创建了一个 UI 实例，并且有 setData 方法，那么调用
// 否则无视，当然，调用也会传递 callback
// 同时 oadaptor 有一个约定，如果你传入的参数 o 包括了 "uiAdaptor" 字段
// 就表示需要先 changeUI 再调用这个新 UI 实例的 setData
// 这里需要主意的是，如果你的代理 UI.setData 不支持 callback 和 context 参数，那么
// oadaptor.setData 传了这两个参数也木用。
oadaptor.setData(o, callback, context);
```

## changeUI

```
// 触发当前控件
//  - uiDef    : UI 的定义对象，需要有两个属性 {uiType:"xxx", uiConf:{..}}
//  - callback : UI 渲染完的回调，默认 context 为新 UI 实例本身
//  - context  : 这里可以指定回调的实例
oadaptor.changeUI(uiDef, callback, context)
```



