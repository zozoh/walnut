# 过滤器简介

`@validate` 用来为检查上下文对象是否符合约束。如果上下文是列表则逐个检查
    
# 用法

```bash
jsonx @validate
  [WnMatch]    # 指定检查的约束
  [-f v.json]  # 约束来自一个文件【更高优先级】
  [-only]      #【选】输入对象必须在集合内
  [-ignoreNil] #【选】空值不检查
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