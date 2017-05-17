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

# 支付宝的返回参数

```
 {
   "gmt_create": "2017-05-17 22:48:04",
   "buyer_email": "zozohtnt@gmail.com",
   "notify_time": "2017-05-17 22:48:12",
   "gmt_payment": "2017-05-17 22:48:12",
   "seller_email": "webmaster@wendal.cn",
   "quantity": "1",
   "subject": "测试支付单",
   "use_coupon": "N",
   "sign": "8124fae4855ba6c4422b418231b38388",
   "discount": "0.00",
   "body": "测试支付单",
   "buyer_id": "2088102930385612",
   "notify_id": "e1bf6556a84e69d95b9c28c8d03eb8dkpi",
   "notify_type": "trade_status_sync",
   "payment_type": "1",
   "out_trade_no": "54po3t1i0gi77qjunnr9las85j",
   "price": "0.01",
   "trade_status": "TRADE_SUCCESS",
   "total_fee": "0.01",
   "trade_no": "2017051721001004610296413990",
   "sign_type": "MD5",
   "seller_id": "2088521239387536",
   "is_total_fee_adjust": "N"
}
```

# dd

- [蚂蚁金服.即时到账](https://doc.open.alipay.com/doc2/detail?treeId=62&articleId=103566&docType=1)


