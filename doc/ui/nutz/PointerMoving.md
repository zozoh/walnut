---
title:PointerMoving - 指针移动上下文
author:zozoh
---

# 设计意图

* 希望触控设备和桌面鼠标设备采用同样的方法来处理下面这些交互行文
    - 移动
    - 拖拽
    - 改变大小
    - 其他收拾
* 本方法给出了一个底层实现的基础
* 本方法是一个 jQuery 插件

# 实现原理

![](js_PointerMoving.png)

* `trigger` : 触发移动的元素
* `viewport` : 移动的范围
    - jQuery | DOM 根据一个元素限定移动范围
    - Rect 指定一个绝对的移动范围
    - null 整个窗体为移动范围
* `FirePoint` : 触发点，记录相对于触发者的位置
* `helper` : 辅助框，用来让回调绘制附加选项的

# 额外要求

* *trigger* 必须是 `position:absolute`
* *viewport* 必须是 `position:relative`
* 当然如果 *viewport* 是 null（整个窗体时）, *trigger* 可以是 `position:fixed`;

# 调用方式

```
new PointerMoving(context, {
    
});
``` 

