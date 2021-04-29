# 过滤器简介

`@params` 向上下文内容设置请求的参数表
 

# 用法

```bash
httpc {URL} @params  
  [{..}, ...]      # 参数的内容，可以支持两种形式：
                   #  1. {Name}={Value}
                   #  2. {...}
  [-decode]        # 读取值时，对值解码
  [-f /path/to]    # 存放请求头的 JSON 文件
```

其中，表单内容的 JSON 格式为:

```js
{
  // 如果是数组，则表示会有多个同名的上传参数
  f0: [VALUE, VALUE],
  // 如果直接就是字符串或者其他简单数据类型
  // 相当于普通的的字段
  nm : "xxx"
}
```

# 示例

```bash
# POST 一个请求
httpc http://demo.com/path/to @params '{..}'

# 从一个文件对象里获取POST请求参数
httpc http://demo.com/path/to @params -f ~/xxx.json

# 从标准输入读取参数表
cat ~/xxx.json | httpc http://demo.com/path/to @params
```
