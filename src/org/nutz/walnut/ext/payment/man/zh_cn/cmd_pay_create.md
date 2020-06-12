命令简介
======= 

`pay create` 命令将创建一个支付单

    
用法
=======

```    
pay create 
    [-br brief]           # 「选」简介，如果没有则会根据字段生成
    [-bu [${type}:${ID}]  # 「必」买家ID，其格式有两种可能：
                          #   walnut:45g..67q 表示Walnut用户
                          #   y8..r2:9ha..3ey 表示某域账户库的用户
    [-sl xxx]             # 「选」卖家域名，默认为操作账户所在域
    [-fee 2300[RMB]]      # 「必」支付金额，结尾三个大写字母表示货币单位(分)，默认为 RMB
    [-cur RMB]            # 「选」支付货币，默认 RMB （会强制大写）
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
demo@~$ pay create -br 测试支付 -bu walnut:xxx -fee 99

# 创建一个美元支付单
demo@~$ pay create -br 测试支付 -bu walnut:xxx -sl site0 -fee 199USD

# 创建一个卖家域的支付单
demo@~$ pay create -br 测试卖家域支付 -bu a9..3a:5t..8f -se site0 -fee 199USD

# 创建一个带后续任务的支付单
demo@~$ pay create -br 测试支付 -bu walnut:xxx -se site0 -fee 99 -callback abc

# 创建并发送一个支付单
demo@~$ pay create -br 测试支付 -bu walnut:xxx -se site0 -fee 99 -pt zfb.qrcode -ta xxx
```