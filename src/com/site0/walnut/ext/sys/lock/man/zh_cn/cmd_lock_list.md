# 过滤器简介

`@list` 获取所有锁列表

# 用法

```bash
lock @list
```

# 示例

```bash
# 获取锁列表
> lock @list
[{
   name: "lockA",
   hold: "2025-01-03 11:36:24.746",
   expi: "2025-01-03 11:53:04.746",
   owner: "bunya",
   hint: "USER_ASK"
}, {
   name: "lockB",
   hold: "2025-01-03 11:36:28.040",
   expi: "2025-01-03 11:53:08.040",
   owner: "bunya",
   hint: "USER_ASK"
}, {
   name: "lockC",
   hold: "2025-01-03 11:36:31.464",
   expi: "2025-01-03 11:53:11.464",
   owner: "bunya",
   hint: "USER_ASK"
}]
```


