---
title: 遮罩控件
author:zozoh
---

# 控件概述

遮罩控件将会覆盖满父元素的区间，并在上面提供插入点。
通常的，调用者会不设它的父元素，以便它成为顶级控件

# 如何创建实例

```
new MaskUI({
    // 尺寸，支持浮点(相当于百分比)，整数，和百分比
    // 百分比，表示相对窗口的尺寸
    // 浮点数，则表示相对自己的宽高，
    // 比如  width:100, height:0.6 那么 height 相当于 100*0.6=60
    // 又如  width:0.3, height:100 那么 width 相当于 100*0.3=60
    // 如果两个都是浮点数，那么，先根据窗口尺寸计算宽度，再计算高度
    width   : 500       // 主体宽度
    height  : "80%"     // 主体高度

    // 行为
    closer  : Boolean   // 是否显示关闭按钮
    escape  : Boolean   // 是否支持 esc 键退出
    
    // 主体使用的 UI，如果没有就是一个空的遮罩
    setup   : {
        uiType : "name/to/ui",
        uiConf : {..}
    }
    
    // 控件事件
    on_close : F(UI)     // "mask:close" 关闭时回调, UI 为承载的 UI

}).render();
```

# 控件属性

## `_body`

当控件渲染完毕，主体部分加载的 UI 实例会记录在这个属性里

# 控件方法

## close

```
// 这个相当于 destroy 方法，不过会触发消息和调用 on_close 回调
mask.close();
```









