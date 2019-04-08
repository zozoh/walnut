# 命令简介 

    `weixin oauth2` 用来处理微信公众号授权

# 用法

    weixin [公众号id] oauth2 [-wxopen] [..参数]
     - wxopen 表示生成给开放平台的重定向 URL

    # 生成重定向请求的 URL
    demo@~$ weixin xxx oauth2 "http://redirect.com"
    
    # 指定信息获取的级别
    demo@~$ weixin xxx oauth2 "http://xxx" -scope snsapi_base
    
    # 指定一个状态码
    demo@~$ weixin xxx oauth2 "http://xxx" -state ANY