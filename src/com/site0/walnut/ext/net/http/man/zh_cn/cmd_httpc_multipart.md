# 过滤器简介

`@multipart` 向请求上下文设置一个 multipart 表单的内容。

# 用法

```bash
httpc URL @multipart [JSON] [-f 路径]
  # JSON    # JSON 描述的 multipart 表单内容，不给且不给 -f 则从管道读取
  -f        # 存放 multipart 表单内容的文件
```

表单内容的 JSON 格式：

```js
[
  // Part: 上传文件的例子
  {
    type: "FILE",
    name: "f0",              // Part 的名称
    value: "~/xxx.jpg",      // 内容来自哪个文件；">>INPUT" 表示从标准输入读取
    fileName : "xxx.jpg",    // 文件名，可选；从标准输入读取时必须指定，否则自动生成随机名
    contentType : "image/jpeg"  // 内容类型，可选；从标准输入读取时必须指定
  },
  // Part: 普通表单字段的例子
  {
    type: "TEXT",
    name  : "xxx",
    value : "xxxx"
  }
]
```

# 示例

```bash
# 发送一个 multipart form 请求
httpc http://demo.com/path/to @multipart '[{type:"FILE",name:"f0",value:"~/xxx.jpg"}]'

# 从文件对象获取 multipart form 内容
httpc http://demo.com/path/to @multipart -f ~/xxx.json

# 从标准输入读取 multipart form 内容
cat ~/xxx.json | httpc http://demo.com/path/to @multipart
```
