# 命令简介

`webx` 用来处理域建站点的相关操作

# 用法

```bash
webx ~/www/website [-cqn] [-ajax] @{Filter} ...
#-----------------------------------------------------
# 其中
[-cqn]             # JSON 输出的格式化方式
[-ajax]            # 开启这个选项，则输出为 ajaxReturn 的包裹
```

# 过滤器列表

```bash
@captcha           # 【设】验证码
@session           # 查看当前会话信息
@auth              # 登录会话，支持账号密码/邮箱/手机/微信小程序/公众号等
@logout            # 注销当前会话 
@bindphone         # 【设】绑定手机
@bindemail         # 【设】绑定邮箱
@passwd            # 【设】修改当前账户密码
```
