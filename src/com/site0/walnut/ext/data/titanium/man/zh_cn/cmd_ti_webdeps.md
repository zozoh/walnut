# 过滤器简介

`@webdeps` 解析 web 的 deps 依赖文件，生成一个引用列表。

一个 web 的 deps 文件格式如下：

```js
[{
  "type" : "js",
  "path" : "@deps:xxx.js"
}, {
  "type" : "css",
  "path" : "@deps:xxx.css"
}]
```

其中 `path` 段的 `@deps:` 前缀会被替换，默认替换为 `/gu/rs/ti/deps/`。

# 用法

```bash
ti @webdeps [deps文件...] [-url 前缀] [-prefix 前缀] [-html] [-cqn]
  # deps文件    # 多个 deps 文件（JSON）
  -url         # 路径前缀要替换成的 URL 前缀，默认 /gu/rs/ti/deps/
  -prefix      # 路径中的前缀，默认 @deps:
  -html        # 作为 HTML 输出，默认输出 JSON
  -cqn         # JSON 输出的格式化开关
```

# 示例

```bash
# 输出一个经过解析后的依赖列表
ti @webdeps /rs/ti/dist/es6/ti-more-all.deps.json

# 输出成 HTML 标签
ti @webdeps /rs/ti/dist/es6/ti-more-all.deps.json -html
```
