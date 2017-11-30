命令简介
======= 

`pay create` 命令将创建一个支付单

    
用法
=======

```    
pay create 
    [-br brief]           # 「选」简介，如果没有则会根据字段生成
    [-bu [%]xxx]          # 「必」买家查询字符串，%开头表示为域用户(dusr)
    [-se xxx]             # 「必」服务商查询字符串
    [-fee 2300[RMB]]      # 「必」支付金额，结尾三个大写字母表示货币单位(分)，默认为 RMB
    [-co xxxx]            # 「选」支付用到的优惠券代码，这个优惠券必须是服务商创建，且属于买家的
    [-scope xxx]          # 「选」使用优惠券的限制范围，仅当 -co 有值时生效
    [-meta JSON]          # 「选」更多元数据，如果为空，则从标准输入读取
    [-callback xxx]       # 「选」支付成功后的后续操作模板脚本的名称
    [-pt wx.qrcode]       # 「选」创建成功后立即想第三方平台发起支付
    [-ta xxx]             # 「选必」如果声明了 -pt 必须声明这个参数，表示发送的目标商户
    [235..]               # 「选」更多参数，比如 wx.scan 需要的付款码
```

示例
=======

```
# 创建一个支付单
demo@~$ pay create -br 测试支付 -bu id:xxx -fee 99

# 创建一个美元支付单
demo@~$ pay create -br 测试支付 -bu xiaobai -se 13910330036 -fee 199USD

# 创建一个卖家域的支付单
demo@~$ pay create -br 测试卖家域支付 -bu %xiaobai -se id:xxx -fee 199USD

# 创建一个带后续任务的支付单
demo@~$ pay create -br 测试支付 -bu xiaobai -fee 99 -callback abc

# 创建并发送一个支付单
demo@~$ pay create -br 测试支付 -bu xiaobai -fee 99 -pt zfb.qrcode -ta xxx
```