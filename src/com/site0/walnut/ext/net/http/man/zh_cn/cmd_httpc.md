# 命令简介 

`httpc` 作为 http 客户端发送请求

# 用法

```bash
httpc [{method}] {URL} [-rhH] [@FILTER ...]
#------------------------------------
# 参数
[{method}]  # 如果第一个参数是 get/post... 等，表示设置方法
{URL}       # 接着第二个参数就是 URL。当然，如果你第一个参数就是 URL
            # 那么本命令会根据上下文自动判断是 GET 还是 POST
            # 当然，你也可以在稍后用 @method 直接指定请求方法
-r          # 跟随重定向连接
-h          # 输入响应内容的时候，也要打印头部
-H          # 表示仅仅显示头部
```

   
它支持的过滤器有：

```bash
@method      # 指明 HTTP 的参数
@header      # 向上下文内容设置请求的头 
@query       # 向上下文内容设置请求的QueryString
@params      # 向上下文内容设置请求的参数表
@body        # 向上下文内容设置请求的BODY内容
@multipart   # 向上下文内容设置一个`multipart`表单的内容
```

    
    
