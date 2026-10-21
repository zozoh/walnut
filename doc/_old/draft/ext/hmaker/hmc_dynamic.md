---
title:控件:动态数据控件
author:zozoh
tags:
- 扩展
- hmaker
---

-------------------------------------------------
# 动态数据控件概述

动态数据控件被设计与 [HTTP接口](../../core/httpapi.md) 协同工作，可以在网页里呈现任意动态数据。由于动态数据部分稍微有点复杂，这个控件除了 *hmaker* 之外，还需要如下的支持:

- `HTTPAPI` 提供动态数据
- `.hmaker/template` 提供数据渲染的 DOM 模板
- `/rs/ext/hmaker/hmc_dynamic.js` 提供运行时逻辑

-------------------------------------------------
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

-------------------------------------------------
# 输出时 DOM 结构

```
<div class="hm-com hm-com-dynamic hmc-dynamic" id="数据列表">
    <!--// 这里是模板控制的范围 -->
</div>
```

-------------------------------------------------
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

> - [《站点模板》](hm_template.md)
> - [《动态设置》](hm_setting.md)

-------------------------------------------------
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

-------------------------------------------------
# 列表分组

如果选择了列表型组件，在显示模板的配置参数里，可以支持一个特殊的属性，以便对结果进行分组显示。
模板的 `template.info.json` 支持选项:

```js
...
"groupBy" : "(分组字段)@input#利用这个字段进行分组，譬如 `lm::Date(yyyy)::年份%s`",
...
```

在模板的业务逻辑里，将按照下面的逻辑理解 `groupBy` 参数

```bash
# 参数格式
字段名[::转换方式[::标题模板]]
# 其中转换方式可选, 譬如
type                         # 普通: type 字段值直接分组
th_nm::S=Unknown             # 字符: th_nm 字段值截取字符串后分组
th_nm::S2=Unknown            # 截取: th_nm 字段值截取字符串后分组
th_nm::S1::Last Name: %s     # 截取: th_nm 字段值截取字符串后分组，标题需映射
th_cate::{0=A,1=B}X          # 映射: th_cate 字段分组，标题需要映射
th_cate::{0=A,1=B}Y::Cate:%s # 映射: th_cate 字段映射后分组，标题需要替换模板
lm::D(yyyy)::Year:%s         # 日期: lm 字段做日期转换后分组，标题需要替换模板
dead::[No,Yes]               # 布尔: dead 字段根据给定布尔值映射后分组
# 区间：price字段必须是数字，根据给定区间归纳
price::R{奢侈>100;普通<20>100;便宜<20}普通 
```

所有的逻辑封装在  `HmRT.parseGroupBy` 函数里，它会解析出一个结构

```js
// type                         # 普通: type 字段值直接分组
{
    name : "type"
    getGroupValue : function(obj){
        return obj[this.name];
    }
}
// th_nm::S2=Unknown            # 截取: th_nm 字段值截取字符串后分组
{
    name : "th_nm",
    len  : 2,
    dft  : "Unknown",
    getGroupValue : function(obj){
        var v = obj[this.name];
        if(_.isUndefined(v) || _.isNull(v))
            return this.dft;
        return this.len > 0
                    ? v.substring(0, this.len)
                    : v;
    }
}
// th_nm::S1::Last Name: %s     # 截取: th_nm 字段值截取字符串后分组，标题需映射
{
    name : "th_nm",
    len  : 2,
    getGroupValue : function(obj){
        return (obj[this.name] + "").substring(0,this.len);
    },
    title : "Last Name: %s",
}
// th_cate::{0=A,1=B}X          # 映射: th_cate 字段分组，标题需要映射
{
    name : "th_cate",
    mapping : {"0":"A", "1":"B"},
    dft : "X",
    getGroupValue : function(obj){
        return this.mapping[obj[this.name]+""] || this.dft;
    }
}
// th_cate::{0=A,1=B}Y::Cate:%s  # 映射: th_cate 字段映射后分组，标题需要替换模板
{
    name : "th_cate",
    mapping : {"0":"A", "1":"B"},
    dft : "Y",
    getGroupValue : function(obj){
        return this.mapping[obj[this.name]+""] || this.dft;
    },
    title : "Cate:%s",
}
// lm::D(yyyy)None::Year:%s      # 日期: lm 字段做日期转换后分组，标题需要替换模板
{
    title : "Year:%s",
    name : "lm",
    fmt  : "yyyy",
    dft  : "None",
    getGroupValue : function(obj){
        return $z.parseDate(obj[this.name]).format(this.fmt);
    }
}
// dead::[否,是]                # 布尔: dead 字段根据给定布尔值映射后分组
{
    name : "dead",
    mapping : ["否", "是"],
    getGroupValue : function(obj){
        return this.mapping[obj[this.name] ? 1 : 0];
    }
}
// price::R{奢侈>100;普通<20>100;便宜<20} 
// # 区间：price字段必须是数字，根据给定区间归纳
{
    name : "price",
    mapping : {
        "奢侈" : [100],
        "普通" : [20, 100],
        "便宜" : 20
    },
    dft : "普通",
    getGroupValue : function(obj){
        var val = obj[this.name] * 1;
        if()
        for(var key in this.ramappingnge) {
            var r = this.mapping[key];
            // 小于
            if(_.isNumber(r)){
                if(val < r)
                    return key;
            }
            // 大于
            else if(_.isArray(r) && r.length == 1) {
                if(val >= r[0])
                    return key;
            }
            // 区间 
            else {
                if(val >= r[0] && val < r[1])
                    return key;
            }           
        }
        return this.dft;
    }
}

```

在 `HmRT.groupData` 函数，将利用 `HmRT.parseGroupBy` 对数据分组，并输出一个这样的结构

```js
[{
    title : "xxx"    // 组标题
    list  : [..]     // 本组的数据对象
}, {
    // ... next group
}, {
    // 未分组数据 ...
    list : [..] 
}]
```
