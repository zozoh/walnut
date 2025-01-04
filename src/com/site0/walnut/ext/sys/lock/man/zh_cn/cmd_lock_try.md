# 过滤器简介

`@try` 尝试加锁

# 用法

```bash
lock @try
  [lockName]        # 锁名
  [-du 3000]        # 锁的有效期，单位毫秒，默认 3000 毫秒
  [-hint xxxx]      # 【选】一段文字的描述，描述锁试用的场景，便于排查死锁
                    # 默认文字为 'USER_ASK'
  [-fail {...}]     # 一段锁对象json，当加锁失败后，默认输出 null
                    # 这里可以重新定义，以便生成一个假的锁对象
  [-block]          # 阻塞模式，这个模式会尝试重复加锁，直到超过最大重试次数
  [-retry  3]       # 阻塞模式下的重试次数，为了防止死锁，这个数值如果不填写
                    # 默认为 3
  [-interval 3000]  # 阻塞模式下的重试的间隔，单位毫秒，
                    # 默认为锁有效期(du)
```

# 示例

```bash
# 创建一个锁, 默认3秒过期
> lock @try MyLock;
{
   name: "MyLock",
   hold: "2024-12-30 01:58:29.273",
   expi: "2024-12-30 01:58:39.273",
   privateKey: "i4sf39w56v",
   owner: "demo",
   hint: "USER_ASK"
}

# 创建一个锁, 20秒过期，如果失败，则输出一个名为 FAIL 的锁
> lock @try MyLock -du 20000 -fail '{name:"FAIL"}'
# ....................... 成功的返回
{
   name: "MyLock",
   hold: "2024-12-30 02:00:04.848",
   expi: "2024-12-30 02:00:24.848",
   privateKey: "8q4qf5pycb",
   owner: "demo",
   hint: "USER_ASK"
}
# ....................... 加锁失败
{
   name: "FAIL"
}
```


