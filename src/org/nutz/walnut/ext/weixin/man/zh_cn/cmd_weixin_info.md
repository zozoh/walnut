# 命令简介 

    `weixin info` 用来显示指定公众号的配置信息

# 用法

    weixin [公众号id] info [正则表达式]

    # 显示全部配置信息
    demo@~$ weixin xxx info
    
    # 显示被正则表达式约束的配置信息
    demo@~$ weixin xxx info "^pay_.+$"