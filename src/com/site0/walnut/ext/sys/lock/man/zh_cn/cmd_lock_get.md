# 过滤器简介

`@get` 获取一个指定的锁信息。
它只是用来读一个锁，因此返回的锁信息并不包括 `privateKey`
从而防止你释放锁。

# 用法

```bash
lock @get
  [lockName ...]    # 锁名，可以说多个
  [-strict]         # 严格模式，任何一个锁不存在，就会抛出异常
                    # 即，输出错误: e.cmd.lock.NoExists
```

# 示例

```bash
# 获取一个锁
> lock @get lockA
{
   name: "lockA",
   hold: "2025-01-03 11:34:48.210",
   expi: "2025-01-03 11:34:51.210",
   owner: "bunya",
   hint: "USER_ASK"
}

# 获取多个锁
> lock @get lockA lockB lockC
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


