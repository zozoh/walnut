---
title: 浏览器插件
author:zozoh
---

# 控件概述

```
{
    // 控件是否保存最后一次切换的当前路径，如果这个字段有值，则将其作为 key
    // 则会将最后一次路径的 oid 存在 localStorage 里
    // 实际上，如果两个 browser 实例的这个字段相同，那么则会共享
    lastObjId : "last-oid",
    
    // 毫秒计算，缓存对象的时间，如果不是强制刷新，那么一个对象最多缓存多少毫秒
    // <=0 表示不缓存
    cacheTimeout : 10000,
}
```

# 本地存储模型

localStorage 存放所有获取下来的对象，以便重复利用

```
oid:431cad.. : {..}
oid:f68ae1.. : {..}
```

每个对象都会有下面的字段

```
{
    __local_cache : 1498...   // 一个绝对毫秒数表示缓存的时间
    ph : "/path/to/obj"       // 路径一定会有的，如果没有就主动拼装出来
    // 如果读取过 children，则会生成这个字段。每次刷新 children
    // 则会更新这个字段，同时添加或删除对应 localStorage 里面的数据
    children : [$id,$id..]
}
```

# 支持的操作

## getObj : 获取对象

```
// 根据 ID 获取对象，如果对象缓存过期，则从服务器获取
var o = UI.getObj("34acd5");

// 根据 ID 获取对象，强制从服务器更新数据，并更新到对应缓存
var o = UI.getObj("34acd5", true);
```

## fetch : 根据路径获取对象

```
// 根据 Path 获取对象，如果对象缓存过期，则从服务器获取
var o = UI.fetch("/path/to/obj");

// 根据 Path 获取对象，强制从服务器更新数据，并更新到对应缓存
var o = UI.fetch("/path/to/obj", true);
```



