---
title:控件:翻页器控件
author:zozoh
tags:
- 扩展
- hmaker
---

# 翻页器控件概述

提供翻页参数

# getComValue 返回

永远返回 JSON 对象

```
{
    pn   : 1,     // 第几页
    pgsz : 50,    // 每页多少数据
    skip : 0,     // 需要跳过多少数据，相当于 (pn-1)*pgsz
}
```


