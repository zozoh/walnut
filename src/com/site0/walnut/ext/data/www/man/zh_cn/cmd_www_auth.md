命令简介
======= 

`www auth` 登录/绑定手机/注册
    

用法
=======

```bash
www auth
    [id:xxx]        # 【必须】站点主目录路径
    [1391..]        # 【必须】用户的登录名/手机号/邮箱/微信票据
    [-p xxx]        # 【四选】登录密码
    [-v xxx]        # 【四选】验证码，只有手机/邮箱时有效
    [-wxcode mp|gh] # 【四选】表示账号为微信登录票据代码类型
                    #  mp: 小程序, gh: 公众号
    [-subscribe]    # 【选】开启这个选项，标识只有关注gh才给登陆
    [-ticket xxx]   # 【选】表示当前已登录的会话需要绑定手机或邮箱
                    # 这个选项必须集合 -v 参数，以便获取验证码
    [-create]       # 【四选】直接为指定账户创建会话
    [-cqn]          # JSON 输出的格式化方式
    [-ajax]         # 开启这个选项，则输出为 ajaxReturn 的包裹
```

> 如果是 wxcode 模式验证码模式登录，指定 `-p` 参数可以设置账户初始化密码

示例
=======

```bash
# 密码登录
www auth id:xxx 13910440054 -p 123456

# 短信验证码登录
www auth id:xxx 13910440054 -v 3547

# 邮件验证码登录
www auth id:xxx zozoh@qq.com -v c56y889a

# 微信票据登录
www auth id:xxx f6d..4ace

# 绑定手机
www auth id:xxx 13910440054 -v 3547 -ticket 56bc..a239

# 绑定邮箱
www auth id:xxx zozoh@qq.com -v c56y889a -ticket 56bc..a239
```