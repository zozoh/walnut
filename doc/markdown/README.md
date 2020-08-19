# Walnut 文档目录

- 写在前面的话
- 关于Walnut的故事
- 你应该怎么阅读这份文档

-----------------------------------------
## 系统核心概念`core-l0`

- Walnut的简要介绍
- [数据存储模型][c0-tab]
- [基础账户授权模型][c0-bam]
- [核心会话服务][c0-css]
- [采用域用户登录系统][c0-abd]
- [基础权限模型][c0-pvg]
- 环境变量
- 命令机制
- [通知机制][c0-noti]

-----------------------------------------
## 系统延申概念`core-l1`

- [数据备份与导出][c1-buk]
- [通知机制][c1-nti]  !等消息队列整合完，要重新设计
- [HTTP API][c1-api]
- 钩子机制
- 应用机制
- [通用实体][c1-gde]

-----------------------------------------
## 系统高阶概念`core-l2`

- [业务权限模型][c2-pvg]
- 自动登录
- [域名映射][c2-dmn]
- 第三方登录(oAuth2)
- 后台任务
- 回收站

-----------------------------------------
## 系统功能整合`func-l0`

- 微信公众号平台整合
  + [微信支付整合][f0-wx-pay]
  + [微信通知整合][f0-wx-nti]
- [支付宝支付接口整合][f0-zfb]
- [阿里云整合][f0-aliyun]
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

- [Thing·概述][th0-ovw]
- [Thing·数据结构][th0-data]
- [Thing·配置文件][th0-thjs]
- Thing·导入导出

-----------------------------------------
# 通用数据集·定制·界面层`thing-l2`

- Thing·界面配置概述
- Thing·引入自定义方法

-----------------------------------------
## Web 核心`webs-l0`

- [通用Web接口][w0-api]
  + [Web授权接口][w0-saa]
- [域站点模型][w0-site]
- 验证码模型
- WNML支持
- Ti 的 www 支持

-----------------------------------------
## Web 扩展`webs-l1`

- [商城模型][w1-shop]

[c0-iob]: core-l0/c0-io-tree-and-bucket.md
[c0-bam]: core-l0/c0-baice-auth-model.md
[c0-abd]: webs-l0/w0-auth-by-domain.md
[c0-css]: core-l0/c0-core-session-service.md
[c0-bpm]: core-l0/c0-basic-privilege-model.md
[c1-api]: core-l1/c1-regapi.md
[c0-pvg]: core-l0/c0-basic-privilege-model.md
[c0-noti]: core-l0/c0-notification.md
[c1-buk]: core-l1/c1-bulk-backup-restore.md
[c1-gde]: core-l1/c1-general-data-entity.md
[c1-nti]: core-l1/c1-notify.md
[c2-dmn]: core-l2/c2-domains.md
[c2-pvg]: core-l2/c2-biz-privilege-model.md
[f0-wx-pay]: func-l0/f0-weixin-payment.md
[f0-wx-pay]: func-l0/f0-weixin-notify.md
[f0-zfb]: func-l0/f0-alipay.md
[f0-aliyun]: func-l0/f0-aliyun.md
[f1-pay]: func-l1/f1-payment.md
[th0-ovw]: thing-l1/th1-overview.md
[th0-data]: thing-l1/th1-data.md
[th0-thjs]: thing-l1/th1-thing-json.md
[w0-api]: webs-l0/w0-api-overview.md
[w0-saa]: webs-l0/w0-site-auth-api.md
[w0-site]: webs-l0/w0-site.md
[w1-shop]: webs-l1/w1-shop.md
