---
title: 标签组控件
author:zozoh
---

# 控件概述

*tabs* 控件控制一组子 UI，用标签来显示

# 如何创建实例

```
new TabsUI({
    mode : "top|left|right|bottom",
    defaultKey : ".." | {c}F()         // 默认显示的 Key，如无定义，显示第一个
    setup : {
        "key1: {
            icon : '<..>',              // 标签的图标
            text : '<..>',              // 标签的文字
            uiType : ".." | {c}F(key),  // 标签对应的 UI 类型
            uiConf : {..} | {c}F(key),  // 标签对应的 UI 配置信息        
        }
    },
    //.....................................
    // 事件
    on_changeUI : {c}F(key, subUI, prevUI)    // 当一个 UI 被切换
}).render();
```

# 控件方法

## getCurrentUI

```
var ui = uiTabs.getCurrentUI();
```

* 返回当前标签组正在显示的子 UI 实例
* 如果没有任何子 UI 在显示，返回 null

## getCurrentKey

```
var key = uiTabs.getCurrentKey();
```

* 返回当前标签组的键值

## isCurrent

```
if(uiTabs.isCurrent("theKey")) {
     // TODO 你的代码
}
```

* 判断当前标签组的键是否为某个值

## changeUI

```
# 改动当前的标签，且会触发 on_changeUI 事件
uiTabs.changeUI("theKey", function(subUI, prevUI){
    // this 为 tabsUI 实例本身
    // 这里的 subUI 为切换后的显示的 UI 实例
    // prevUI 为之前的当前子 UI，为空表示之前没有显示过 UI
});

# 改动当前的标签，且不会会触发 on_changeUI 事件
uiTabs.changeUI("theKey", false);

# 当然也可以
uiTabs.changeUI("theKey", function(subUI, prevUI){
    // .. TODO 你的逻辑
}, false);
```

## setMode

```
// 标签在顶部「默认」
uiTabs.setMode();
uiTabs.setMode("top");

// 标签在左侧
uiTabs.setMode("left");

// 标签在右侧
uiTabs.setMode("right");

// 标签在底部
uiTabs.setMode("bottom");
```

