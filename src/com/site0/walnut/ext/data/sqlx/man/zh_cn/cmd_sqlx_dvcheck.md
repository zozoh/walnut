# 过滤器简介

`@dvcheck` 检查数据版本

对于一条或者一组数据，如果多端竞争写入，就会发生不可预知的意外。
采用数据版本的策略，可以有效的避免这种局面。

本过滤器需要下面几个信息:

1. before 调用者期望的数据库版本号
2. after 验证后，希望数据空中版本号变成什么
1. 采用什么 sql 获取数据库中的版本号(`before`)，并同时将版本号改成什么(`after`)
   传入这个 SQL 的变量集，除了 `before, after` 还可以自定义变量集。用以查询条件
   通常这个SQL 为 UPDATE xx='${after}' WHERE id=${id} AND xx='${before}'
   如果更新了1条语句，那么就表示检查通过了

# 用法

```bash
sqlx @dvcheck
  [sqlName]           # sql 模板的名称
  [versionKey]        # 数据表中存版本号的字段名
  [$before:$after]    # 期望版本号:成功后修改的版本号
```

# 示例

```bash
sqlx @vars '{id:"xxx",title:"world"}' @dvcheck pet.check ver v0:v2 @exec pet.update
```
