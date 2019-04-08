# 命令简介 

    `weixin qrcode` 生成微信二维码

# 用法

    weixin [公众号id] qrcode [..参数]
    
    # 显示二维码地址
    demo@~$ weixin xxx qrcode url -qrticket xxxxxxx
    
    # 显示二维码图片内容
    demo@~$ weixin xxx qrcode img -qrticket xxxxxxx
    
    # 临时二维码
    # qrsid 整数，场景 ID,若传入0,代表自增
    # qrexpi 过期秒数
    # cmd 扫描后执行的命令,同时接受从标准输入流获取
    demo@~$ weixin xxx qrcode QR_SCENE -qrsid 2 -qrexpi 3600 -cmd "weixin xxx out xxxx"
    
    # 永久二维码
    # qrsid 整数，场景 ID
    demo@~$ weixin xxx qrcode QR_LIMIT_SCENE -qrsid 2
    
    # 永久字符串二维码
    # qrsid 字符串，场景 ID
    demo@~$ weixin xxx qrcode QR_LIMIT_STR_SCENE -qrsid "hahaID"