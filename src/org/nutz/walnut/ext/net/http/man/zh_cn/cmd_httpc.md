# 命令简介 

`httpc` 作为 http 客户端发送请求

# 用法

```bash
httpc {URL} [-rhH] [@FILTER ...]
#------------------------------------
# 参数
-r      # 跟随重定向连接
-h      # 输入响应内容的时候，也要打印头部
-H      # 表示仅仅显示头部
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

    
    
