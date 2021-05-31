# 过滤器简介

`@flat` 将上下文中的树型结构展平

# 用法

```bash
o @flat
  [-ignore]        # 开启这个选项，本操作将被跳过
  [-by children]   # 存放子节点的键为什么，默认为 children
  [-leaf]          # 只保留叶子节点
  [-tagby K1,K2..] # 半角逗号分隔的键，找到第一个键，作为所有子节点的标签
                   # 子节点标签根据多级父目录，会累加为一个列表
  [-tagkey tag]    # 将子节点标签存放到子节点的哪个键上，默认为 tag
```

# 示例

```bash
# 读取两层目录，并展平
o ~/mydir @children -depth 2 -leaf @flat
```

