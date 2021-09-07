# 过滤器简介

`@view` 显示当前条目内容

# 用法

```bash
ooml @view
  [Entry Path]       # 条目的路径
  [-meta]            # 显示条目的元数据
  [-cqn]             # 元数据 JSON 的输出格式化方式
  [-as xml|...]      # 显示方式
                     #  - xml : 作为 XML
                     #  - str : 纯文本
                     #  - tree : XML 树以便查看结构
```

# 示例

```bash
ooml demo.docx @checkout word/document.xml @view
```