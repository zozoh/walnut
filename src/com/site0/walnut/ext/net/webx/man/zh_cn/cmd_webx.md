# 命令简介

`webx` 用来处理域建站点的相关操作

# 用法

```bash
webx ~/www/website @{Filter} ...
```

# 过滤器列表

```bash
@captcha           # 【设】验证码
@session           # 查看当前会话信息
@auth_wx           # 通过微信公众平台code验证账号身份
@auth_phone        # 【设】通过手机号验证账号身份
@auth_email        # 【设】通过邮箱验证账号身份
@signin            # 【设】通过用户名密码验证账号身份
@signout           # 【设】注销当前会话 
@bindphone         # 【设】绑定手机
@bindemail         # 【设】绑定邮箱
@passwd            # 【设】修改当前账户密码
```
