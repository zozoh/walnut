# 过滤器简介

`@payre` 接收支付宝的异步支付通知：从管道读取回调参数（JSON），校验签名后输出 `success` 或 `fail`。

# 用法

```bash
alipay 别名 @payre < 回调参数JSON
```

# 示例

```bash
# 校验回调并输出结果
cat ~/payment/notify.json | alipay myshop @payre
```
