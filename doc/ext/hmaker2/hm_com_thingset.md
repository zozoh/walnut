---
title:组件:ThingSet
author:zozoh
tags:
- 扩展
- HMaker
---

# ThingSet 控件的 DOM 结构

```
.hm-com        <- UI.$el
    @ctype  : "thingset"
    @th_set : ID
    @mode   : - none     # [默认]未设置
              - ok       # 正常
              - gone     # 不存在
              - invalid  # 不是一个 thingset
    # 用 JSON 字符串存放显示字段映射 mapping
    <script type="text/x-template">
    {
        ...
    }
    </script>
    # 针对的内容是控件内元素的样式，纯输出节点
    # 会根据上面 <script> 的内容，补上控件的 ID 作为选择器
    # 如果是编辑器，则还会补上 .hmc-ths-main 作为选择器前缀路径
    <style>
    #com86 .hmc-ths-filter xxx {
       ...
    }
    </style>
    .ui-arena hmc-thingset
        # 下面节点对应不同的模式
        .hmc-th-W[mode="none"]
        .hmc-th-W[mode="gone"]
        .hmc-th-W[mode="invalid"]
        .hmc-th-W[mode="lackdef"]
        .hmc-th-W[mode="wrongdef"]
        .hmc-th-W[mode="tmplnone"]
        .hmc-th-W[mode="tmplgone"]
        .hmc-th-W[mode="tmplnodom"]
        .hmc-th-W[mode="tmplnocss"]
        .hmc-th-W[mode="tmpldom_E"]
        .hmc-th-W[mode="tmplcss_E"]
        .hmc-th-W[mode="ok"]    # ok 模式的话，下面是预览版式
            .hmc-ths-filter         # 过滤器
            .hmc-ths-list           # 列表
                .hmc-ths-item       # 每个列表项的版式
            .hmc-ths-pager          # 翻页器
        
```

# ThingSet 控件的属性面板

```
数据源
    thingSet ID

过滤器
    是否显示过滤器
    过滤条件
    - 关键字
        + 字段1
        + 字段2
        + 字段3
    - 范围1 & 范围2 & 范围3
    - 值1 & 值2 & 值3
    - [添加]
    排序
    - 排序1 | 排序2 | 排序3
    

列表版式
    更换版式，会更换对应的 版式 <style> 和 DOM
    版式存放在
        ~/.hmaker/template/thingset/
            aaa            # 版式名称
                dom.html   # DOM 结构
                css.json   # CSS 的JSON描述

翻页器
    是否翻页
    每页大小
```

# 数据源

控件的数据源来源有下面几种:

- `thing:ID` : 来自一个数据集合
- `api:/myapp/list` : 来自一个 HttpApi

无论怎样，最终都要得到一个下面结构的 JSON 数据

```
{
    // 翻页器
    pager : {
        pn   : 1,     // 第几页
        pgsz : 50,    // 每页多少数据
        pgnb : 1,     // 一共多少页
        sum  : 0,     // 一共多少记录
        nb   : 0      // 本页实际获取了多少数据
    },
    // 当页列表
    list  : [..]
}
```

这个数据会用模板进行显示

# 关于数据和显示

## 属性对象

```
{
    // 数据源
    dsId : "thing:xxxxx"
    
    // 对应到版式目录的文件名
    // 模板是一个目录，存放在
    //  ~/.hmaker/template/thingset/xxx
    template : "xxx"
    
    // 将 thingset 的字段映射到版式文件的 DOM 中
    mapping : {
        "dom.key0" : "th_nm",         // 直接对应到对象某字段
        "dom.key1" : null,            // null 表示不显示
        "dom.key2" : "",              // 空字符串表示字段显示，但是没值
                                      // 通常和 as="html" 联用
        "dom.key3" : "cat:detail",    // 表示获取对象内某对象内容
        "dom.key4" : "obj:detail",    // 表示获取对象内某对象全部元数据
        "dom.key5" : "obj.nm:detail", // 表示获取对象内某对象某个元数据
        "dom.key6" : "obj:detail/*",  // 表示获取对象内某目录全部子对象

        ...
    }
    
    // 显示过滤器
    fltEnabled : true,
    
    
    // 显示翻页条
    pgEnabled : true,
    
    // 默认页大小
    pgSize : 50
}
```

## dom.html

```
<div class="hm-th-obj">
    <ul>
        <li class="hm-tho-fld" key="xxx" as="text" ...>
            <span for="-1">live</span>
            <span for="2">dead</span>
        </li>
        ...
    </ul>
</div>
```

支持如下值的配置

* `.hm-th-obj > .hm-th-W > ul > li ` 结构是必须的
* `class` : 你可以随便定义，以便配合 *css.json*
* `key`   : 每个 `<li>` 都必须有一个 `key` 字段作为标识
* `as`    : 每个字段的具体如何显示，通过 as 来指定:
    - *text*   : 普通文字节点
    - *img*    : 图片节点，这要求值必须是一个 WnObj 的 ID
    - *html*   : 值本身就是一段HTML，直接输出就好了
    - *sub:eq* : 在节点内寻找可以匹配的子元素
    - *sub:loop* : 值必须是个数组或者集合，将子节点循环输出
* `replace` : 输出的时候，将自身替换成什么元素
    - `DIV` : 默认
    - `unwrap` : 解除包裹
* `required` 属性表示这个字段是必须的，不能被标记删除

## css.json

```
{
    "selector" : {
        // rule， 采用驼峰名名法，即 background-color 这里应该是 backgroundColor
    }
}
```



