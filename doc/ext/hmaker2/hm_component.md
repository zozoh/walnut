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
    <script for="block">     # 控件的块布局配置信息
    <script for="com">       # 控件的内容配置信息
    .hm-com-W                # 内容包裹，确保下面的内容相对自己是 relative 的
        .hm-com-assist       # 块的辅助编辑元素
            .rsz-N        # 顶部 resize 区
            .rsz-W        # 左侧 resize 区
            .rsz-E        # 右侧 resize 区
            .rsz-S        # 底部 resize 区
            .rsz-NW       # 左上角 resize 区
            .rsz-NE       # 右上角 resize 区
            .rsz-SW       # 左下角 resize 区
            .rsz-SE       # 右下角 resize 区
            .hmv-hdl      # 修改组件树结构的控制柄
        .ui-arena         # 每个控件在这个节点内定制自己的显示内容
```

# 各个控件的 DOM 结构

## image : 图片控件

```html
<div class="ui-arena hmc-image hm-del-save">
    <img class="hmc-image-pic">
    <div class="hmc-image-txt">这里面是图片的标注文字</div>
    <div class="hmc-image-link-tip"><i class="zmdi zmdi-link"></i></div>
</div>
```

## rows : 水平分栏

```html
<div class="ui-arena hmc-rows">
    <div class="hm-area" area-id="Area1">
        <div class="hm-area-con">
            <!--// 这里是子控件的内容 -->
        </div>
    </div>
    <div class="hm-area">..</div>
</div>
```















