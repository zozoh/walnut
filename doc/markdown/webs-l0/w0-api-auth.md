---
title: 接口·账户权鉴
author: zozohtnt@gmail.com
---

--------------------------------------
# 动机：为什么要有站点账户授权接口

基于[基础账户授权模型][c0-bam]，对于域的某个站点，我们可以做更多的假设，以便封装更多的逻辑。

--------------------------------------
# 计划应用场景

- 用户自建的网站
- 微信/移动应用

--------------------------------------
# 设计思路与边界

- 采用[基础账户授权模型][c0-bam]的域站点用户管理会话和权限
- 提供一套标准的 HTTP 接口封装客户端操作

--------------------------------------
# 接口概览

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

--------------------------------------
# 接口详情

> 客户端通过 `Ajax` 方式调用接口

--------------------------------------
## `/auth/site`获取当前默认站点信息

### 请求头

```bash
HTTP GET /api/${YourDomain}/auth/site
#---------------------------------
# Query String
# 无
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    /*请参考《基础账户授权模型》站点数据结构*/
  }
}
```
### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "www.order.nil.products",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.www.order.nil.products` : 空订单
- `e.www.order.nil.accounts` : 没有设置账户库
- `e.www.order.nil.buyer_id` : 找不到买主账户信息
- `e.www.order.nil.pay_tp` : 未指定支付类型
- `e.www.order.invalid.pay_tp` : 错误的支付类型


### 初始化脚本

```bash
# API(auth): 取得当前站点信息
@FILE .regapi/api/auth/site
{
   "http-header-Content-Type" : "text/json"
}
%COPY:
obj ~/www -cqn
%END%
```

--------------------------------------
## `/auth/checkme`创建订单和支付单

### 请求头

```bash
HTTP GET /api/${YourDomain}/auth/checkme
#---------------------------------
# Query String
site   : "34t6..8aq1"     # 【必】站点的ID
ticket : "34t6..8aq1"     # 【必】登录会话的票据
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    /*请参考《基础账户授权模型》会话数据结构*/
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.auth.ticked.noexist",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.www.api.auth.nologin` : 未指定会话票据
- `e.auth.ticked.noexist` : 会话票据不存在


### 初始化脚本

```bash
# API(auth): 取得当前会话信息
@FILE .regapi/api/auth/checkme
{
   "http-header-Content-Type" : "text/json"
}
%COPY:
www checkme id:${http-qs-site?unkonw} ${http-qs-ticket?-nil-} -ajax -cqn
%END%
```

--------------------------------------
## `/auth/setme`设置当前账户元数据

### 请求头

```bash
HTTP POST /api/${YourDomain}/auth/setme
#---------------------------------
# Query String
site   : "34t6..8aq1"     # 【必】站点的ID
ticket : "34t6..8aq1"     # 【必】登录会话的票据
```

### 请求体:JSON

> 就是一个要修改的用户元数据 JSON

```js
{
  nickname : "小白",
  sex : 2
}
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    /*请参考《基础账户授权模型》会话数据结构*/
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.auth.ticked.noexist",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.www.api.auth.nologin` : 未指定会话票据
- `e.auth.ticked.noexist` : 会话票据不存在


### 初始化脚本

```bash
# API(auth): 修改当前会话账户元数据
@FILE .regapi/api/auth/setme
{
   "http-header-Content-Type" : "text/json"
}
%COPY:
cat id:${id} \
  | www checkme id:${http-qs-site?unkonw} ${http-qs-ticket?-nil-} -u -ajax -cqn
%END%
```

--------------------------------------
## `/auth/getaccount`获取指定账户信息

### 请求头

```bash
HTTP GET /api/${YourDomain}/auth/getaccount
#---------------------------------
# Query String
site : "34t6..8aq1"     # 【必】站点的ID
uid  : "34t6..8aq1"     # 【必】目标用户ID
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    /*请参考《基础账户授权模型》账户数据结构*/
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.auth.account.noexists",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.auth.account.noexists` : 目标账户不存在


### 初始化脚本

```bash
# API(auth): 获取指定账户信息
@FILE .regapi/api/auth/getaccount
{
   "http-header-Content-Type" : "text/json"
}
%COPY:
www account id:${http-qs-site?unkonw} ${http-qs-uid?-nil-} -ajax -cqn
%END%
```

--------------------------------------
## `/auth/login_by_wxcode`微信自动登录
--------------------------------------
## `/auth/login_by_passwd`账号密码登录
--------------------------------------
## `/auth/login_by_phone`短信密码登录
--------------------------------------
## `/auth/bind_account`绑定手机/邮箱
--------------------------------------
## `/auth/get_sms_vcode`获取短信验证码
--------------------------------------
## `/auth/get_email_vcode`获取邮箱验证码
--------------------------------------
## `/auth/captcha`获取各个场景下的图形验证码

--------------------------------------
## `/auth/chpasswd`修改当前账户的用户名密码

### 请求头

```bash
HTTP POST /api/${YourDomain}/auth/chpasswd
#---------------------------------
# Query String
site   : "34t6..8aq1"     # 【必】站点的ID
ticket : "34t6..8aq1"     # 【必】登录会话的票据
```

### 请求体:JSON

```js
{
  "oldpwd" : "123456",      // 【必】旧密码
  "newpwd" : "654321"       // 【必】新密码
}
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    /*请参考《基础账户授权模型》账户数据结构*/
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.auth.ticked.noexist",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.www.site.noexists` : 站点不存在
- `e.www.api.auth.nologin` : 未指定会话票据
- `e.auth.ticked.noexist` : 会话票据不存在
- `e.auth.passwd.old.invalid` : 旧密码不正确
- `e.auth.passwd.new.invalid` : 新密码不符合规范


### 初始化脚本

```bash
# API(auth): 获取指定账户信息
@FILE .regapi/api/auth/getaccount
{
   "http-header-Content-Type" : "text/json"
}
%COPY:
cat id:${id} \
  | www passwd ${http-qs-site?~/www} \
    -ticket ${http-qs-ticket?NoTicket} \
    -check \
    -ajax -cqn
%END%
```

--------------------------------------
## `/auth/logout`注销当前会话

--------------------------------------
# 相关知识点

- [基础账户授权模型][c0-bam]
- [核心会话服务][c0-css]

[c0-bam]: ../core-l0/c0-baice-auth-model.md
[c0-css]: ../core-l0/c0-core-session-service.md
[c0-pvg]: ../core-l0/c0-pvg-basic.md
[c2-pvg]: ../core-l2/c2-pvg-more.md
[f0-wxp]: ../func-l0/f0-weixin-payment.md
[f0-zfb]: ../func-l0/f0-alipay.md
[f1-pay]: ../func-l1/f1-payment.md