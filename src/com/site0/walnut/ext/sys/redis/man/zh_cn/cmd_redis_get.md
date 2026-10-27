# 过滤器简介

`@get` 读取字符串键的值（对应 Redis `GET`）。

键不存在时返回 `null`；值只能是字符串类型，对其他类型（哈希/集合）会报错。

# 用法

```bash
redis @get 键
```

# 示例

```bash
# 读取键的值
redis @get mykey

# 管道中使用：先写后读，一次连接两个操作
redis @set mykey 100 @get mykey
# {"SET:mykey":"OK","GET:mykey":"100"}

# 从 Walnut 对象管道喂值
o ~/cfg/theme.json -cqn | redis @set site:theme
```
