---
title:微信公众号扫码登陆
author:wendal
tags:
- 系统
- 扩展
- 微信
---

# 流程

* 页面点击按钮,带UU32识别号
* 弹出公众号的临时二维码
* 微信扫一扫,若未关注,则关注之
* 微信服务器发送SCAN EVENT
* 触发weixin scan命令,执行`~/.weixin/gh_xxxxx/scene/${scene_id}`对应的命令脚本

# 登陆成功的信息存在于 `~/.weixin/gh_xxxxx/mplogin` 下

	~/.weixin/gh_xxxxx/mplogin
	                         -- ulcvkur3iginurdbq7ijaf1ni1
	                         -- o1udm87eigi1sr61cjtbclgmav

文件的内容是 微信用户的 openid , 验证成功后, 文件即删除

# 验证登陆信息的入口方法

	/u/check/mplogin?uu32=ulcvkur3iginurdbq7ijaf1ni1
	
# 关于生成临时二维码

通过regapi调用weixin qrcode命令 (root用户下)

	~/.regapi/api/mplogin/qrcode

	weixin gh_72b9a9f8e20a qrcode QR_SCENE -qrsid 0 -qrexpi 120 -cmd "echo '${weixin_FromUserName}' > ~/.weixin/gh_72b9a9f8e20a/mplogin/${http-qs-uu32}" | json -q
	
# wxconf中的扫描事件映射

```
    "handlers": [
        {
            "id": "scan",
            "match": {
                "Event": "SCAN"
            },
            "context": false,
            "command": "weixin gh_72b9a9f8e20a scan -openid ${weixin_FromUserName} -eventkey ${weixin_EventKey} -c"
        }
    ],
```