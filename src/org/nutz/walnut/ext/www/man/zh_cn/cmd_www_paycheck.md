命令简介
======= 

`www paycheck` 检查一个订单的支付状态，实际上相当于调用 `pay check`

用法
=======

```bash
www paycheck
    [OrderID]       # 【必】订单的 ID
    [-ticket xx]    # 【必】用户登录票据
    #-----------------------------------------------
    # 输出的JSON数据的配置信息
    [-cqn]    # JSON 输出的格式化方式
```

示例
=======

```bash
# 检查一个订单的支付状态
demo:> www paycheck 4r..wa 
```