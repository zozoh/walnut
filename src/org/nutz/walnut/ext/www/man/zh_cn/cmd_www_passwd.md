命令简介
=======

`www passwd` 修改站点用户的密码

用法
=======

```bash
www passwd
    [id:xxx]        # 【必须】站点主目录路径
    [PASSWD]        # 【必须】新密码
    [-u 1391..]     # 【选1】用户的登录名/手机号/邮箱/ID
    [-ticket xxx]   # 【选1】用户的会话票据
```

> 如果是 -u 模式，则当前操作会话必须为站点管理员或者 root/op组成员
    
示例
=======

```bash
// 修改用户密码，手动指定新密码
www passwd ~/www 123456 -u xiaobai

// 修改自己密码
www passwd ~/www 123456 -ticket 45u..91a
```
    
    
