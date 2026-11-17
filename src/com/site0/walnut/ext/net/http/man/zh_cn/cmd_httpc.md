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
@body        # 设置请求的BODY内容
@header      # 设置请求的头 
@method      # 指明 HTTP 的请求方法
@multipart   # 设置一个`multipart`表单的内容
@output      # 设定响应的输出目标
@params      # 设置请求的参数表
@proxy       # 设定代理服务器
@query       # 设置请求的QueryString
@timeout     # 超时设定
```

# 安全提醒（用代理时必读）

`@proxy` 走的是 JDK 原生 `java.net.Proxy`，**只有明文 HTTP CONNECT / SOCKS，没有加密**。
把 `@proxy` 指向公网上的明文代理，Basic 认证口令会**明文过网**。

正确做法：本机（或同信誉域内）先起一条加密隧道（Reality / Hysteria2 / TLS 代理之类），
让 `httpc` 连 `127.0.0.1:<端口>`；口令和证书都归隧道管。

```bash
# 本地隧道监听 127.0.0.1:7899
httpc https://api.openai.com/v1/models @proxy 7899 127.0.0.1
```

详见 `man cmd_httpc_proxy`。

    
    
