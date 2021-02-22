# 过滤器简介

`@children` 读取上下文对象的所有子节点（可递归）

# 用法

```bash
o @children
  [Match...]      # 符合任意条件的对象，均可继续读取子节点（必须是目录）
                  # 如果没有值，那么只要是目录就会读取
  [-by children]  # 存放子节点的键为什么，默认为 children
  [-hidden]       # 如果是隐藏节点，也不跳过判断
```

# 示例

```bash
# 将某个文件夹向下展开 2 层，并将名为 abc 的对象作为上下文的当前对象
$demo:> o path/to/dir @tree -top ~ @enter @children 'tp:"MyCate"'
```

