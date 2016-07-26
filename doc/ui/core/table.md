---
title: 表格控件
author:zozoh
---

# 控件概述

*table* 控件假想输入是一组 JS对象数组，它会将对象显示成一个表格。
同时它也提供对这个列表的数据操作方法。

*table* 控件仅仅负责数据的显示，要想触发它的显示，就调用 `setData` 方法即可

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
        //........................................................
        // 参见 form 控件的 field 段定义
        // key | type | tip | uiType | uiConf
        //........................................................

        //........................................................
        // 显示 : 如果没声明 icon|text|display 则直接显示对象字段的值 toText
        title : 'i18n:xxxx'       // 列标题
        hide : Boolean            // 本列是否隐藏
        icon : Tmpl|{c}F(o):HTML  // 自定义单元格图标
        text : Tmpl|{c}F(o):Str   // 自定义单元格文本
        
        // 自定义显示内容，一般配合 escapeHtml:true 来自定义内容
        // 如果声明了这个，icon 和 text 会被无视
        display : Tmpl|{c}F(o):HTML 

        
        // 是否将输出逃逸 HTML，默认 true，输出的内容将会不会被认为是 HTML
        escapeHtml : Boolean
    }]
    
    //............................................................
    // 绘制完一行后，你可以补充对这行的后续处理
    on_draw_row : {c}F(jRow, obj)

    //............................................................
    // 在激活某个对象前的回调，如果函数返回的是 false
    // 那么会禁止这个对象的激活
    on_before_actived : {c}F(o,jq):Boolean

    //............................................................
    // 事件
    on_change  : {c}F()          // "table:change"!首次加载一定会被触发
    on_add     : {c}F(objs)      // "table:add"   !setData 时不会触发
    
    // "table:checked" 这里说明一下，objs 表示所有被选中的对象（包括以前）
    // 而 jRows 表示所有本次被选中的行对象（不包括以前）
    on_checked   : {c}F([objs, jRows])
    
    // "table:unchecked" 
    on_unchecked : {c}F([objs, jRows])

    // "table:actived"  激活一项的时候触发
    on_actived : {c}F(o,jRow, prevObj, prevRow)
    
    // "table:blur" 当一个列取消选中的时候，会被触发
    // 四个参数，前两个不解释，后两个表示即将被激活的元素
    on_blur : {c}F([objs,jRows, nextObj, nextRow])
    
    //............................................................
    // 上下文，可以之际指定 {c} 对应的上下，默认 UI 自身
    context : UI
}).render();
```

# 控件方法

## getObjId

```
var id = uiTable.getObjId(obj);
```

* 实际上会返回对象的 *id* 字段的值
*  *id* 字段由 *idKey* 来配置，默认为 "id"

## getObjName

```
var id = uiTable.getObjName(obj);
```

* 实际上会返回对象的 *name* 字段的值
*  *name* 字段由 *nmKey* 来配置，如果不声明，本函数总是会返回 *undefined*

## getActived

```
// 获取当前被激活的对象
var o = uiTable.getActived();
```

## getActivedId

```
// 获取当前被激活的对象的 ID
var id = uiTable.getActivedId();
```

## setActived

```
// 激活第3个对象 (0 base)
uiTable.setActived(2);

// 激活某 DOM 元素所在的对象
uiTable.setActived($(..));

// 激活最后一个对象
uiTable.setActived(-1);

// 激活某个 ID 的对象
uiTable.setActived("45cfad78a3ec99ade12");

// 相当于调用 blur
uiTable.setActived();
```

## setAllBure

```
uiTable.blur();
```

## getChecked

```
var objList = uiTable.getChecked();
```

## check

```
// 选中第3个对象 (0 base)
uiTable.check(2);

// 选中某 DOM 元素所在的对象
uiTable.check($(..));

// 选中最后一个对象
uiTable.check(-1);

// 选中某个 ID 的对象
uiTable.check("45cfad78a3ec99ade12");

// 全部选中
uiTable.check();
```

## uncheck

```
// 取消选中第3个对象 (0 base)
uiTable.uncheck(2);

// 取消选中某 DOM 元素所在的对象
uiTable.uncheck($(..));

// 取消选中最后一个对象
uiTable.uncheck(-1);

// 取消选中某个 ID 的对象
uiTable.uncheck("45cfad78a3ec99ade12");

// 取消全部选中
uiTable.uncheck();
```

## toggle

```
// 将给定的东东，全部反选，并触发相应的消息
// 如果没有参数，则表示全部反选
uiTable.toggle(Element | jQuery | Number | "selector" | {..})
```

## has

```
// 是否存在某个 ID 的行
uiTable.has("45cfad78a3ec99ade12");
```

## getData

```
// 获取第3个对象 (0 base)
var obj = uiTable.getData(2);

// 获取某 DOM 元素所在的对象
uiTable.getData($(..));

// 获取最后一个对象
uiTable.getData(-1);

// 获取某个 ID 的对象
uiTable.getData("45cfad78a3ec99ade12");

// 获取全部数据
var objs = uiTable.getData();
```

## setData

```
uiTable.setData(objs)
```

## add

```
// 在第3个对象 (0 base) 后插入
var obj = uiTable.add(objs, 3);

// 在第3个对象 (0 base) 前插入
var obj = uiTable.add(objs, 3, true);

// 在某 DOM 元素所在的对象后插入
uiTable.add(objs, $(..));

// 在某 DOM 元素所在的对象前插入
uiTable.add(objs, $(..), true);

// 在最后一个对象后插入
uiTable.add(objs, -1);

// 在最后一个对象前插入
uiTable.add(objs, -1, true);

// 在某个 ID 的对象后插入
uiTable.add(objs, "45cfad78a3ec99ade12");

// 在某个 ID 的对象前插入
uiTable.add(objs, "45cfad78a3ec99ade12", true);

// 在当前对象后插入
uiTable.add(objs);

// 在当前对象前插入
uiTable.add(objs, null, true);
```

* 参数 *objs* 可以是单个对象，函数判断不是数组，会用数组包裹

## remove

```
// 删掉对象，但是保持至少有一个
var jN2 = uiTable.remove("45cfad78a3ec99ade12", true);

// 删掉最后一个
var jN2 = uiTable.remove(-1);
```

* 删除操作会返回高亮被删除对象的下一个节点
* 调用者可以根据心情，决定是否高亮这个节点 `setActived(jN2)`
* 如果没有可被高亮的下一个节点，函数返回的是 null

## update 

```
// 更新第3个对象 (0 base)
var obj = uiTable.update(obj, 2);

// 更新某 DOM 元素所在的对象
uiTable.update(obj, $(..));

// 更新最后一个对象
uiTable.update(obj, -1);

// 更新某个 ID 的对象
uiTable.update(obj, "45cfad78a3ec99ade12");

// 更新持有相同 ID 的对象
uiTable.update(obj);
```

## showLoading

```
uiTable.showLoading();
```





