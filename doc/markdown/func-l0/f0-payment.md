---
title: 通用支付模型
author: zozohtnt@gmail.com
key: f1-pay
---

--------------------------------------
# 动机：为什么要有通用支付模型

众所周知，支付宝，微信，美团，银联，PayPal 等等等等，各自有自己的支付接口。
同时，无论怎样的接口，它们都有如下共性：

1. 接口基于 HTTP 
2. 两段式请求，先发起支付，然后等待回调

每个接口的参数细节，返回信息的形式均有不同。这对于我们的业务逻辑造成了很大的困扰。
我们不禁畅想，如果能有一个比较通用的支付模型，业务层仅仅针对这个模型变成，具体的业务
细节模型来处理，那么支付相关的业务的实现将是多么轻松惬意的事情啊。

为此，我们很自然的希望有一个如下结构的实现：

```bash
+------+ +------+ +------+ +------+ +------+
| BizA | | BizB | | BizC | | BizD | | BizE |
+------+ +------+ +------+ +------+ +------+              
   |         |        |        |        |                  
+------------------------------------------+
|   WGPM (Walnut General Payment Model)    |
+------------------------------------------+
    V            V           V         V
    V            V           V         V
+-------+   +--------+   +--------+    V
| WxPay |   | AliPay |   | PayPal |  [...]
+-------+   +--------+   +--------+
```

> 本文将详细说明这个通用支付模型`WGPM`的主要细节。

--------------------------------------
# 计划应用场景

- 商城等支付类型的网站应用
- Walnut的用户App内购
- 对于微信小程序，获取移动App的后台接口支付支持

--------------------------------------
# 设计思路与边界

首先，`WGPM` 将只专注与支付，用户，商品及其订单应该是别的模型负责的事情。它需要保证的是支付信息以及支付过程的完备记录。同时为了考虑安全性，所有支付的记录将由系统管理员账户（`root`）所有。

为了方便各个域管理，提供了 `pay` 命令以便各个域使用。

为了更方便整合，提供了 `org.nutz.walnut.ext.payment.WnPayment` 来封装全部的业务逻辑。这样再其他服务类，或者使用者在`类库开发模式`下，可以很方便的自己通过 Java 来编写支付相关的扩展。

--------------------------------------
# 数据结构描述

支付模型的核心，是一个被称为`支付单`的数据结构，它是存放在 `/var/payment/` 目录下的文件。文件的元数据记录了支付的相关信息以及历史，文件的内容记录了支付成功后的回调（一个命令脚本模板）

> 一个支付单的元数据看起来应该是这样的：
```bash
#-----------------------------------------
# 标识
id : ID            # 一个支付单的标识
nm : ID            # 与 ID 相同
tp : "wn_payment"  # 支付单的文件类型，固定为 wn_payment
#-----------------------------------------
# 支付的描述，通常它是不可更改的
brief : "xxx"      # 支付单简要描述
price : 2300       # 显示的销售价格（分）
fee   : 2300       # 实际支付的金额（分）
cur   : "RMB"      # 货币，默认是 RMB
#-----------------------------------------
# 支付单状态，可选值为：
#  - NEW  : 新创立的支付单(默认)
#  - OK   : 已经成功支付完成
#  - FAIL : 已经支付失败，本支付单将不在可用
#  - WAIT ： 等待第三方平台返回实际支付的结果
st : "NEW"

# 当向第三方平台提交后，平台会返回什么值
#  - LINK   : 平台返回的是一个支付网页的链接，譬如支付宝会返回二维码页面
#  - QRCODE : 支付二维码的内容（文本），需要界面层渲染出这个二维码
#  - JSON   : 一个JSON对象，譬如微信公众号平台网页支付的初始化对象
#  - TEXT   : 文本信息（这个我忘记了要干什么，好像暂未遇到实际场景，可能支付宝要用）
re_tp : "LINK"

# 平台返回的具体内容
re_obj : {
  # ... 这里根据不同的平台会不一样咯
}
#-----------------------------------------
# 交易类型决定了系统将具体采用哪个平台的支付流程
# 这个是在 pay send 的时候决定的
#  - wx.qrcode  : 微信主动扫二维码付款
#  - wx.jsapi   : 微信公众号内支付
#  - wx.scan    : 微信被物理码枪扫付款码支付
#  - zfb.qrcode : 支付宝主动扫二维码付款
#  - zfb.scan   : 支付宝被物理码枪扫付款码支付
pay_tp   : "wx.qrcode"

# 付款的目标商户
pay_target : "xxx"
#-----------------------------------------
# 买家信息
# 买家可能是一个 Walnut 用户或某个卖家域的用户
#  - "walnut" : 表示 walnut 用户
#  - ID(a..t) : 表示卖家域的某个账户库的ID
buyer_tp : ID | "walnut"
buyer_id : "xxx"   # 买家ID
buyer_nm : "xxx"   # 【冗余】买家名称或者其他有助于人类记忆的标识

# 卖家信息
# 卖家必须是某个 Walnut 的工作域
seller_id  : ID      # 卖家信息（域主ID）
seller_nm  : "xxx"   # 卖家信息（域名，即域主登录名）

#-----------------------------------------
# 时间戳
ct : AMS         # 支付单创建时间
lm : AMS         # 支付单最后修改时间

# 下面两个时间戳默认为 0 
send_at  : AMS   # 向第三方平台发起支付请求的时间
close_at : AMS   # 支付单得到第三方平台回调，从而关闭的时间

#-----------------------------------------
# 下面的是支付历史追踪信息，根据不同的支付类型，会有不同的元数据
# 这个具体请看对应平台的支付文档
# ...

#-----------------------------------------
# 自由的元数据
# 可以在 create 的时候追加更多的元数据譬如商品列表，优惠券等
# 以便后续可以更好的追踪统计支付信息, 下面推荐一些字段
or_id : ID          # 订单 ID
```

--------------------------------------
# 使用方式

--------------------------------------
# 相关知识点

- [微信支付整合][f0-wxp]
- [支付宝支付接口整合][f0-zfb]

[c0-pvg]: ../core-l0/c0-pvg-basic.md
[c2-pvg]: ../core-l2/c2-pvg-more.md
[f0-wxp]: ../func-l0/f0-weixin-payment.md
[f0-zfb]: ../func-l0/f0-alipay.md
[f1-pay]: ../func-l1/f1-payment.md