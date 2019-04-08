# 命令简介 

    `weixin` 用来处理微信公众号平台的业务逻辑。
    它的输入就是 httpapi 的数据文件输入
    如果处理成功了，就会标识 'clean_after' 以便 objclean 命令清除
    
    本命令如果是主动向微信服务器发送请求，则需要读取 `~/.weixin/公众号` 目录下的 `wxconf` 文件。
    该文件的格式为:
    
    {
    	appID      : 'wx0d4caad29f23b326',
    	appsecret  : 'd68d9507835139b0e21d28b4806c1aa7',
    	token      : 'TOKEN'
    }

用法
=======

```
weixin in           # 处理微信服务器发来的信息
weixin info         # 获取微信公众号的配置信息
weixin jssdk        # JS-SDK相关参数
weixin media        # 素材上传下载
weixin menu         # 自定义菜单
weixin oauth2       # 公众号授权
weixin order        # 订单生成
weixin out          # 生成符合微信要求的响应
weixin pay          # 发起支付
weixin payre        # 校验支付结果
weixin qrcode       # 二维码相关
weixin scan         # 处理扫码
weixin shake        # 摇一摇相关
weixin text         # 快捷生成文本响应
weixin tmpl         # 模板消息
weixin user         # 用户信息获取
```