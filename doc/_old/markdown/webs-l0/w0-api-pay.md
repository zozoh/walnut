---
title: 接口·订单及支付
author: zozohtnt@gmail.com
key: w0-api-pay
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
  // 商品列表
  "products": [{
    "id": "78u..sg9",      // 商品 ID
    "amount": 3            // 购买数量，默认为 1
  }],

  //【选】需要物流信息的订单需要填写
  "addr_ship_country" : "CN",         // 国家编码，默认 CN
  "addr_user_code" : "110108017000",  // 收货地址（12位地址编码）

  //【选】优惠券列表
  "coupons": [{"id":ID}]
}
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    //------------------------------------
    // 产品部分详情
    "products": [{
      "id": "u5...o8",      // 产品ID
      "title": "xxx",       // 产品标题
      "price": 90.0,        // 计算后产品单价
      "weight": 1.2,        // 用作计算运费的重量（公斤）
      "freight": 0.0,       // 固定运费
      "amount": 50,         // 购买数量
      "pcount": 53,         // 同一价格体系下的商品购买数量
      "subtotal": 4500.0,   // 小计 (price x amount)
      "pro_id": "3h..4h",   // 【选】价格规则
      "price_by": "rule002" // 【选】价格规则下的价格体系名称
    }],
    //------------------------------------
    // 运费部分详情
    "freightDetail" : {
      // 根据输入分析的重量
      "weight" : {
        "first"      : 1,       // 首重（公斤）
        "additional" : 21.5,    // 续重（公斤）
      },
      // 找到的匹配规则
      "rule" : {
        "title"        : "北京至全国",  // 标题（助记）
        "ship_code"    : "110000",     // 发货地（六位地址编码）
        "target_code"  : "000000",     // 目的地址（六位地址编码）
        "first"        : 10.5,         // 首重价格（元）
        "additional"   : 0.8           // 续重价格（元）
      },
      "first"      : 10.5,       // 首重价格（元）
      "additional" : 32,         // 续重总价格（元）
      "total"      : 42.5        // 总运费
    },
    //------------------------------------
    // 价格详情
    //------------------------------------------
    // 订单总金额 = total + freight
    // 支付总金额 = total + freight - discount
    //------------------------------------------
    "total"    : 245.12,    // 商品总金额
    "freight"  : 23.5,      // 总运费（包括计算运费以及固定运费）
    "discount" : 16.8,      // 优惠总金额
    "price"    : 321.56,    // 订单总金额（包括运费）
    "fee"      : 319.12,    // 优惠后金额，用来实际支付
    "currency" : "RMB"      // 货币单位，默认 RMB
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

  //【选】需要物流信息的订单需要填写
  addr_user_country : "CN",             // 国家编码，默认 CN
  addr_user_code    : "110108017000",   // 12位地址编码
  addr_user_door    : "xxx xx A-302",   // 详细到门牌的地址
  user_name  : "xxx"      // 联系人姓名
  user_phone : "139.."    // 联系人手机
  user_email : "zz@z.com" // 联系人邮箱

  // 下面的是地址的冗余信息，通常可以通过 addr_user_code 查表得到
  addr_user_province : "Beijing"  // 省/直辖市
  addr_user_city     : "BeiJing"  // 城市
  addr_user_area     : "BeiJing"  // 区县
  addr_user_street   : "xxx"      // 乡镇/街道

  // 【选】发票信息
  "invoice_id" : ID

  //【选】优惠券列表
  "coupons": [{"id":ID}]
  
  //【选】备注信息
  "note" : "xxxx"
}
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    /*请参考订单数据结构 w1-shop.md#order*/
  }
}
```

> @see [商城模型][w1-shop]

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

--------------------------------------
## `/pay/pay`根据订单创建支付单

### 请求头

```bash
HTTP GET /api/pay/pay
#---------------------------------
# Query String
ticket : "34t6..8aq1"     #【必】登录会话的票据
id     : "69a..e1q"       #【必】订单 ID
#---------------------------------------------
#【选】重新设置订单的支付类型
#  - wx.qrcode  : 微信主动扫二维码付款
#  - wx.jsapi   : 微信公众号内支付
#  - wx.scan    : 微信被物理码枪扫付款码支付
#  - zfb.qrcode : 支付宝主动扫二维码付款
#  - zfb.scan   : 支付宝被物理码枪扫付款码支付
# 如果支付类型改变，会强制生成新支付单
pt : "wx.qrcode"
#---------------------------------------------
#【选】指定为true，强制生成新支付单
# 默认的，如果生成过支付单，就不再生成新支付单了
force  : false
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    /*请参考订单数据结构 w1-shop.md#order*/
  }
}
```

> @see [商城模型][w1-shop]

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
- [微信支付整合][f0-wx-pay]

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

[f0-wx-pay]: ../func-l0/f0-weixin-payment.md
[f0-zfb]: ../func-l0/f0-alipay.md
[f1-pay]: ../func-l1/f1-payment.md
[w1-shop]: ../webs-l1/w1-shop.md
