# 命令简介 

`httpout` 将给定输出输出成标准的 HTTP 响应
    
# 用法

```bash
httpout xxxx               # 参数就是响应体的内容，如果指定 -body 则会被无视
   [-headers {..}]         # 一段 JSON 表示响应头
   [-status 200]           # 指定状态码，默认 200
   [-status_text xxx]      # 指定状态文本，默认采用标准文本，譬如 200 就是 'OK'
   [-body /path/to/file]   # 指定了一个文件，将其内容作为响应体
   [-etag xxx]             # 输出为标准 HTTP 响应，同时根据 etag 判断是否输出 304
   [-range bytes=0-7183]   # 当输出 HTTP 响应时，分段下载表示，符合 Http头的 Range 规范
   [-UserAgent xxx]        # 当输出 HTTP 响应时，指定下载客户端的信息
                           # 只有 -download 模式才有效，因为需要根据
                           # 这个信息编码下载目标名称
   [-mime text/plain]      # 【选】指明内容类型
   [-base64]               # 【选】指明内容需要进行 base64 编码
                           # 如果开启这个选项，则 mime 一定会变成 text/plain
   [-download]             # 指定 Content-Disposition 的名称，在这种模式下
                           # -UserAgent 才会生效
                           # 当然，如果 -headers 里指定了  Content-Disposition
                           # 会更加优先
```
      
> 这里需要说明的是，如果 `httpout` 没有有效的 `-body` 且没有指定 `xxx` 这种直接输入的参数，那么它
> 会尝试从管道或者标准输入读取内容
    
# 示例

```bash
# 输入某个重定向
demo@~$ httpout -status 302 -headers "Location: 'http://nutzam.com'"
HTTP/1.1 302 Found
X-Power-By: Walnut
Location: http://nutzam.com


# 指定 Cookie
demo@~$ httpout -headers "'Set-Cookie' : 'AID=34cdeeddww'"
HTTP/1.1 200 OK
X-Power-By: Walnut
Set-Cookie: AID=34cdeeddww
Content-Length: 0 

# 将文件内容输出
demo@~$ httpout -body ~/a/b/c/html

# 将文件内容输出（管道方式）
demo@~$ cat ~/a/b/c/html | httpout
```