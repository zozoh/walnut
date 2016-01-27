---
title:bpml的语法
author:zozoh
---

# bpml语法概述

1. 就是 HTML
2. 多了一些特殊的标签和属性
3. *bp_app* 根据这些标签提供界面给 *服务商*


# 一个 bpml 的样子 

```
<!DOCTYPE html>
<html>
<head>
<!-- 这里被 cmd_bp | bp_app 接管了 -->
</head>
<body>
<div bptp="columns">
    <div bp-col-val="30%">
        <div bptp="text">{{bp.text.tip}}</div>
    </div>
    <div bp-col-val="*">
        <div bptp="pic"></div>
    </div>
</div>
</body>
</html>
```

* 实际上元素的关键属性 *bptp* 决定了这个元素的类型，我们称为 *控件*
* 设计师可以定义控件的外观
* 平台定义了控件的种类和行为
* 有的控件专门负责前端显示，有的专门负责数据处理，更多的两者兼有
* 控件对自己的子 DOM 结构有约定，必须遵守这个约定 


## columns : 分栏布局

```
<div bptp="columns">
    <div bp-col-val="30%" class="bp-col">
        <div class="bp-col-con">
            <!--// 这里是随便什么内容 -->
        </div>
    </div>
</div>
```

* `bp-col-val` 支持如下格式
    - 30% 百分比
    - 100 像素
    - "*" 分配剩余
* `.bp-col-wrapper` 内可以像一个 *block* 一样依次排放内容

## abslayout : 绝对布局

```
<div bptp="abslayout">
    <div class="bp-abslayout-con">
       <!--// 这里是随便什么内容 -->
    </div>
</div>
```

* 任何控件在这里都必须有一个绝对位置

## block : 普通块布局

```
<div bptp="block">
    <div class="bp-block">
       <!--// 这里是随便什么内容 -->
    </div>
</div>
```

* 厄，没啥可说的

## text : 文本

```
<div bptp="text">
    <div class="bp-text">
       <!--// 这里是文本内容 -->
    </div>
</div>
```

* 文本内容可以支持链接
* 也就是一段 HTML
* 支持 B,I,U,OL,UI,BLOCKQUOTE,P

## pic : 图片

## slider : 轮换图

## table : 表格

## flist : 文件列表








