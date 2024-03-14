# 命令简介 

`weixin pay` 用来处理微信支付订单

# 用法

```bash
weixin {ConfName} pay [..参数]
```

# 示例

```bash
# 从文件里获取支付签名内容
weixin xxx pay id:xxxx
    
# 其他命令输出里获取支付签名内容
echo xxx | weixin xxx pay

# 直接读取支付签名内容
weixin xxx pay '{..}'
```
    
# 说明
  
默认的命令会输出 xml 内容，你可以直接 post 给微信服务器。如果增加 `-json` 参数，则会输出成 JSON内容，以便你查看
    
下面这些参数，会覆盖签名文档里面的同名键

 ParamName  | Description
------------|----------------------------------------
 dev        | "WEB" 签名设备（参见微信支付文档）
 trade_type | "JSAPI" 交易方式（参见微信支付文档）