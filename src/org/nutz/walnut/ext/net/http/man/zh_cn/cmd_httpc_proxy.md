# 过滤器简介

`@headers` 向上下文内容设置一个代理
 

# 用法

```bash
httpc {URL} @proxy
  [{PORT}]       # 代理的接口
  [{HOST}]       # 代理的地址，默认为 127.0.0.1
  [-socket]      # 指定代理为 Socket 代理，否则当作 Http 代理
```

# 示例

```bash
# 发送一个代理请求
httpc https://facebook.com @proxy 10080
```
