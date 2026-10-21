---
title:支付宝命令
author:zozoh
tags:
- 系统
- 扩展
- 支付宝
---

# 支付宝命令概述

1. 主动调用支付宝的接口
2. 处理自定义菜单

# 微信的权限验证

支付宝服务器与第三方服务器的加密信息存放在目录 `~/.alipay/` 下。

    ~/.alipay/
        nutzcn             # 某个支付宝账号的配置存放目录
            alipayconf     # 存放应用信息
        another_商家                      # 每个商家一个目录

## alipayconf 的文件格式 

```
seller_email : "webmaster@wendal.cn" # 签约的支付宝账号
partner : "2088521239387536"         #合作者身份PID
key : "xxxx"                         # 安全校验key
rsa_private : "yyyyyyyyy"            # RSA密钥, PK8格式
```