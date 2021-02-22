# 过滤器简介

`@filter` 将上下文中符合条件的保留下来

# 用法

```bash
o @filter
  [Match...]      # 一组条件（或）
```

# 示例

```bash
# 将某个文件夹向下展开 2 层，并将名为 abc 的对象作为上下文的当前对象
$demo:> o @filter 'nm:"abc"'
```

