# 过滤器简介

`@push` 向上下文中所有对象压入对象数组字段

# 用法

```bash
o @push
  [{..}]          # 多个值
  [-to xxx]       # 【必】向哪个键压入值
  [-uniq]         # 确保数组唯一
  [-raw]          # 如果不声明这个选项，值会被自动解析
```

# 示例

```bash
# 向某字段推入值
$demo:> o abc @push -to names A B C @json '^(id|names)$'
{"id":"0cnhdbir8sin3q88himugvf40h","names":["A", "B", "C"]}

# 向某字段推入值，并保持唯一
$demo:> o abc @push -uniq -to names A A C @json '^(id|names)$'
{"id":"0cnhdbir8sin3q88himugvf40h","names":["A", "C"]}

# 根据推入的值，自动转换为对应的变量类型
$demo:> o abc @push -to names 1 true A @json -cqn '^(id|names)$'
{"id":"0cnhdbir8sin3q88himugvf40h","names":[1, true, "A"]}

# 强制将推入的值全都当作字符串来处理
$demo:> o abc @push -to names 1 true A -raw @json -cqn '^(id|names)$'
{"id":"0cnhdbir8sin3q88himugvf40h","names":["1", "true", "A"]}
```

