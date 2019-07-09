---
title:自定义应用
author:zozoh
tags:
- 系统
- 自定义应用
---

----------------------------------------------------------
# 什么是自定义应用

*这里给一个应用的结构*

----------------------------------------------------------
# 初始化脚本

通过 `app init` 创建一个域的目录结构设置

```js
{
	domain : 'mydomain', // 提供服务的域，通常与 /home/xxx/ 名称一致
  domainName : '我的管理域', // 提供服务的域显示文字
  wxOpenName: 'wxlogin',  // 微信开放平台配置目录名称
  wxOpenAppId : 'wx43..', // 微信开放平台 ID
  wxOpenAppSecret : '3c..69',  // 微信开放平台秘钥
	wxAppId : 'wx68..', // 微信公众号平台 ID
	wxAppSecret : '5e..c9', // 微信公众号平台秘钥
	smsKey : '9a..c4', // 云片网短息平台秘钥
  smtpHost : "smtp.qq.com", // SMTP 服务地址
  smtpPort : 25, // SMTP 服务端口
  smtpAccount : "abc@xx.com", // SMTP 账号
  smtpAlias : "abc", // SMTP 账号别名
  smtpPasswd : "123456", // SMTP 密码
}
```