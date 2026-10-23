# 命令简介

`truck` 命令用来从不同的索引和桶管理器中迁移数据。

首先用 `mount` 命令确定目标目录的映射，然后在 `@from` 过滤器中设置源索引管理器或桶管理器。

# 用法

```bash
truck [-quiet] [-json] [-ajax] [-cqn] [-t 字段,...] [-bish] [-tmpl 模板] [@过滤器...]
  -quiet    # 静默输出
  -json     # 结果输出为 JSON
  -ajax     # 非静默输出时加 AJAX 包裹
  -cqn      # JSON 输出的格式化开关
  -t        # 表格输出，指定字段列表（如 'id,nm,color,age'）
  -bish     # 表格输出的格式化开关（边框/行号/汇总/表头）
  -tmpl     # 模板输出方式（如 '@{id}'）
```

支持的过滤器：

```bash
@from    # 设置转换源（桶或者索引）
@to      # 设置转换目标（桶或者索引）
@buf     # 设置缓冲区的大小
```

# 示例

```bash
# 将 Mongo 里的数据转换到 SQL 数据源
# 前提：~/.tmp/xyz 已映射到 dao(xyz) 数据源（mount dao(xyz) ~/.tmp/xyz）
truck -t 'id,nm,color,age' -bish @from ~/.tmp/xyz -index mongo @to

# 多次插入时，预检查数据是否存在
truck -t 'id,nm,color,age' -bish @from ~/.tmp/xyz -index mongo @to -noexists

# 将 SQL 数据源的数据转换到 Mongo 某个目录
# 前提：~/.tmp/xyz 未做任何映射（umount ~/.tmp/xyz）
truck -t 'id,nm,color,age' -bish @from ~/.tmp/xyz -index dao(xyz) @to

# 从映射目录迁移，固定顺序分页
truck -t 'id,nm' -bish @from ~/account -match '{age:{$gt:10}}' -sort '{ct:-1}' @to -index dao(abc)
```
