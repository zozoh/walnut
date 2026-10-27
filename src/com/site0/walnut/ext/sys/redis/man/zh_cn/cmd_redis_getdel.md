# 过滤器简介

`@getdel` 读取值并**立即删除**（对应 Redis 6.2+ `GETDEL`），读和删是原子的。

典型用途：**一次性令牌**——取出来就用掉，别人再拿不到。

# 用法

```bash
redis @getdel 键
```

# 示例

```bash
# 一次性令牌：取出即失效
redis @set login:token:abc 'uid=100' -ex 3600
redis @getdel login:token:abc
# {"GETDEL:login:token:abc":"uid=100"}

# 再取就什么都没了
redis @getdel login:token:abc
# {"GETDEL:login:token:abc":null}
```
