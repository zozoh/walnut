# 过滤器简介

`@remove` 从上下文中移除特殊的键

# 用法

```bash
@remove [REGEX | key ...]  # 一个WnMatch表达式或者是键值，可以是多个
  [-value]                 # 表达式匹配的是而不是键
```

# 示例

```bash
# 按照正则表达式过滤
jsonx @remove '^(a|b|c)$'

# 直接过滤键名
jsonx @remove key1 key2 key3

# 混合模式
jsonx @remove key1 "^(a|b|c)" key3
```

