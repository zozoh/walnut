# 命令简介 

    `domain apply` 应用一个支付单的修改
    
    - 执行者必须是某一支付单的卖家
    - 支付单必须完成

    其中支付单必须有元数据:
    
    - buyer_host : 买家的域名
    - buy_for    : 购买的商品 M[1-9] | Y[1-9]
    - apply_at   : 应用支付单的时间(AMS)，保证一个支付单只能使用一次

# 用法

    domain apply [poId]    # 支付单 ID

# 示例

    # 为某域名应用一个支付单
    demo@~$ domain apply 8a2..2g3
    
        
    
