# 过滤器简介

`@send` 向第三方平台发送请求，并输出返回结果。

# 用法

```bash
xapi @send 平台名 账号名 请求名 [-vars JSON] [-proxy 路径] [-timeout 超时] [-force] [-cqn]
       # 平台名    # 应用平台名称
       # 账号名    # 平台的账号名
       # 请求名    # 平台的请求对象名称
  -vars      # 参数表的上下文 JSON，不给则从标准输入读取
  -proxy     # 指向 proxy 配置文件的路径，文件存在就启用代理，内容为 {type:"http",host,port}
             # 不指定会自动寻找 ~/.domain/proxy.json；除非指明 "none"
  -timeout   # 请求过期（秒），如 60:10（连接 60 秒:握手 10 秒）或 60
  -force     # 强制重新获取 Access-Token
  -cqn       # 输出的 JSON 格式化开关
```

# 示例

```bash
# 获取微信公众号用户的信息
xapi @send wxgh demo gh_user_info -vars 'openid:"xxx"'
# {
#     "subscribe": 1,
#     "openid": "o6_bmjrPTlm6_2sgVt7hMZOPfL2M",
#     "nickname": "Band",
#     ...
# }
```
