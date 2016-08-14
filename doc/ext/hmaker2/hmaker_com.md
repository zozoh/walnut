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

 Message        | Params        | Comments 
----------------|---------------|----------
active:rs       | o             | 选中资源项目
active:folder   | o             | 文件夹被激活
active:other    | o             | 其他对象被激活
active:page     | o             | 页面被激活
active:block    | jBlock        | 块被激活
active:area     | jArea         | 栏被激活
active:com      | jCom          | 组件被激活
change:block    | {..}          | 块被修改
change:area     | {..}          | 栏被修改
change:com      | {..}          | 组件被修改
change:com:ele  : {..}          | 组件内元素被修改后
show:com:ele    | --            | 需要显示控件的扩展属性面板
hide:com:ele    | --            | 需要隐藏控件的扩展属性面板



# 页面保存时的逻辑

```

```

# 组件方法

## getData

获取数据，会调用控件实现类的 `getProp` 获取额外方法

```
{
    _seq   : 1,      # 序号
    _type  : "text", # 类型
    
    ... 剩下的由 getProp 来填充
}
```

## setData

根据数据设置显示

## getProp

子类实现

## paint

子类实现，绘制 `$el` 区域的 DOM 显示

## setupProp

子类实现，返回属性面板的自定义设置，返回的数据格式为

```
{
    uiType : "xxxxx",    # UI 的定义
    uiConf : {           # UI 的配置信息
        ...
    }
}
```



