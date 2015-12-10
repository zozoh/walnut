---
title:菜单控件
author:zozoh
---

# 控件概述

创建多级菜单 

# 如何创建实例

```
new UIMenu({
    $pel    : $(document.body),
    // 给定一个数组，说明菜单如何布局，每个数组项目都是一个菜单项
    // 如果给的不是数组，则会被数组包裹
    setup   : [..]
    // 指明每个菜单项被调用的时候，函数的上下文是什么，默认为菜单控件的父视图
    // 如果没有父视图，则用自身
    context : UIMenu
}).render();
```

# 菜单项:按钮

```
{
    type    : "button"           // 类型
    icon    : '<i class=..>'     // 【选】是否显示图标，以及图标的 HTML 片段
    text    : "i18n:xxx"         // 按钮文字，支持 i18n
    handler : {c}F($jq, e)      // 菜单项的回调
    context : undefined          // 菜单项回调时，特殊的调用上下文，默认采用全局配置
}
```

* 如果没写 `type`，那么如果主要判断 `handle` 段是不是一个函数

# 菜单项:命令组

```
{
    type   : "group"         // 类型
    icon   : '<i class=..>'  // 【选】是否显示图标，以及图标的 HTML 片段
    text   : "i18n:xxx"      // 组文字，支持 i18n
    items  : [..]            // 子菜单项，命令组可以层层嵌套
}
```

* 如果没写 `type`，那么主要判断 `items` 段是不是一个数组

# 菜单项:分隔线

```
{type : "separator"}
```

* 如果没写 `type`，你直接写个空对象也成，比如 `{}`

