# 过滤器简介

`@to` 设置转换目标，同时确定转换模式。

# 用法

```bash
truck @to [目录] [-mode 模式] [-noexists] [-genid]
       # 目录     # 默认采用 @from 的路径，该目录的管理器即目标管理器
  -mode      # 转换方式：INDEXER 仅索引 | BM 仅桶 | BOTH 索引和桶，默认 INDEXER
  -noexists  # 只有不存在的才插入（预先检查，速度慢一些）
  -genid     # 转换的数据需要新生成 ID
```

# 示例

```bash
# 转换到当前目录的默认管理器
truck @from ~/account -index mongo @to

# 转换索引和桶
truck @from ~/account @to -mode BOTH

# 预检查插入
truck @from ~/account @to -noexists
```
