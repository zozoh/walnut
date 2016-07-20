---
title: 对象搜索器
author:zozoh
---

# 控件概述

`search` 提供了对数据搜索和翻页的功能。

# 如何创建实例

```
new SearchUI({
    // 是否支持更多的操作
    //  - 支持快捷按钮配置字符串 "new", "delete", "edit", "refresh"
    // 具体配置信息 @see menu 控件
    menu : ["create","delete","edit"]

    // 动作模板，支持了这些动作模板后， menu 才能支持对应的快捷动作
    // 否则会产生错误 
    edtCmdTmpl : {
        "create"  : "obj id:xxxxx -new '<%=json%>' -o",
        "delete"  : "rm -rf id:{{id}}",
        "edit"    : "obj id:{{id}} -u '<%=json%>'"
    }
    
    // 【选】编辑/新建快捷弹出层的样式
    maskConf : {..}
    
    // 【选】编辑/新建快捷弹出层表单控件的样式
    formConf : {..}

    /*
    如何获取数据
    但是由于搜索条件是来自控件本身，那么这个条件的格式是固定的:
    如果是命令，格式类似:
    
        obj -match '<%=match%>'
            -sort '<%=sort>'
            -skip {{skip}}
            -limit {{limit}}
            -l -json -pager
    */
    data : ..      // 参见 $z.evalData
    
    /*
    执行查询的上下文基础对象，这个对象会与 filter, pager 控件一起
    构建一个查询上下文
    */
    queryContext : {..} |{UI}F(),
    
    /*
    过滤控件, getData 返回:
    {
        // 查询的 JS 对象，参见 cmd_obj 的 match 命令
        match  : {..}
        // 排序的 JS 对象
        sort   : [{nm:1},{tp:-1}]
    }
    @see filter 控件一节
    */
    filter : {..}

    /*
    列表控件
    */
    list : {..}

    /*
    翻页控件, getData 返回:
    {
        skip  : 0     // 这个值就相当于 (pn-1)*pgsz 
        limit : 50    // 相当于 pgsz
    }
    setData 支持:
    {
        pn   : 1,     // 第几页
        pgsz : 10,    // 每页多少数据
        pgnb : 4      // 一共多少页
        sum  : 32,    // 一共多少记录
        skip : 0,     // 跳过了多少数据
        nb   : 10     // 本页实际获取了多少数据
    }
    如果参数为空，则恢复默认值，那么 getData() 则会返回默认数据
    */
    pager : {..}
    
    //............................................................
    // 事件
    on_refresh  : {c}F(list,pager)    // "search:refresh" 当刷新完毕时

}).render();
```

# filter 控件

```
{
    /*
    如果 filter 内部被输入了普通字符串，那么将在对象的哪几个字段里搜索
    默认，是 "nm" 字段。如果你想根据不同的值，对应不同的字段名，
    你可以给一个判断的列表
    
    [F(str):key, {regex:/../, key:"xxx"}, "mobile:^[0-9]+$", "nm"]
    
    .................................
    F(str):key
    是一个函数，控件会把字符串传入，你返回这个字符串应该对应什么 key
    返回任何可以变成 false 的值，则表示你不接受这个字符串
    .................................    
    {regex:/../, key:"xxx"}
    这个没啥好说的，控件会把字符串用正则表达式匹配，成功了，就返回你给定的 Key
    当然，你的 regex 也可以是字符串，只要能被解析成正则表达式就好
    .................................
    "mobile:^[0-9]+$"
    这种是个简写的形式，如果包含了 :^ 字符，那么这个字符串会被拆分，前半部分是 key
    后半部分是个正则表达式
    .................................
    "nm"
    普通字符串，直接被当做 key    
    */
    keyField : "nm"
}
```

# 创建/编辑对象时编辑字段

当用 `search` 控件内置的 *create/edit* 按钮创建/编辑某个记录的时候，你可能需要强制为这个记录设置某几个字段的值，比如 `tp` 字段，这是你可以声明 *formConf* 段:

```
{
    formConf : {
        formatData : function(o){
            o.tp   = 'box';
            o.race = 'DIR';
            return o;
        }
    }
}
```

# 控件方法

.. 以后再添加 ..





