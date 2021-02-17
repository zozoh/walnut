# 命令简介 

`truck` 命令用来从不同的索引和桶管理器中迁移数据。

首先，你需要确定目标目录的映射（用 `mount` 命令设置好）
之后，你可以在 `@from` 子命令中设置源索引管理器或者桶管理器

# 用法

```bash
truck 
  [-quiet]                       # 静默输出
  [-json]                        # 将结果输出为 JSON
  [-cqn]                         # 非静默输出的 JSON 格式
  [-ajax]                        # 非静默输出时，要 ajax 包裹
  [-t K1,K2...]                  # 表格输出
  [-bish]                        # 表格输出时的格式化
  [-tmpl @{xx}]                  # 模板输出（默认的输出方式 @{id}）
  [[@filter filter-args...]...]  # 过滤器
```

它支持的过滤器有：

```bash
@from       # 设置转换源（桶或者索引）
@to         # 设置转换目标（桶或者索引）
@buf        # 设置缓冲区的大小
```


# 示例

```bash
# 将记录在 Mongo 里的数据，转换到 SQL 数据源上
# 假设， ~/.tmp/xyz 目录已经映射到了 dao(xyz) 数据源
# 即，执行过了 mount dao(xyz) ~/.tmp/xyz
# 我们希望把之前 Mongo 里面的数据转换到 SQL 数据源中:
truck -t 'id,nm,color,age' -bish @from ~/.tmp/xyz -index mongo @to
# | id                                                    | nm    | color | age
--+-------------------------------------------------------+-------+-------+----
0 | rv6govmbsojhkonuvsuosiool7:pbmu8dcilqiqcqmc5c2vfkuhvr | hello | #FFF  | 13
1 | rv6govmbsojhkonuvsuosiool7:reambksvhsivnqo9ikcte5s2b4 | mmm   | blue  | 34
--+-------------------------------------------------------+-------+-------+----
total 2/-1 items, skip 0 page 1/-1, 1000 per page

#
# 如果多次插入，为了可以加入 -noexist 参数，预先检查数据是否存在
truck -t 'id,nm,color,age' -bish @from ~/.tmp/xyz -index mongo @to -noexists

# 将记录在 SQL 数据源里的数据，转换到 Mongo 某个目录里
# 假设， ~/.tmp/xyz 目录并未有任何映射
# 即，执行过了 umount ~/.tmp/xyz
# 我们希望把之前 SQL 数据源里的数据转换到这个文件夹中:
truck -t 'id,nm,color,age' -bish @from ~/.tmp/xyz -index dao(xyz) @to
# | id                                                    | nm    | color | age
--+-------------------------------------------------------+-------+-------+----
0 | rv6govmbsojhkonuvsuosiool7:pbmu8dcilqiqcqmc5c2vfkuhvr | hello | #FFF  | 13
1 | rv6govmbsojhkonuvsuosiool7:reambksvhsivnqo9ikcte5s2b4 | mmm   | blue  | 34
--+-------------------------------------------------------+-------+-------+----
total 2/-1 items, skip 0 page 1/-1, 1000 per page
# 注意，上面的输出，我们还是可以看到两段式 ID，这表示是从一个映射迁移过来的对象
# 实际上，从 SQL 数据源迁移到 Mongo 里时，仅会使用后一段的 ID
#
# 如果多次插入，为了可以加入 -noexist 参数，预先检查数据是否存在
truck -t 'id,nm,color,age' -bish @from ~/.tmp/xyz -index dao(xyz) @to -noexists
```