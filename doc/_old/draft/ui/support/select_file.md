---
title:文件选择控件
author:zozoh
tags:
- UI
- support
---

# 控件概述

控件再给定选区绘制一个界面，

- 可以让用户获取一个或者多个文件
- 也可以支持上传

# 控件用法

```
new SelectFileUI({
    
    // 是否支持多选，默认为 true
    // 这个选项会被 browser 和 uploader 的对应配置信息覆盖
    // 可以作为他们的默认选项
    multi : true,
    
    // 文件浏览器的配置信息
    browser : {
        ...
    },
    
    // 上传控件的配置信息
    uploader : {
        ...
    },

}).render();
```

# 控件支持的方法

## getFile : 获取选择的文件

```
var o = uiSF.getFile();
```

- 如果没有被选择，则为 null
- 如果是多选模式，那么会返回第一个文件对象
- 总之它一定会返回一个对象

## getFiles : 获取选择的文件列表

```
var oList = uiSF.getFiles();
```

- 如果没有选择，返回的是空数组 `[]`
- 如果是单选模式，会返回一个单元素数组
- 总之它一定会返回一个数组

## getData : 获取数据

```
var data = uiSF.getData();
```

- 自动根据 `multi` 的设定来调用 `getFile` 或者 `getFiles`
- 如果 `multi==true` 会调用 `getFiles`
- 否则会调用 `getFile`


