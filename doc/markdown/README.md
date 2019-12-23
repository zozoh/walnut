# Walnut 文档目录

- 写在前面的话
- 关于Walnut的故事
- 你应该怎么阅读这份文档

-----------------------------------------
## 系统核心概念`core-l0`

- Walnut的简要介绍
- [数据存储模型][c0-ios]
- [基础账户授权模型][c0-bam]
- [核心会话服务][c0-css]
- 环境变量
- 命令机制

-----------------------------------------
## 系统延申概念`core-l1`

- 数据备份与导出
- 钩子机制
- HttpApi

-----------------------------------------
## 系统高阶概念`core-l2`

- [业务权限模型][c2-bpm]
- 应用机制
- 自动登录
- 域名映射
- 第三方登录(oAuth2)
- 后台任务
- 回收站

-----------------------------------------
## 系统功能整合`func-l0`

- 微信公众号平台整合
  + [微信支付整合][f0-wxp]
- [支付宝支付接口整合][f0-zfb]
- 内容搜索(ESI)整合
- websocket
- weather
- mqttc
- mt90
- sqltool
- tfodapi
- ftp
- ffmpeg
- qrcode
- whois
- shorturl
- ip2region

-----------------------------------------
## 系统功能扩展`func-l1`

- mediax 通用爬取器
- imagic 图片处理
- 通用短信发送支持
- 通用邮件发送支持
- [通用支付模型][f1-pay]
- 通用通知机制

-----------------------------------------
## 界面层核心概念`ti-l0`

- Wn 帮助函数
- pc_tmpl
- wn.manager
- wn-form
- wn-table

-----------------------------------------
# 通用数据集核心概念`thing-l0`

- Thing 概述
- Thing 的数据结构
- Thing 的操作方式
- Thing 的界面框架

-----------------------------------------
## Web 核心`webs-l0`

- [站点账户授权接口][w0-saa]
- [采用域用户登录系统][w0-abd]
- 验证码模型
- WNML支持
- [购物与订单模型][w0-buy]
- Ti 的 www 支持

[c0-ios]: core-l0/c0-io-store.md
[c0-bam]: core-l0/c0-baice-auth-model.md
[c0-css]: core-l0/c0-core-session-service.md
[c0-bpm]: core-l0/c0-basic-privilege-model.md
[c2-bpm]: core-l2/c2-biz-privilege-model.md
[f0-wxp]: func-l0/f0-weixin-payment.md
[f0-zfb]: func-l0/f0-alipay.md
[f1-pay]: func-l1/f1-payment.md
[w0-abd]: webs-l0/w0-auth-by-domain.md
[w0-saa]: webs-l0/w0-site-auth-api.md
[w0-buy]: webs-l0/w0-buy.md

