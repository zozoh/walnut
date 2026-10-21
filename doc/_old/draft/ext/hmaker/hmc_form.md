---
title:控件:表单控件
author:zozoh
tags:
- 扩展
- hmaker
---

# 表单控件的配置属性

```js
{
    // 具体的字段
    fields : [..]
}
```

## 普通字段

```js
{
   // 有 key 就是普通字段
   key       : "nm",         // 字段键名
   icon      : '<i..>',      // 字段的图标
   title     : "xxx",        // 字段的标题
   tip       : "xxx",        // 字段的提示说明
   required  : true,         // 字段是否必须
   className : "XXXX",       // 指定特殊的类选择器

   //.................................................
   // 字段类型: 
   //  - string : 字符串
   //  - object : JS对象，可以是空白对象或者数组
   //  - daterange : 日期范围，一个数组 [Date, Date]
   //  - datetime  : 日期时间, Date()
   //  - time      : 时间, $z.parseTime() 输出的格式
   //  - int       : 整数
   //  - float     : 浮点
   //  - boolean   : 布尔 
   type     : "string",
   
   // 如果控件的值为 null，是否当做 undefined 来处理
   // 默认为 false
   nullAsUndefined : false,
   
   // 当 type==string 时，支持属性。即如果是空字符串，会被当做 null
   emptyAsNull : true,
   
   //.................................................
   editAs   : "input"       // 快捷的编辑控件类型
   uiConf   : {..}          // 编辑控件的配置信息
}
```

