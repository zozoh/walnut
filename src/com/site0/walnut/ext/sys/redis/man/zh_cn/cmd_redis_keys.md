# 过滤器简介

`@keys` 返回匹配模式的所有键（对应 Redis `KEYS`）。

模式语法：`*` 任意多字符，`?` 一个字符，`[abc]` 字符集合。

> ⚠️ `KEYS` 会一次性扫描整个 keyspace，键多时会**阻塞 Redis**（其他请求全部等待）。
> 生产环境遍历请用 `@scan`；它只适用于键很少的场景和调试。

# 用法

```bash
redis @keys 模式
```

# 示例

```bash
# 所有键（仅调试！）
redis @keys '*'

# 某前缀的键
redis @keys 'user:*'

# 单字符通配
redis @keys 'log:2026102?'
```
