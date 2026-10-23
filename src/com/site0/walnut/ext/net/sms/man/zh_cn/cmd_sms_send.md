# 过滤器简介

`@send` 发送短信（默认处理器，可省略不写）。

消息正文来自参数（多个参数用空格连接）或管道；配合 `-t` 模板时，正文是填充模板的 JSON 上下文。

# 用法

```bash
sms @send -r 手机号 [消息...] [-t 模板] [-lang 语言] [-header 头部] [-provider 名] [-config 配置名] [-vars JSON]
  -r         # 接收者手机号，多个用半角逗号分隔
  -t         # 模板文件名（存放在 ~/.sms 下）；'i18n:xxx' 则在 ~/.sms/i18n/<lang>/ 下读取
  -lang      # 模板语言，默认取配置里的 lang，再没有则用 zh-cn
  -header    # 指定消息头部
  -provider  # 提供商，默认 Yunpian
  -config    # 配置文件名，默认 config_<provider>
  -vars      # 更多操作变量，根据 provider 的不同有不同的解释
```

# 示例

```bash
# 发送一条自定义消息
sms -r 13910223421 机器AABBCCDDEEFF流量异常500mb

# 正文从管道读
echo "磁盘已满" | sms -r 13910223421

# 发送模板短信
sms -r 13910223421 -t signup "code:8976"

# 发送 i18n 模板短信
sms -r 13910223421 -t i18n:signup "code:8976"
```
