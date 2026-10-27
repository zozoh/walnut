# 过滤器简介

`@strlen` 返回字符串值的长度（对应 Redis `STRLEN`），键不存在返回 0。

# 用法

```bash
redis @strlen 键
```

# 示例

```bash
redis @set mykey hello @strlen mykey
# {"SET:mykey":"OK","STRLEN:mykey":5}
```
