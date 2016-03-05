---
title: 滑杆控件
author:zozoh
---

# 控件的调用方法

让当前选区所有子元素超出选区的部分折叠在一起

```
jQuery.slidebar({
    // 在何处绘制滑杆
    //  inner : 在元素内部
    //  dock  : 停靠在元素外(默认)
    pos : "dock",
    
    // 如果 pos 为 dock，那么 dock 的模式是什么
    // 默认 VC
    dockMode : "VC",
    
    /*
    绘制的模式是
      H : 水平
      V : 垂直
    根据 pos 自动决定:
      inner -> H
      dockH -> H
      docV  -> V
    水平绘制:
       +....... StartPoint
       V
       +------|-|---------+
       |      pos         |
       +------|-|---------+
                          ^
           EndPoint ......|
    垂直绘制:
        +---+ <--- EndPoint
        |   |
        |   |
        <-O->  <--- pos
        |   |
        |   |
        |   |
        +---+ <--- StartPoint
    */
    mode : "horizontal|vertical"
    
    // 滑块的物理尺寸，根据模式的不同，可以代表宽度或者高度
    // 统一为 px，默认取元素的值，最小的话不能小于 50px
    size : 100
    
    
    // 【选】 设置 StartPoint 和 EndPoint 的值
    // 默认为 [0,1] 回调的时候回给出这个区间的一个值
    range : [0,  1]
    
    // 计算值的时候，是否取整
    // 不是下面三个值的时候，表示不取整
    // 默认 round 表示四舍五入
    valueBy : "round|ceil|floor" || {c}F(v, pos)
    
    // 初始化的值，如果有这个值，则进行换算，设置滑块位置
    value : 0,
    
    // 显示标尺，默认 0 表示没有标尺
    // 如果显示标尺，则会将区域分作给定空间，空间内部会显示标尺
    ruler : 5,
    
    // 配合上标尺，如果为 true，则拖动的时候回自动吸附到标尺的位置
    magnet : false,
    
    // 回调，接受参数:
    // v   : 根据 range 计算出来的数值
    // pos : 用浮点表示滑块当前位置(0.0-1.0)
    change : {c}F(v, pos)     // 当滑块移动的时候调用
    
    // change 回调的上下文
    context : this
});
```

# 销毁控件

控件不能被自动销毁，你需要显示的调用

```
jQuery.slidebar("destroy");
```

来销毁这个控件

# 改动控件的值

```
// 直接改动位置
jQuery.slidebar("pos", pos);

// 根据值来改动位置
jQuery.slidebar("val", val);
```

# 如何根据位置计算出值

如果 `range=[0, 100]`，即开始点值比较小

```
得到 pos 为开始点到滑块的距离与整个长度的比值
val = (100 - 0) * pos;
最后看看怎么取整
```

如果 `range=[100, 0]`，即开始点值比较大
```
得到 pos 为开始点到滑块的距离与整个长度的比值
val = 100 + ((0 - 100) * pos);
最后看看怎么取整
```

# 如何根据值计算出位置

如果 `range=[0, 100]`，即开始点值比较小

```
pos = Min(val,100) / (100 - 0);
```

如果 `range=[100, 0]`，即开始点值比较大
```
pos = 1.0 - Min(val,100) / (100 - 0);
```


