---
title: 时间列表控件
author:zozoh
---

# 控件的调用方法

时间列表控件会简单将一天的时间均分，并让你选择一个时间点，或者多个时间范围

```
jQuery.timelist({
    
    // 默认的，在顶级元素，会添加 "timelist" 的样式选择器
    // 如果声明了这个属性，则会在之后添加更多的样式选择器
    // 这个用来定制控件的显示样式，默认为 "skin-light"
    // 如果你想指定自己独有的日历控件显示样式，改变这个属性
    // 再配合上自己的 css 文件即可。 
    // 当然你需要预先研究一下控件输出的 DOM 结构
    className : "skin-light",

    // 是选择一个时间点还是范围，默认 "default" 表示时间点
    mode : "default",

    // 是否允许多选, 默认 true 
    // 如果 mode=="range"，单选情况，则必须是一段连续的时间点
    multi : true,
    
    // 显示方式是横向("horizontal")还是纵向("vertical")
    // 默认 "horizontal"
    display :  "horizontal",
    
    // 以多少分钟为一个单位，默认 60 分钟
    timeUnit : 60,
    
    // 多少个单位成为一组，默认 1
    groupUnit : 1,
    
    // 定义了显示时间的范围，默认显示24 小时 
    // 数据可以是一天的绝对秒数，或者是一个时间字符串 
    scopes    : [["00:00", "24:00"]],
        
    // 定义了显示的数据，给定的时间只要再区块内，就算该区块被选中
    // 数据可以是一天的绝对秒数，或者是一个时间字符串 
    data : ["12:00", "13:22"]
    
    // 定义了 on_change 等回调中，tps 数组的格式
    // "auto" 默认，表示不改变数组的格式
    // "asec" 表示确保都为绝对秒数
    // "ssec" 表示确保都为 "08:12:23" 格式的时间字符串
    // "smin" 表示确保都为 "08:12" 格式的时间字符串
    callbackArgType : "auto",
    
    // 当控件选择内容改变后的回调
    // 参数是一个绝对秒数的数组
    on_change : {$ele}F(tps)
});
```

# 控件的命令

## get : 获取时间点

```
// 获取时间范围，即得到所有选中时间的范围数组，如果只选中一个项目
// 得到的应该类似 ["16:00"]
var tr= jQuery.timelist("get");
console.log(tr);   // log: ["12:00", "14:00","15:30"]

// 得到绝对秒数
var tr = jQuery.timelist("get", "sec");
console.log(tr);   // log: [3600, 7200]

// 得到小时数
var tr = jQuery.timelist("get", "H");
console.log(tr);   // log: [0, 3,  8]

// 得到一组时间结构对象
var tr = jQuery.timelist("get", "obj");
console.log(tr);   // log: [{..}, {..}]
```

## set : 设置时间点

```
// 选中多个时间点，其他的时间点会被清除选中
jQuery.timelist("set", ["12:00","14:00","15:30"]);

// 一个时间值也是有效的
jQuery.timelist("set", "12:00");

// 甚至一个绝对秒数
jQuery.timelist("set", 7233);
```

## clear : 清除时间点

```
jQuery.timelist("clear");
```

##  add : 添加时间点

```
// 选中多个时间点，其他的时间点会被保持选中
jQuery.timelist("add", ["12:00","14:00","15:30"]);

// 一个时间值也是有效的
jQuery.timelist("add", "12:00");

// 甚至一个绝对秒数
jQuery.timelist("add", 7233);
``` 




