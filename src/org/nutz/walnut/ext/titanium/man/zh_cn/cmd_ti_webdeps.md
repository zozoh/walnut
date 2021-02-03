# 命令简介 

`ti webdeps` 解析`web`的`deps`依赖文件，生成一个引用列表。

一个`web`的`deps`文件，格式如下

```js
[{
  "type" : "js",
  "path" : "@deps:xxx.js"
}, {
  "type" : "css",
  "path" : "@deps:xxx.css"
}]
```

其中`path`段的`@deps:`前缀会被替换，默认的，替换值为`/gu/rs/ti/deps/`


-------------------------------------------------------------
# 用法
 
```bash
ti webdeps
  [/path/to/obj ...]      # 多个 deps 文件（JSON）
  [-url /gu/rs/ti/deps/]  # 路径前缀要替换成的 URL 前缀
  [-prefix @deps:]        # 路径中的前缀，默认为 `@deps:`
  [-html]                 # 【选】作为 HTML 输出，默认作为JSON输出
  [-cqn]                  # 【选】JSON 输出的格式化   
```

-------------------------------------------------------------
# 示例

```bash
# 输出一个经过解析后的依赖列表
demo:~$ ti webdeps /rs/ti/dist/es6/ti-more-all.deps.json
```