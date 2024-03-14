# 过滤器简介

`@docx` 将上下文的DOM输出为docx

# 用法

```bash
dom @docx
  [{ID}]           # 一个已经存在的 docx 的文档 ID，作为输出模板
                   # 如果不指定，则会新创建一个 docx
  [-vars {..}]     # 指定文档变量，如果是 '{..}' 则表示变量
                   # 如果是一个路径，则表示一个 json 文件，
                   # 里面存放所有文档变量
  [-style {..}]    # 指定一个样式映射表，下面有详细描述
  [-out]           # 指定输入文件路径。如果不指定，则直接写到标准输出
```

## 样式映射

元素（特别是`H1~6`），应该应对到哪个`w:style`呢？
元素（特别是`DIV`）采用了特别的`className`，应该应对到哪个`w:style`呢？
元素标记了特殊属性应该应对哪个`w:style`呢？ 

这里是一个映射表: 

```js
{
  "${tagName}" : "${styleId}",
  "${className}" : "${styleId}",
  "@${attrName}=${attrValue}" : "${styleId}",
  "#header" : "${styleId}",
  "#footer" : "${styleId}"
}
// 譬如
{
  "H1" : "10",
  "H2" : "20",
  "my-title" : "ab",
  "@doc-p=title" : "a0",
}
```

# 示例

```bash
# 输出上下文的文档到 docx
dom @html a.html @docx -out ~/xyz.docx
```

