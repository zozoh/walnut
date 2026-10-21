---
title:控件:面包屑菜单
author:zozoh
tags:
- 扩展
- hmaker
---

# 面包屑控件概述

面包屑菜单是大多数网站都需要的功能，目的是为了让用户知道自己所在的位置。
本控件在 `HTML代码片段` 分组里，利用`动态皮肤`的功能实现。

# 实现的机理

- [ ] dynamic 控件读取数据后，将数据记录在本身 `jq.data("@WNDATA", {..})`
- [ ] page 面板，增加选项，编辑当前页面的 hm_hierarchy
    + 暂时用一个 Textarea 来表示吧
- [ ] 输出每个页面，都带上 `window.__HM_HIERARCHY`，附在页面 `<head>` 部分
- [ ] 提供一个 `htmlcode` 的皮肤，专门输出 `hm_hierarchy`
    + 在 IDE 环境，直接读取 oPage.hm_hierarchy 就好
    + 在 runtime 环境，读取 `window.__HM_HIERARCHY`

# `hm_hierarchy` 元数据格式:

```
hm_hierarchy : "+首页:/index\n+产品中心:/product"
```

它具体的文本语法为：

```
+ 首页 : /index                       # 静态链接非常简单
+ 产品中心 : /product                 # 空格会被无视
+ #dynamic_0.th_cate                 # 从某个元素获取数据「@WNDATA」并取得某个字段
    : /product_list_{{th_cate}}      # 链接支持动态的写法
    : A=原汁机,B=破壁机,C=原汁破壁机     # 对于显示值的映射
+ #dynamic_0.lbls 
    : /product_list_{{th_cate}}?lbls={{lbls}}&th_cate={{th_cate}}
+ #dynamic_0.th_model
```

# 输出的 DOM 结构

```
<div class="hm-hierarchy">
<ul>
    <li><a href="index.html">首页</a></li>
    <li><a href="product.html">产品中心</a></li>
    <li><a href="product_list_a.html">原汁机</a></li>
    <li><a href="product_list_a.html?lbls=高效出汁型&th_cate=A">高效出汁型</a></li>
    <li><b>HU-910WN-M</b></li>
</ul>
</div>
```

