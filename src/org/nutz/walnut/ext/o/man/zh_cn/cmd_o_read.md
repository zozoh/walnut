# 过滤器简介

`@read` 读取上下文对象的内容

# 用法

```bash
o @read 
  [path]         # 读取文件内容，默认的读取自己的内容
                 # 如果值为 `~ignore~` 也无视
                 # 如果对象为目录，没有这个值，则无视
                 # 支持特殊值 `~self~` 表示 null，即读取自己
  [-to content]  # 将读取的内容设置到对象的哪个键，默认为 content
  [-as text]     # 将内容格式化，默认为 text 表示纯文本
                 # 如果为 json，则表示变成一个 JSON 对象设置到
                 # 对象的目标存储键
```

# 示例

```bash
# 读取对象本身内容
o path/to @read 

# 读取一组对象的文本信息
o ... @read .data/zh_cn.md -to contente
```

