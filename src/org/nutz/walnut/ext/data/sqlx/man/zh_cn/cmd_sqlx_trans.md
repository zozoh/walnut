# 过滤器简介

`@trans` 事务相关操作

这里有一个限制，这个事物相关操作只能在同一个节点完成。如果跨越多个节点的事物，这
里还没有相应的机制。

# 用法

```bash
sqlx @trans
  [-begin TransName]       # 开始一个事务。
  [-level 1]               # 开始一个事务的级别的：
                           #  1 - TRANSACTION_READ_UNCOMMITTED
                           #  2 - TRANSACTION_READ_COMMITTED
                           #  4 - TRANSACTION_REPEATABLE_READ
                           #  8 - TRANSACTION_SERIALIZABLE
  [-commit TransName]      # 提交一个事务
  [-rollback TransName]    # 回滚一个事务
  [-end TransName]       # 结束一个事务
```

# 示例

在在一个脚本里，你可以这么使用事务：

```
sqlx @trans -begin A

... 执行脚本其他逻辑 ...

sqlx @exec -trans A pet.xxx
sqlx @exec -trans A pet.xxx
sqlx @exec -trans A pet.xxx

... 执行脚本其他逻辑 ...

sqlx @trans -commit A
```
