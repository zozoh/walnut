# 过滤器简介

`@passwd` 修改当前账户密码
    

# 用法

```bash
webx ~/www/website @passwd
    [xxxxx]            # 【选】当前会话票据
    [-name xxxx]       # 【选】手机号/登录名/邮箱
                       # 只有域管理员，才能直接指定用户名
    [-passwd ***]      # 旧静态密码（如果没设置过，则不需要）
    [-strict]          # 严格模式，必须需要动态密码
    [-captcha 6784]    # 动态密码
    [-newpass ***]     # 【必】新密码
```

> 如果是 wxcode 模式验证码模式登录，指定 `-p` 参数可以设置账户初始化密码

# 示例

```bash
webx ~/www/website @auth_wx mp 0e1gBU000qmhUT1wXh100h8QKd0gBU0M -ajax
```