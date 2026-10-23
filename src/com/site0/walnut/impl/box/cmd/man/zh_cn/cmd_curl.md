# 命令简介

`curl` 发起 HTTP 请求并输出响应

URL 可作为参数或从管道读入。默认 GET；`-data` 或 `-f` 自动转 POST，
`-X`/`-request` 可指定任意方法。响应非 2xx 时输出错误码到标准错误。

# 用法

```bash
curl [URL] [-data 内容] [-f 文件] [-X 方法] [-header 'K:V;K2:V2'] [-connect-timeout 秒] [-I]
  -data             # POST 数据（自动设为 POST）
  -f                # POST 请求体来自文件对象
  -X / -request     # 指定 HTTP 方法（GET/POST/PUT/DELETE...）
  -header           # 自定义请求头，分号分隔多个 K:V
  -connect-timeout  # 连接超时（秒）
  -I                # 只输出响应头
```

# 示例

```bash
# 显示网页内容
curl http://nutzam.com/index.html

# POST JSON 数据
curl http://api.example.com/pet -X POST -data '{name:"xiaobai"}'

# POST 文件内容
curl http://api.example.com/upload -f ~/data.bin

# 自定义请求头
curl http://api.example.com/pet -header 'Authorization:Bearer xxx;Accept:application/json'

# 只看响应头
curl -I http://nutzam.com

# URL 从管道读入
echo 'http://nutzam.com' | curl
```
