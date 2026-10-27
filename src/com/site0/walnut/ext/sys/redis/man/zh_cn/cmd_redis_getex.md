# 过滤器简介

`@getex` 读取值并**更新它的过期时间**（对应 Redis 6.2+ `GETEX`）。

支持 `-ex <秒>` / `-px <毫秒>` / `-exAt <秒时间戳>` / `-pxAt <毫秒时间戳>` 四种时间参数。

> ⚠️ 必须带至少一个时间参数，否则过滤器什么都不做（不读值也不报错）。

# 用法

```bash
redis @getex 键 [-ex 秒 | -px 毫秒 | -exAt 秒时间戳 | -pxAt 毫秒时间戳]
```

# 示例

```bash
# 读会话并顺手续期 30 分钟
redis @getex session:abc -ex 1800

# 读取并清除过期时间（保持持久）
# 目前没有 -persist 参数，可用 @get + @persist 组合替代
```
