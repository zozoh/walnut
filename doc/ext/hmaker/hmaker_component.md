---
title:编辑器控件
author:zozoh
tags:
- 扩展
- 网站
---

# 什么是编辑器控件

hmaker 对一个页面具体的编辑是是由它内置的一个个控件来完成的

![](hmaker_component.png)

编辑器规划出四个区域交由控件的实现来控制:

1. 控件的菜单(menu): 对于控件编辑区执行的命令
2. 控件编辑区(view): 控件在编辑区所见即所得的部分
3. 控件的标题(title): 控件的标题区，可以显示控件的:
    - 名称 (text)
    - 图标 (icon)
    - 帮助 (help)
4. 控件的属性(prop): 提供一个表单项，修改对应的编辑区

# 编辑器的内存逻辑结构

编辑器控件是一种虚UI，即，它的 `$pel | $el` 均是空。
具体来说，是这么创建的
```
// 之前激活控件释放
AC.release();

// 创建新控件实例
AC = new HmakerComponent({
    menu : $menu        // 菜单的 $pel
    title : $title      // 标题区的 $pel
    prop : $prop        // 属性区的 $pel
    // 编辑区的 DOM，如果 $view.hasClass("hmc-new")
    // 那么会创建控件的编辑 DOM将其替换，
    // 否则将这个 DOM作为 $el
    view : $view
});
```
在控件的 init 函数里，会首先为四个区域创建分别创建子 UI

```
AC.uiMenu
AC.uiTitle
AC.uiProp
AC.uiView
```





