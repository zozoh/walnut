命令简介
======= 

`www pay` 根据订单创建支付单，并自动设置支付单的回调

用法
=======

```bash
www pay
    [id:xxx]        # 【必】站点主目录路径，
                    # 主目录必须设置了orders(订单库)/products(商品库)
                    # 以便进行服务器校验。数据集均为 Thing
    [OrderID]       # 【必】订单的 ID
    [-ticket xx]    # 【必】用户登录票据
    #-----------------------------------------------
    # 输出的JSON数据的配置信息
    [-cqn]    # JSON 输出的格式化方式
    [-ajax]   # 开启这个选项，则输出为 ajaxReturn 的包裹
```

示例
=======

```bash
# 支付一个订单
demo:> www pay ~/www 4r..wa -ticket uy..93
```