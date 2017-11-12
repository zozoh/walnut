---
title: 默认搜索分页器
author:zozoh
---

# 如何创建

```
new SearchPagerUI({
    // 默认的数据值，当 setData 不传参数，就用这个
    dft : {
        pn   : 1,     // 第几页
        pgsz : 50,    // 每页多少数据
        pgnb : 1,     // 一共多少页
        sum  : 0,     // 一共多少记录
        nb   : 0      // 本页实际获取了多少数据
    }
}).render();
```

