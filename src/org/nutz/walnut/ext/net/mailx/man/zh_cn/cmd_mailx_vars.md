# 过滤器简介

`@vars` 向上下文增加一组变量
 

# 用法

```bash
@vars [{..} ...]      # 多个 JSON Map 表示变量
```


# 示例

```bash
# 指定元数据
mailx @vars '{x:100, y:99}'

# 从标准输入读取元数据
mailx @vars
```

