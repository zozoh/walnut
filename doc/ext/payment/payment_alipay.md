---
title:支付宝的支持
author:zozoh
tags:
- 系统
- 支付
---


# 支付宝的配置信息

配置信息存放在各个卖家域中，路径为 `~/.payment/alipay/${seller}/alipay.conf`

配置文件为一个 JSON 文件，内容为:

```
seller_email : "webmaster@wendal.cn" # 签约的支付宝账号
partner : "2088521239387536"         # 合作者身份PID
key : "xxxx"                         # 安全校验key
rsa_public : "xxxsxsssss"            # RSA公钥
rsa_private : "yyyyyyyyy"            # RSA密钥
```

# 支付单的补充信息

```
// 发送给支付宝服务器的订单请求数据，是一个 JSON
"alipay_send" : {..},

// 付款结果信息。 即支付宝异步方式传来的请求对象参数表
// 这个信息需要通过 JSON 形式提交给 WnPay3x.complete
// 在调用接口函数之前，会预先存储到这个字段
alipay_result : {..}
```

