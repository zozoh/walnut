# 过滤器简介

`@trans` 事务相关操作

这里有一个限制，这个事物相关操作只能在同一个节点完成。如果跨越多个节点的事物，这
里还没有相应的机制。

它有两种使用场景:

## 单命令周期

```bash
sqlx @trans -begin @exec ....
```

在一个命令周期开始处声明一个事务，它会在命令上下文记入，待命令结束则会结束事务
中间任何异常，都会回滚

## 跨命令周期

```bash
sqlx @trans -keep
> 命令输出 {transId: "T0001"} 表示事务的标识
...
...这里可以执行多条 sqlx 命令
...
sqlx @trans -end T0001
```

这种模式可以让 js 等脚本很好的支持事务

# 用法

```bash
sqlx @trans
  [-level 1]               # 开始一个事务的级别的：
                           #  1 - TRANSACTION_READ_UNCOMMITTED
                           #  2 - TRANSACTION_READ_COMMITTED
                           #  4 - TRANSACTION_REPEATABLE_READ
                           #  8 - TRANSACTION_SERIALIZABLE
  [-commit TransName]      # 提交一个事务
  [-rollback TransName]    # 回滚一个事务
  [-end TransName]         # 结束一个事务
  [-keep]                  # 本事务跨越多个命令周期，需要主动调用
                           # sqlx @trans -end {TransName} 来结束
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
