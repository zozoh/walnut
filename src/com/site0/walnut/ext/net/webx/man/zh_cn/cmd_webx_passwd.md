# 过滤器简介

`@passwd` 修改当前账户密码
    

# 用法

```bash
webx ~/www/website @passwd
    [-phone 139...]    # 【选1】手机号
    [-email x@xx.x]    # 【选1】邮箱
    [-name xxxx]       # 【选1】登录名
    [-passwd ***]      # 旧静态密码（如果没设置过，则不需要）
    [-strict]          # 严格模式，必须需要动态密码
    [-captcha 6784]    # 动态密码
```

> 如果是 wxcode 模式验证码模式登录，指定 `-p` 参数可以设置账户初始化密码

# 示例

```bash
webx ~/www/website @auth_wx mp 0e1gBU000qmhUT1wXh100h8QKd0gBU0M -ajax
```