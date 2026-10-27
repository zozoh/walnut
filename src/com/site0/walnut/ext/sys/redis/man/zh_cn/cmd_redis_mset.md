# 过滤器简介

`@mset` 一次性设置多个键（对应 Redis `MSET`），返回 `"OK"`。

与官方不同的是，键值对通过 `-map` 传一个 JSON 对象，而不是成对的参数。
整个操作是**原子**的。

# 用法

```bash
redis @mset -map '{键1:值1, 键2:值2, ...}'
```

# 示例

```bash
# 批量写入配置
redis @mset -map '{site:name:"Demo", site:mode:"prod", site:theme:"dark"}'

# 写完后立刻验证其中一个
redis @mset -map '{a:1, b:2}' @get a
# {"MSET:a,b":"OK","GET:a":"1"}
```
