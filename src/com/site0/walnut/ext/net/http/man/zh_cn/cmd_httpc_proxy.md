# 过滤器简介

`@headers` 向上下文内容设置一个代理
 

# 用法

```bash
httpc {URL} @proxy
  [{PORT}]       # 代理的接口
  [{HOST}]       # 代理的地址，默认为 127.0.0.1
  [-auto]        # 自动检查系统代理设置
  [-f /path.json]# 指定一个代理配置文件，如果没用就无视
                 # 如果什么都不声明，默认会检查 ~/.domain/proxy.json
  [-socket]      # 指定代理为 Socket 代理，否则当作 Http 代理
```

# 示例

```bash
# 发送一个代理请求
httpc https://facebook.com @proxy 10080
```

# 注意：不要直连公网明文代理

`@proxy` 用的是 JDK 原生的 `java.net.Proxy`，**只能说明文 HTTP CONNECT / SOCKS，没有传输加密**。
如果代理在公网上又开了 Basic 认证，那么口令是**明文过网**的，链路上任何人都抓得到。

所以正确的姿势是：**代理只连回环地址，加密的事交给本地隧道**。

```bash
# 推荐：本机先起一条加密隧道（Reality / Hysteria2 / TLS 代理等），
# 它监听 127.0.0.1:7899，httpc 只跟回环说话
httpc https://api.openai.com/v1/models @proxy 7899 127.0.0.1

# 不推荐：直连公网上的明文代理（口令会裸奔）
# httpc https://api.openai.com/v1/models @proxy 8777 jump.example.com
```

> 一句话：**口令传输的安全性是隧道的事，不是 `httpc` 的事。**
> `httpc` 不负责加密，也不该为此增加认证参数。
