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
HTTP GET /api/auth/site
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
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
}
%COPY:
obj ~/www -cqn
%END%
```

--------------------------------------
## `/auth/checkme`获取当前会话账户信息

### 请求头

```bash
HTTP GET /api/auth/checkme
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
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
}
%COPY:
www checkme id:${http-qs-site?unkonw} ${http-qs-ticket?-nil-} -ajax -cqn
%END%
```

--------------------------------------
## `/auth/setme`设置当前账户元数据

### 请求头

```bash
HTTP POST /api/auth/setme
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
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
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
HTTP GET /api/auth/getaccount
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
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
}
%COPY:
www account id:${http-qs-site?unkonw} ${http-qs-uid?-nil-} -ajax -cqn
%END%
```

--------------------------------------
## `/auth/login_by_wxcode`微信自动登录

### 请求头

```bash
HTTP GET /api/auth/login_by_wxcode
#---------------------------------
# Query String
site : "34t6..8aq1"     #【必】站点的ID
code : "34t6..8aq1"     #【必】微信权鉴码
#【选】权鉴码类型
#  - gh : 公众号
#  - mp : 小程序 
ct   : "mp"
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    "ticket" : "34..a1", // 会话票据
    "uid" : ID,          // 用户 ID
    "unm" : "xiaobai",   // 用户登录名
    // 用户详细数据
    "me" : {
      "id" : ID,          // 用户 ID
      "nm" : "xiaobai",   // 用户登录名
      "role"  : "user",   // 角色
      "nickname" : "xx",  // 用户昵称
      "thumb" : "id:xx",  // 用户头像
      "phone" : "139..",  // 手机号
      "email" : "x@x.x",  // 邮箱
      "sex" : 1   // 性别。0:未知，1:男, 2:女
    },
    "grp" : "xiaobai",   // 用户主组
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.www.api.auth.fail_login",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.auth.login.invalid.wxCode` : 错误的微信权鉴码
- `e.auth.login.invalid.wxCodeType` : 微信权鉴码类型错误
- `e.www.api.auth.fail_login` : 会话创建失败


### 初始化脚本

```bash
# API(auth): 微信自动登录
@FILE .regapi/api/auth/login_by_wxcode
{
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
}
%COPY:
httpparam -in id:${id} \
  | run www auth $${site} $${code?} -wxcode $${ct?mp} -ajax -cqn
%END%
```

--------------------------------------
## `/auth/login_by_passwd`账号密码登录

### 请求头

```bash
HTTP POST /api/auth/login_by_passwd
#---------------------------------
# Query String
site   : "34t6..8aq1"     #【必】站点的ID
name   : "xiaobai"        #【必】用户登录名
passwd : "xxxxxxx"        #【必】密码（明文）
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    "ticket" : "34..a1", // 会话票据
    "uid" : ID,          // 用户 ID
    "unm" : "xiaobai",   // 用户登录名
    // 用户详细数据
    "me" : {
      "id" : ID,          // 用户 ID
      "nm" : "xiaobai",   // 用户登录名
      "role"  : "user",   // 角色
      "nickname" : "xx",  // 用户昵称
      "thumb" : "id:xx",  // 用户头像
      "phone" : "139..",  // 手机号
      "email" : "x@x.x",  // 邮箱
      "sex" : 1   // 性别。0:未知，1:男, 2:女
    },
    "grp" : "xiaobai",   // 用户主组
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.www.api.auth.fail_login",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.auth.account.noexists` : 账户不存在
- `e.auth.login.invalid.passwd` : 密码错误
- `e.auth.login.NoSaltedPasswd` : 密码未加盐
- `e.www.api.auth.fail_login` : 会话创建失败


### 初始化脚本

```bash
# API(auth): 账号密码登录
@FILE .regapi/api/auth/login_by_passwd
{
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
}
%COPY:
httpparam -in id:${id} \
  | run www auth $${site} $${name?} -p $${passwd?} -ajax -cqn
%END%
```

--------------------------------------
## `/auth/login_by_phone`短信密码登录

### 请求头

```bash
HTTP POST /api/auth/login_by_phone
#---------------------------------
# Query String
site   : "34t6..8aq1"     #【必】站点的ID
name   : "139..."         #【必】手机号
vcode  : "3542132"        #【必】短信密码
scene : "auth"            #【选】场景
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    "ticket" : "34..a1", // 会话票据
    "uid" : ID,          // 用户 ID
    "unm" : "xiaobai",   // 用户登录名
    // 用户详细数据
    "me" : {
      "id" : ID,          // 用户 ID
      "nm" : "xiaobai",   // 用户登录名
      "role"  : "user",   // 角色
      "nickname" : "xx",  // 用户昵称
      "thumb" : "id:xx",  // 用户头像
      "phone" : "139..",  // 手机号
      "email" : "x@x.x",  // 邮箱
      "sex" : 1   // 性别。0:未知，1:男, 2:女
    },
    "grp" : "xiaobai",   // 用户主组
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.www.api.auth.fail_login",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.auth.login.NoPhoneOrEmail` : 不是手机号
- `e.auth.captcha.invalid` : 短信密码错误
- `e.www.api.auth.fail_login` : 会话创建失败


### 初始化脚本

```bash
# API(auth): 短信密码登录
@FILE .regapi/api/auth/login_by_phone
{
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
}
%COPY:
httpparam -in id:${id} \
  | run www auth $${site} $${name?} -scene $${scene?auth} -v $${vcode?} \
    -ajax -cqn
%END%
```

--------------------------------------
## `/auth/login_by_email`邮件密码登录

### 请求头

```bash
HTTP POST /api/auth/login_by_email
#---------------------------------
# Query String
site   : "34t6..8aq1"     #【必】站点的ID
name   : "x@x.x"          #【必】邮箱地址
vcode  : "3542132"        #【必】邮箱密码
scene : "auth"            #【选】场景
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    "ticket" : "34..a1", // 会话票据
    "uid" : ID,          // 用户 ID
    "unm" : "xiaobai",   // 用户登录名
    // 用户详细数据
    "me" : {
      "id" : ID,          // 用户 ID
      "nm" : "xiaobai",   // 用户登录名
      "role"  : "user",   // 角色
      "nickname" : "xx",  // 用户昵称
      "thumb" : "id:xx",  // 用户头像
      "phone" : "139..",  // 手机号
      "email" : "x@x.x",  // 邮箱
      "sex" : 1   // 性别。0:未知，1:男, 2:女
    },
    "grp" : "xiaobai",   // 用户主组
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.www.api.auth.fail_login",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.auth.login.NoPhoneOrEmail` : 不是手机号
- `e.auth.captcha.invalid` : 短信密码错误
- `e.www.api.auth.fail_login` : 会话创建失败


### 初始化脚本

```bash
# API(auth): 邮件密码登录
@FILE .regapi/api/auth/login_by_email
{
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
}
%COPY:
httpparam -in id:${id} \
  | run www auth $${site} $${name?} -scene $${scene?auth} -v $${vcode?} \
    -ajax -cqn
%END%
```

--------------------------------------
## `/auth/bind_account`绑定手机/邮箱

### 请求头

```bash
HTTP POST /api/auth/bind_account
#---------------------------------
# Query String
site   : "34t6..8aq1"     #【必】站点的ID
name   : "x@x.x"          #【必】手机号或邮箱地址
vcode  : "3542132"        #【必】短信或邮箱密码
ticket : "34t6..8aq1"     # 【必】登录会话的票据
scene : "auth"            #【选】场景
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    "ticket" : "34..a1", // 会话票据
    "uid" : ID,          // 用户 ID
    "unm" : "xiaobai",   // 用户登录名
    // 用户详细数据
    "me" : {
      "id" : ID,          // 用户 ID
      "nm" : "xiaobai",   // 用户登录名
      "role"  : "user",   // 角色
      "nickname" : "xx",  // 用户昵称
      "thumb" : "id:xx",  // 用户头像
      "phone" : "139..",  // 手机号
      "email" : "x@x.x",  // 邮箱
      "sex" : 1   // 性别。0:未知，1:男, 2:女
    },
    "grp" : "xiaobai",   // 用户主组
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.www.api.auth.fail_login",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.auth.login.NoPhoneOrEmail` : 不是手机号
- `e.auth.captcha.invalid` : 短信/邮件密码错误
- `e.www.api.auth.fail_login` : 会话创建失败


### 初始化脚本

```bash
# API(auth): 绑定手机/邮箱
@FILE .regapi/api/auth/bind_account
{
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
}
%COPY:
httpparam -in id:${id} \
  | run www auth $${site} $${name?} -scene $${scene?auth} -v $${vcode?} \
    -ticket $${ticket?-nil-} \
    -ajax -cqn
%END%
```

--------------------------------------
## `/auth/get_sms_vcode`获取短信验证码

### 请求头

```bash
HTTP GET /api/auth/get_sms_vcode
#---------------------------------
# Query String
site    : "34t6..8aq1"     #【必】站点的ID
account : "x@x.x"          #【必】手机号
captcha : "3542"           #【必】图形验证码（防机器人）
scene   : "auth"            #【选】场景
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    "scene" : "auth",      // 验证码场景
    "account" : "139..",   // 手机号
    "retry" : 0,           // 已经重试次数
    "maxRetry" : 3,        // 最大重试次数
    "expi" : AMS           // 过期时间（绝对毫秒数）
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.auth.captcha.invalid",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.auth.captcha.invalid` : 图形验证码错误
- `e.www.captcha.fail_send_by_sms` : 短信密码发送错误

### 初始化脚本

```bash
# API(auth): 绑定手机/邮箱
@FILE .regapi/api/auth/bind_account
{
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
}
%COPY:
httpparam -in id:${id} \
  | run www auth $${site} $${name?} -scene $${scene?auth} -v $${vcode?} \
    -ticket $${ticket?-nil-} \
    -ajax -cqn
%END%
```

--------------------------------------
## `/auth/get_email_vcode`获取邮箱验证码

### 请求头

```bash
HTTP GET /api/auth/get_email_vcode
#---------------------------------
# Query String
site    : "34t6..8aq1"     #【必】站点的ID
account : "x@x.x"          #【必】邮件地址
captcha : "3542"           #【必】图形验证码（防机器人）
scene   : "auth"            #【选】场景
```

### 响应成功(JSON)

```js
{
  ok : true,
  data : {
    "scene" : "auth",      // 验证码场景
    "account" : "139..",   // 手机号
    "retry" : 0,           // 已经重试次数
    "maxRetry" : 3,        // 最大重试次数
    "expi" : AMS           // 过期时间（绝对毫秒数）
  }
}
```

### 响应失败(JSON)

```js
{
  ok : false,
  errCode : "e.auth.captcha.invalid",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.auth.captcha.invalid` : 图形验证码错误
- `e.www.captcha.fail_send_by_email` : 邮件密码发送错误

### 初始化脚本

```bash
# API(auth): 获取邮箱验证码
@FILE .regapi/api/auth/get_email_vcode
{
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
}
%COPY:
www captcha ${http-qs-site} ${http-qs-scene?auth} ${http-qs-account?} \
  -cap ${http-qs-captcha} \
  -as email -du 20 \
  -ajax -cqn
%END%
```

--------------------------------------
## `/auth/captcha`获取各个场景下的图形验证码

### 请求头

```bash
HTTP GET /api/auth/get_email_vcode
#---------------------------------
# Query String
site    : "34t6..8aq1"     #【必】站点的ID
account : "x@x.x"          #【必】邮件地址
size    : "100x50"         #【选】验证码图片尺寸
scene   : "robot"          #【选】场景
```

### 响应成功(image/png)

```
FF D8 FF E1 00 18 45 78 69 66 00 00 49 49 2A 00
08 00 00 00 00 00 00 00 00 00 00 00 FF EC 00 11
44 75 63 6B 79 00 01 00 04 00 00 00 50 00 00 FF
...
```

### 响应失败(JSON)

*应该不会失败*

### 初始化脚本

```bash
# API(auth): 获取图形验证码
@FILE .regapi/api/auth/captcha
{
   "http-header-Content-Type" : "image/png",
   "http-cross-origin" : "*"
}
%COPY:
www captcha ${http-qs-site} ${http-qs-scene?robot} ${http-qs-account?} \
  -size ${http-qs-size?100x50} \
  -as png
%END%
```

--------------------------------------
## `/auth/chpasswd`修改当前账户的用户名密码

### 请求头

```bash
HTTP POST /api/auth/chpasswd
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
   "http-header-Content-Type" : "text/json",
   "http-cross-origin" : "*"
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

### 请求头

```bash
HTTP GET /api/auth/logout
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
    "ticket" : "34..a1", // 会话票据
    "uid" : ID,          // 用户 ID
    "unm" : "xiaobai",   // 用户登录名
    // 用户详细数据
    "me" : {
      "id" : ID,          // 用户 ID
      "nm" : "xiaobai",   // 用户登录名
      "role"  : "user",   // 角色
      "nickname" : "xx",  // 用户昵称
      "thumb" : "id:xx",  // 用户头像
      "phone" : "139..",  // 手机号
      "email" : "x@x.x",  // 邮箱
      "sex" : 1   // 性别。0:未知，1:男, 2:女
    },
    "grp" : "xiaobai",   // 用户主组
  }
}
```

### 响应失败（JSON）

```js
{
  ok : false,
  errCode : "e.auth.ticked.noexist",
  msg : "xxx"
}
```
其中 `errCode` 可能的值包括：

- `e.www.site.noexists` : 站点不存在
- `e.auth.ticked.noexist` : 会话票据不存在

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