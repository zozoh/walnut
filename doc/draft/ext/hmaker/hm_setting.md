---
title:约定:动态设置
author:zozoh
tags:
- 扩展
- hmaker
---

# 动态设置概述

主要是从 `dynamic` 控件的需求出发，我们需要动态生成一个 `form` 控件，为用户提供便利的界面，以便采集输入。现在主要的两个地方会用到 *动态设置*：

- `dynamic` 的数据源参数表
- `dynamic` 的模板选项表

我们不敢肯定将来是否有其他地方也用到类似的功能，因此把这个规范独立出来。以备将来扩展只需

*动态设置* 本质上就是，接受一个 JSON 对象（我们称这个对象为 `setting`）作为输入，并根据这个输入生成一个约束字段的信息。调用者根据这个信息，可以生成表单控件，或者做其他的随便什么是事情。

如果生成了表单，那么用户填充后，返回的结果（我们称为 `result`）格式又会有要求。

因此，最关键的是两点: 

1. *setting* 对象的格式
2. *result* 对象的格式

# setting 对象的格式

就是一个简单名值对:

```
key : "[*][(参数显示名)]@类型[=默认值][:参数[{映射表}][#注释]]"
```

这个值的 JSON 形式就是 form 的 fld 的一个变种

```
{
    // 项目类型，下面是一些特殊的值
    // input|TSS|thingset|site|com|link|toggle|fields
    // 如果是上述值，则会解析时进行特殊处理，配置特殊的表单控件以符合具体场景要求
    type     : "thingset",  
    
    // 下面和 form field 字段意义相同
    key      : "xxx",       // 字段名
    title    : "xxx",       // 字段显示名
    tip      : "xxx",       // 提示信息
    required : true,        // 字段是否必须
    uiType   : "xxx",
    uiConf   : "xxx",
    
    // 映射表（基本只有@com类型才会有用），默认为 null
    mapping  : {..}
}
```

下面我们分别根据类型，依次详细解释这些参数的意义：

## 数据源@thingset

本参数将收集数据源的 `ThingSetId`

- `@thingset`
- `*(数据源)@thingset`
- `*(数据源)@thingset#请选择一个数据源`

## 站点@site

本参数将收集站点目录对象的某个元数据。默认是收集站点的名称。
你如果你想收集站点的 ID，请用 `@site:id`。
通常，如果不选择这个参数，默认值就应该是 `${siteName}`。
当然如果是 `@site:id`，默认值就应该是 `${siteId}`。

> 关于这两个特殊占位符，请参看[特殊占位符](#特殊占位符) 一节

- `@site`
- `(所属站点)@site:id`

## 页面控件@com

参数将收集当前页面上的控件ID。参数表示指定的类型（多个类型竖线分隔）。
类型如果没有指定，将返回全部控件
运行时，控件获取的数据（@see [页面控件概述](hm_component.md) > *控件运行时的通用行为*），
将会作为 `GET/POST` 参数融合到本控件的参数表里

- `@com:page#这里是提示信息`
- `@com:page|htmlcode#可以选择两种控件`

在进行参数表融合的时候，有可能页面控件给出的值可能与接口参数需求不匹配。这里可以通过参数声明一个映射表来解决这个问题。映射表格式为普通JSON对象，下面给一个例子:

```
@com:page|htmlcode{limit:'pgsz',skip:'skip'}#这里是提示信息
```

- 提交参数 `limit` 来自控件值的 `pgsz` 段，而 `skip` 就来自 `skip` 段。

## 超链接@link

参数将收集一个超链接。如果站内链接，将一律以 `/` 开头，相对于站点根目录的路径。
发布的时候，服务器转换器会对其进行转换.

- `@link`
- `*(链接)@link#对应页面`

## 数据映射@mapping

参数将收集一个映射表。映射表可以被 `$z.mappingObj` 函数理解，即:

- `=key`  表示从源对象中取对应键值
- `xxx`   直接就是静态值


## 字段列表@fields

参数将收集一个字段列表，表示需要采集对象的哪些字段

```
@field#这里是提示信息
```

## 布尔开关@toggle

参数将收集一个布尔型的值，它本质上就是一个静态值，不过它可以在界面上显示一个开关而已

```
# 打开值为'on'否则为'off'，默认为'on'
(测试布尔开关)@toggle=on:on/off
#---------------------------------------
# 打开值为'yes'否则为'no'，默认值为 'no'
(测试布尔开关)@toggle:yes/no
#---------------------------------------
# 打开值为true否则为false,默认为 false
(测试布尔开关)@toggle
(测试布尔开关)@toggle:true/false

# 打开值为'on'否则为null，默认为 null
(测试布尔开关)@toggle:on
#---------------------------------------
# 打开值为'yes'否则为null，默认值为 'yes'
(测试布尔开关)@toggle=yes:yes
#---------------------------------------
# 打开值为true否则为null,默认为 null
(测试布尔开关)@toggle:true
```

JSON 参数形式:

```
{
   type     : "toggle",
   arg      : "no/yes",
}
```

## 普通值@input

这个没啥好说的，基本就是用户随便填

- `@input`
- `(分类标签)@input=hello#输入分类标签，半角逗号分隔`

## 一个示例

```
"params": {
    pid:  "*(数据源)@thingset",
    c:    "(分类标签)@input=hello#输入分类标签，半角逗号分隔",
    site: "@site"
    com : "@com:page#这里是提示信息"
    href : "*(链接)@link#对应页面"   
},
```

# result 对象的格式

用户通过 `API` 的参数表，填写了参数值，有些值会被这么理解:

## 特殊占位符

在值的任何位置，都支持 `${xxx}` 的写法，表示特殊占位符，现在支持的占位符包括:

```
${siteName} :  站点主目录对象名称
${siteId}   :  站点主目录对象ID
```

## 来自请求参数

```
@<c>ade329913
```

- 本值在运行时会被 HTTP 参数（无论GET/POST) `c` 填充
- 默认值是 `ade329913`

## 来自控件的参数

```
#<pager_1>
```

- 本值会在运行时被控件 `pager_1` 的值所代替
- 控件的值来自控件的 `getComValue` 方法，返回是一个 JSON 对象或者是一个普通值
- 如果是普通值，则直接使用
- 如果是 JSON 对象则会被展开，融合到提交参数名值对中

## 站内链接

```
/abc
```

- 站内链接必须以 `/` 开头表示相对于站点根的绝对路径
- 服务器展开时，会将其替换成相对于当前页面的相对路径

