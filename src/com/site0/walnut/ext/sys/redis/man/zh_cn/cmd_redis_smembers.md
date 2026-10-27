# 过滤器简介

`@smembers` 列出集合的全部成员（对应 Redis `SMEMBERS`），返回数组。
成员无序，顺序不可依赖。

# 用法

```bash
redis @smembers 键
```

# 示例

```bash
redis @sadd tags a b c @smembers tags
# {"SADD:tags:a,b,c":3,"SMEMBERS:tags":["a","b","c"]}
```
