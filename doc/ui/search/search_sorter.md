---
title: 默认搜索排序器
author:zozoh
---

# 如何创建

```
new SearchSorterUI({
    // 可选排序列表
    setup : [{
        icon : '<..>',      // 显示的图标
        text : "i18n:xxx",  // 显示的文字
        value : {           // 排序值
            nm:-1, lm:1
        }
    }, {
        // 第二个排序条件
    }],
    // 声明了这个值，将会在本地记住排序的选择
    // 只要刷新这个页面，就会维持原来的状态 
    storeKey : "xxx"
}).render();
```



