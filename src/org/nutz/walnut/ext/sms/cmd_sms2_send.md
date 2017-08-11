# 命令简介 

`sms2 send` 用来发送短信
    
# 用法

    sms send
        [-r 13910223421]            # 接受者手机
        [-config /path/to/conf]     # 「选」配置文件路径 
        [-provider Yunpian]         # 「选」提供商(当前仅Yunpian)
        
        [-vars {..}]                # 更多操作变量，根据 provider 的不同，有不同的解释
        ..............................................................................
        [-debug]                    # 开启调试模式
        [-t 模板文件名称]             # 短信模板文件名，模板文件必须存放在 ~/.sms 下
                                    # 如果是 i18n:xxx 格式，则在 ~/.sms/i18n/$lang/ 下读取
        <msg|json>                  # 消息正文，如果 -t 模式，则为一个 JSON，表示填充上下文
                                    # 如果不填写，则从管道读取
        [-lang zh-cn]               # 「选」指定模板语言，默认为从 conf 里读取，还是没有的话用 zh-cn
        [-header xxxx]              # 「选」指定消息头部
    
# 示例
    
    # 发送一条自定义消息
    sms send -debug -header 【nutz科技】-r 13910223421 机器AABBCCDDEEFF流量异常500mb
    
    # 发送模板消息
    sms send -r 13910223421 -t signup "code:8976"
    
    # 发送 i18n 模板消息
    sms send -r 13910223421 -t i18n:signup "code:8976"
    