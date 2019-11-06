---
title: 对象选择器
author:zozoh
---

# 控件概述

在选区内给出一个选择框，用户可以选择任何一个或者多个对象

# 如何创建实例

```
{
    setup : {
        // 所有的 mask 控件属性
        // 所有的 obrowser 控件属性
        
        // !!! 同时 checkable 虽然是 obrowser 控件的属性
        // 本控件也会将其理解成为多选开关
        // 如果多选，本控件 getData() 将返回一个数组
        // 否则就是单个 WnObj
    }
    
    // 如果是多选， setup.checkable == true
    // 那么新增的对象附加到呈现框前要不要清除框里现有的内容
    // 默认 true 表不清除
    keepAppend : true,
    
    // 是否可以清除选择，默认 true
    clearable : Boolean
    
    // 将传入的 JS 对象转换成标准的 WnObj
    // 同步的，如果想异步，请看 asyncParseData
    parseData : {c}F(obj)
    
    // 异步的将传入的 JS 对象转换成标准的 WnObj
    // 同步的，如果想异步，请看 asyncParseData
    // 回调函数 callback 格式 {c}F(o)
    asyncParseData : {c}F(obj, callback}
    
    // 将一个标准的 WnObj 转换成期望的 JS 对象
    // !!!  注意，如果清除了对象，且是单选框，那么这个回调会收到 o == null
    formatData : {c}F(o)
    
}
```


