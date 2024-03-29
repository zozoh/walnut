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

  // 【选】支付类型
  //  - wx.qrcode  : 微信主动扫二维码付款
  //  - wx.jsapi   : 微信公众号内支付
  //  - wx.scan    : 微信被物理码枪扫付款码支付
  //  - zfb.qrcode : 支付宝主动扫二维码付款
  //  - zfb.scan   : 支付宝被物理码枪扫付款码支付
  // 如果未声明本字段，则不会自动创建支付单
  "pay_tp" : "wx.qrcode"

  //【选】需要物流信息的订单需要填写
  "addr_ship_country" : "CN",         // 国家编码，默认 CN
  "addr_user_code" : "110108017000",  // 收货地址（12位地址编码）
  "addr_user_door" : "xxx xx A-302",  // 详细到门牌的地址

  // 【选】发票信息
  "invoice_id" : ID

  //【选】优惠券列表
  "coupons": [{"id":ID}]
}
```

用法
=======

```bash
www buy
    [id:xxx]      # 【必】站点主目录路径，
                  # 主目录必须设置了orders(订单库)/products(商品库)
                  # 以便进行服务器校验。数据集均为 Thing
    [-ticket xx]  # 【必】用户登录票据
    #-----------------------------------------------
    # 采用消息队列创建支付单
    # 因为涉及到第三方平台沟通，所以创建支付单会是一个比较慢或者不可靠的行为
    # 可能因此阻塞订单系统的处理能力
    # 将创建好的订单加入一个消息队列，稍后创建支付单是一个很好的应对策略
    # 这个选型打开后，本命令不会直接创建支付单，而是向一个消息队列
    # 发送主题 "sys"（这个看参数）的消息，内容是 www pay 的执行脚本
    # 当然，前提是，系统已经在全局配置里打开了 mq-enabled=true 选项
    [-mq sys]
    #-----------------------------------------------
    # 【选】商品价格为动态规则
    # 这个要求商品对象指定两个选项
    #  - pro_id   : 应用哪个规则(值为规则 ID)
    #  - price_by : 采用规则列表里的哪个规则
    # 本选项的值表示，规则对象的哪个键存放了规则列表，默认为 prices
    # 支持 "@content" 这样的特殊键，表示对象的内容为规则列表
    [-prices prices],
    #-----------------------------------------------
    # 创建支付单时，从用户元数据挑选选数据
    # 是一个映射表:
    # 键为用户元数据名
    # 值为，写到支付单对象里的键名
    # !仅在输入订单时，声明  payType 时有效
    [-upick {wx_mp_xxx:'wx_openid'}]
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
demo:> cat order.json | www buy ~/www -ticket xxx
```

