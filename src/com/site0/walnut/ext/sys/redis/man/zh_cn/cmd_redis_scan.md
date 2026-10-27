# 过滤器简介

`@scan` 以游标方式增量遍历键空间（对应 Redis `SCAN`），不会阻塞服务，是 `@keys` 的生产替代。

- 游标从 `0` 开始，**每次调用返回新的游标**，游标回到 `0` 表示遍历完成
- `-match <模式>` 按模式过滤（匹配可能在服务端，也可能在遍历过程中增量过滤）
- `-count <N>` 暗示每批返回数量（默认 100，服务端不保证精确）

输出为 `{cursor: 新游标, results: [...]}`。

# 用法

```bash
redis @scan [游标=0] [-match 模式] [-count 每批数量]
```

# 示例

```bash
# 第一批
redis @scan 0 -match 'user:*' -count 100

# 用返回的 cursor 取下一批，直到 cursor 为 0
redis @scan 17 -match 'user:*' -count 100

# 搭配 jsc 写个完整遍历脚本
jsc 'var cu=0; do { var r=sys.exec2("redis @scan "+cu+" -match \'tmp:*\' -cqn"); ... } while(cu>0)'
```
