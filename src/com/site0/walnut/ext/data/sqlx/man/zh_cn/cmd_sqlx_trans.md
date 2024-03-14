# 过滤器简介

`@trans` 事务相关操作

这里有一个限制，这个事物相关操作只能在同一个节点完成。如果跨越多个节点的事物，这
里还没有相应的机制。

# 用法

```bash
sqlx @trans
  [-begin]                 # 开始一个事务。
  [-level 1]               # 开始一个事务的级别的：
                           #  1 - TRANSACTION_READ_UNCOMMITTED
                           #  2 - TRANSACTION_READ_COMMITTED
                           #  4 - TRANSACTION_REPEATABLE_READ
                           #  8 - TRANSACTION_SERIALIZABLE
  [-commit TransName]      # 提交一个事务
  [-rollback TransName]    # 回滚一个事务
  [-end TransName]         # 结束一个事务
```

# 示例

在在一个脚本里，你可以这么使用事务：

```js
// 开启事务
var transId = sys.exec('sqlx @trans -begin')

try {
  //
  // 每个执行的 SQL 都带上事务的 ID
  sys.exec('sqlx -trans ' + transId + ' @exec pet.xxx @exec pet.xxx2')
  sys.exec('sqlx -trans ' + transId + ' @exec pet.xxx')
  //
  // ... 执行脚本其他逻辑 ...
  //
  sys.exec('sqlx @exec -trans ' + transId + ' pet.xxx')
  // 事务提交
  sys.exec('sqlx @trans -commit ' + transId )
}
// 遇到异常回滚事务
catch(err) {
  sys.exec('sqlx @trans -rollback ' + transId)
}
// 最后确保结束事务
finally {
  sys.exec('sqlx @trans -end ' + transId)
}
```
