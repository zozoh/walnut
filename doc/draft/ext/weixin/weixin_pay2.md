---
title:微信支付
author:zozoh
tags:
- 系统
- 扩展
- 微信
- 支付
---

# 扫码支付

## 商户的配置

```
appID      : 'wx45ac..',   # 公众号 API 的 ID 号
appsecret  : '39b0..',     # 公众号 API 秘钥
pay_mch_id : "xxxx"        # 商户号
pay_key    : "xxxx"        # 支付秘钥
pay_time_expire : 10       # 订单过期时间(分钟)，默认10
pay_notify_url : "http:xx" # 支付成功的回调
```

## 发起支付的过程

```
B : 浏览器
C : 微信客户端
W : Walnut 服务
S : 微信服务

# 具体流程
1. B -> W : 发起 HTTP 请求  # /api/demo/pay2
2. W -> W : 分析参数 order(订单号), client_ip(IP), openid(谁)
3. W -> S : 处理订单对象 @see 订单对象的元数据
4. W -> B : JSON 内含二维码的文本内容
5. B -> C : 用户使用微信扫一扫进行扫码, 唤起微信的支付输入界面
```

## .regapi 的写法

```
# 提交订单支付信息
weixin xxx order -submit ${http-qs-id} 
           -client_ip ${http-remote-host} 
           -openid ${http-qs-openid}
           -json
           -type NATIVE
```


## 支付成功后的处理

```
C : 客户端
W : Walnut 服务
S : 微信服务

# 具体流程
1. S -> W : 发起 HTTP 请求  # /api/demo/payresult
2. W -> W : 分析请求内容  # weixin xxx payre id:${id} -SUCCESS "
3. W ?> W : 如果成功就标记状态 # @see 订单对象的元数据
```

## 订单对象的元数据




# 附录

参考文章:

* [微信支付#统一下单API](https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=9_1)
* [微信支付#扫码支付_模式二](https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=6_5)

