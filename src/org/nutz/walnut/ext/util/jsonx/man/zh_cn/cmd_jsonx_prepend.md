# 过滤器简介

`@append` 假设上下文为列表，向其首部压入更多对象

# 用法

```bash
o @prepend
  [{..}]          # 多个值,均为 json 对象
  [-to key]       # 指明上下文对象键，否则会认为上下文就是一个列表
  [-path]         # 对象键未路径格式
```

# 示例

```bash
# 向某字段推入值
$demo:> echo '[{N:1}]' | jsonx -cqn @prepend '{N:2}'
[{N:2}, {N:1}]

$demo:> echo '{N:1}' | jsonx -cqn @prepend '{N:2}'
[{N:2}, {N:1}]

$demo:> echo '{x:["A"]}' | jsonx -cqn @prepend -to "x" 'B'
{x:["B", "A"]}
```

