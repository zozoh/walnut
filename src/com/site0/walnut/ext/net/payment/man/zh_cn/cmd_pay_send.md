命令简介
======= 

`pay send` 将一个支付单信息真正执行与第三方支付平台交互的操作

    
用法
=======

```    
pay send [poId]          # 「必」支付单的ID
         [wx.qrcode]     # 「必」支付类型
         [xxx]           # 「必」支付目标（比如微信公众号，支付宝目录等）
         [235..]         # 「选」更多参数，比如 wx.scan 需要的付款码
```

其中支付类型支持:

 - wx.qrcode  : 微信主动扫付款码
 - wx.jsapi   : 微信公众号支付
 - wx.scan    : 微信被物理码枪扫码支付
 - zfb.qrcode : 支付宝主动扫付款码
 
命令输出:

```
{
    status : "WAIT",        // 支付状态。OK:成功，FAIL:失败，WAIT:等待用户确认
    changedKeys : [..],     // 改变了支付单哪些字段
    type : "LINK"           // 内容类型。
                            //  - LINK   : 第三方付款链接，
                            //  - QRCODE : 第三方付款二维码
                            //  - JSON   : 一个JSON的Map对象，比如微信公众号支付的配置项
    data : ..               // 返回的数据可能是一个URL，也可能是一段二维码内容
                            // 也可能是一个 Map 表示的一个 JSON 对象
                            // 总之根据 type 段的值来决定啦
}
```

示例
=======

```
# 发起微信主动扫码支付
demo@~$ pay send 4g..3a wx.qrcode xxx

# 发起微信公众号支付
demo@~$ pay send 4g..3a wx.jsapi xxx

# 发起微信被物理码枪扫码支付
demo@~$ pay send 4g..3a wx.scan xxx 238..871

# 发起支付宝主动扫码支付
demo@~$ pay send 4g..3a zfb.qrcode xxx

```