---
title: 名值对控件
author:zozoh
---

# 控件概述

提供一个编辑名值对的简单控件

# 如何创建实例

```
new CPairUI({
    // 获取值时，截取值的左右空白，默认 true
    trimSpace : true,
    
    // 获取值时，与传入的对象融合，默认 false
    mergeWith : false,
    
    // 对象模板作为默认值
    templateAsDefault : true
}).redraw(function(){
    this.setData({
        x : 100,
        y : 99
    });
});
```

# 控件方法

继承自 `form_ctrl`

## setObjTemplate

```
uiPair.setObjTemplate(ot, cleanData)
```

