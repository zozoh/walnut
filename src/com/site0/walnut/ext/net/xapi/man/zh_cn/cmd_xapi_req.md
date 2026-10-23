# 过滤器简介

`@req` 获取向第三方平台请求的对象内容，可以输出 URL、curl 命令或直接发送。

# 用法

```bash
xapi @req 平台名 账号名 请求名 [-vars JSON] [-url] [-curl] [-N] [-cache] [-force] [-cqn]
       # 平台名    # 应用平台名称
       # 账号名    # 平台的账号名
       # 请求名    # 平台的请求对象名称
  -vars     # 参数表的上下文 JSON，不给则从标准输入读取
  -url      # 只输出请求的 URL（自动拼上基础路径和参数）
  -curl     # 输出 curl 调用的命令
  -N        # 输出的 curl 命令不换行
  -cache    # 只输出请求的缓存键
  -force    # 强制重新获取 Access-Token
  -cqn      # 输出的 JSON 格式化开关
```

# 示例

```bash
# 输出请求的 URL
xapi @req wxgh funcook gh_user_info -url -vars 'openid:"xxx"'
# https://api.weixin.qq.com/cgi-bin/user/info?access_token=xxx-xxx&openid=xxx&lang=zh_CN

# 输出 curl 命令
xapi @req wxgh funcook gh_user_info -curl -vars 'openid:"xxx"'
```
