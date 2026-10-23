# 过滤器简介

`@as` 输出上下文信息。

# 用法

```bash
dsync @as [模式] [-quiet] [-cqn]
       # 模式     # 输出模式，默认 brief
                #  - brief   : 仅输出上下文索引树摘要
                #  - archive : [JSON]输出上下文归档对象元数据
                #  - metas   : [JSON]输出上下文索引树的元数据表
                #  - tree    : 输出索引树摘要和内容
  -quiet   # 静默输出；在 regapi 里直接写 @as 时，加此开关可令其失效
  -cqn     # JSON 模式下的格式化开关
```

# 示例

```bash
# 生成索引树并输出摘要
dsync @tree @as
# ------------------------------------------------------------
# DSYNC PACKAGE: [dsync-6e3b41d6e5e1fb29caa3dc45a8abdac718d5d8ac.zip]
# ------------------------------------------------------------
# TREE[dsync-site]
# sync_time: 2021-03-30 23:51:15
# item_count: 2426
# ...
```
