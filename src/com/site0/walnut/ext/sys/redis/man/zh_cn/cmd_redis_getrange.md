# 过滤器简介

`@getrange` 返回字符串的子串（对应 Redis `GETRANGE`，即原来的 `SUBSTR`）。

- `start` / `end` 都是闭区间下标，从 0 开始
- 负数表示从末尾数：`-1` 是最后一个字符
- 越界部分自动截断，键不存在返回空串

# 用法

```bash
redis @getrange 键 起 止
```

# 示例

```bash
redis @set mykey 'This is a string'
redis @getrange mykey 0 3        # "This"
redis @getrange mykey -3 -1      # "ing"
redis @getrange mykey 0 -1       # 整个字符串
redis @getrange mykey 10 100     # "string"（越界自动截断）
```
