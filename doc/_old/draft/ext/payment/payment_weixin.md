---
title:微信支付的支持
author:zozoh
tags:
- 系统
- 支付
---


# 微信支付的配置信息

配置信息存放在各个卖家域中，路径为 `~/.payment/wxpay/${mch_id}/wxpay.conf`

配置文件为一个 JSON 文件，内容为:

```
pay_mch_id : "xxxx"        # 商户号
pay_key    : "xxxx"        # 支付秘钥
pay_time_expire : 10       # 订单过期时间(分钟)，默认10
pay_notify_url : "http:xx" # 支付成功的回调
```

同时在公众号的配置里面做关联

```
appID      : 'wx45ac..',   # 公众号 API 的 ID 号
appsecret  : '39b0..',     # 公众号 API 秘钥
payment    : "xxx",        # 商户配置目录名（通常为商户号）
```

# 支付单的补充信息

```
// 向微信服务器提交的订单信息，这个信息为 JSON 
// 这个字段将在真正的提交前生成
wxpay_send : {..}

// 微信服务器的返回，如果有个信息，表示微信服务器有返回了
wxpay_rexml : <...>

// 对 wxpay_rexml 的内容做完签名校验后，转成 JSON 保存到这里
// 以便随时取用，其中的 "prepay_id" 是生成预付款对象所必须的
wxpay_remap : {..}

/*......................................................
预付款对象，JS-SDK 通过这个对象可以唤起支付界面
    
wx.chooseWXPay({
    // 支付签名时间戳，注意微信jssdk中的所有使用timestamp字段均为小写。
    // 但最新版的支付后台生成签名使用的timeStamp字段名需大写其中的S字符
    timestamp  : "1498.."
    nonceStr   : "xxx"      // 支付签名随机串，不长于 32 位
    
    // 统一支付接口返回的prepay_id参数值，提交格式如：prepay_id=***）
    package    : "prepay_id=xxxx" 
    
    signType   : "MD5"   // 签名方式，默认为'SHA1'，使用新版支付需传入'MD5'
    paySign    : "xxx"   // 支付签名
});

这个对象一旦生成，以后微信公众号界面再次调用订单的付款，就可以复用了
......................................................*/
wxpay_jsobj : {..}

// 付款结果信息。 即微信通过异步回调方式传来的 XML
// 这个信息需要通过 JSON 形式提交给 WnPay3x.complete
// 在调用接口函数之前，会预先存储到这个字段
wxpay_result : {..}

// 记录付款结果的绝对时间
wxpay_time : MS
```

# 参考资料

- [微信支付](https://pay.weixin.qq.com/wiki/doc/api/index.html)
- [微信JS-SDK说明文档#发起一个微信支付请求](http://mp.weixin.qq.com/wiki/11/74ad127cc054f6b80759c40f77ec03db.html#.E5.8F.91.E8.B5.B7.E4.B8.80.E4.B8.AA.E5.BE.AE.E4.BF.A1.E6.94.AF.E4.BB.98.E8.AF.B7.E6.B1.82)


