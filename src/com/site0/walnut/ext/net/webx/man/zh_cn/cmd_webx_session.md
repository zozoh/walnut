# 过滤器简介

`@session` 查看当前会话信息
    

# 用法

```bash
webx ~/www/website @session [xxxxx]  # 当前会话票据
```

# 示例

```bash
# 获取当前会话信息
webx ~/www/website -cqn @session 0p63...
# > {ticket:'xxx', user: {id,name...}, expiAt:1598.., env: {...}}

# 获取当前会话信息，并作为 ajax 结果返回
webx ~/www/website -cqn -ajax @session 0p63...
# > {ok:true, data:{ticket:'xxx', user: {id,name...}, expiAt:1598.., env: {...}}}
```