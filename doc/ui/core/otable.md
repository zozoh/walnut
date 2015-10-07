---
title: 对象表格
author:zozoh
---

# 控件概述

otable 假想输入是一组 `WnObj` 对象，它会将对象显示成一个表格。
同时它也提供对这个列表的数据操作方法

# 如何创建实例

```
new UIOList({
    // @see .. ZUI 通用的属性 ..
    //............................................................
    // 下面是和 olist 控件意义完全相同的属性
    data  : ..                      // @see olist.data
    idKey : "id"                    // @see olist.idKey
    checkable : {..} || Boolean     // @see olist.checkable
    activable : Boolean             // @see olist.activable
    blurable  : Boolean             // @see olist.blurable
    
    icon : Template | F(o):HTML     // @see olist.icon
    iconClass : {..} | F(o)         // @see olist.iconClass

    display : {..}                  // @see olist.display

    // 这些事件意义也是相同的
    on_change  : F([o..])    // "olist:change"  首次加载一定会被触发
    on_checked : F([o..])    // "olist:checked" 参数为本次被选的对象
    on_uncheck : F([o..])    // "olist:uncheck" 参数为本次被选的对象
    on_actived : F(o,jq)     // "olist:actived" 参数为本次激活的对象

    //............................................................
    // 下面是 otable 特有的配置项目
    resizable : Boolean       // 是否可以调整列宽
    indexBase : 0 | 1         // 如果声明了，则表示要显示一列为自动序号

    // 按顺序显示表格的列
    columns : [{
        key      : "nm",          // 列对应的对象键
        title    : "i18n:xxx",    // 列的标题
        hide     : Boolean,       // 本列是否隐藏
        // 定制值的显示，默认直接输出值
        display  : Template | F(o, key),
        escapeHtml : true,        // 是否将输出逃逸 HTML，默认 true

        // 下面的可以用来编辑
        type     : "String",      // 值的类型
        editor   : "input",       // 编辑控件，为空则不可编辑
    }]


}).render();
```

# 控件方法

下面这些方法与 `olist` 控件意义相同

* getActived
* setActived
* getChecked
* check
* uncheck
* toggle
* getData
* setData
* refresh








