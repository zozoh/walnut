---
title:组件:单张图片
author:zozoh
tags:
- 扩展
- 网站
---


# 概述

![](hmaker_image.png)


# DOM 结构

```
<div class="hm-com hmc-image" ctype="image" id="text1">
    <script type="text/x-template" class="hmc-prop">...</script>
    <div class="hmc-assist">...</div>
    <div class="hmc-wrapper">
        <img src="/o/read/id:xxxx" path="image/x.jpg">
    </div>
</div>
```

# 编辑区行为

在 textarea 里随便写咯

# 控件属性

## borderRadius : 图片圆角

属性:

```
borderRadius: 4
```

编辑时生成 *CSS* :

```
#(控件ID) img{
    border-radius : 4px;
}
```

渲染时生成 *CSS* :

```
#(控件ID) {
    border-radius : 4px;
}
```

