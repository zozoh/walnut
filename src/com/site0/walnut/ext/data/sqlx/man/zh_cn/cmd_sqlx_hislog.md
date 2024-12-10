# 过滤器简介

`@hislog` 指定了本批次 SQL 执行的历史记录方式

# 设计

我们希望通过少量的配置，可以在用户执行 `@exec` 的时候悄悄的向某个历史记录表
记录信息，这个表默认的是与当前处于相同的数据源，当然你也可以指定一个特殊的数据源。

我们通过这个过滤器，我们可以指定:

1. 哪些 SQL 是需要记录的
2. 记录哪些上下文，记录成什么形式
3. 还有没有更多需要记录的信息

# 用法

```bash
sqlx @hislog
  [sqlName]           # sql 模板的名称
  [versionKey]        # 数据表中存版本号的字段名
  [$before:$after]    # 期望版本号:成功后修改的版本号
```

# 示例

```bash
sqlx @vars '{id:"xxx",title:"world"}' @dvcheck pet.check ver v0:v2 @exec pet.update
```
