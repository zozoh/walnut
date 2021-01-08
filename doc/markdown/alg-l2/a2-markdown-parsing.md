---
title: Markdown 解析细节
author: zozohtnt@gmail.com
---

# 解析上下文

```bash
WnDocParsing
#
# 输入字符缓冲
# 提供行读取等便捷读取操作
#
|-- input : WnCharArray
#
# 这里记录一下行号，每次从输入力按行读取
# 行号为 0 base
#
|-- lineNumber: 0
#
# 解析的文档头信息，譬如 title, author, tags 等
#
|-- header : NutMap
#
# 输入字符流解析的目标元素
# 如果遇到 HTML 标签，或者 Markdown 标签，则会递归压栈
#
|-- body : WnDocElement
```

# 文档节点类型

```bash
WnDocNode           # 文档节点抽象类
|-- WnDocElement    # 元素
|   |-- WnDocClosedElement  # 闭合元素（无子节点）
|-- WnDocText       # 文本
|-- WnRawData       # 未解析文本
```
