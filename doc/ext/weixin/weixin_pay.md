---
title:微信支付
author:zozoh
tags:
- 系统
- 扩展
- 微信
- 支付
---

# 公众号支付

## 公众号的配置

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
C : 客户端
W : Walnut 服务
S : 微信服务

# 具体流程
1. C -> W : 发起 HTTP 请求  # /api/demo/pay
2. W -> W : 分析参数 order(订单号), client_ip(IP), openid(谁)
3. W -> S : 处理订单对象 @see 订单对象的元数据
4. W -> C : JSON 以便 JS-SDK 发起微信支付请求
5. C -> C : 唤起微信的支付输入界面
```

## .regapi 的写法

```
# 提交订单支付信息
weixin xxx order -submit ${http-qs-id} 
           -client_ip ${http-remote-host} 
           -openid ${http-qs-openid} 

# 处理订单支付的返回
weixin xxx order -result id:${id}
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

一个订单对象就是 WnObj，我们为其开辟了几个字段，专门跟踪一个订单进行支付的各个步骤的信息

```
id : "xxxx"       // 订单的ID通常就是 WnObj 对象的 ID

// 订单所属的用户的 openid。这个字段的名字可以换
// 通过 weixin xxx order -submit xxx -openid_key "abc" 来指定
// 默认为 "openid"
openid : "xxxx"  

# 订单状态信息
#  0.新建 - 过期不付款，将被清除
#  1.已付款 - 付款的订单会一直保存
#  2.已取消 - 订单被取消
#  3.已完成 - 订单已经履约完成
#  4.已退款 - 订单如果被取消，已付的款项是否已经退款
or_status : 0

pay_type : "weixin"  // 订单的付款方式为微信
pay_send : {..}      // 想微信服务器提交的订单信息

/*......................................................
根据当前订单的信息，创建一个微信支付那边的订单。这个内容是个 xml
具体格式，可以参见微信支付文档的 "API列表>统一下单" 一节

为了生成这个 xml，可以使用微信命令来帮助你生成

    echo $JSON | weixin xxx pay
    
你只需要提供下面的一个 JSON 字符串，格式如下 : 
{
    body : "XXX"          // 商品描述
    out_trade_no : "xxx"  // 商户系统内部的订单号 or.id _ SEQ
    total_fee : 888       // 订单总金额，单位为分
    openid : "xxx"        // 用户标识
    spbill_create_ip : "xxxxx"      // 网页支付提交用户端ip
    time_expire : "20091227091010"  // 过期时间           
}
!注: time_expire 是可选的，通常它来自订单对象的 'order_expi' 段。
如果你没有指定，通常会采用 wxconf.pay_time_expire 段的描述来自动填充过期

 weixin pay 命令会帮生成一个合法的 xml，这段 xml 需要被转成 JSON 保存在 "pay_send" 段里。 之后你将这个 xml 发送到微信服务器
    
    echo $XML | httpc POST https://api.mch.weixin.qq.com/pay/unifiedorder

服务器会返回一段 xml，你把这段 xml 保存到这个字段了，表示你已经提交过了
......................................................*/
pay_rexml : <...>       // 微信服务器的返回

// 对 pay_rexml 的内容做完签名校验后，转成 JSON 保存到这里
// 以便随时取用，其中的 "prepay_id" 是生成预付款对象所必须的
pay_remap : {..}

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

这个对象一旦生成，以后界面再次调用订单的付款，就可以复用了
......................................................*/
pay_jsobj : {..}

/*......................................................
付款结果信息。 是根据微信发送来的 xml 进行校验:
    
    weixin xxx payre id:${id}

校验后，命令输出一个 JSON 字符串 （具体请参看微信支付的文档）
{
    ...
    out_trade_no   // 商户系统内部的订单号 or.id _ SEQ
                   // 可以用这个来找回订单的ID
    ...
}
......................................................*/
pay_result : {..}

// 记录付款结果的绝对时间
pay_time : MS
```


# 附录

参考文章:

* [微信支付](https://pay.weixin.qq.com/wiki/doc/api/jsapi.php)
* [微信JS-SDK说明文档#发起一个微信支付请求](http://mp.weixin.qq.com/wiki/11/74ad127cc054f6b80759c40f77ec03db.html#.E5.8F.91.E8.B5.B7.E4.B8.80.E4.B8.AA.E5.BE.AE.E4.BF.A1.E6.94.AF.E4.BB.98.E8.AF.B7.E6.B1.82)

