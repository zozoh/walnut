# 过滤器简介

`@remove` 从上下文中所有对象删除内部文档对象值

# 用法

```bash
o @put
  [KEY]           # 指定一个目标键
  [k1,k2...]      # 多个字段用半角逗号分隔
```

# 示例

```bash
#--------------------------------------------------------
# 假设有一个对象，pet={name:"xiaobai", age:15}
#--------------------------------------------------------
# 从对象的 pet 键内文档删除 age 字段
$demo:> o abc @remove pet age @json '^(id|pet)$' -cqn
{"id":"0cnhdbir8sin3q88himugvf40h","pet":{"name":"xiaobai"}}
```

