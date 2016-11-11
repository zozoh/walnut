---
title:支付宝命令
author:zozoh
tags:
- 系统
- 扩展
- 支付宝
---

# 微信命令概述

alipay命令与 [httpapi](../core/httpapi.md) 

1. 处理支付宝发来的消息(支付宝也有公众号)
2. 主动调用支付宝的接口
3. 处理自定义菜单

# 微信的权限验证

支付宝服务器与第三方服务器的加密信息存放在目录 `~/.alipay/` 下。

    ~/.alipay/
        nutzcn       # 某个微信公众号的配置存放目录
            alipayconf     # 存放应用信息
            context        # 存放与用户会话的上下文
                78a5gg..   # 每个 openid 一个目录
                           # 后面的章节有详细描述
        another_商家                      # 每个商家一个目录

## alipayconf 的文件格式 

```
seller_email : "webmaster@wendal.cn" # 签约的支付宝账号
partner : "2088521239387536"         #合作者身份PID
key : "xxxx"                         # 安全校验key
rsa_private : "yyyyyyyyy"            # RSA密钥, PK8格式
```