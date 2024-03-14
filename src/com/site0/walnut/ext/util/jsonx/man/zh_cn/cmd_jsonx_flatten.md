# 过滤器简介

`@flatten` 将一个树型结构的 JSON 数据转换为一个列表。
它支持的上下文为两种:

```bash
# 1. 根节点模式
{
  ...
  children: [
    {
      ...
      children: [...]
    }
  ]
}

# 2. 列表模式
[
  {
    ...
    children: [
      {
        ...
        children: [...]
      }
    ]
  }
]
```
 
# 用法

```bash
@flatten 
  [children]         # 指定一个对象键作为子对象，默认为 "children"
  [-notop]           # 如果根节点模式，将忽略根节点
  [-onlyleaf]        # 只有叶子节点才会加入
  [-leaf WnMatch]    # 指定了哪些节点为 Leaf 节点，
                     # 默认的，只要没有 children 即为叶子节点
  [-ignore WnMatch]  # 指定了哪些节点会被忽略（黑名单）
  [-filter WnMatch]  # 指定了哪些节点会被保留（白名单）
```

# 示例

```bash
jsonx @flatten 
```

