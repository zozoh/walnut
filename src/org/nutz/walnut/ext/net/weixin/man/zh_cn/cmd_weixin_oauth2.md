# 命令简介 

`weixin oauth2` 用来处理微信公众号授权

# 用法

```bash
weixin {ConfName} oauth2 
    [URL]         # 需要重定向的 URL，会被微信服务器添加 code 参数
    [-wxopen]     # 表示生成给开放平台的重定向 URL
    [-scope snsapi_base]   # 【微信】获取用户信息级别
    [-state 999]           # 【微信】状态码
```

# 示例

```bash
# 生成重定向请求的 URL
demo@~$ weixin xxx oauth2 "http://redirect.com"

# 指定信息获取的级别
demo@~$ weixin xxx oauth2 "http://xxx" -scope snsapi_base

# 指定一个状态码
demo@~$ weixin xxx oauth2 "http://xxx" -state ANY
```