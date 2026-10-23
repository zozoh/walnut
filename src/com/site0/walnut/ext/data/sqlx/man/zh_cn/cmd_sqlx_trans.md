# 过滤器简介

`@trans` 设置当前命令周期内 SQL 执行的事务隔离级别。

> 限制：事务只在同一个命令周期内有效。跨命令周期的事务机制（`-begin/-commit/-rollback/-end`、`-keep`）暂不支持，`-keep` 会抛出 `e.cmd.sqlx.trans.KeepAliveNotSupport`。

在一个命令周期开始处声明事务级别，周期内的 SQL 用同一连接执行，命令结束时提交；中间任何异常都会回滚。

# 用法

```bash
sqlx @trans [-level N]
  -level    # 事务级别，默认 -1 采用数据库默认
            #  1 - TRANSACTION_READ_UNCOMMITTED
            #  2 - TRANSACTION_READ_COMMITTED
            #  4 - TRANSACTION_REPEATABLE_READ
            #  8 - TRANSACTION_SERIALIZABLE
            # -1 - 数据库默认
```

# 示例

```bash
# 一个命令周期内执行多条 SQL，任一失败即全部回滚
sqlx @trans @exec pet.insert @exec pet.update
```
