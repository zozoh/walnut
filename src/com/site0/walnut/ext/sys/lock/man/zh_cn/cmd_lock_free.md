# 过滤器简介

`@free` 释放一个指定的锁
由于释放的时候需要你指定 `privateKey`，因此你在 `lock @try`
的时候必须记录这个 `privateKey` 到某个地方

# 用法

```bash
lock @free
  [lockName]         # 锁名
  [privateKey]       # 锁的Key
```

# 示例

```bash
# 释放一个锁
> lock @free lockA 64kfvwuk9m
# ....................... 释放成功
{
   name: "lockA",
   hold: "2025-01-03 11:36:24.746",
   expi: "2025-01-03 11:53:04.746",
   privateKey: "xzkwn9rumk",
   owner: "bunya",
   hint: "USER_ASK"
}
# ....................... 释放失败
{
   name: "lockA",
   hint: "Failed"
}
```


