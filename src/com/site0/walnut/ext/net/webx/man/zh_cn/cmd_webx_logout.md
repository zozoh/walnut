# 过滤器简介

`@logout` 注销当前会话

# 用法

```bash
webx ~/www/website @logout [xxxxx]  # 当前会话票据
```

> 如果是 wxcode 模式验证码模式登录，指定 `-p` 参数可以设置账户初始化密码

# 示例

```bash
# 注销当前会话信息
webx ~/www/website -cqn @logout 0p63...
# > {ticket:'xxx', user: {id,name...}, expiAt:1598.., env: {...}}

# 注销当前会话信息，并作为 ajax 结果返回
webx ~/www/website -cqn -ajax @logout 0p63...
# > {ok:true, data:{ticket:'xxx', user: {id,name...}, expiAt:1598.., env: {...}}}
```
