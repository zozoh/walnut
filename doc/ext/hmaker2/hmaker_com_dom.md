---
title:组件的DOM结构
author:zozoh
tags:
- 扩展
- HMaker
---

# DOM 结构

```
.hm-block       # 最外层，负责样式
    .hmb-con    # 内容包裹，确保下面的内容相对自己是 relative 的
        .hmb-area  # 区域，默认撑满 
            .hm-com [ctype="xxx"]     # 开始控件 UI
                <script.hmc-prop>     # 控件的配置信息
                .ui-arena             # 控件的 DOM
```

* 移动时，向上找到第一个 `.hmb-area`  就是当前 block 的视口，默认是 body
* 点击 `.hmb-com` 的时候激活 `.hm-block[current=yes]`
* 一个 `.hmb-area` 可以有多个 `.hm-com`
* 通过 `.hmb-area` 可以嵌套另外一个 `.hm-block`

# 属性面板

* 块
    * position,top,left,bottom,right,width,height
    * padding,border,border-radius 
    * background, color
* 栏
    * 有几栏
    * 栏位置（居中，撑满)
* 控件
    * 不同的控件，显示不一样的属性

# 关于块

## 块属性

```
{
    mode : "abs",
    posBy   : "top,left,width,height",
    posVal  : "10px,10px,300px,200px",
    padding : "10px",
    border : 0 ,   // "1px solid #000",
    borderRadius : "5px",
    background : "#CCC",
    color : "#F00",
    boxShadow : null, // "1px 1px 3px #000"
}
```

## 绝对定位

```
<div class="hm-block"
    hmb-mode="abs"
    hmb-pos-by="top,left,width,height"
    hmb-pos-val="10px,10px,300px,200px">
```

## 在页面中跟随

```
<div class="hm-block" hmb-mode="inflow">
```

## 在页面中指定宽度

```
<div class="hm-block" hmb-mode="inflow" hmb-width="80%">
```

## 其他属性

```
<div class="hm-block"
    hmb-padding="10px"
    hmb-border="1px solid #FFF"
    hmb-border-radius="4px"
    hmb-background="id(xxx) #000"
    hmb-color="#FFF">
```
* 这些属性的值均遵循 CSS 规范
* *background* 的值，`url(xxx)` 部分，可以用 `id(xxx)` 表示某个 Walnut文件

# 消息

hmaker 的子 UI 可以监听下列通知:

 Message      | Params        | Comments 
--------------|---------------|----------
prop:update   | key,val       | 属性面板被编辑
rs:actived    | o             | 选中资源项目
rs:blur       | o             | 资源项目失去焦点
com:actived   | jCom          | 组件被激活
com:change    | {..}          | 组件被修改
area:actived  | jArea         | 栏被激活
area:change   | {..}          | 栏被修改
block:actived | jBlock        | 块被激活
block:change  | {..}          | 块被修改
page:actived  | *--*          | 整个页面被激活


# 一些主要的调用逻辑

## 打开一个文件

```
1. resource.actived -> rs:actived(o)
2. hmaker.changeMain(o) {
    网页 ?= o
    hm_page.update(o)
    
    文件夹 ?= o
    hm_folder.update(o)
}
```

## 加载网页的编辑界面

```

```

# 页面保存时的逻辑

```

```




