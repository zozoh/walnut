# 过滤器简介

`@flatten` 将一个树型结构的 JSON 数据转换为一个列表。支持的上下文为两种：

```bash
# 1. 根节点模式
{
  ...
  children: [
    { ..., children: [...] }
  ]
}

# 2. 列表模式
[
  { ..., children: [...] }
]
```

# 用法

```bash
jsonx @flatten [子对象键] [-noTop] [-onlyleaf] [-leaf WnMatch] [-ignore WnMatch] [-filter WnMatch]
       # 子对象键    # 指定一个对象键作为子对象，默认为 children
  -noTop     # 根节点模式下忽略根节点
  -onlyleaf  # 只有叶子节点才会加入
  -leaf      # 指定哪些节点为 Leaf 节点；默认没有 children 即为叶子节点
  -ignore    # 指定哪些节点会被忽略（黑名单）
  -filter    # 指定哪些节点会被保留（白名单）
```

注意：参数名是 `-noTop`（大小写敏感）。

# 示例

```bash
# 把树形 JSON 展平成列表
jsonx @flatten

# 忽略根节点，只保留叶子
cat tree.json | jsonx @flatten -noTop -onlyleaf
```
