# 命令简介 

`dsync @as` 输出上下文信息

# 用法

```bash
dsync @as
  [{Mode}]    # 输出模式，默认为 `breif`
              #  - brief   : 仅输出上下文索引树摘要
              #  - archive : [JSON]输出上下文归档对象元数据
              #  - metas   : [JSON]数据上下文索引树的元数据表
              #  - tree    : 输出索引树摘要和内容
  [-quiet]    # 静默输出。有时候，在 regapi 里，会直接写 @as，
              # 加一个开关可以让其失效比较有腾挪的余地
  [-cqn]      # JSON 模式下的格式化输出
```
# 示例

```bash
#-----------------------------------------------------------
# 根据域文件内容，生成索引数
demo@~$ dsync @tree @as
------------------------------------------------------------
DSYNC PACKAGE: [dsync-6e3b41d6e5e1fb29caa3dc45a8abdac718d5d8ac.zip]
------------------------------------------------------------
- dsync-site
------------------------------------------------------------
TREE[dsync-site]
sync_time: 2021-03-30 23:51:15
tree_obj: 499.78KB(511774bytes)
meta_obj: 1.09MB(1144391bytes)
item_count: 2426
dir_count: 1051
file_count: 1375
size_count: 430.9MB(451827305bytes)
------------------------------------------------------------
- dsync-medias
------------------------------------------------------------
TREE[dsync-medias]
sync_time: 2021-03-30 13:27:09
tree_obj: 941Bytes
meta_obj: 2.81KB(2876bytes)
item_count: 5
dir_count: 0
file_count: 5
size_count: 3.99MB(4179114bytes)
```