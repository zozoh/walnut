---
title: 图像控件
author:zozoh
---

# 控件概述

提供一个文本输入框

# 如何创建实例

```
new CImageUI({
    width  : undefined     // 「选」图片固定宽度
    height : undefined     // 「选」图片固定高度

    // 上传目标，可以有以下形式
    //  "id:xxx"  - 指定了文件的 ID
    //  "/path/to/file" - 绝对路径
    //  "~/path/to/file" - 指定主目录下的路径
    //  "id:xxxx/path/to/file" - 指定某ID文件夹下的文件
    // 所指向的文件必须存在，如果不存在，会自动创建一个（id:xxx) 形式的除外，因为无法指定
    target : "xxxx"

    // 返回的是图片的路径/ID/还是完整对象
    // - obj  : 完整对象
    // - path : obj.ph
    // - id   : obj.id
    // - idph : "id:" + obj.id
    dataType : "obj|path|id|idph"

    // 检查上传文件的合法性，默认会动态的根据 target 来判断
    validate : {c}F(file, UI):Boolean

    // 回调
    done     : {c}F(re)
    fail     : {c}F(re)
    complete : {c}F(re, status)
});
```

# 控件方法

继承自 `form_ctrl`

## getData

```
var obj = uiImage.getData();
-> {
    标准 WnObj
}
```

## setData

```
uiImage.setData({..});   // 参数是标准 WnObj
```

