# 过滤器简介

`@put` 向上下文中所有对象压入对象数组字段

# 用法

```bash
o @put
  [KEY]           # 指定一个目标键
  [{..} ...]      # 多个字段集合
  [-dft]          # 作为默认值加入
```


# 示例

```bash
#--------------------------------------------------------
# 假设有一个对象，pet={name:"xiaobai"}
#--------------------------------------------------------
# 向对象的 pet 键推入内容
$demo:> o abc @put pet '{age:15}' @json '^(id|pet)$' -cqn
{"id":"0cnhdbir8sin3q88himugvf40h","pet":{"name":"xiaobai","age":15}}

# 向对象的 pet 键推入默认内容
$demo:> o abc @put pet '{name:"haha"}' -dft @json '^(id|pet)$' -cqn
{"id":"0cnhdbir8sin3q88himugvf40h","pet":{"name":"xiaobai"}}
```

