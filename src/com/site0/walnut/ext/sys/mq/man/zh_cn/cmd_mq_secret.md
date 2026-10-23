# 过滤器简介

`@secret` 为当前用户生成队列密钥。

# 用法

```bash
mq @secret [-len 位数] [-Q]
  -len    # 生成多少位密钥，默认 50
  -Q      # 静默输出，否则显示密钥
```

# 示例

```bash
# 重新生成密钥
mq @secret

# 生成 80 位密钥
mq @secret -len 80
```
