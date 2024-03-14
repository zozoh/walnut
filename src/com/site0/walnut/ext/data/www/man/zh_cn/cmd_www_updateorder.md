命令简介
======= 

`www updateorder` 根据调整后的订单基础金额和运费设置订单费用

用法
=======

```bash
www updateorder
    [id:xxx]        # 【必】站点主目录路径，
                    # 主目录必须设置了orders(订单库)/products(商品库)
                    # 以便进行服务器校验。数据集均为 Thing
    [OrderID]       # 【必】订单的 ID
    [-m_freight_key freight_m]  # 【选】订单上修订的运费字段
    [-m_prefee_key   prefee_m]   # 【选】订单上修订的总价字段
    #-----------------------------------------------
    # 输出的JSON数据的配置信息
    [-cqn]    # JSON 输出的格式化方式
```

示例
=======

```bash
# 修改一个订单上的金额
demo:> www updateorder ~/www 4r..wa
```