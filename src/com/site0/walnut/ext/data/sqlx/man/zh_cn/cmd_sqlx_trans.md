# 过滤器简介

`@trans` 事务相关操作

> 这里有一个限制，这个事物相关操作只能在同一个节点完成。如果跨越多个节点的事务，
还没有相应的机制。

它有下面两种使用场景:

## 单命令周期

```bash
sqlx @trans @exec ....
```

在一个命令周期开始处声明一个事务，它会在命令上下文记入，待命令结束则会结束事务
中间任何异常，都会回滚

## 跨命令周期

```bash
var re = sys.exec2("sqlx @trans -begin");
// > 命令输出 {transId: "1j46qulejghahr2e6jhgtd17ie"} 表示事务的标识
try{
    ...
	...这里可以执行多条 sqlx 命令
	...
}
// 如果出现错误，这里回滚
catch(err) {
   sys.exec2("sqlx @trans -rollback 1j46qulejghahr2e6jhgtd17ie")
}
// 最好确保事务关闭，如果没有执行下面的操作，这个事务对应的数据库连接
// 将会在一定时间后被子都回收，通常时间是60s
// 可以在 @trans -begin 的时候设置这个回收时间
finally {
   sys.exec2("sqlx @trans -end 1j46qulejghahr2e6jhgtd17ie")
}
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
                           # -1 - 【默认】采用数据库默认事务级别
  [-begin 60]              # 开始一个事务，命令会输出一个事务的信息
                           # 主要是事务 ID. 60 表示这个事务连接
                           # 最多60秒过期，如果超过了这个时间，
                           # 再有数据库操作，会优先释并复用这个连接
                           # 不指定，直接 -begin，默认就是60秒
                           # 这个时间有个范围 [1, 3600] 
                           # 就是说，你设置这个过期时间不能超过1个小时
                           # 而且最短也得是1秒钟
  [-commit TransID]        # 提交一个事务
  [-rollback TransID]      # 回滚一个事务
  [-end TransID]           # 结束一个事务
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
