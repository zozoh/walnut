---
title:验证码机制
author:zozoh
---

# 验证码机制的概述

在应用很多时候需要验证手机号，或者验证邮箱地址。为了发送验证信息，通常又需要图形验证码。
同时图形验证码又会应用在很多地方，除了注册，比如登录等需要预防机器人的地方。

因此我们需要一套通用验证码机制，这个机制假设:

- 一个账号（通常是手机号或者邮箱）只能同时进行一次验证，一个账号在多个地点进行验证，有可能会有不可预知的失败
- 不依赖 session，因为并不能假设当前账号已经登录并创建了会话信息了。

# 数据结构

验证码存放的路径为 `/var/vcode/域/场景/账号`

```
# 图片验证码
/var/vcode/site0/captcha/13924452341
/var/vcode/site0/captcha/abc@xyz.com

# 注册用手机验证码
/var/vcode/site0/signup/13924452341

# 邮件验证码
/var/vcode/site0/signup/abc@xyz.com
```

- *域* : 即操作用户的主组
- *场景* : 表示本次验证的应用场景，比如 *signup*，又比如 *chpasswd*，还比如 *login*

一个验证码文件的元数据格式:

```
nm    : "13910445672"    # 文件名就是手机号
expi  : 1478..           # 验证码文件过期时间，通常为 10min
// 短信验证
v_code  : "xxxx"         # 验证码
v_retry : 0              # 已经验证的次数
v_remax : 5              # 最大验证次数
```

# 使用方式

系统内置了 HTTP 访问接口，可以支持如下验证码操作

## 获取图形验证码: /u/vcode/captcha/get

GET 参数

```
d  : "walnut"      # 域名
a  : "139xxx"      # 手机号
```

返回图片的二进制流，格式为 `image/png`:

```
0a 89 cd 6c 89 34 09 f1 ..
0a 89 cd 6c 89 34 09 f1 ..
0a 89 cd 6c 89 34 09 f1 ..
```

例如，你可以在网页里嵌入 img 标签

```
<img src="/u/vcode/captcha/get?d=hope&a=13910110055">

服务器会:
1. 生成4位的数字验证码
2. 将这个验证码变成图片流输出下去
3. 可以通过命令 vcode check hope/captcha/13910110055 xxxx 来验证
   命令什么都不输出，则表示正确
   如果输入 e.cmd.vcode.check_fail 则表示验证失败
```

当然，一般图形验证码是给发短信或者邮件用的。因此后面你可以看到，
获取手机验证码和邮箱验证码的时候，也都需要一个图片验证码。

总之你可以把图片验证码用到任何你想防止机器人的地方，通过 `vcode check`
你在服务器端就能轻松检查发上来的验证码对不对.

## 获取手机验证码: /u/vcode/phone/get

GET 参数

```
d  : "walnut"      # 域名
s  : "login"       # 场景
a  : "139xxx"      # 手机号
t  : "xxx"         # 图片验证码，防机器人
```

返回 AJAX Return:

```
成功:
{
    ok   : true,
    data : true
}
失败
{
    ok   : true,
    data : false
}
```

例如，你发送请求 

```
http://xxx.com/u/vcode/phone/get?d=hope&s=login&a=13910110055&t=6894

服务器会:
1. 验证图片验证码 6894 是不是属于手机号 13910110055
2. 如果属于，则生成一个 6 位数字验证码发向目标手机，默认10分钟有效期
3. 可以通过命令 vcode check hope/login/13910110055 6894 来验证
   命令什么都不输出，则表示正确
   如果输入 e.cmd.vcode.check_fail 则表示验证失败
```

## 获取邮件验证码: /u/vcode/email/get

GET 参数

```
d  : "walnut"      # 域名
s  : "login"       # 场景
a  : "abc@qq.com"  # 邮箱
t  : "xxx"         # 图片验证码，防机器人
```

返回 AJAX Return:

```
成功:
{
    ok   : true,
    data : true
}
失败
{
    ok   : true,
    data : false
}
```

例如，你发送请求 

```
http://xxx.com/u/vcode/email/get?d=hope&s=login&a=abc@qq.com&t=6894

服务器会:
1. 验证图片验证码 6894 是不是属于邮箱 abc@qq.com
2. 如果属于，则生成一个 8 位数字字符验证码发向目标邮箱，默认2天有效期
3. 可以通过命令 vcode check hope/login/abc@qq.com 6894 来验证
   命令什么都不输出，则表示正确
   如果输入 e.cmd.vcode.check_fail 则表示验证失败
```




