---
title:支付宝支付
author:wendal
tags:
- 系统
- 扩展
- 支付宝
- 支付
---

# 支付宝商家信息

```
seller_email : "webmaster@wendal.cn" # 签约的支付宝账号
partner : "2088521239387536"         # 合作者身份PID
key : "xxxx"                         # 安全校验key
rsa_public : "xxxsxsssss"            # RSA公钥
rsa_private : "yyyyyyyyy"            # RSA密钥
```

## 发起支付的过程

```
B : 浏览器
C : 支付宝手机客户端
W : Walnut 服务
S : 支付宝服务

# 具体流程
1. B -> W : 发起 HTTP 请求  # /api/demo/alipay/pay
2. W -> W : 分析参数 order(订单号)
3. W -> S : 处理订单对象 @see 订单对象的元数据
4. W -> B : JSON, 内含重定向地址, @see 支付宝订单跳转数据
5. B -> C : 跳到支付宝地址,展示付款二维码
5. C -> C : 用户使用支付宝手机客户端扫码
```

## .regapi 的写法

```
# 提交订单支付信息
alipay xxx order -submit ${http-qs-id}

# 处理订单支付的返回
alipay xxx order -result id:${id}
```

## 支付成功后的处理

```
W : Walnut 服务
S : 支付宝服务

# 具体流程
1. S -> W : 发起 HTTP 请求  # /api/demo/aplypay/payresult
2. W -> W : 分析请求内容  # alipay xxx payre id:${id} -SUCCESS "
3. W -> W : 如果成功就标记状态 # @see 订单对象的元数据
```

## 订单对象的元数据

```
id : "xxxx"       // 订单的ID通常就是 WnObj 对象的 ID

# 订单状态信息
#  0.新建 - 临时建立的订单数据
#  1.已确认 - 用户确认了这个订单
#  2.已付款 - 付款的订单会一直保存
#  3.已取消 - 用户申请退款
#  4.已退款 - 退款成功，则标记取消
or_status : 0

pay_type : "alipay"  // 订单的付款方式为支付宝
```

## 订单对象的内容

```java
{
    "body" : "XXX",          // 商品描述
    "out_trade_no" : "xxx",  // 商户系统内部的订单号 ,64位字符串
    "total_fee" : 888,       // 订单总金额，单位为分,实际支付时会转为元
    "time_expire" : "20091227091010",  // 过期时间
    "req_send" : "...",      // 发送给支付宝服务器的订单请求数据
    "redirec" : "https://..."// 订单URL
    "pay_result" : "..."     // 回调参数
}
```

## 支付宝订单链接

```java
{
	"ok" : true,
	"url" : "https://..........."
}
```

## 附录

[即时到账](https://doc.open.alipay.com/doc2/detail?treeId=62&articleId=103566&docType=1)

