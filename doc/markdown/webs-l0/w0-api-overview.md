---
title: 域站点接口概览
author: zozohtnt@gmail.com
---

# 全局·基础

  URL   | Method| Date |Auth | Description
--------|-------|------|-----|----------
`thumb` | `GET` |`img` |`---`| 获取对象缩略图
`media` | `GET` |`img` |`---`| 获取对象本身
`objs`  | `GET` |`json`|`---`| 我的收货地址

# 账户权鉴

  URL                 |Method| Date |Auth | Description
----------------------|------|------|-----|----------
`auth/accounts`       |`POST`|`json`|`Pvg`| 账户列表（带翻页）
`auth/bind_account`   |`GET` |`json`|`---`| 绑定手机/邮箱
`auth/captcha`        |`GET` |`json`|`---`| 获取图形验证码
`auth/checkme`        |`POST`|`json`|`---`| 校验 Ticket
`auth/get_email_vcode`|`POST`|`json`|`---`| 发送邮件密码
`auth/get_sms_vcode`  |`GET` |`json`|`---`| 发送短信密码
`auth/getaccount`     |`GET` |`json`|`Pvg`| 获取指定账户信息
`auth/login_by_email` |`POST`|`json`|`---`| 用邮箱密码登录
`auth/login_by_passwd`|`POST`|`json`|`---`| 用密码登录
`auth/login_by_phone` |`GET` |`json`|`---`| 用短信密码登录
`auth/login_by_wxcode`|`GET` |`json`|`---`| 用微信票据登录
`auth/logout`         |`POST`|`json`|`---`| 注销当前 Ticket
`auth/chpasswd`       |`POST`|`json`|`Yes`| 重置密码
`auth/setme`          |`GET` |`json`|`Yes`| 更新个人资料
`auth/site`           |`GET` |`json`|`---`| 获取站点信息

# 通用·数据集

  URL              |Method| Date |Auth | Description
-------------------|------|------|-----|----------
`thing/create`     |`POST`|`json`|`---`| 创建数据记录
`thing/delete`     |`GET` |`json`|`---`| 删除数据数据
`thing/file/add`   |`GET` |`json`|`---`| 上传附件
`thing/file/get`   |`GET` |`json`|`---`| 获取附件元数据
`thing/file/read`  |`GET` |`bin` |`---`| 获取附件内容
`thing/file/remove`|`GET` |`json`|`---`| 删除附件
`thing/get`        |`GET` |`json`|`---`| 获取记录元数据
`thing/list`       |`GET` |`json`|`---`| 列出全部数据记录
`thing/query`      |`GET` |`json`|`---`| 查询数据记录
`thing/update`     |`POST`|`json`|`---`| 更新数据记录

# 订单及支付

  URL            |Method| Date |Auth | Description
-----------------|------|------|-----|----------
`pay/buy`        |`POST`|`json`|`Yes`| 创建订单和支付单
`pay/check`      |`GET` |`json`|`Yes`| 检查支付结果
`pay/pay`        |`GET` |`json`|`Yes`| 根据订单创建支付单
`pay/re_paypal`  |`GET` |`json`|`---`| 处理 paypal 回调
`pay/re_wx`      |`GET` |`json`|`---`| 处理微信支付回调
`pay/re_zfb`     |`GET` |`json`|`---`| 处理支付宝回调

# 实体·购物车

  URL                |Method| Date |Auth | Description
---------------------|------|------|-----|----------
`entity/buy/all`     |`GET` |`json`|`Yes`| 获取购物全部商品
`entity/buy/clean`   |`GET` |`json`|`Yes`| 清空购物车
`entity/buy/it`      |`GET` |`json`|`Yes`| 加减购物车商品
`entity/buy/rm`      |`GET` |`json`|`Yes`| 从购物车删除

# 实体·收藏夹

  URL                |Method| Date |Auth | Description
---------------------|------|------|-----|----------
`entity/favor/all`   |`GET` |`json`|`Yes`| 获取全部收藏（翻页）
`entity/favor/it`    |`GET` |`json`|`Yes`| 收藏/取消一个对象
`entity/favor/when`  |`GET` |`json`|`Yes`| 判断是否收藏（可多个）

# 实体·打分

  URL                |Method| Date |Auth | Description
---------------------|------|------|-----|----------
`entity/score/cancel`|`GET` |`json`|`Yes`| 取消打分
`entity/score/get`   |`GET` |`json`|`Yes`| 获取指定对象分数（可多个）
`entity/score/it`    |`GET` |`json`|`Yes`| 为对象打分


# 实体·消息流

  URL            |Method| Date |Auth | Description
-----------------|------|------|-----|----------
`newsfeed/clean` |`POST`|`json`|`Yes`| 清除所有已读消息
`newsfeed/mine`  |`GET` |`json`|`Yes`| 我的所有消息
`newsfeed/read`  |`GET` |`json`|`Yes`| 标记消息已读未读
`newsfeed/remove`|`GET` |`json`|`---`| 删除消息
`newsfeed/star`  |`GET` |`json`|`---`| 给消息加减星

# 地理及地址

  URL           |Method| Date |Auth | Description
----------------|------|------|-----|----------
`lbs/countries` |`GET` |`json`|`Yes`| 获取全部国家列表和编码

# 收货地址

  URL            | Method | Date |Auth | Description
-----------------|--------|------|-----|----------
`address/create` | `POST` |`json`|`Yes`| 创建收货地址
`address/delete` | `GET`  |`json`|`Yes`| 删除收货地址
`address/mine`   | `GET`  |`json`|`Yes`| 我的收货地址
`address/update` | `POST` |`json`|`Yes`| 更新收货地址

# 微信整合

  URL             |Method| Date |Auth | Description
------------------|------|------|-----|----------
`weixin/oauth2_gh`|`GET` |`302` |`---`| 生成微信跳板URL
`weixin/jssdk`    |`GET` |`json`|`---`| JSSDK的签名
