---
title:日历控件
author:zozoh
---

# 控件概述

这个控件就是把 `jQuery.zcal` 插件 UI 化

# 如何创建实例

```
new UILayout({
    $pel    : $(document.body),
    
    // 声明这个属性，点击 body 的时候，将会取消单元格的激活
    // 默认 true
    blurable : true
    
    // 下面的属性是 zcal 支持的全部属性
    ...
    // 附加的，控件会对应的支持相关消息
    on_cell_resize : {UI}F(d)        // "cal:cell:resize"
    on_cell_click  : {UI}F(d)        // "cal:cell:click"
    on_actived     : {UI}F(d)        // "cal:actived"
    on_blur        : {UI}F(d)        // "cal:blur"
    on_switch: {UI}F(d, dFrom, dTo)  // "cal:switch"
    on_range_change: {UI}F(d, dFrom, dTo)  // "cal:range:change"
}).render();
```

# 控件支持的命令

控件支持 `jQuery.zcal` 提供的命令，只不过是换成了函数调用的形式

## getCurrent : 得到当前日历日期 

```
var d = cal.current();
```

## setCurrent : 设置当前日历日期

```
cal.current(new Date());
```

## refresh : 重绘日历

```
cal.refresh();
```

## viewport : 获取当前控件显示的日期范围(包含)

```
var dateRange = cal.viewport();
console.log(dateRange);  // logs [Mon Sep 28 1998..., Mon Oct 16 1998..] 
```

## getActived : 获取当前被激活的日期

```
var d = cal.actived();
console.log(d.toLocaleString());
```

* 如果是日期范围选择模式，返回的永远是最后一次点击的日期
* 如果没有激活，则返回 *null*

## setActived : 改变被激活的日期

```
var d = new Date('1995-12-17T03:24:00');
var re = cal.setActived(d);
console.log(re);   // logs true
```

* 注意，如果这个日期不在显示范围内，则无效，返回的是 「false」

## getRange : 获取日期范围

```
var dateRange = cal.getRange();
console.log(dateRange);  // logs [Mon Sep 28 1998..., Mon Oct 16 1998..] 

// 仅仅获取绝对毫秒
var msRange = cal.getRange("ms");
console.log(msRange);  // logs [1447287120000, 1447538190000] 
```

* 如果当前是单选模式，数组的两个元素是相同的，都是当前被激活的日期
* 但是时间，一个是 `00:00:00` 一个是 `23:59:59`
* 如果是多选模式，`dateRange[0]` 时间一定是 `00:00:00`，而 `dateRange[1]` 时间一定是 `23:59:59`

## setRange : 设置日期范围

```
// 传入数组，表示设置一个日期范围, 比如设置从今天起四天的范围
cal.setRange([new Date(), (new Date()).getTime() + 3 * 86400000]);
```





