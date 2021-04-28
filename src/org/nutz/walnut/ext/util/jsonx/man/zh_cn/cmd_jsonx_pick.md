过滤器简介
======= 

`@pick` 从上下文中挑出特殊的键值
 

用法
=======

```bash
# 按照正则表达式过滤
jsonx @pick '^(a|b|c)$'

# 直接过滤键名
jsonx @pick key1 key2 key3

# 混合模式
jsonx @pick key1 "^(a|b|c)" key3
```

