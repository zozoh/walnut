# 命令简介 

    `weixin out` 用来输出符合微信要求的响应消息,通过与 `weixin in`配合使用

# 用法

    weixin [公众号id] out [..参数]
    
    # 输出一段简单文本
    demo@~$ weixin xxx out "text:Hello world" -openid xxxx
    
    # 输出一段链接
    demo@~$ weixin xxx out "article:Hello;;brief;;http://xxxxx" -openid xxx
    
    # 输出更复杂的消息
    demo@~$ weixin xxx out "{..}" -openid xxx
    
    # 根据直接回复消息
    demo@~$ weixin out "text:xxxxx" -inmsg id:xxxx
    