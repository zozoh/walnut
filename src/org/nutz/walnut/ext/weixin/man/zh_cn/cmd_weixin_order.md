# 命令简介 

    `weixin order` 用来生成微信支付订单,当前仅支持JS SDK

# 用法

    weixin [公众号id] order [..参数]

    # 生成 JS-SDK 的支付准备对象
    weixin xxx order -submit xxx -openid xxx -client_ip xxx

    # 接受支付的回调
    weixin xxx order -result id:xxx