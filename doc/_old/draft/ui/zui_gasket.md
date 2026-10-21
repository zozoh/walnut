---
title: ZUI 的扩展
author: zozoh
tags:
- ZUI
---

# 什么是 gasket

如果你为一个 UI 指定了父

```
new MyUI({
    parent : xxx,
    gasketName : "abc"
});
```

那么这个 UI 实例将会记录到父 UI 里，同时它也会将自身渲染到指定的扩展点去。
在父 UI 里，可以在任何 DOM 里面指定扩展点:

```
<div ui-gasket="abc"></div>
```

只要声明了 `ui-gasket` 属性的元素，都可以成为扩展点。 

# 另一种指定扩展点的方法

```
new MyUI({
    $pel : $xxx,
    gasketName : "abc"
});
```
你直接指定当前 UI 的 $pel，那么就相当于认为这个元素就是一个扩展点。只要你声明了 gasketName，那么这个扩展点也会被记录在父 UI 里


