---
title:控件:动态数据控件
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

# 编辑时 DOM 结构

```
<div class="hm-com hm-com-dynamic" ctype="dynamic" id="数据列表" hm-actived="yes">
    <div class="hm-com-W">
        <div class="hmc-dynamic">
            <!--// 这里是模板控制的范围 -->
        </div>
    </div>
</div>
```

# 输出时 DOM 结构

```
<div class="hm-com hm-com-dynamic hmc-dynamic" id="数据列表">
    <!--// 这里是模板控制的范围 -->
</div>
```

# 配置信息

通过 *hmaker* 编辑出来的属性信息格式如下:

```
{
    // HttpAPI 的接口
    "api": "/gbox/list",
    
    // HttpAPI 接口的请求方式，默认为 GET
    // 你也可以声明成 POST
    // 来自 oApi.api_method
    "api_method" : "GET",
    
    // 接口参数表: @see《动态设置》
    "params": {..},
    
    // 声明了 HttpApi 返回数据的格式 @see HttpApi 返回类型
    // 来自 oApi.api_return
    "api_return" : "list",
    
    // 模板名称
    "template": "列表项_应用",
    
    // 模板选项表: @see《站点模板》
    "options": {..}
}
```

> [《站点模板》](hm_template.md)
> [《动态设置》](hm_setting.md)

# HttpApi 约定

## 请求方法：`api_method`

API 对象必须有元数据 `api_method`，可能的值是 `GET` 或者 `POST`。必须为全大写字母。如果非要写小写，不保证一定能工作正常。

## 参数表：`params`

API 对象必须有元数据 `params`，格式参见[《动态设置》](hm_setting.md)

## 返回类型：`api_return`

API 对象必须有元数据 `api_return`, 这个值会被 IDE 记录到控件参数里。返回数据的格式可能有下面几种:

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



