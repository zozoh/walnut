# 过滤器简介

`@auth_wx` 通过微信公众平台code验证账号身份
    

# 用法

```bash
webx ~/www/website @auth_wx
    [mp|gh]         # 【必】表示账号为微信登录票据代码类型
                    #  mp: 小程序, gh: 公众号
    [0e1..U0M]      # 微信票据
    [-subscribe]    # 【选】开启这个选项，标识只有关注gh才给登陆
    [-signin]       # 开启这个选项，则自动登录
    [-cqn]          # JSON 输出的格式化方式
    [-ajax]         # 开启这个选项，则输出为 ajaxReturn 的包裹
```

> 如果是 wxcode 模式验证码模式登录，指定 `-p` 参数可以设置账户初始化密码

# 示例

```bash
webx ~/www/website @auth_wx mp 0e1gBU000qmhUT1wXh100h8QKd0gBU0M -ajax
```