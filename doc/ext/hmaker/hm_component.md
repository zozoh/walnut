---
title:组件
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

# 组件的 DOM 通用结构

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

