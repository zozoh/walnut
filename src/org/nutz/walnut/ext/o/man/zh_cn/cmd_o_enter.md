# 过滤器简介

`@enter` 进入上下文列表（树）对象，并将其作为根对象

# 用法

```bash
o @enter
  [Match...]      # 多个判断条件，每一个表示进入一层
                  # 每一层均会寻找第一个匹配条件的对象进入
                  # 如果没有条件，相当于进入一层
  [-by children]  # 存放子节点的键为什么，默认为 children
```

# 示例

```bash
# 将某个文件夹向下展开 2 层，并将名为 abc 的对象作为上下文的当前对象
$demo:> o path/to/dir @tree -depth 2 @enter 'nm:"abc"'
```

