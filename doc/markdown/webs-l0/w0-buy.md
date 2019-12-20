---
title: Web购物模型
author: zozohtnt@gmail.com
tags:
- WWW
- 购物
---

--------------------------------------
# 动机：为什么要有Web购物模型

产品/订单/优惠券太烦，做成一个统一的比较爽

--------------------------------------
# 计划应用场景

- 网页端商城
- 微信小程序
- 移动端APP

--------------------------------------
# 设计思路与边界

实际上是一个[支付单][f1-pay]的封装

--------------------------------------
# 数据结构描述

支付相关的三个数据结构为：

- 订单
- 商品
- 优惠券

这三个数据结构都是`ThingSet`，关联在站点元数据据里。
请参看[基础账户模型][c0-acc]的`站点元数据`一节获取更多详情。

--------------------------------------
## 订单的数据结构

```js
{
  id: ID,        // 【自】订单唯一ID
  //-----------------------------
  // 【必】商品列表
  products: [{
    id : ID,      // 【必】商品 ID 
    title :"xxx", // 【查】商品名
    price :29.9,  // 【查】商品价格（元）
    amount:3      // 【必】购买数量
  }],
  //-----------------------------
  // 【选】优惠券列表
  coupons: [{
    id : ID,        // 【必】优惠券 ID
    title : "xxx",  // 【查】优惠券名
    type  : 1,      // 【查】类型，1:代金券，2:折扣券
    value : 20,     // 【查】代金券：元；折扣券0.0~1.0浮点
    thres : 200,    // 【查】订单满足这个金额方可应用，0表示不限
    expi  : AMS     // 【查】过期时间
  }]
  //-----------------------------
  // 【选】摘要信息
  // 默认为支付商户的名称
  title: "xxx"
  // 卖家名称（支付配置目录名）
  // 【查】根据pay_tp读站点设置
  seller: "theName"
  //-----------------------------
  // 买家信息
  accounts : ID   // 【自】买家用户库ID
  buyer_id : ID   // 【自】买家ID，通常从当前会话中获取
  //-----------------------------
  // 支付信息
  price  : 321.56       // 【查】订单金额
  fee    : 319.12       // 【查】优惠后金额
  pay_tp : "wx.qrcode"  // 【必】支付类型
  pay_id : ID           // 【自】支付单ID 
  //-----------------------------
  // 状态信息【自】
  //  - NW : 新建
  //  - WT : 等待支付（已经关联了支付单）
  //  - OK : 买家已经支付成功
  //  - FA : 买家已经支付失败
  //  - SD : 卖家已发货
  //  - DN : 订单完成
  status : "NW"
  //-----------------------------
  // 时间戳
  ct : AMS       // 【自】创建时间
  lm : AMS       // 【自】最后变动时间
  wt_at : AMS    // 【自】创建支付单时间
  ok_at : AMS    // 【自】买家支付成功时间
  fa_at : AMS    // 【自】买家支付失败时间
  sp_at : AMS    // 【自】卖家已发货时间
  dn_at : AMS    // 【自】订单完成时间
}
```

- `【查】`: 表示该字段的值是自行查询获得
- `【自】`: 表示该字段的值是自动生成
- `【必】`: 表示该字段是从客户端请求中读取的，所以不能缺少
- `【选】`: 表示该字段是从客户端请求中读取的，如果不指定则会有默认值

--------------------------------------
## 商品的数据结构

```bash
id    : ID       # 产品ID
title : "xxx"    # 产品名称
price : 78.12    # 价格（元）
```

> 当然，商品还可以有各种各样的其他字段，但是本模型仅关心上面这几个字段

--------------------------------------
## 优惠券的数据结构

```bash
id    : ID       # 优惠券ID
title : "xxx"    # 优惠券名称
# 类型，1:代金券，2:折扣券
type  : 1
# 代金券：元；折扣券0.0~1.0浮点
value : 40
# 订单满足这个金额方可应用，0表示不限
thres : 0
# 过期时间（绝对毫秒数）
cpn_expi : AMS
```

> 当然，优惠券还可以有各种各样的其他字段，但是本模型仅关心上面这几个字段

--------------------------------------
# 使用方式

客户端通过 `Ajax` 方式调用接口

--------------------------------------
## `/pay/buy`创建订单和支付单

### 请求头

```bash
HTTP POST /api/${YourDomain}/pay/buy
#---------------------------------
# Query String
ticket : "34t6..8aq1"     # 【必】登录会话的票据
```

### 请求体:JSON

```js
{
  // 商品列表
  "products": [{
    "id": "78u..sg9",  // 商品 ID
    "amount": 3        // 购买数量，默认为 1
  }],
  // 支付类型
  //  - wx.qrcode  : 微信主动扫二维码付款
  //  - wx.jsapi   : 微信公众号内支付
  //  - wx.scan    : 微信被物理码枪扫付款码支付
  //  - zfb.qrcode : 支付宝主动扫二维码付款
  //  - zfb.scan   : 支付宝被物理码枪扫付款码支付
  "pay_tp" : "wx.qrcode"
}
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    /*请参考上面的订单数据结构*/
  }
}
```

- `【查】`: 表示该字段的值是自行查询获得
- `【自】`: 表示该字段的值是自动生成
- 未标识`【查】`和`【自】`的字段，是从客户端请求中读取的

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "www.order.nil.products",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.www.order.nil.products` : 空订单
- `e.www.order.nil.accounts` : 没有设置账户库
- `e.www.order.nil.buyer_id` : 找不到买主账户信息
- `e.www.order.nil.pay_tp` : 未指定支付类型
- `e.www.order.invalid.pay_tp` : 错误的支付类型


### 初始化脚本

```bash
# API : 支付:创建订单和支付单
@FILE .regapi/api/pay/buy
%COPY:
cat id:${id} | www buy ~/www -cqn -ajax -ticket "${http-qs-ticket}"
%END%
```

--------------------------------------
## `/pay/check_order`检查订单支付状态

### 请求头

```bash
HTTP GET /api/${YourDomain}/pay/check_order
#---------------------------------
# Query String
id : "34t6..8aq1"     # 【必】订单ID
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    /*请参考上面的订单数据结构*/
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.io.obj.noexists",
  msg : "对象不存在"
}
```

### 初始化脚本

```bash
# API : 支付:检查订单支付状态
@FILE .regapi/api/pay/check_order
%COPY:
www paycheck "${http-qs-id}" -cqn
%END%
```

--------------------------------------
## 支付宝回调

支付成功后，支付宝服务器会主动调用这个接口。
这个接口地址是在创建支付单时传给支付宝的，详情请参考:

- [通用支付模型][f1-pay]
- [支付宝支付接口整合][f0-zfb]

### 请求头

```bash
HTTP GET /api/${YourDomain}/pay/check_order
#---------------------------------
# Query String
out_trade_no : "34t6..8aq1"     # 【必】订单ID
```

### 初始化脚本

```bash
# API : 支付:支付宝回调
@FILE .regapi/api/pay/re_zfb
%COPY:
httpparam -in id:${id} | pay re -idkey out_trade_no -s
%END%
```

--------------------------------------
## 微信回调

支付成功后，微信服务器会主动调用这个接口。
这个接口地址是在创建支付单时传给微信的，详情请参考:

- [通用支付模型][f1-pay]
- [微信支付整合][f0-wxp]

### 请求头

```bash
HTTP GET /api/${YourDomain}/pay/check_order
#---------------------------------
# Query String
out_trade_no : "34t6..8aq1"     # 【必】订单ID
```

### 初始化脚本

```bash
# API : 支付:微信回调
@FILE .regapi/api/pay/re_wx
%COPY:
xml tojson id:${id} | pay re -idkey out_trade_no -s
%END%
```

[c0-acc]: ../core-l0/c0-account-basic.md
[f1-pay]: ../func-l1/f1-payment.md
[f0-wxp]: ../func-l0/f0-weixin-payment.md
[f0-zfb]: ../func-l0/f0-alipay.md