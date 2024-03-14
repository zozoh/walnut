# 过滤器简介

`@view` 查看上下文压缩包的实体信息

# 用法

```bash
unzipx abc.zip @view 
        [-json]   # 开启这个选项，将实体路径输出为 json 数组
        [-cqn]    # json 模式下的格式化方式
```

# 示例

```bash
# 列出压缩包中的所有实体
unzipx abc.zip @view 
```

