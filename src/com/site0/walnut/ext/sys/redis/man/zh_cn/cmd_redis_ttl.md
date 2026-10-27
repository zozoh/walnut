# 过滤器简介

`@ttl` 查询键的剩余存活时间，单位**秒**（对应 Redis `TTL`）。

- 返回值 > 0：剩余秒数
- `-1`：键存在但没有设置过期时间
- `-2`：键不存在

# 用法

```bash
redis @ttl 键
```

# 示例

```bash
redis @setex code 300 8845 @ttl code
# {"SETEX:code":"OK","TTL:code":300}

# 判断键是不是永久的
redis @ttl mykey
```
