---
title: 搜索控件
author:zozoh
---

# 控件概述

本控件提供一个搜索，列表和翻页的组合套装。具体的控件集合结构为:

```
search
   |--- filter   # 「选」过滤器
   |--- menu     # 「选」菜单
   |--- sorter   # 「选」排序器
   |--- list     # 「必」兼容 List 规范的列表控件
   |--- pager    # 「必」分页器
```

# 创建方法

```
new SearchUI({
    // 是否支持更多的操作
    //  - 支持快捷按钮配置字符串 "new", "delete", "edit", "refresh"
    // 具体配置信息 @see menu 控件
    // 你也可以用 {qkey:"create", icon:"<...>"} 来修改某个快捷菜单的图标和显示文字
    menu : ["create","delete","edit"]
    
    // 指定菜单回调函数的上下文，默认为 Search 控件自身
    menuContext : UI

    // 动作模板，支持了这些动作模板后， menu 才能支持对应的快捷动作
    // 否则会产生错误 
    edtCmdTmpl : {
        "create"  : "obj id:xxxxx -new '<%=json%>' -o",
        "delete"  : "rm -rf id:{{id}}",
        "edit"    : "obj id:{{id}} -u '<%=json%>'"
    }
    
    /*
    执行动作模板的快捷命令时，补充的上下文
    */
    cmdTmplContext : {..} | {UI}F(),
    
    // 【选】编辑/新建快捷弹出层的样式
    // 仅当指定了 menu{create,edit} 且指定了 edtCmdTmpl 下的对应命令才生效
    maskConf : {..}
    
    // 【选】编辑/新建快捷弹出层表单控件的样式
    // 仅当指定了 menu{create,edit} 且指定了 edtCmdTmpl 下的对应命令才生效
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
    执行查询的上下文基础对象，这个对象会与 filter, sorter, pager 控件一起
    构建一个查询上下文
    {  // <-- 由 queryContext 提供，默认为 {}
        match : {..}     // 由 filter 提供
        sort  : {..}     // 由 sorter 提供
        pager : {..}     // 由 pager 提供
    }
    */
    queryContext   : {..} | {UI}F(),
       
    /*
    这里指定过滤器部分的宽度的参考值，如果过滤器区域宽度小于这个值
    那么就会尝试收缩 action 部分扩展其显示。默认 50%
    */
    filterWidthHint : "50%",
    
    /*
    过滤控件的具体配置信息
    @see `search_filter` 的详细描述
    */
    filter : {..}
    
    /**
    排序控件的具体配置信息 
    @see `search_sorter` 的详细描述
    */
    sorter : {..}

    /*
    列表控件的具体配置信息
    @see `search_list` 的详细描述
    */
    list : {..}

    /*
    翻页控件的具体配置信息
    @see `search_pager` 的详细描述
    */
    pager : {..}
    
    //............................................................
    // 事件
    on_refresh  : {c}F(list,pager)    // "search:refresh" 当刷新完毕时

}).render();
```

# 可自定义的子控件

search 控件组合了四个控件 `filter`, `sorter`, `list`, `pager`。

其实它并不限制具体控件的实现了，只是子控件必须实现两个函数:

- `getData` 获取数据
- `setData` 设置数据

如果想指定 **filter** 控件的类型，你需要这么声明

```
filter : {
    uiType : "xxxx"   // 指定控件类型
    uiConf : {
        // 这里是该控件的特殊配置信息
    }
}
```

同时各个子控件需要接受的配置参数，请参看对应的章节：

- [搜索过滤器](search_filter.md)
- [搜索排序器](search_sorter.md)
- [搜索列表](search_list.md)
- [搜索分页器](search_pager.md)

# 子控件的数据结构规范

如果你仅仅是希望使用默认的 **filter** 控件，你可以这么声明

```
filter : {
    // 这里是控件的配置信息
}
/////////////////////////////////
// 实际上，上面与下面的等效
filter : {
    uiType : "ui/search2/search_filter",
    uiConf : {
        // 这里是控件的配置信息    
    }
}
```

除了 **filter** 控件，所有的四个子控件也都支持上面的用法。

search 控件对四个子控件的 `get/setData` 理解如下：

## filter: 过滤器

- `getData` 返回一个普通的 Js 对象就好了，Search 控件会将其转换成 JSON 字符串，设置给 `queryContext.match` 段
- `setData` 设置一个普通 JS 对象作为查询条件，不同的过滤过滤器实现有自己的理解。
    + 其中默认过滤器会认为这是一组隐含的条件，它不会显示出来，同时每次 getData 都会带有这些条件

## sorter: 排序器

`set/getData` 面对的对象结构为:

```
{
    nm: -1,   // 降序排序（从大到小）
    ct: 1     // 升序排列（从小到大
}
```

你可以定制任意多的键，查询的时候，search 控件会将其转化成 JSON ，供给`queryContext.sort` 段。

## list: 列表

`set/getData` 均为对象列表

## pager: 分页器

**getData** 返回:

```
{
    skip  : 0     // 这个值就相当于 (pn-1)*pgsz 
    limit : 50    // 相当于 pgsz
}
```

**setData** 接受对象：

```
{
    pn   : 1,     // 第几页
    pgsz : 10,    // 每页多少数据
    pgnb : 4      // 一共多少页
    sum  : 32,    // 一共多少记录
    skip : 0,     // 跳过了多少数据
    nb   : 10     // 本页实际获取了多少数据
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




