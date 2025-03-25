# 过滤器简介

`@auth` 通过微信公众平台小程序code验证账号身份
    

# 用法

```bash
webx ~/www/website @auth
    [-phone 139...]    # 【选1】手机号，可使用动态密码
    [-email x@xx.x]    # 【选1】邮箱，可使用动态密码
    [-name xxxx]       # 【选1】登录名
    [-passwd ***]      # 【选1】静态密码
    [-strict]          # 严格模式，必须需要动态密码
    [-captcha 6784]    # 动态密码
    [-wxmp 0e1..U0M]   # 微信小程序票据
    [-wxgh 0e1..U0M]   # 微信公众号票据
    [-autoadd]         # 自动模式，如果用户不存在，自动创建账号
```

# 示例

```bash
# 采用账号 + 静态密码登录
webx ~/www @auth -name xiaobai -passwd 123456

# 采用微信小程序票据登录，如果不存在则自动创建账号
webx ~/www/website @auth -wxmp 0c1pDC0w3eFRB43MUc4w3zU8Y82pDC0T

# 采用微信小程序票据登录，如果不存就报错，并不自动创建账号
webx ~/www/website @auth -strict -wxmp 0c1pDC0w3eFRB43MUc4w3zU8Y82pDC0T
```