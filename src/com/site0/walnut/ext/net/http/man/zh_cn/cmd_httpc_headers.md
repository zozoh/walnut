# 过滤器简介

`@headers` 向上下文内容设置请求的头
 

# 用法

```bash
httpc {URL} @headers  
  [{..}, ...]      # 请求头的内容，可以支持两种形式：
                   #  1. {Name}={Value}，多个用 & 分隔
                   #  2. {...}
  [-f /path/to]    # 存放请求头的 JSON 文件
```

其中，所有的头，格式都是 `Xxxx-Xxxx`

```js
{
  // 如果是数组，则表示会有多个同名的头
  contentType: "text/plain",
  // 如果直接就是字符串或者其他简单数据类型
  // 相当于普通的的字段
  contentLenght : "xxx"
}
```

# 示例

```bash
# 发送一个请求
httpc http://demo.com/path/to @headers '{..}'

# 从一个文件对象里获取请求头的内容
httpc http://demo.com/path/to @headers -f ~/xxx.json

# 从标准输入读取请求头
cat ~/xxx.json | httpc http://demo.com/path/to @headers
```
