---
title:站点模板
author:zozoh
tags:
- 扩展
- hmaker
---

# 什么是站点模板

任何一个站点都可以选用一套模板，来为 `dynamic` 控件数据提供 DOM 渲染逻辑. 模板存放在域中 `.hmaker` 目录下以便多个站点共享

```
~/.hmaker/template
    templateA           # 模板所有的资源存放目录
        templagte.info.json  # 一个JSON文件，以便编辑器理解皮肤
        jquery.fn.js         # 模板的 jQuery 插件
```

# templagte.info.json

```
{
    // 模板的 jQuery 插件名
    "name"     : "wn_plst_th_video",
    
    // 模板显示名
    "title"    : "默认视频列表",
    
    // 可以匹配什么样的数据接口返回类型
    // 可选值为: list | obj
    "dataType" : "list",
    
    // 模板选项表，格式参见 《动态设置》
    "options"  : { ... }
}
```

> [《动态设置》](hm_setting.md)

# jquery.fn.js

就是一个标准的 jQuery 插件


