命令简介
======= 

`www buy` 用来生购买商品的订单

根据传入的 `-site id:xxx` 可获得站点信息。
本次操作需要站点主目录元数据提供如下映射信息

```js
accounts  : "~/accounts",   // 【必】用户库路径
orders    : "~/orders",     // 【必】订单库路径
products  : "~/products",   // 【必】商品库路径
coupons   : "~/coupons",    // 优惠券库路径
// 商户映射表；根据支付类型前缀来决定
sellers : {
    // 微信配置目录名称，该目录位于 ~/.weixin/ 目录下
    // 微信登录，公众号消息推送，微信支付等功能均在这个目录下配置
    // 当支付类型为 `wx.` 时需要这个配置项目
    wx : "theName"
    
    // 支付宝配置目录名称，该目录位于 ~/.alipay/ 目录下
    // 支付宝支付等功能均在这个目录下配置
    // 当支付类型为 `zfb.` 时需要这个配置项目
    zfb : "theName"
}
```

**限制条件**

- 如果是微信公众号支付，则需要账户里有 `openid` 以便主动调起 `JSSDK` 

**注意**

本命令只会生成订单（在用户域中），真正支付需要创建系统支付单，
由 `www pay` 命令根据订单信息调用 `pay` 命令创建并发送一个真正的系统支付单

输入/返回结果
=======

命令的输入是从标准输入读取的的 JSON数据，格式如下：

```js
id: ID,        // 订单唯一ID
//-----------------------------
// 商品列表
products: [{
  id : ID,      // 商品 ID 
  title :"xxx", // 【查】商品名
  price :29.9,  // 【查】商品价格（元）
  amount:3      // 购买数量
}],
//-----------------------------
// 优惠券列表
coupons: [{
  id : ID,
  title : "xxx",  // 【查】优惠券名
  type  : 1,      // 【查】类型，1:代金券，2:折扣券
  value : 20,     // 【查】代金券：元；折扣券0.0~1.0浮点
  thres : 200,    // 【查】订单满足这个金额方可应用，0表示不限
  expi  : AMS     // 【查】过期时间
}]
//-----------------------------
// 摘要信息
// 默认为支付商户的名称
title: "xxx"
// 卖家名称（支付配置目录名）
seller: "theName"  // 【查】根据pay_tp读站点设置
//-----------------------------
// 买家信息
accounts : ID   // 【自】买家用户库ID
buyer_id : ID   // 【自】买家ID
//-----------------------------
// 支付信息
price  : 321.56       // 【查】订单金额
fee    : 319.12       // 【查】优惠后金额
pay_tp : "wx.qrcode"  // 支付类型
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
```

上面所有字段:

- 标识了 【查】的字段会主动再次从数据库里验证
- 标识了 【自】的字段会在程序运行中自动生成

最后整个对象会写回到输出流。

用法
=======

```bash
www buy
    [id:xxx]      # 【必】站点主目录路径，
                  # 主目录必须设置了orders(订单库)/products(商品库)
                  # 以便进行服务器校验。数据集均为 Thing
    [-ticket xx]  # 【必】用户登录票据
    #-----------------------------------------------
    # 输出的JSON数据的配置信息
    [-cqn]           # JSON 输出的格式化方式
    [-ajax]          # 开启这个选项，则输出为 ajaxReturn 的包裹
```

示例
=======

```bash
# >>>>>>>>>>>>>>>>>>>>>
# 如果我们有一个  order.json ，内容类似：
demo:> cat order.json
{
  "products": [{
    "id": "7o7j07o08ujtfqq04kaaaagsg9", 
    "amount": 3
  }],
  "pay_tp" : "wx.qrcode"
}
# >>>>>>>>>>>>>>>>>>>>>
# 创建一个订单
demo:> cat order.json | www buy id:xxx -ticket xxx

```
