# 过滤器简介

`@export` 将文件解压缩到指定目标

# 用法

```bash
unzipx abc.zip @export 
        [/path/to]   # 要输出的目标目录
        [-metas ]    # 输出目标的补充元数据集合，键为实体路径
```

# 示例

```bash
# 列出压缩包中的所有实体
unzipx abc.zip @export ~/tmp/ 
```

