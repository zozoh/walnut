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

# 组件的 DOM 结构

```
.hm-block             # 最外层布局块
    .hmb-con          # 内容包裹，确保下面的内容相对自己是 relative 的
        .hmb-assist       # 块的辅助编辑元素
            .rsz-N        # 顶部 resize 区
            .rsz-W        # 左侧 resize 区
            .rsz-E        # 右侧 resize 区
            .rsz-S        # 底部 resize 区
            .rsz-NW       # 左上角 resize 区
            .rsz-NE       # 右上角 resize 区
            .rsz-SW       # 左下角 resize 区
            .rsz-SE       # 右下角 resize 区
            .hmv-hdl      # 修改组件树结构的控制柄
        .hmb-area     # 盛放组件
            .hm-com [ctype="xxx"]     # 开始控件 UI
                <script.hmc-prop>     # 控件的配置信息
                .ui-arena             # 控件的 DOM
     <script.hmc-prop-block>     # 控件的配置信息
```

要做的:

1. 用 pmoving 代替 moveresizing




