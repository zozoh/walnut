---
title: 接口·订单及支付
author: zozohtnt@gmail.com
---

--------------------------------------
# 接口概览

  URL            |Method| Date |Auth | Description
-----------------|------|------|-----|----------
`pay/price`      |`POST`|`json`|`Yes`| 计算订单价格（包括运费）
`pay/buy`        |`POST`|`json`|`Yes`| 创建订单和支付单
`pay/check`      |`GET` |`json`|`Yes`| 检查支付结果
`pay/pay`        |`GET` |`json`|`Yes`| 根据订单创建支付单
`pay/re_paypal`  |`GET` |`json`|`---`| 处理 paypal 回调
`pay/re_wx`      |`GET` |`json`|`---`| 处理微信支付回调
`pay/re_zfb`     |`GET` |`json`|`---`| 处理支付宝回调

--------------------------------------
# 接口详情

> 客户端通过 `Ajax` 方式调用接口

--------------------------------------
## `/pay/price`计算价格（包括运费）

### 请求头

```bash
HTTP POST /api/pay/price
#---------------------------------
# Query String
ticket : "34t6..8aq1"     # 【必】登录会话的票据
```

### 请求体:JSON

```js
{
  // 订单类型
  //  - A: 普通订单，会执行下单->支付->发货->完成这一标准步骤
  //  - Q: 简单订单，适用于虚拟物品，支付成功就直接完成
  // 默认"A"，需要提供收货地址
  "tp": "A",

  // 商品列表
  "products": [{
    "id": "78u..sg9",      // 商品 ID
    "amount": 3            // 购买数量，默认为 1
  }],

  //【选】收货地址，A订单则为必填项
  // 需要物流信息的订单需要填写
  "addr_ship" : ID,

  //【选】发票对象的 ID
  "invoice" : ID,

  //【选】优惠券ID
  "coupon": ID
}
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    //------------------------------------------
    // # 订单总金额 = total + freight
    // # 支付总金额 = total + freight - discount
    //------------------------------------------
    total: 245.12,      // 商品总金额
    freight: 23.5,      // 运费
    discount: 16.8,     // 优惠金额
    price : 321.56,     // 订单总金额（包括运费）
    fee   : 319.12      // 优惠后金额，用来实际支付
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.www.order.nil.products",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.www.order.nil.products` : 空订单
- `e.www.order.nil.addr_ship` : 未指定收货地址


### 初始化脚本

```bash
# API : 支付:创建订单和支付单
@FILE .regapi/api/pay/price
%COPY:
cat id:${id} | www price ~/www -cqn -ajax -ticket "${http-qs-ticket}"
%END%
```

--------------------------------------
## `/pay/buy`创建订单和支付单

### 请求头

```bash
HTTP POST /api/pay/buy
#---------------------------------
# Query String
ticket : "34t6..8aq1"     # 【必】登录会话的票据
```

### 请求体:JSON

```js
{
  // 订单标题，默认为 seller 的名称
  "title": "xxx",

  // 订单类型
  //  - A: 普通订单，会执行下单->支付->发货->完成这一标准步骤
  //  - Q: 简单订单，适用于虚拟物品，支付成功就直接完成
  // 默认"A"
  "tp": "A",

  // 商品列表
  "products": [{
    "id": "78u..sg9",      // 商品 ID
    "amount": 3            // 购买数量，默认为 1
  }],

  // 支付类型
  //  - wx.qrcode  : 微信主动扫二维码付款
  //  - wx.jsapi   : 微信公众号内支付
  //  - wx.scan    : 微信被物理码枪扫付款码支付
  //  - zfb.qrcode : 支付宝主动扫二维码付款
  //  - zfb.scan   : 支付宝被物理码枪扫付款码支付
  "pay_tp" : "wx.qrcode"

  // 【选】收货地址
  // 需要物流信息的订单需要填写
  "addr_ship" : ID

  // 【选】发票信息
  "invoice_id" : ID

  //【选】优惠券ID
  "coupon": ID
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
  errCode : "e.www.order.nil.products",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.www.order.nil.products` : 空订单
- `e.www.order.nil.accounts` : 没有设置账户库
- `e.www.order.nil.buyer_id` : 找不到买主账户信息
- `e.www.order.nil.pay_tp` : 未指定支付类型
- `e.www.order.nil.addr_ship` : 未指定收货地址
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
## `/pay/check`检查订单支付状态

### 请求头

```bash
HTTP GET /api/pay/check_order
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
HTTP GET /api/pay/check_order
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
HTTP GET /api/pay/check_order
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