---
title:组件:ThingSet
author:zozoh
tags:
- 扩展
- HMaker
---

# ThingSet 控件的 DOM 结构

```
.hm-com        <- UI.$el
    @ctype  : "thingset"
    @th_set : ID
    @mode   : - none     # [默认]未设置
              - ok       # 正常
              - gone     # 不存在
              - invalid  # 不是一个 thingset
    # 这里用 JSON 记录了控件的内部元素的样式
    <script type="text/x-template">
    {
        ".hmc-ths-filter xxx" : {
            // .... 这里是具体的 CSS
        }
    }
    </script>
    # 针对的内容是控件内元素的样式，纯输出节点
    # 会根据上面 <script> 的内容，补上控件的 ID 作为选择器
    <style>
    #com86 .hmc-ths-filter xxx {
       ...
    }
    </style>
    .ui-arena hmc-thingset
        # 下面节点对应不同的模式
        .hmc-th-W[for-mode="none"]
        .hmc-th-W[for-mode="gone"]
        .hmc-th-W[for-mode="invalid"]
        .hmc-th-W[for-mode="ok"]    # ok 模式的话，下面是预览版式
            .hmc-ths-filter         # 过滤器
            .hmc-ths-list           # 列表
                .hmc-ths-item       # 每个列表项的版式
            .hmc-ths-pager          # 翻页器
        
```

# ThingSet 控件的属性面板

```
数据源
    thingSet ID

过滤器
    是否显示过滤器
    过滤条件
    - 关键字
        + 字段1
        + 字段2
        + 字段3
    - 范围1 & 范围2 & 范围3
    - 值1 & 值2 & 值3
    - [添加]
    排序
    - 排序1 | 排序2 | 排序3
    

列表版式
    更换版式，会更换对应的 版式 <style> 和 DOM
    版式存放在
        ~/.hmaker/thingset/
            aaa            # 版式名称
                dom.html   # DOM 结构
                css.json   # CSS 的JSON描述

翻页器
    是否翻页
    每页大小
```



