---
title:页面控件概述
author:zozoh
tags:
- 扩展
- hmaker
---

# 什么是组件

*组件* 是一个网页基本的组成单元，组件有两种

1. 普通组件
2. 布局组件

只有布局组件可以嵌套子组件

# 控件的 DOM 通用结构

```
.hm-com [ctype="xxx"] [ui-id] [hm-actived]   # 最外层布局块
    <script class="hm-prop-block">   # 控件的块布局配置信息
    <script class="hm-prop-com">     # 控件的内容配置信息
    .hm-com-W                # 内容包裹，确保下面的内容相对自己是 relative 的
        .hm-com-assist       # 块的辅助编辑元素
            .hmc-ai [m="N"]  # 顶部 resize 区
            .hmc-ai [m="W"]  # 左侧 resize 区
            .hmc-ai [m="E"]  # 右侧 resize 区
            .hmc-ai [m="S"]  # 底部 resize 区
            .hmc-ai [m="NW"] # 左上角 resize 区
            .hmc-ai [m="NE"] # 右上角 resize 区
            .hmc-ai [m="SW"] # 左下角 resize 区
            .hmc-ai [m="SE"] # 右下角 resize 区
            .hmc-ai [m="H"]  # 修改组件树结构的控制柄
        .ui-arena         # 每个控件在这个节点内定制自己的显示内容
```

# 控件运行时的通用行为

任何控件输出后，都有自己的顶级元素，通常是 `DIV`,

```
<div class="hm-com xxxx" ctype="xxxx" wn-runtime-jq-fn="xxxx">
    ...
```

这里关键是这个 `wn-runtime-jq-fu` 属性。表示运行时，一个 jQuery 插件来控制其行为。
同时 `dynamic` 控件会根据这个插件调用 `"value"` 指令以便获取/设置控件运行时的值。

所有符合 `hmaker` 标准的控件，如果要提供动态获取/设置值的能力，那么就要接受 "value" 指令。并在其输出的 DOM 上，标识 `wn-runtime-jq-fn` 属性。

比如控件:

```
<div class="hm-com hm-com-pager hmc-pager" wn-runtime-jq-fn="hmc_pager">
    ...
```

那么如果运行时，想获取关键的翻页信息，需要调用:

```
var pg = $(xxx).hmc_pager("value");
// 这里返回值 pg 的对象结构为:
{
    pn   : 1,    // 期望跳转的页数
    pgsz : 50,   // 页大小
    skip : 0     // 相当于 (pn-1)*pgsz
}
```

如果想设置控件运行时信息，需要调用

```
$(xxx).hmc_pager("value", {...});
```



