---
title: 遮罩控件
author:zozoh
---

# 控件概述

遮罩控件将会覆盖满父元素的区间，并在上面提供插入点。
通常的，调用者会不设它的父元素，以便它成为顶级控件

# 遮罩层的 DOM 结构

```
<div ui-id="view33" class="ui-mask">       <!-- 控件的主包裹元素 -->
<div class="ui-arena" ui-fitparent="yes">  <!-- 控件的显示区域 --> 
    <div class="ui-mask-bg"></div>         <!-- 遮罩的背景 -->
    <div class="ui-mask-main" ui-gasket="main"></div> <!-- 遮罩的可绘制区域 -->
    <div class="ui-mask-closer"></div>    <!-- 关闭按钮 -->
</div>
</div>
```

如果调用者想提供自定义的 DOM 结构，只要符合上述规范，Mask 控件就能很好的工作。即，你给的 DOM
结构大约是这样的:

```
DIV.ui-arena
    DIV.ui-mask-bg
    DIV.ui-mask-main  ui-gasket="main"
    DIV.ui-mask-closer
```

譬如，`ui/pop/pop.html` 就提供了带 `OK`, `cancel` 两个按钮的 DOM。这两个按钮，可以通过创建 Mask 实例时，`events` 段来规定其行为。

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

    // MaskUI 会在 `<body>` 最后附加上自己的 DOM
    // 同时它会把自己之前的元素都进行标识，以便通过 CSS 指定类似毛玻璃的效果
    // 表示的类选择器，默认为 "ui-mask-others"
    // 如果为 null 则不标识，而是直接将前面的元素都隐藏
    markPrevBy : "ui-mask-others" 
    
    // 主体使用的 UI，如果没有就是一个空的遮罩
    setup   : {
        uiType : "name/to/ui",
        uiConf : {..}
    }
    
    // 控件事件
    on_close : F(UI)     // "mask:close" 关闭时回调, UI 为承载的 UI

}).render(function(){
    // 通常一个 Mask 控件被绘制后，会在这个回调里设置内容
    // 本函数的 this 就是 MaskUI 实例本身
    // this.$main 是一个 jQuery 元素，指向  DIV.ui-mask-main
    // this.body 是一个 UI 实例，如果你定义了 `setup` 段，那么 MaskUI
    // 也会同时创建你指定的元素。你可以通过 this.body 访问到
    // 实际上，如果你的 MaskUI 实例为 uiMask，那么 uiMask.body 就是你在
    // `setup` 段指定的子 UI 的实例
});
```

# 控件属性

## `body`

当控件渲染完毕，主体部分加载的 UI 实例会记录在这个属性里

## `$main`

是一个 jQuery 元素，指向  `DIV.ui-mask-main`

# 控件方法

## close

```
// 这个相当于 destroy 方法，不过会触发消息和调用 on_close 回调
mask.close();
```









