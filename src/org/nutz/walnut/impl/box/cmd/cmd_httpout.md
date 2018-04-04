# 命令简介 

    `httpout` 将给定输出输出成标准的 HTTP 响应

# 用法

    httpout xxxx               # 参数就是响应体的内容，如果指定 -body 则会被无视
       [-headers {..}]         # 一段 JSON 表示响应头
       [-status 200]           # 指定状态码，默认 200
       [-status_text xxx]      # 指定状态文本，默认采用标准文本，譬如 200 就是 'OK'
       [-body /path/to/file]   # 指定了一个文件，将其内容作为响应体
       [-etag xxx]             # 输出为标准 HTTP 响应，同时根据 etag 判断是否输出 304
       [-range bytes=0-7183]   # 当输出 HTTP 响应时，分段下载表示，符合 Http头的 Range 规范
       [-UserAgent xxx]        # 当输出 HTTP 响应时，指定下载客户端的信息
       [-download false]       # 当输出 HTTP 响应时，开启下载模式，即指定 Content-Disposition
             
    
# 示例

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