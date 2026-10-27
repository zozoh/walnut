# 过滤器简介

`@set` 设置字符串键的值（对应 Redis `SET`），返回 `"OK"`。

支持过期参数：

- `-ex <秒>` — 多少秒后过期
- `-px <毫秒>` — 多少毫秒后过期
- `-exAt <秒时间戳>` — 到指定 Unix 秒过期
- `-pxAt <毫秒时间戳>` — 到指定 Unix 毫秒过期

> ⚠️ 与官方 `SET` 不同，目前**不支持** `NX/XX` 语义
> （不存在才写 / 存在才写）。「不存在才写」请用 `@setnx`。

# 用法

```bash
redis @set 键 值 [-ex 秒 | -px 毫秒 | -exAt 秒时间戳 | -pxAt 毫秒时间戳]
```

# 示例

```bash
# 基本写入
redis @set mykey 100

# 写入并 60 秒后过期（验证码场景）
redis @set sms:code:13900001111 8845 -ex 300 @ttl sms:code:13900001111

# 管道：写入后立即读回
redis @set site:theme dark @get site:theme
# {"SET:site:theme":"OK","GET:site:theme":"dark"}
```
