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
# 控件·数据

控件 `th3_main` 记录了数据:

```js
{
    home  : {},         // 主目录对象
    conf  : {},         // 配置对象
    currentId : "xxx"   // 当前激活的数据ID
                        // null 表示当前没有编辑任何数据
}
```

每个子控件都可以通过 `getMainData` 获取这个数据

-----------------------------------------------------
# 控件·事件

```bash
# 整体事件
layout:ready       # 布局以及相关子控件 redraw 完毕

# 关于对象
obj:selected       # 选中某个或者多个对象
    [[obj1,obj2]]
obj:blur           # 取消激活对象（并且没有其他对象将被激活）
    []

# 关于列表
list:remove    # 移除一个对象(由列表监听，并判断后续是next还是blur)
               # 因此移除的是列表选中的对象
list:refresh   # 通知列表刷新，会导致对象重新被选中
    [callback, jumpToHead] # 参数是一个回调，当加载完毕后，应该调用这个函数以便清理


# 对象元数据更新
meta:updated       # 更新了对象的元数据
    [obj]

# 全局事件
do:create      # 创建一个对象(由th3_main监听并打开一个创建窗口)
do:import      # 需要执行导入
do:export      # 需要执行导出
do:cleanup     # 清空回收站
do:restore     # 从回收站恢复
```