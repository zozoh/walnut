---
title: 综合数据管理控件(Thing)
author:zozoh
---

-----------------------------------------------------
# 控件·结构

```bash
th3_main  -----+  (Global Methods for menu)
   |           V (listen)
   |-- layout {bus}   <----+ (listen)
         |                 |
         |-- [search]     -> th3_search ------+
         |-- [meta]       -> th3_meta         | layout_methods
         |-- [media]      -> th3_media        | th3_methods
         |-- [attachment] -> th3_attachment   |
         |-- [content]    -> th3_content -----+
```

-----------------------------------------------------
# 控件·事件

```bash
# 整体事件
layout:ready       # 布局以及相关子控件 redraw 完毕

# 对象的选择
obj:selected       # 选中某个或者多个对象
    [[obj1,obj2]]
obj:blur           # 取消激活对象（并且没有其他对象将被激活）
    []

# 对象元数据更新
meta:updated       # 更新了对象的元数据
    [obj]
```