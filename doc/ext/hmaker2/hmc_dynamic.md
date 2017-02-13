---
title:控件:动态数据对象
author:zozoh
tags:
- 扩展
- hmaker
---

# 动态数据控件概述

动态数据控件被设计与 [HTTP接口](../../core/httpapi.md) 协同工作，可以在网页里呈现任意动态数据。由于动态数据部分稍微有点复杂，这个控件除了 *hmaker* 之外，还需要如下的支持:

- `HTTPAPI` 提供动态数据
- `.hmaker/template` 提供数据渲染的 DOM 模板
- `/rs/ext/hmaker/hmc_dynamic.js` 提供运行时逻辑

# 配置信息

通过 *hmaker* 编辑出来的属性信息格式如下:

```
{
    // HttpAPI 的接口
    "api": "/gbox/list",
    
    // HttpAPI 接口的请求方式，默认为 GET
    // 你也可以声明成 POST
    "api_method" : "GET",
    
    // 接口参数表 @see 参数表规范
    "params": {
        "pid": "输入数据集合的ID",
        "c": "输入分类标签",
        "site": "${siteName}"
    },
    
    // 声明了 HttpApi 返回数据的格式 @see HttpApi 返回类型
    "api_return" : "list",
    
    // 模板名称
    "template": "列表项_应用",
    
    // 模板参数表 @see 参数表规范
    "options": {
        "href": "目标页面"
    }
}
```

# 参数表规范

根据参数表规范，控件可以动态生成一个用户友好的表单，让用户编辑参数表信息。

```
"params": {
    // 只能是 thingset 的 ID
    pid:  "@thing",
    
    // 普通单行文本输出框
    c:    "@input",
    
    // 站点名称列表，默认是 ${siteName} 表当前站点
    site: "@sites"
    
    // 当前页面上的控件,得到值是控件的ID
    // ':' 后面表示指定的类型（半角逗号分隔）
    // 如果没有指定类型，则可以是除自己之外的任意控件
    // 运行时，控件获取的数据，将会作为 GET/POST 参数融合到本控件的参数表里
    com : "@com:pager"
},
```

# HttpApi 返回类型

HttpApi 返回数据的格式可能有下面几种:

- `obj`  : 一个普通 JSON 对象
- `list` : 对象列表，就是一个 JSON 数组
- `page` : 带翻页的对象列表，格式为:

```
{
  list  : [..],   // 当前页的 JSON 数组
  pager : {       // 分页信息
      pn : 1      // 当前第几页, 1 base
      pgsz : 50   // 一页最多多少数据
      pgnb : 4    // 一共多少页
      sum  : 197  // 一共多少条数据
      skip : 0    // 本页跳过了多少数据，即: (pn-1)*pgsz
      nb   : 50   // 本页实际有多少数据
  }
}
```

