---
title  : Walnut 文档目录
author : zozohtnt@gmail.com
---

# 关于

- [你应该怎么阅读这份文档][a0-guide]
- [写在前面的话][a0-intro]
- [关于Walnut的故事][a0-story]

# 核心概念

- [采用域用户登录系统][c0-abd]
- [基础账户授权模型][c0-bam]
- [基础权限模型][c0-pvg]
- [对象过期机制][c0-expi]
- [数据存储模型][c0-tab]
- [通用锁服务][c0-lock]
- [队列通知机制][c0-noti]

# 延申概念

- [Bulk:数据的备份与导出（概念文档）][c1-buk]
- [通用实体][c1-gde]
- [HTTP注册接口][c1-api]

# 扩展概念

- [域应用初始化][c2-init]
- [业务权限模型][c2-pvg]
- [域名映射机制][c2-dmn]
- [动态校验语法][c2-sxm]

# 内置功能

- [阿里云整合][f0-aliyun]
- [微信整合·通知][f0-wxnti]

# 扩展功能

- [通用支付模型][f1-pay]

# 通用数据集

- [Thing·数据结构][th0-data]
- [Thing·概述][th0-ovw]
- [Thing·配置文件详解][th0-thjs]

# webs-l0

- [域站点接口概览][w0-api]
- [接口·账户权鉴][w0-api-auth]
- [接口·全局基础][w0-api-base]
- [接口·实体·购物车][w0-api-en-buy]
- [接口·实体·收藏夹][w0-api-en-fav]
- [接口·地理及地址][w0-api-lbs]
- [接口·订单及支付][w0-api-pay]
- [接口·通用数据集][w0-api-thing]
- [域站点模型][w0-site]

# webs-l1

- [商城模型][w1-shop]


[a0-guide]: about/guide.md
[a0-intro]: about/introduction.md
[a0-story]: about/story.md
[c0-abd]: core-l0/c0-auth-by-domain.md
[c0-bam]: core-l0/c0-baice-auth-model.md
[c0-pvg]: core-l0/c0-basic-privilege-model.md
[c0-expi]: core-l0/c0-expi-objs.md
[c0-tab]: core-l0/c0-io-tree-and-bucket.md
[c0-lock]: core-l0/c0-lock-service.md
[c0-noti]: core-l0/c0-notification.md
[c1-buk]: core-l1/c1-bulk-backup-restore.md
[c1-gde]: core-l1/c1-general-data-entity.md
[c1-api]: core-l1/c1-regapi.md
[c2-init]: core-l2/c2-app-init.md
[c2-pvg]: core-l2/c2-biz-privilege-model.md
[c2-dmn]: core-l2/c2-domain-host-mapping.md
[c2-sxm]: core-l2/c2-syntax-match.md
[f0-aliyun]: func-l0/f0-aliyun.md
[f0-wxnti]: func-l0/f0-weixin-notify.md
[f1-pay]: func-l1/f1-payment.md
[th0-data]: thing-l0/th0-data.md
[th0-ovw]: thing-l0/th0-overview.md
[th0-thjs]: thing-l0/th0-thing-json.md
[w0-api]: webs-l0/w0-api.md
[w0-api-auth]: webs-l0/w0-api-auth.md
[w0-api-base]: webs-l0/w0-api-base.md
[w0-api-en-buy]: webs-l0/w0-api-entity-buy.md
[w0-api-en-fav]: webs-l0/w0-api-entity-favor.md
[w0-api-lbs]: webs-l0/w0-api-lbs.md
[w0-api-pay]: webs-l0/w0-api-pay.md
[w0-api-thing]: webs-l0/w0-api-thing.md
[w0-site]: webs-l0/w0-site.md
[w1-shop]: webs-l1/w1-shop.md
