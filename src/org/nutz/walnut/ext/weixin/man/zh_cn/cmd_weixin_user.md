# 命令简介 

    `weixin user` 用来获取用户信息

# 用法

    weixin [公众号id] user [..参数]
    
    # 根据 OpenID 获取用户信息
    weixin xxx user -openid xxx
    
    # 指定语言
    weixin xxx user -openid xxx -lang zh_CN
    
    # 根据 code 获取用户信息(仅OpenId)
    weixin xxx user -code xxx
    
    # 根据 code 获取用户信息(仅关注者)
    weixin xxx user -code xxx -infol follower
    
    # 根据 code 获取用户信息(任何人)
    weixin xxx user -code xxx -infol others