---
title:组件:对象列表
author:zozoh
tags:
- 扩展
- hmaker
---

# 组件的属性信息

```
{
    // @see 数据接口
    api : "/thing/query",
    
    // @see 参数表
    params : {..},
    
    // @see 显示模板
    template : "abc",
    
    // @see 过滤器
    filter : {..}
    
    // @see 排序器
    sorter : {..}

    // @see 分页器
    pager : {..}
}
```
## 数据接口:api

* 数据来自哪个 API
* 这个是给页面 JS 调用的。
* 当然，系统之后也会优化成首屏直接在服务器端获取数据。
    - 也是调用这个 API 背后的命令从而获得输出。

### 数据接口文件元数据

与普通 API 不同，这里的 API 对象需要支持 `params : [p0,p1,p2..]` 这样的元数据，来说明自己能接受哪些参数。因为 HTTP 协议参数都是字符串，因此组件也会把输入给 API 的参数都转成字符串。 

如果没有这个元数据的 API，默认会被组件认为是下面形式的 API:

```
params:["id","cnd","limit","skip","sort"]
```

这几个参数，当然有可能是错的，但是没办法哦

### 接口返回的数据格式

组件假设接口会返回下面格式的 JSON 数据:

```
{
   // 翻页信息
   pager : {
       pn   : 1,     // 第几页
       pgsz : 50,    // 每页多少数据
       pgnb : 1,     // 一共多少页
       sum  : 0,     // 一共多少记录
       nb   : 0      // 本页实际获取了多少数据
   },
   // 当页列表
   list  : [{..}, {..}]
}
```

## 参数表:params

```
...
params : {
   id  : "45cd.."      // 固定字符串
   cnd : {
       base  : {...},
       from  : "filter",
       key   : null,
       merge : true
   },
   limit : {
       base  : 50,
       from  : "pager",
       key   : "limit",
       merge : false
   },
   skip : {
       base  : 0,
       from  : "pager",
       key   : "skip",
       merge : false
   },
   sort : {
       base  : {nm:1},
       from  : "sorter",
       key   : null,
       merge : false
   },
},
...
```

* 定义了组件如何向接口发送请求
* 键是接口的参数，值可以是固定值或者动态值
* 固定值不解释，就是字符串或者数字啥的
* 动态值是一个对象，结构为:

```
{
   // 默认值，或称基础值
   base  : ..

   // 动态值来自何处, '否' 表示不获取
   // filter|pager|sorter 是组件根据用户输入获取的信息
   // HTTP_GET 是页面的请求的 URL 里面的 GET 参数 (已经被转成了JS Object)
   // HTTP_COOKIE 是页面的 cookie 对象(已经被转成了JS Object)
   from  : "filter|pager|sorter|HTTP_GET|HTTP_COOKIE"

   // 值是动态值的哪个键，'否' 表示全部对象，否则就是一个具体的键对应的值
   // 当然这个值必须是在 from 键有效的前提才有效
   key   : null

   // 如果默认值是对象，动态值（取键后）也是对象，是否要融合
   merge : true
}
```
    
## 显示模板:template

```
...
template : "xxx"    // 模板的名称
...
```

声明了显示模板的名称，一个正确的模板的路径格式如下（假设模板名为abc）

```
~/.hmaker/template/abc
   abc.wnml     // 服务端渲染代码 @see WWW 机制
   abc.js       // JS 端渲染代码，是一个 jQuery 插件
   abc.css      // 模板的 CSS
   mapping.js   // 一个 JSON 对象，描述模板需要字段映射关系
```

- 模板内文件，会随发布 copy 到发布目标 `comt` 目录下，因此保留唯一名字很重要
- `abc.js | abc.css` 这两个文件如果缺失或者损坏，组件在编辑的时候将不能正常工作
- 系统可能会优化首次加载服务器端渲染，因此需要 `abc.wnml`
- 如果没有声明这个模板，组件编辑将不能正常工作

## 过滤器:filter

```
...
// 过滤器段，如果为否，则表示不显示过滤器
filter : {
    // 显示关键字搜索框，同时指定这个搜索框对应要搜索哪些字段
    // 如果字段有分隔符 ":" 则表示这个字段只有符合后面的正则表达式才列入
    keyword : ["nm", "title", "brief", "phone:^\d{11,20}$"]
    
    // 其他扩展字段过滤条件列表
    // 过滤条件之间都是 AND 的关系
    // 过滤条件里面可以是 or 的关系
    fields : [{
        key  : "age",        // 对应对象的哪个键
        text : "年龄",        // 文字表述
        
        // 这个条件是否允许用户多选
        multi : false,
        
        // 是否默认显示
        show : false,
        
        // 下面是可以让用户点选的值，这些值通常是一个个的范围
        // 或者固定值，用户选了一个就不会选别的
        //  - 范围是一个区间的字符串表现形式
        //  - 固定值就是固定值
        list : [{
            text  : "小于23岁",
            value : "[,23)",
        }, {
            text  : "23-45岁",
            value : "[23,45]",        
        }]
    }]
}
...
```

## 排序器:sorter

```
...
// 排序的候选列表，用户只能同时选择一个，但是可以指定是升序还是降序
// 同时一个排序字段里面可以增加子排序设定，这个是用户不能指定的
sorter : {
    // 本地存储排序设定的键，如果没声明，则不存储
    localStoreKey : "hm_objlist_sort",
    
    // 排序字段列表
    fields : [{
        key   : "age"     // 排序字段
        text  : "按年龄"   // 排序名称
        order : 1 | -1    // 1 为asc默认，-1 为 desc
        
        // 是否用户可以修改正序反序
        toggleable : false
    
        // 选择本项后的，还会附加的固定排序条件
        more  : [{nm:1,ct:-1}]
    
    }]
}
...
```

## 翻页器:pager

```
...
// 翻页器指定了翻页的类型
pager : {
    // 可选的页大小，如果只有一项，则表示不能改动页大小
    sizes : [20, 50, 100, 200],
    
    // 默认页大小
    defaultPageSize : 50,
    
    // 如果用户修改了页大小是否存储在本地
    // 默认为 null 表示不存储
    localStoreKey : "hm_objlist_pgsz",
    
    // 如果只有一页，是否显示翻页条
    autoHide : true,
    
    // 翻页条风格
    // - normal 风格表示显示 [prev][1]..[4][5][6]..[23][next]
    // - jump 风格表示 [first][prev][5/23v][next][last] 页码可点击
    style : "normal|jump",
    
    // 翻页条文字，默认控件会为你填写
    i18n : {
        "prev"  : "前页",
        "next"  : "后页",
        "first" : "首页",
        "last"  : "尾页",
        "pgsz"  : "页大小",
        "sum"   : "共{{pn}}页"
    }
}
...
```

