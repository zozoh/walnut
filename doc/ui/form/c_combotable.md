---
title: 组合输入表格
author:zozoh
---

# 控件概述

提供一个表格，以及一个文本输入框，可以方便向表格中增加内容。
返回的是一个对象数组

# 如何创建实例

```
new ComTableUI({
    // 定义了表格的各个字段 
    fields : [{
        key   : "xxx"           // 字段的键
        title : "i18n:xxx"      // 字段标题 
        type  : "int"           // 字段类型，默认 String
        // 显示控件，其中如果以 "@" 开头，表示快捷控件定义
        // 比如 @input 等
        uiType : "xxx",
        uiConf : {..}       // 配置信息
    }],
    // 声明了这个，会在表格下面显示一个 combobox，每当有输入
    // 就表示新增加一项
    combo : {
        items : Cmd | F(val || itemArgs)  // 当输入框有值就用 val
        itemArgs : ANY                    // 默认 Item 的属性
        icon  : {c}F(o):Html,       // 显示项目的图标，不定义就没有
        text  : {c}F(o):String,     // 显示项目的文字
        value : {c}F(o):Object      // 显示项目的值，默认复用 text 的结果
    },
    // 根据 combo 出来的字符串，如何变成表格的对象
    // 这个函数必须是一个同步函数
    //  - this 根据 context 参数而定，默认为 ComboTableUI 本身
    //  - val 是 combo 框给出的值
    // 函数需要返回一个 Object，本控件会根据字段定义，设置到表格里
    // 如果返回的不为真，则会弹出警告信息
    getObj : {UI}F(val):Object
    
    // 如果添加的对象不存在，指定一个错误信息
    // 默认为 "i18n:com.combotable.noneobj"
    msgNoExists : "i18n:com.combotable.noneobj",
    
    // 回调函数的上下文
    context : UI,
});
```

# 在 form 中调用的例子

```
new FormUI({
    parent : UI,
    gasketName : "myform",
    on_change : function(key, val){
        console.log("form change:", key, val);
    },
    title : "测试高级控件",
    uiWidth : "all",
    fields : [{
        key   : "abc_list",
        title : "物品列表",
        tip   : "你就看着填吧",
        type  : "object",
        uiWidth : "auto",
        uiType : "@combotable",
        uiConf : {
            fields : [{
                    title  : "ID",
                    key    : "id",
                    hide   : true,
                }, {
                    title  : "名称",
                    key    : "nm",
                    width  : "60%",
                    uiType : "@label",
                }, {
                    title  : "价格",
                    key    : "price",
                    type   : "int",
                    dft    : 3,
                    width  : "20%",
                    uiType : "@input",
                }, {
                    title  : "数量",
                    key    : "amont",
                    type   : "int",
                    dft    : 1,
                    width  : "20%",
                    uiType : "@input",
                }],
            combo : {
                items : 'obj ~ -match \'nm:"^{{val}}"\' -limit 8 -json -l',
                itemArgs : {val : ".+"},
                icon  : function(o){
                    return Wn.objIconHtml(o);
                },
                text : function(o) {
                    return o.nm;
                },
                filter : function(o, dataList) {
                    for(var i=0; i<dataList.length; i++) {
                        if(o.id == dataList[i].id)
                            return false;
                    }
                    return true;
                }
            },
            getObj : function(val) {
                var nm = $.trim(val);
                if(!nm)
                    return null;
                return Wn.fetch("~/" + nm, true);
            }
        }
    }]
}).render(function(){
    this.setData({
        abc_list : [{
                "id":"vnt8bmelr4g9mp5tsus5hpsfts",
                "nm":".hmaker",
                "price":null,
                "amont":null
            },{
                "id":"7jtsk47i5ghbuqiggn9c8grgjf",
                "nm":".thumbnail",
                "price":null,
                "amont":null}]
        //myphoto : {fid:'4thoboi83khmdqmqqvf5arogki'}
    });
});
```


