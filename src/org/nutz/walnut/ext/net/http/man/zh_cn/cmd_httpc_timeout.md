# 过滤器简介

`@headers` 向上下文内容设置连接过期时间
 

# 用法

```bash
httpc {URL} @timeout
  [{readTimeout}]       # 读取内容的过期时间（毫秒），如果<=0 则无效
  [{connTimeout}]       # 建立连接的过期时间（毫秒），如果<=0 则无效
```

# 示例

```bash
# 设置读取过期时间 5秒
httpc https://facebook.com @timeout 5000

# 设置读取过期时间 5秒， 连接过期时间 2秒
httpc https://facebook.com @timeout 5000 2000

# 仅设置连接过期时间 2秒
httpc https://facebook.com @timeout 0 2000
```
