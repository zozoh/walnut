---
title: 日历控件
author:zozoh
---

# 控件的调用方法

```
// 在给定选区创建一个日历
jQuery.zcal({
    //------------------------ 整体配置
    // 默认的，在顶级元素，会添加 "zcal" 的样式选择器
    // 如果声明了这个属性，则会在之后添加更多的样式选择器
    className : null,
    
    // 指定了日历的模式，是日期范围选择模式，还是单选模式
    // 默认是单选模式 "default"
    mode : "range",
    
    // 要绘制几个日历块
    blockNumber : 3,
    
    // 如果 blockNumber==1，可以自动适应父元素的宽度
    // 默认 false
    fitParent : false,
    
    // 是否显示单元格的边框
    showBorder : false,

    // 指定了一个日期，以这个日期所在周为开始，如果是月历
    // 则绘制日期所在的月，这必须是一个 JS 的 Date 对象
    current : Date,
    
    // 周数，如果 >=1 表示从current所在周绘制一个固定周数
    // 否则自动判断，生成日期所在月份的视图
    weeks : 0,
    
    // 如果是 weeks 为 0， 那么绘制的时候是否补全上下个月的日子
    // 不声明表示 false，即补全。在 weeks>0 的时候，这个参数会被无视
    onlyCurrentMonth : false,
    
    // 是否将周一作为一周的开始 
    firstDayIsMonday : false,

    // 声明了本地语言，如果没有，默认为下面的配置
    // 两个数组，分别表示月份和周的本地化字符串。
    // !! 注意，星期天，必须是第0个
    i18n: {
        month : ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],
        week  : ["Sun","Mon","Tue","Wed","Thu","Sat","Mon"]
    },
    
    // 自定义月历标题的显示，如果没有定义，则控件会根据 i18n.month 给定的月份文字显示
    // 默认的显示模板为 "{{yyyy}}-{{MM}}-{{dd}}"
    // 函数的返回将作为 html 插入到 $title 所在的元素
    // 函数的输入参数，c 为一个解析好的上下文对象，d 为 Date 对象
    // c 的样子类似:
    /*
    // 单月视图
    {
        dd : "05",
        d  : 5,
        MM : "01",
        M  : 1,
        Month : "Jan",   // 这个会根据 i18n.month 来取值
        yy : 15,
        yyyy : "2015"
    }
    // 如果是固定周或者范围选择视图
    {
        from : {..},   // 对象内容参见单月视图上下文
        to   : {..}
    }
    */
    title : F(c, dFrom, dTo),
    
    // 如果声明为 false 则明确表示不显示标题区域，
    // 即日历标题，菜单，切换按钮等统统不显示
    head : true,
    
    // 声明了日期转换按钮的显示内容
    // 如果内容声明成 false 则不显示日历转换器  
    swticher : {
        today : "Today"   // 未定义或者null表示不显示
        prev  : "&lt;"
        next  : "&gt;"
    }
    
    // 指明，月份切换按钮应该在右边
    swticherAtRight : false,
    
    // 右侧的扩展动作按钮区域的回调
    menuDraw : {$menu}F(),

    //------------------------- 单元格配置
    // 绘制单元格，默认的实现就是显示一个当前日期 
    // 返回一段 html 作为这个单元格的内容
    cellDraw  : F(d, opt, blockMonth),
    
    // 如果是跨月的视图，将每月1号替换成月份
    // 默认开启。这个选项只有在 cellDraw 为默认时才生效
    // 当然你可以在自己的实现函数里自行处理
    markMonthFirstDay : true,
    
    // 当单元格的尺寸改变的时候，即 resize 被触发后的回调
    cellResize : {$cell}F(d),
    
    // 当单元格被单击的时候的额外操作。
    // 默认的，单元格本单击将触发激活事件，相当于高亮当前单元格
    // 如果设置成 false 则表示强制让默认行为失效
    // 如果设置成 "in"  则表示，每个块，当月内的单元格才能被点击
    // 如果设置成 "out" 则表示，每个块，当月外的单元格才能被点击
    cellClick : {$cell}F(d),
    
    //------------------------- 事件
    onActived : {$cell}F(d),    // 当一个日期格子被激活
    onBlur    : {$cell}F(d),    // 当一个日期格子取消激活
    
    // 当前的日历日期(current)被切换到哪一日
    onSwitch  : {$ele}F(d, dFrom, dTo)
    
    // 如果是日期选择模式，当范围改变时候会调用
    onRangeChange : {$ele}F(dFrom, dTo)
    
    //------------------------- 行为配置
});
```

* 日期单元格记录的日期对象，时间一定是 `00:00:00` 的

# 控件的命令

## current : 获取/改变当前控件的日期返回

```
var d = jQuery.zcal("current");
console.log(d.toLocaleString());
```

如果传入日期参数，则会改变当前控件的显示日期

```
var d = new Date('1995-12-17T03:24:00');
jQuery.zcal("current", d);
```

## redraw : 重绘控件

```
jQuery.zcal("redraw", {options});
```

* 可以用新的 options 覆盖早先的配置，并重新执行绘制

## viewport : 获取当前控件显示的日期范围(包含)

```
var dateRange = jQuery.cal("viewport");
console.log(dateRange);  // logs [Mon Sep 28 1998..., Mon Oct 16 1998..] 
```

## actived : 获取当前被激活的日期

```
var d = jQuery.zcal("actived");
console.log(d.toLocaleString());
```

* 如果是日期范围选择模式，返回的永远是最后一次点击的日期

## active : 改变被激活的日期

```
var d = new Date('1995-12-17T03:24:00');
var re = jQuery.zcal("active", d);
console.log(re);   // logs true
```

* 注意，如果这个日期不在显示范围内，则无效，返回的是 「false」

## range : 获取选择的日期范围

```
var dateRange = jQuery.zcal("range");
console.log(dateRange);  // logs [Mon Sep 28 1998..., Mon Oct 16 1998..] 
```

* 如果当前是单选模式，数组的两个元素是相同的，都是当前被激活的日期
* 但是时间，一个是 `00:00:00` 一个是 `23:59:59`
* 如果是多选模式，`dateRange[0]` 时间一定是 `00:00:00`，而 `dateRange[1]` 时间一定是 `23:59:59`







