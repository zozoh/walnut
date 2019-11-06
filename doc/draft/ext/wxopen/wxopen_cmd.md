---
title:微信开放平台命令
author:wendal
tags:
- 系统
- 扩展
- 微信
---

# 什么是微信开发平台

官网: https://open.weixin.qq.com

跟微信公众号平台相对独立的体系, 主要服务于第三方客户端, 即微信客户端之外的客户端, 例如第三方App,浏览器

该平台具有的功能主要是: 微信登录, 微信支付, 微信智能接口

其中, 微信支付仅限于App支付, 而常用的扫码支付,依然需要去微信公众号平台.

而微信智能接口,暂时无视.

换言之, 真正要关注的, 只有微信登录.

微信登录的目的: 获取openid,unionid,及用户基本信息(昵称,性别,省份等).

# 微信登录的基本形式

### 第一步,是如何触发登录:


* App下的微信登录, 只能通过微信SDK唤起微信
* 浏览器(泛指各种PC浏览器/内嵌Webkit的WebView)登录, 则有2种方式: 跳转到微信登录专属页面, 或JS SDK, 两种方式的效果均为显示二维码.

### 第二步, 获取用户登录的code

code是换取access token的唯一凭证,且只能使用一次. 而access token是获取用户信息的唯一凭证, 默认有效期2小时.

* App下,若用户确认登录,通过回调可获得code
* 浏览器登录, 用户登录成功后, 均跳转到自定义的URL, 并带上code参数.

### 第三步, 通过code换取access token

无论是App登录还是浏览器登录, 步骤均一致

```
curl "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code"
```

其中appid,secret均需要在"管理中心"中获取.

而code参数,就是第二步获取的code

返回的内容是json

```json
{ 
"access_token":"ACCESS_TOKEN", 
"expires_in":7200, 
"refresh_token":"REFRESH_TOKEN",
"openid":"OPENID", 
"scope":"SCOPE",
"unionid":"o6_bmasdasdsad6_2sgVt7hMZOPfL"
}
```

可以看到, 这一步能获取最重要的 openid,unionid,access_token

若无需获取用户基本信息, 微信登录中与微信服务器交互已经结束.

### 第4步, 获取用户基本信息

```
curl https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
```

其中, access_token和openid均为上一步获取的值.

响应内容:

```json
{ 
"openid":"OPENID", // 对于该移动应用/网站应用的用户唯一识别码
"nickname":"NICKNAME", // 昵称, 若存入mysql,必须转义, 或使用uft8mb4编码
"sex":1, // 1为男性，2为女性
"province":"PROVINCE", // 省份属性
"city":"CITY", // 城市属性
"country":"COUNTRY", // 国家属性
"headimgurl": "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
"privilege":[ // 特权属性, 例如微信沃卡会出现 chinaunicom,
	"PRIVILEGE1", 
	"PRIVILEGE2"
],
"unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL" // 该微信开放平台下所有应用均一致的用户唯一识别码
}
```

### 微信登录与Walnut账号体系的关联

微信用户,有2个属性, openid 和  unionid.

若用户同时使用[网站登录,App登录, 公众号登录],且已经统一关联到微信开发平台, 那么unionid是最佳选择

但如果使用本功能的开发者,需要把不同登录渠道关联到不同的用户,那么openid是最佳选择

恩, 这似乎不是wxopen命令需要考虑的问题, wxopen命令应该只处理与微信服务器交互的部分,对吗? 账号关联就交给具体的开发者去选择吧.

# 参考文档


[移动应用微信登录开发指南](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419317851&token=&lang=zh_CN)

[网站应用微信登录开发指南](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419316505&token=&lang=zh_CN)