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
    过滤控件, getData 返回:
    {
        // 查询的 JS 对象，参见 cmd_obj 的 match 命令
        match  : {..}
        // 排序的 JS 对象
        sort   : [{nm:1},{tp:-1}]
    }
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

# 控件方法

.. 以后再添加 ..



