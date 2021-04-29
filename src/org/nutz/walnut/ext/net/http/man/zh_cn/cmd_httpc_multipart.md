# 过滤器简介

`@multipart` 向上下文内容设置一个`multipart`表单的内容
 

# 用法

```bash
httpc {URL} @multipart 
    [[..]]           # 采用 JSON 描述的 multipart 表单内容
    [-f /path/to]    # 存放 multipart 表单内容的文件
```

其中，指明表单内容的 JSON 格式为:

```js
[
  // Part: 上传文件的例子  
  {
    // Part 的名称
    name: "f0",
    // 内容来自哪个文件
    // 当然，如果想从标准输入读取，可以写成
    // - ">>INPUT"
    path: "~/xxx.jpg",
    // 这里可以指明特殊文件名。否则就是用 file 指定的文件名
    // 如果文件内容是从标准输入读取，由于没有文件名。就需要这个字段了。
    // 当然如果还是没有指定，则会自动生成一个随机文件名字符串
    fileName : "xxx.jpg",
    // 内容类型，通常根据 file 文件自动获取
    // 当然，从标准输入读取的，则需要这个字段来指定
    // 这个对应： "Content-Type" 属性
    contentType : "image/jpeg"
  },
  // Part: 普通表单字段的例子
  {
    // Part 的名称
    name  : "xxx",
    // Part 的值
    value : "xxxx"
  }
]
```

# 示例

```bash
# 发送一个 multipart form 请求
httpc http://demo.com/path/to @multipart '{..}'

# 从一个文件对象获取 multipart form 内容
httpc http://demo.com/path/to @params -f ~/xxx.json

# 从标准输入读取 multipart form 内容
cat ~/xxx.json | httpc http://demo.com/path/to @multipart
```
