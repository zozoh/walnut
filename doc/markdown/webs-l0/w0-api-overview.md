---
title: 域站点接口概览
author: zozohtnt@gmail.com
---

# 全局·基础

  URL   | Method| Date | Description
--------|-------|------|----------
`thumb` | `GET` |`img` | 获取对象缩略图
`media` | `GET` |`img` | 获取对象内容
`read`  | `GET` |`img` | 读取对象内容
`objs`  | `GET` |`json`| 根据ID获取对象列表

> [全局基础接口](w0-api-base.md)

# 账户权鉴

  URL                 |Method| Date | Description
----------------------|------|------|----------
`auth/accounts`       |`POST`|`json`| 账户列表（带翻页）
`auth/bind_account`   |`GET` |`json`| 绑定手机/邮箱
`auth/captcha`        |`GET` |`json`| 获取图形验证码
`auth/checkme`        |`POST`|`json`| 校验 Ticket
`auth/get_email_vcode`|`POST`|`json`| 发送邮件密码
`auth/get_sms_vcode`  |`GET` |`json`| 发送短信密码
`auth/getaccount`     |`GET` |`json`| 获取指定账户信息
`auth/login_by_email` |`POST`|`json`| 用邮箱密码登录
`auth/login_by_passwd`|`POST`|`json`| 用密码登录
`auth/login_by_phone` |`GET` |`json`| 用短信密码登录
`auth/login_by_wxcode`|`GET` |`json`| 用微信票据登录
`auth/logout`         |`POST`|`json`| 注销当前 Ticket
`auth/chpasswd`       |`POST`|`json`| 重置密码
`auth/setme`          |`GET` |`json`| 更新个人资料
`auth/site`           |`GET` |`json`| 获取站点信息

> [账户权鉴接口](w0-api-auth.md)

# 通用·数据集

  URL              |Method| Date | Description
-------------------|------|------|----------
`thing/create`     |`POST`|`json`| 创建数据记录
`thing/delete`     |`GET` |`json`| 删除数据数据
`thing/get`        |`GET` |`json`| 获取记录元数据
`thing/list`       |`GET` |`json`| 列出全部数据记录
`thing/query`      |`GET` |`json`| 查询数据记录
`thing/update`     |`POST`|`json`| 更新数据记录
`thing/file/add`   |`GET` |`json`| 上传附件
`thing/file/get`   |`GET` |`json`| 获取附件元数据
`thing/file/read`  |`GET` |`bin` | 获取附件内容
`thing/file/remove`|`GET` |`json`| 删除附件

> [通用数据接口](w0-api-thing.md)

# 订单及支付

  URL            |Method| Date | Description
-----------------|------|------|----------
`pay/buy`        |`POST`|`json`| 创建订单和支付单
`pay/check`      |`GET` |`json`| 检查支付结果
`pay/order`      |`GET` |`json`| 获取当前用户某份订单
`pay/pay`        |`GET` |`json`| 根据订单创建支付单
`pay/re_paypal`  |`GET` |`json`| 处理 paypal 回调
`pay/re_wx`      |`GET` |`json`| 处理微信支付回调
`pay/re_zfb`     |`GET` |`json`| 处理支付宝回调

> [订单及支付接口](w0-api-pay.md)

# 实体·购物车

  URL                |Method| Date | Description
---------------------|------|------|----------
`entity/buy/all`     |`GET` |`json`| 获取购物全部商品
`entity/buy/clean`   |`GET` |`json`| 清空购物车
`entity/buy/it`      |`GET` |`json`| 加减购物车商品
`entity/buy/rm`      |`GET` |`json`| 从购物车删除

> [实体购物车接口](w0-api-entity-buy.md)

# 实体·收藏夹

  URL                |Method| Date | Description
---------------------|------|------|----------
`entity/favor/all`   |`GET` |`json`| 获取全部收藏（翻页）
`entity/favor/it`    |`GET` |`json`| 收藏/取消一个对象
`entity/favor/when`  |`GET` |`json`| 判断是否收藏（可多个）

> [实体收藏夹接口](w0-api-entity-favor.md)

# 实体·打分

  URL                |Method| Date | Description
---------------------|------|------|----------
`entity/score/cancel`|`GET` |`json`| 取消打分
`entity/score/get`   |`GET` |`json`| 获取指定对象分数（可多个）
`entity/score/it`    |`GET` |`json`| 为对象打分


# 实体·消息流

  URL            |Method| Date | Description
-----------------|------|------|----------
`newsfeed/clean` |`POST`|`json`| 清除所有已读消息
`newsfeed/mine`  |`GET` |`json`| 我的所有消息
`newsfeed/read`  |`GET` |`json`| 标记消息已读未读
`newsfeed/remove`|`GET` |`json`| 删除消息
`newsfeed/star`  |`GET` |`json`| 给消息加减星

# 地理及地址

  URL           |Method| Date | Description
----------------|------|------|----------
`lbs/countries` |`GET` |`json`| 获取全部国家列表和编码
`lbs/cn`        |`GET` |`json`| 获取中国国家行政区信息

> [地理及地址接口](w0-api-lbs.md)

# 收货地址

  URL            | Method | Date | Description
-----------------|--------|------|----------
`address/create` | `POST` |`json`| 创建收货地址
`address/delete` | `GET`  |`json`| 删除收货地址
`address/mine`   | `GET`  |`json`| 我的收货地址
`address/update` | `POST` |`json`| 更新收货地址

# 微信整合

  URL             |Method| Date | Description
------------------|------|------|----------
`weixin/oauth2_gh`|`GET` |`302` | 生成微信跳板URL
`weixin/jssdk`    |`GET` |`json`| JSSDK的签名
