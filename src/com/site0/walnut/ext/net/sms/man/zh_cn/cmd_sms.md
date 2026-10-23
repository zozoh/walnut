# 命令简介

`sms` 用来处理和短信相关的操作：发送短信、查询发送状态。

默认读取当前用户下的发送配置 `$HOME/.sms/config_<provider>`，以云片网为例：

```bash
{
    apikey  : "abc1234567890",  # 提供商的 apikey
    header  : "xxx",            # 强制添加的头部，如 【文尔科技】
    lang    : "zh-cn",          # 默认模板语言
    timeout : 5000              # 请求超时（毫秒），默认 5000
}
```

其中 `lang`、`timeout` 为所有 provider 均支持的字段，其他字段根据 provider 实现不同各不相同。

# 用法

```bash
sms [@send|@query] [-provider 名] [-config 配置名] [-vars JSON] [-cqn]
  -provider    # 提供商，默认 Yunpian
  -config      # 配置文件名，默认 config_<provider>
  -vars        # 更多操作变量，根据 provider 的不同有不同的解释
  -cqn         # 输出 JSON 的格式化开关
```

支持的处理器：

```bash
@send     # 发送短信（默认处理器，可省略）
@query    # 查询短信发送状态
```

# 示例

```bash
# 发送一条短信
sms -r 13910223421 机器AABBCCDDEEFF流量异常500mb

# 发送模板短信
sms -r 13910223421 -t signup "code:8976"

# 查询最近 10 分钟的发送状态
sms @query
```
