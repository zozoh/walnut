---
title: 输入框控件
author:zozoh
---

# 控件概述

提供一个文本输入框

# 如何创建实例

```
new CInputUI({
    // 指定宽度，数字表示像素，如果带单位，用字符串，和css的一样
    width : 89,
    
    // 获取数据时，是否 trim 前后空白，默认为 true
    trimData : true,
    
    // 是否显示输入单位，默认为 null
    unit : "px",
    
    // 输入框的占位符
    placeholder : "i18n:xxx"
    
    // 辅助按钮。如果给定了辅助按钮 UI，那么输入框会显示一个辅助按钮
    // 当用户点击，会弹出一个 UI，帮助用户输入，比如选择日期范围等
    // 通过辅助按钮可以获得更好的用户体验
    assist : {
        icon : '<..>',       // 按钮图标
        text : "i18n:xxx",   // 按钮文字
        autoOpen : true,     // 输入时自动展开，默认 true
        padding : 0,         // 助手悬浮窗的边距，默认为皮肤来指定
        // 适配输入框的上下箭头到助理界面的指定方法
        adaptEvents : {
            "UP"   : "selectPrev",
            "DOWN" : "selectNext",
        },
        // 下面是助手的 UI 定义
        uiType : "xxx",
        uiConf : {..}
    }
});
```

# 控件方法

继承自 `form_ctrl`

## setUnit

```
// 设置单位
uiInput.setUnit("px");

// 隐藏单位
uiInput.setUnit(null);
```
## setPlaceholder

```
// 设置占位符
uiInput.setPlaceholder("i18n:xxx");

// 隐藏占位符
uiInput.setPlaceholder(null);
```

## setAssist

```
// 设置助理
uiInput.setAssist({
    icon : '<xxx>',
    uiType : 'ui/form/c_number_range',
});

// 清除助理
uiInput.setAssist(null);
```

## openAssist

```
// 打开助理
uiInput.openAssist();
```

> 如果没有设置助理，则无效

## closeAssist

```
// 关闭助理
uiInput.closeAssist();
```

>  如果助理没有打开，则无效

## focus

让输入框聚焦

```
uiInput.focus();
```

