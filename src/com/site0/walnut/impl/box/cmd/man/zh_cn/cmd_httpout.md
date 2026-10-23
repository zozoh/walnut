# 命令简介

`httpout` 将内容输出成标准的 HTTP 响应

是 regapi 体系里组装响应的最后一环：内容可以来自参数、文件对象、
全局桶 SHA1 或管道；支持状态码、响应头、ETag/304、Range 分段下载、
强制下载等完整 HTTP 语义。

# 用法

```bash
httpout [内容] [-body 路径或sha1] [-headers JSON] [-status 码] [-etag xxx]
        [-range bytes=a-b] [-mime 类型] [-base64] [-download [名称]] [-UserAgent ua]
  -headers    # 响应头，一段 JSON
  -status     # 状态码（默认 200）
  -body       # 响应体来源：对象路径，或 sha1:指纹（走全局桶），缺省读参数/管道
  -etag       # 配合客户端 If-None-Match，内容未变时输出 304
  -range      # 分段下载（Range 规范）
  -mime       # 内容类型（Content-Type）
  -base64     # 内容做 base64 编码（此时 mime 变为 text/plain）
  -download   # 声明下载：true 用原名，或指定下载文件名；
              # 此时 -UserAgent 生效（用于编码下载名，默认内置 Chrome UA）
```

# 示例

```bash
# 302 重定向
httpout -status 302 -headers "Location: 'http://nutzam.com'"

# 设置 Cookie
httpout -headers "'Set-Cookie' : 'AID=34cdeeddww'"

# 输出文件内容
httpout -body ~/a/b/c.html

# 管道方式输出文件内容
cat ~/a/b/c.html | httpout

# 从全局桶按 SHA1 输出（内容寻址，免权限检查路径）
httpout -body 'sha1:45cdad34...'

# 强制下载并指定文件名
httpout -body ~/report.pdf -download 月报.pdf
```
