# 过滤器简介

`@bitcount` 统计字符串中被置 1 的比特数量（对应 Redis `BITCOUNT`）。

只传键名统计全部；传 `起 止` 则只统计字节区间内（注意：**字节**区间，不是比特偏移）。
配合 `@setbit` 的签到数据，一行就能算出「这个月签到了几天」。

# 用法

```bash
redis @bitcount 键 [起 止]
```

# 示例

```bash
redis @bitcount sign:u1001
# 假设一个月 31 天签到数据，统计前 31 字节（足够覆盖 248 个比特位）
redis @bitcount sign:u1001 0 30
```
