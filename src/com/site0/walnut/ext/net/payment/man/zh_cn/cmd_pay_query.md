命令简介
======= 

`pay query` 命令将查询一组支付单状态

    
用法
=======

```    
pay query [brief]       # brief 段的过滤关键字
    [-bu @wn:xx:xxxx]   # 买家过滤项： 类型:ID:名称， 类型为 @wn 表示Walnut用户
                        #  "@wn::"     表示全部 walnut 用户
                        #  ":45..d2:"  表示某指定 buyer_id 的记录
                        #  "::xiaobai" 表示某指定 buyer_nm 的记录
    [-se xx:xxxx]       # 服务商过滤项: ID:名称
                        #  "45..d2:"  表示某指定 seller_id 的记录
                        #  ":redfox"  表示某指定 seller_nm 的记录
                        # !! 如果开启本项，需要 op/root 的成员权限
                        # !! 否则将会强制指定当前域作为约束条件
    [-fee (23.6,)]      # 支付金额过滤项，一个浮点数区间
    [-cur RMB]          # 货币单位过滤项，会强制大写
    [-c_at 0]           # 完成时间过滤项， 0 表示所有未完成的支付单
                        # 支持时间区间，以及区间的 %ms 宏指令
    [-s_at 0]           # 发送请求时间过滤项， 0 表示所有未发送的支付单
                        # 支持时间区间，以及区间的 %ms 宏指令
    [-st NEW]           # 支付单状态过滤项，支持正则表达式 
    
    #........................ 下面的参数与 obj 命令等效
    # 但是一定会有 limit，默认为 10
    [-e $REGEX] 
    [-t fld0,fld1..]
    [-bishcqnl]
    [-ibase 1 or 0]
    [-pager]
    [-limit 1]
    [-skip 0]
    [-match {..}]
    [-json]
```

示例
=======

```
# 查询某指定金额范围的已完成支付单
demo@~$ pay query -fee [100,200] -st ^(OK|FAIL)

# 查询某指定金额范围的已完成支付单(按关闭时间）
demo@~$ pay query -fee [100,200] -c_at [1,]

# 查询某指定金额范围的已成功的支付单
demo@~$ pay query -fee [100,200] -st OK

# 查询某买家全部正等待1小时还未确认的支付单
demo@~$ pay query -bu ::xiaobai -c_at [,%ms:now-1h] -st WAIT

```