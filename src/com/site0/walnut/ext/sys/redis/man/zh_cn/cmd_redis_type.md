# 过滤器简介

`@type` 查看键的数据类型（对应 Redis `TYPE`），返回 `string` / `hash` / `set` / `none`（不存在）。
先用它确认类型，再选择正确的命令族，可以避免很多「WRONGTYPE」错误。

# 用法

```bash
redis @type 键
```

# 示例

```bash
redis @type user:1        # "hash"
redis @type mykey         # "string"
redis @type no_such       # "none"
```
