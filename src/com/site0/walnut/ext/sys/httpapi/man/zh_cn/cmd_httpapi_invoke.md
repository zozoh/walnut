# 过滤器简介

`@invoke` 调用某一个指定的 API。

# 用法

```bash
httpapi @invoke API路径 [-req 路径] [-cookie 内容] [-get JSON|@pipe] [-post JSON|@pipe] [-file 路径] [-body 内容] [-u 用户] [-ncnd 键] [-cqn]
       # API路径    # 相对于 API 主目录的路径
  -req      # 请求文件，没有会自动创建一个
  -cookie   # 请求的 Cookie 内容
  -get      # 请求的 QueryString；JSON 或 @pipe 从管道读
  -post     # 表单提交参数，会自动将请求设置为 POST
  -file     # 将一个文件的内容拷贝成请求文件
  -body     # 自由的 Body 内容；声明但无内容则从管道读
  -u        # 指定一个用户，只有 root 和 op 组成员才能执行这个操作
  -ncnd     # 如果 API 对象包含给定键，用其检查是否给了必要条件
  -cqn      # 输出的 JSON 格式化开关
```

注意：`get|post|body` 都可以从管道读内容，但只有一个有效（管道读一次就没了），优先级 `get > post > body`。

# 示例

```bash
# 用 GET 方式执行某个 API
httpapi @invoke demo/get -get "id:'12345'"

# 用 POST 方式执行某个 API
httpapi @invoke demo/get -post "id:'12345'"

# 参数从管道读
httpparam id:xxx | httpapi @invoke demo/get -get @pipe
```
