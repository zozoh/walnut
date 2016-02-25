---
title:网站编辑器
author:zozoh
tags:
- 扩展
- 网站
---

# 概述

本应用就是在一个文件夹下制作静态网页，静态网页组件包括:

* 导航按钮
* 简单图片
* 简单文本框

# 项目目录结构

```
%随便一个目录都可以是工程目录%
icons            # 图标目录
images           # 图片素材目录
index.html
abc.html
xyz.html
```

* 工程目录通过命令 `hmaker publish [src dir] [dest dir]` 生成到输出目录
* 说是生成，实际上就是 copy 咯，除非以后有更复杂的诉求

```
%随便一个目录都可以是输出目录%
icons            # 图标目录
images           # 图片素材目录
index.html
abc.html
xyz.html
```

* 通常输出目录有 `www` 属性，因此可以直接被访问

# hmaker 编辑器

![](hmaker_overview.png)

# 所有的控件的属性都用 CSS 来界定

所有的控件的顶级元素都支持 CSS 属性:

```
position: absolute      // 绝对位置
top,left,right,bottom,
width,height            // 指定宽高，没值表示自动
padding                 // 内边距
margin                  // 外边距
```

# 导航按钮

![](hmaker_navbtns.png)

```
................................ DOM
<div class="hmc-navbtns">
    <div class="hmc-nbi">
        <div class="hmc-nbi-pic">
            <img src="a.png">
        </div>
        <div class="hmc-nbi-txt">xxx</div>
    </div>
</div>
................................ 支持样式
- color   # 按钮文字颜色
```

# 简单图片

![](hmaker_image.png)

```
................................ DOM
<div class="hmc-image">
    <img>
</div>
................................
支持 CSS
- img.border-radius    # 图片圆角
- img.border-width     # 边框宽度
- img.border-style     # 边框样式
- img.border-color     # 边框颜色
```

# 简单文本框

![](hmaker_text.png)

```
................................ 编辑 DOM
<textarea class="hmc-text"></textarea>
................................ 支持样式
 - color        # 文字颜色
 - font-size    # 文字大小
 - line-height  # 行高
 - text-align   # 水平对齐
 ................................ 输出 DOM
 <div class="hmc-text"></div>
```


