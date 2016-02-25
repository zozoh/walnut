---
title: ZUI 的创建
author: zozoh
tags:
- ZUI
---

# 顶级

```
new MyUI({});
```
或
```
new MyUI({
    pel : $(document.body)
});
```

# 父扩展点

```
new MyUI({
    parent : xxx,
    gasketName : "abc"
});
```
或
```
new MyUI({
    $pel : $xxx,
});
```
* 因为你指定了 `$pel`，那么其所在的 UI 就自动成为父UI
* `$pel` 不在任何 UI 下，那么就是顶级 UI

# 使用某存在的节点

```
new MyUI({
    $el : $xxx
    keepDom : true  // 默认true, false 表清空
});
```
* `$el` 如果是某 UI 顶级节点，则释放该UI
* 自动找到 `$pel`，且其所在的 UI 自动成为父 UI
* `$el->document.body` 的情况呢？ 呵呵，自然是顶级 UI 咯
* 默认 *keepDom*，如果指明 false，则清空其内所有子元素


