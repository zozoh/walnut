# 过滤器简介

`@pay` 生成支付宝即时到账的支付请求链接。

支付单（`out_trade_no`、`subject`、`total_fee`、`body` 等）来自参数、文件或管道；
`notify_url`/`return_url` 依次取命令参数、配置里的默认值。

# 用法

```bash
alipay 别名 @pay [支付单JSON或文件] [-json] [-cqn]
              # 支付单     # JSON 字符串或文件路径；不给则从管道读
              # -json      # 以 JSON（{url:...}）输出，默认输出纯链接
              # -pay_notify_url 支付结果异步通知地址，缺省取配置
              # -return_url     支付完成跳转地址，缺省取配置
              # -total_fee      金额，默认 0.01
              # -body           商品描述
              # -c         # JSON 输出时紧凑显示
              # -q         # JSON 输出时键名加双引号
              # -n         # JSON 输出时不忽略 null
```

# 示例

```bash
# 支付单来自管道
echo '{out_trade_no:"T20261023",subject:"订单",total_fee:"0.01"}' | alipay myshop @pay

# 支付单来自文件
alipay myshop @pay ~/payment/order.json

# 以 JSON 输出链接
echo '{subject:"订单"}' | alipay myshop @pay -json
```
