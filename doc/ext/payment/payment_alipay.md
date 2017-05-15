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
// TODO 这个字段还要吗？ 应该包括在 "alipay_send" 里了吧
// zfb 接口支持的订单过期时间
"alipay_expi" : "20091227091010",

// 发送给支付宝服务器的订单请求数据，是一个 JSON
// 这个字段将在真正的提交前生成
"alipay_send" : {..},

// 这个是支付宝即时到账重定向的结果，实现类会拿到重定向的 GET
// 参数表，立即存储进这个字段
alipay_restr : "xx=xx&xx=xx..."

// 对 alipay_restr 的内容做完校验后，转成 JSON 保存到这里
// 以便随时取用
alipay_remap : {..}

// 付款结果信息。 即支付宝异步方式传来的请求对象参数表
// 这个信息需要通过 JSON 形式提交给 WnPay3x.complete
// 在调用接口函数之前，会预先存储到这个字段
alipay_result : {..}


```

