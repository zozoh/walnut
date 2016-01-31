---
title: 对象表格
author:zozoh
---

# 控件概述

table 控件假想输入是一组 JS对象数组，它会将对象显示成一个表格。
同时它也提供对这个列表的数据操作方法。

table 控件仅仅负责数据的显示，要想触发它的显示，就调用 `setData` 方法即可

# 如何创建实例

```
new TableUI({
    //............................................................
    // 数据方面
    // 获取一个对象的唯一标识，这个标识会记录到 DOM 元素的 `oid` 属性
    // 快速索引到这个 DOM 元素，每个 DOM 元素都会记录对应的对象
    // 默认的，会取这个对象的 "id" 字段的值作为 ID
    idKey : "xxx" | {c}F(obj)
    
    // 与 idKey 相仿，会记录 DOM 元素的 `onm` 属性。
    // 不是必须的，没声明就没这个属性
    nmKey : "xxx" | {c}F(obj)
    
    // 【选】同步预先解析设置进来的数据
    parseData : {c}F(Array) : Array

    // 【选】异步预先解析设置进来的数据    
    asyncParseData : {c}F(Array, callback:F(Array));
    
    // 【选】同步处理自己的数据
    formatData : {c}F(callback:{UI}F(opt):Array) : Array
    
    //............................................................
    // 行为
    checkable : Boolean       // 是否显示 checkbox
    multi     : Boolean       // 是否多选，如果 checkable 默认可以多选
    activable : Boolean       // 可以激活行
    blurable  : Boolean       // 可以取消高亮行
    resizable : Boolean       // 是否可以调整列宽

    // 表格的布局模式
    layout : {
        /*
        计算尺寸的依据，有下面几种可能性
         0      : 名称列将会保持原来的宽度
         577    : 一个正数表示名称列是一个固定的宽度
         -99    : 一个负数表示列参与自动分配，但是最小不能少于多少
         *      : 实际上相当于 -1
         [..]   : 一个数组，给出了各个列的宽度，数组的元素也可以是 * | Number
         
         其他值，全当做 0 处理
        */
        sizeHint : 0       // 默认 0

        // 如果单元格内容超出预计长度，该怎么办？
        //  - wrap   : 表示维持单元格宽度，但是折行
        //  - nowrap : 表示维持单元格宽度，但是不折行，内容会被裁切
        //  - extend : 表示将扩大单元格的长度，以便显示全部内容
        cellWrap : "nowrap"    // 默认 nowrap

        // 计算尺寸的时候，是否也要把标题列包括进去
        withHeader : Boolean   // 默认 true
    }

    // 按顺序显示表格的列
    fields : [{
        // ..
        // 参见 form 控件的 field 段定义
        // ..
        // 定制值的显示，默认直接输出值
        hide     : Boolean,       // 本列是否隐藏
        display  : Template | {c}F(o, fld, v),
        // 是否将输出逃逸 HTML，默认 true，输出的内容将会不会被认为是 HTML
        escapeHtml : true
    }]
    
    //............................................................
    // 绘制完一行后，你可以补充对这行的后续处理
    on_draw_row : {c}F(jRow, obj)

    //............................................................
    // 事件
    on_change  : {c}F()      // "table:change"  首次加载一定会被触发
    on_add     : {c}F(objs)  // "table:add" 添加新数据后触发，setData 不会触发
    on_checked : F([o..])    // "table:checked" 参数为本次被选的对象
    on_blur    : F([o..])    // "table:blur"    参数为本次被选的对象
    on_actived : F(o,jq)     // "table:actived" 参数为本次激活的对象

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
* addLast
* refresh








