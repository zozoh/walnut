# 过滤器简介

`@getbit` 读取字符串某偏移位的比特值（对应 Redis `GETBIT`），返回 0/1。

偏移超出字符串长度时返回 0。

# 用法

```bash
redis @getbit 键 偏移
```

# 示例

```bash
redis @getbit sign:u1001 1       # 1（签到了）
redis @getbit sign:u1001 99      # 0（超出长度，视为未设置）
```
