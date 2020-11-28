---
title: 域站点模型
author: zozohtnt@gmail.com
key: w0-site
---

--------------------------------------
# 动机：为什么要有域站点模型

每个域都可以开启一个或者多个站点。这篇文档就描述了这个机制的细节

--------------------------------------
# 计划应用场景

希望提供公共访问的：

- 博客/WIKI/企业官网/商城等
- 手机APP
- 微信小程序/公众号页面

--------------------------------------
# 设计思路与边界

基本上，就是找个地方弄个目录，这个目录标识上一个元数据 `www` 里面的内容就能被访问了。

考虑到用户需要自己注册域名，因此这个机制是建立在[域名映射机制][c2-dmn]之上的。

同时为了让站点有更丰富的可配置信息，你可以为其增加更多元数据，下面章节有具体描述

--------------------------------------
# 站点元数据

```bash
id : ID,       # 站点的 ID

# 本站的映射域名，可以是  www.youdomain.com 之类的域名
# 只要在 `/domain` 里做了映射，则会转到这个目录下
# 默认 "ROOT" 的话要用 /www/your-domain/ 来直接指明
www : "ROOT"

# 当访问站点，但是没有指明入口页时，默认跳转到哪个页面，
# 默认是 index.wnml | index.html
www_entry : "enter.html",

# 声明虚拟页，给定的路径匹配的话，则采用指定虚拟页
www_pages : ["index.wnml:abc/page/*,xyz/page/*"]

# 这里是站点关联的集合（ThingSet）
accounts  : "~/accounts"   # 账户库   
roles     : "~/roles"      # 【选】角色库
orders    : "~/orders"     # 【选】订单库（购物功能必须）
coupons   : "~/coupons"    # 【选】优惠券库
addresses : "~/addresses"  # 【选】地址库
freight_sheet : "~/.domain/freight_sheet.json" #【选】运费表

# 这里指定嵌入历史记录的埋点
# 声明了这个元数据，那么指定的行为下创建历史记录
#  - signup : 用户注册
#  - login  : 用户创建会话
#  - pay : 支付订单
history : {
  # 创建会话时的历史记录
  # 上下文为对象
  #  - @name   : "session:created"
  #  - @domain : 当前域名
  #  - @home   : 主目录路径
  #  - @me     : 当前账号 {id,nm, nickname,phone,email ...}
  #  - @session: 当前会话 {ticket ...}
	"session:created" : {
    uid : "=@me.id",
    unm : "=@me.nickname|me.email|me.phone|me.nm",
    utp : "=@me.role?user",
    tid : "=@domain",
    ttp : "user",
    opt : "login"
	},
  # 创建用户时的历史记录
  # 上下文为对象
  #  - @name   : "account:created"
  #  - @domain : 当前域名
  #  - @home   : 主目录路径
  #  - @me     : 当前账号 {id,nm, nickname,phone,email ...}
	"account:created" : {
    uid : "=@me.id",
    unm : "=@me.nickname",
    utp : "=@me.role?user",
    tid : "=@domain",
    ttp : "user",
    opt : "signup"
	},
	# 订单支付后（A and Q)
  #  - @name   : "order:pay"
  #  - @domain : 当前域名
  #  - @home   : 主目录路径
  #  - @me     : 当前账号 {id,nm, nickname,phone,email ...}
  #  - @order  : 当前订单 {id,tp,pro_c ...}
	"order:pay" : {
	  uid : "=@me.id",
    unm : "=@me.nickname",
    utp : "=@me.role?user",
    tid : "=@order.id",
    tnm : "->${@order.tp}:${@order.title}",
    ttp : "order",
    opt : "pay"
	},
	# 订单支付后（A and Q)，每个商品的记录
  #  - @name    : "order:pay"
  #  - @domain  : 当前域名
  #  - @home    : 主目录路径
  #  - @me      : 当前账号 {id,nm, nickname,phone,email ...}
  #  - @order   : 当前订单 {id,tp,pro_c ...}
  #  - @product : 当前商品 {id,title,amount,cate,price ...}
	"order:products" : {
		uid : "=@me.id",
    unm : "=@me.nickname",
    utp : "=@me.role?user",
    tid : "=@product.id",
    tnm : "=@product.title",
    ttp : "product",
    opt : "pay"
	},
  # A订单支付后
	"order:A:pay" : {..},
  "order:A:products" : {..},
  # Q订单支付后
	"order:Q:pay" : {..},
  "order:Q:products" : {..},
}
# 指定历史记录数据源的名称，默认为 _history
# 这个需要在 ~/.domain/history/ 下面做自定义
# 关于详情可以参看下列相关文档，了解历史记录机制更过细节
#  - c1-general-data-entity.md#历史记录
#  - cmd_history.md
hisname: "web"

# 这是一个站点权限表，访问这个站点的 API 默认采用的权限表
# 参看靠下面列表，获取更多相关信息
#  - c1-regapi.md > API对象 > pvg-setup
#  - c2-biz-privilege-model.md
pvg_setup: "~/.domain/pvg.json"

# 这里是站点所属账户库，如果登录系统账户后默认的环境变量
# 它会替换在 init-usr-envs: 声明的环境变量值
env : {
  OPEN      : "wn.console",
	PATH      : "/bin:/sbin:~/bin",
	THEME     : "light",
	APP_PATH  : "/rs/ti/app:/app",
	VIEW_PATH : "/rs/ti/view/",
	QUIT      : "/www/abc",
	SIDEBAR_PATH : "~/.ti/sidebar.json:/rs/ti/view/sidebar.json"
}

# 微信配置目录名，用来做公众号或小程序登录等操作
weixin   : {
  mp   : "{wxConfName}",    # 小程序配置
  gh   : "{wxConfName}",    # 公众号配置
  open : "{wxConfName}"     # 开放平台公号配置
}

# 商户映射表；根据支付类型前缀来决定
sellers : {
  # 微信配置目录名称，该目录位于 ~/.weixin/ 目录下
  # 微信登录，公众号消息推送，微信支付等功能均在这个目录下配置
  # 当支付类型为 `wx.` 时需要这个配置项目
  wx : "theName"
  
  # 支付宝配置目录名称，该目录位于 ~/.alipay/ 目录下
  # 支付宝支付等功能均在这个目录下配置
  # 当支付类型为 `zfb.` 时需要这个配置项目
  zfb : "theName"

  # PayPal配置目录名称，该目录位于 ~/.paypal/ 目录下
  # PayPal支付等功能均在这个目录下配置
  # 当支付类型为 `paypal` 时需要这个配置项目
  paypal : "theName"
}

# 站点订单价格实际支付按照那种价格来计算
#  - TOTAL   : 按照价格体系
#  - NOMINAL ：按照产品的标称价格
# 默认 TOTAL
fee_mode : "TOTAL"

# 站点支付默认采用的货币单位
# 默认的话，用 RMB
currency: "RMB"

# 站点的订单默认过期时间（分钟）
# 0 或者负数 表示永不过期
# >0 表示秒
# 默认为 15 分钟
order_du_min : 15

# 默认会话时长（秒）
se_dft_du : 86400

# 临时会话时长（秒） 其实我也不知道这个干什么用的，和系统的对称吧
se_tmp_du : 60

# 站点的接口，请求对象缓存默认时长
# 0  表示请求完毕立即删除
# > 0 表示 秒
# 负数相当于没有设置
# 默认 -1
api_req_du : 0
```

--------------------------------------
# 相关知识点

- [基础权限模型][c0-pvg]
- [HTTP API][c1-api]
- [站点账户授权接口][w0-saa]

[c0-pvg]: ../core-l0/c0-basic-privilege-model.md
[c1-api]: ../core-l1/c1-regapi.md
[c2-pvg]: ../core-l2/c2-biz-privilege-model.md
[c2-dmn]: ../core-l2/c2-domains.md
[w0-saa]: ../webs-l0/w0-site-auth-api.md