命令简介
=======

`www passwd` 修改站点用户的密码

用法
=======

```bash
www passwd
    [id:xxx]        # 【必须】站点主目录路径
    [PASSWD]        # 【选2】新密码
    [-check {..}]   # 【选2】密码校验表单
    [-u 1391..]     # 【选1】用户的登录名/手机号/邮箱/ID
    [-ticket xxx]   # 【选1】用户的会话票据
    [-init]         # 【选】本次设置的是初始化密码，即
                    # 如果用户已经有了密码，则无视
```

如果是 -u 模式，则当前操作会话必须为站点管理员或者`root/op`组成员

如果开启了`-check`模式，则会读取参数JSON（或者从管线里读取），这个JSON表单的形式类似:

```js
{
  "oldpwd" : "123456",      // 【必】旧密码
  "newpwd" : "654321"       // 【必】新密码
}
```
    
示例
=======

```bash
# 修改用户密码，手动指定新密码
www passwd ~/www 123456 -u xiaobai

# 修改自己密码
www passwd ~/www 123456 -ticket 45u..91a

# 校验模式修改自己的密码
www passwd ~/www -ticket 43..u7 -check '{oldpwd:"123456",newpwd:"654321"}'
```
    
    
