---
title:支付流程
author:pw
tags:
- 系统
- 支付
---

# 支付完整流程

![支付流程图](media/%E6%94%AF%E4%BB%98%E6%B5%81%E7%A8%8B%E5%9B%BE.png)


# 商户（域）

域根目录下只要包含.store目录与对应配置即被当做是一个商户

```
$HOME/.store
    store_info.json             # 商户相关基本信息
    store_logo.jpg              # 商户默认logo
    /default_home               # 默认商户登陆页面，该目录需link到www相关目录下
        ....
```

# 商品

1. 任何域都可以提供商品
2. 商品可以包含一样东西或多样东西
3. 商品可以具有包含已有其他商品，但仅限一层嵌套关系
4. 商户采用个walnut中thing对象进行管理与存储

商品thing.js定义如下：

```
{
    w_nm  : "xxxx"           # 商品名称，支持 i18n:xxx 格式
    w_price    : 34.50       # 商品价格，单位元
    w_currency : "RMB"       # 货币单位，默认 RMB
    w_expi : AMS             # 销售过期时间, 之后无法购买
    w_buy_min: 1             # 最少购买件数 不能小于1
    w_buy_max: 5             # 最多购买件数 小于等于min表示无限制
    w_pkg: [{                # 商品包含其他商品
        w_id: $THING_ID,
        num: 1
    }, {
        w_id: $THING_ID,
        num: 1
    }]
}
```

w_pkg默认为空，则该商品为单一商品
w_pkg不为空时，w_price根据商品包内价格自动计算，但如果强制设置了w_price，则使用w_price价格（类似设定折后价）

# 折扣策略与优惠券

1. 商户可以主动发放优惠券，或者设定整体折扣策略
2. 折扣策略与优惠券类似商品，都是采用thing结构

折扣策略thing.js定义 (discount)

```
{
    dis_id ： ID
    dis_nm :  "xxxx"           # 名称，支持 i18n:xxx 格式
    dis_mode: ""               # 折扣模式
}
```


# 订单

订单存放位置：

```
/sys/order/
        ord_$dmn1_xxxxxx
        ord_$dmn1_yyyyyy
        ord_$dmn2_xxxxxx
        ord_$dmn2_yyyyyy
```

订单生成，查询等命令有 *cmd_pay* 实现


1. n件(n>0)商品可以组成一个订单
2. 订单内包含下单商品的必要信息（copy过来），之后商品修改不影响订单内商品信息
3. 订单作为用户消费的唯一标示
4. 订单有多个状态，不同状态可进行的操作不同


```
{
    tp    : "order"           # 类型一定是 order
    race  : "FILE"            # 一定是目录
    //........................................... 购买的商品
    ord_ws : [{                  #  商品列表
        id: w_id
        w_nm  : "xxxx"            # 商品名称
        w_price    : 34.50        # 商品价格
        w_buy_num  : 1            # 购买数量
    },
    {
        id: w_id
        w_nm  : "xxxx"            # 商品名称
        w_price    : 34.50        # 商品价格
        w_num      : 1            # 购买数量
        w_pkg: [{                 # 商品包含其他商品
            w_nm  : "xxxx"        # 商品名称
            w_price    : 34.50    # 商品价格            
            w_num: 1              # 购买数量
        }, {
           w_nm  : "xxxx"         # 商品名称
           w_price    : 34.50     # 商品价格            
           w_num: 1               # 购买数量        
        }]
    }],
    ord_ds: [{                    # 折扣列表
        id: dis_id,
        dis_nm: "xxx",            # 折扣名称
    }],
    ord_price      : 34.50       # 订单价格， （商品价格-折扣价格）
    ord_currency   : "RMB"       # 订单货币单位
    ord_status     : "xxxx"      # 订单状态 未支付->支付->消费
    ord_expi       : AMS         # 订单过期时间
    //........................................... 购买人信息
    sto_id         : ID          # 商户域ID
    sto_nm         : "xxxxx"     # 商户域名称
    //........................................... 支付信息
    // TODO    
    
}
```


# 支付

1. 根据订单内容向支付通道发起请求
2. 订单更新，等待支付平台回调后修改订单状态
3. 订单状态修改，通知商户，购买者




# 命令

cmd_payment支持以下几个子命令

1. 商品命令，查看修改当前域下商品信息
2. 折扣命令，查看修改当前域下折扣信息
3. 订单命令，查看系统中关于本域的订单信息
4. 支付命令，完成订单的支付与回调



## 商品命令(payment w)

```
# 查看当前商品
payment w list

```

## 折扣命令(payment d)

```
# 查看当前优惠策略
payment d list

```

## 订单命令(payment o)

## 支付命令(payment p)

