---
title: 对象表单控件
author:zozoh
---

# 控件概述

表单控件将根据配置信息，创建一个对象表单。以及维护这个表单的保存行为

# 如何创建实例

```
new OFormUI({
    // 默认的，控件将调用者通过 setData(o) 传入的对象 o 存储，当 getData 的时候
    // 控件将 o 与最新的编辑信息合并，返回给调用者
    // 如果希望 getData 返回一个新对象，那么将这个属性设置为 false
    mergeData  : true

    // 表单可以指定一个标题，你可以随意写一段 HTML
    // 如果未定义，标题将不显示
    title : HTML

    // 表单的显示模式，现在支持的有:
    //  - "flow" : 每个组依次排布下来
    //  - "tabs" : 每个组一个标签
    mode : "flow"     // 默认 "flow"

    // 如果只有一个组，那么是否还显示组的标题
    // 默认 true 表示，不显示
    hideGroupTitleWhenSingle : true,

    // 声明控件底部支持什么操作
    actions : [{
        icon    : '<i class=..>'  // 【选】是否显示图标，以及图标的 HTML 片段
        text    : "i18n:xxx"      // 按钮文字，支持 i18n

        // 按钮的动作是调用一个回调函数
        handler : {context}F(o)
        context : undefined       // 回调时，特殊的调用上下文，默认采用全局配置
        args    : undefined       // 回调时特殊的参数，格式是一个数组，默认没参数 

        // 按钮的动作是发送一个请求
        url    : URL
        ajax   : {..}       // @see jQuery ajax 请求的配置信息，默认为 POST + JSON
        reType : "json"     // 【json】返回的直接是一个 JSON 对象，可以用来 setData
                            //   "ajax" - 则表示 NutzWeb 的 AjaxReturn
                            // 其他值非法

    }]
    
    // 字段的国际化表，如果没有给定，默认当做 {"zh-cn":{}}
    // "deault" 指明了默认采用的语言字符串集，如果没有指明，采用第一个
    field_i18n : {
        "default" : "zh-cn"
        "zh-cn"   : {..}
        "en-us"   : {..}
    }
    
    // 控件最主要的属性，表示控件将展示一个对象的哪些字段
    // 值必须是一个数组，元素可以是字段或者字段组
    // 控件会自行整理，所有第一层的字段会归纳到未命名的组里
    fields : [{
        // 有 key 就是普通字段
        key      : "nm"          // 字段对应的对象键
        icon     : HTML          // 字段的图标
        title    : "i18n:xxx"    // 字段的标题
        type     : "String"      // 【String】值的类型,下面有详细介绍
        editAs   : "input"       // 【input】 编辑控件,下面有详细介绍
    }, {
        // 如果 items 就是普通组
        icon     : HTML          // 组的图标
        title    : "i18n:xxx"    // 组不能再有子组了，如果写了，会被忽略
                                 // 组如果没有标题，就不显示
        type     : "String"      // 组默认的值类型,下面有详细介绍
        editAs   : "input"       // 组默认的编辑控件,下面有详细介绍
        items    : [..]          // 组里的字段
    }, {
        // 没 key 也没有 items 的就是动态组，
        // 可以存放对象所有没在 fields 里声明的字段 
        // 可以用 filter 来指明哪些字段可以被包括在组内
        // 默认的，对象所有没有被指明的字段都会被 others 组包括
        // 同时，others 组可以有多个
        title    : "i18n:others"  // 【i18n:others】
        filter   : "!^__.+$"
        // 依然支持下面的属性
        icon   : HTML
        type   : "String"
        editAs : "input"
    }]

    // 控件的事件
    on_change  : F(v,fld)     // "oform:change"  每当一个字段被修改触发

}).render();
```

# 控件属性

## `groups`

记录本控件所有的字段组列表。这个列表是控件创建后，根据传入的参数归纳后的结果。它的数据格式类似:
```
groups : [{
       group  : true,    // 普通组
       title  : "i18n:xxx",
       type   : "String",
       editAs : "input",
       items  : [{
            key    : "nm",
            title  : "xxxx",
            type   : "String",
            editAs : "input",
            $el    : jQuery    // 本字段对应的 DOM 元素
            $nm    : jQuery    // 名称对应的DOM元素
            $val   : jQuery    // 值对应的 DOM 元素
        }],
        $el     : jQuery        // 记录了自己对应的 DOM 元素
    },{
       others : true,    // 动态组
       title  : "i18n:xxx",
       type   : "String",
       editAs : "input",
       items  : [..],
       $el     : jQuery
    }]
```

# 控件方法

## getData

```
// 返回控件正在编辑的表单数据
var o = oform.getData();
```

## setData

```
// 为控件设置数据以便展示编辑界面
oform.setData(o);
```









