# 过滤器简介

`@query` 向上下文内容设置请求的QueryString
 

# 用法

```bash
httpc {URL} @query 
  [{..}, ...]      # 请求头的内容，可以支持两种形式：
                   #  1. {Name}={Value}，多个用 & 分隔
                   #  2. {...}
  [-decode]        # 读取值时，对值解码
  [-reset]         # 同时重置上下文中的 QuerString
                   # 因为 URL 里会带有 QueryString
                   # 如果值是以`{`开始且以`}`结束，那么就被认为是一个
                   # JSON，会解析为多个值
                   # 不指明这个参数，会和 URL 里的 QueryString 合并
```

# 示例

```bash
# 发送简单的 GET 请求
httpc http://demo.com/path/to?a=100&b=99

# 发送简单的 GET 请求
httpc http://demo.com/path/to @query a=100 b=99

# 从文件读取 query string 内容
httpc http://demo.com/path/to @query -f ~/xxx.json

# 从标准输入读取query string内容
cat ~/xxx.txt | httpc http://demo.com/path/to @query a=100 b=99
```
