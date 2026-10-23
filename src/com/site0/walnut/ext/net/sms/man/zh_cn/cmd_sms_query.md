# 过滤器简介

`@query` 查询短信发送状态。

时间参数可以是一个日期区间 `[from,to)`，或从现在起向前的一段时间：`20m`（20 分钟）、`1h`（1 小时）、`1d`（1 天）、`100s`（100 秒），默认 `10m`。

# 用法

```bash
sms @query [时间区间] [-r 手机号] [-pgsz N] [-pn N] [-l] [-cqn] [-provider 名] [-config 配置名] [-vars JSON]
  # 时间区间    # 如 30m、1h、1d；默认 10m
  -r         # 接收者手机号
  -pgsz      # 一页查询几条记录，上限由 provider 解释
  -pn        # 查询第几页
  -l         # 强制输出数组；否则只有一条记录时输出对象
  -cqn       # 输出 JSON 的格式化开关
  -provider  # 提供商，默认 Yunpian
  -config    # 配置文件名，默认 config_<provider>
  -vars      # 更多操作变量，如云片网的 'type:"all"'
```

# 示例

```bash
# 查询最近 10 分钟的发送状态
sms @query

# 查询 30 分钟内某个接收者的短信状态
sms @query 30m -r 13910223421

# 查询某接收者 1 小时内的全部短信（type 仅云片网支持）
sms @query 1h -r 13910223421 -vars 'type:"all"'
```
