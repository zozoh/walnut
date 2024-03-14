# 过滤器简介

`@get` 从上下文获取一个值，并设置为当前对象

# 用法

```bash
@get [KeyPath]   # 取值路径，支持 '.'
```

# 示例

```bash
echo '{pos:{x:100}}' | jsonx @get 'pos.x'
```

