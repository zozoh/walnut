# 过滤器简介

`@append` 假设上下文为列表，向其尾部压入更多对象

# 用法

```bash
o @append
  [{..}]          # 多个值,均为 json 对象
```

# 示例

```bash
# 向某字段推入值
$demo:> echo '[{N:1}]' | jsonx -cqn @append '{N:2}'
[{N:1}, {N:2}]

$demo:> echo '{N:1}' | jsonx -cqn @append '{N:2}'
[{N:1}, {N:2}]
```
