---
title: Markdown 文档标记映射表
author: zozohtnt@gmail.com
---

--------------------------------------
# 动机：为什么要有Markdown标记映射表

Markdown 文档实际上某种程度可以作为 HTML 的一个超集。虽然，世俗的 Markdown 仅仅包括 HTML 标准很少的一部分标签。实际上， Markdown 本身也没啥标准。

如果你想解析一个 Markdown 文档，你最大的问题就是，我到底应该解析成一个什么样的结构呢？

考虑到 HTML 的超强表达力（实际上是 XML 的超强表达力）我们认为将 Markdown 解析位一个类似 HTML 的文档树，是一个最自然和妥帖的方式。

我们不考虑解析过程，仅仅考虑解析的最终结果。

--------------------------------------
# 映射表

## 块级元素

| Name         | Markdown | HTML                        |
| ------------ | -------- | --------------------------- |
| `OL`         | `1.a.A.` | `UL.as-md|li.as-md`         |
| `UL`         | `-+*`    | `UL.as-md|li.as-md`         |
| `UL`         | `- [x]`  | `UL.as-md as-task|li.as-md` |
| `P`          | `.+`     | `P.as-md`                   |
| `TABLE`      | `--|--`  | `TABLE.as-md`               |
| `CODEBLOCK`  | `'''`    | `PRE.as-md as-code-gfm`     |
| `CODEBLOCK`  | `....`   | `PRE.as-md as-code-indent`  |
| `BLOCKQUOTE` | `>`      | `BLOCKQUOTE.as-md`          |
| `HR`         | `---`    | `HR.as-md`                  |
| `#`          | `#`      | `H1~6.as-md @md-leval=1~6`  |

## 行内元素 

| Name     | Markdown | HTML           |
| -------- | -------- | -------------- |
| `STRONG` | `**`     | `STRONG.as-md` |
| `EM`     | `*`      | `EM.as-md`     |
| `B`      | `__`     | `B.as-md`      |
| `I`      | `_`      | `I.as-md`      |
| `Code`   | `'xxxx'` | `CODE.as-md`   |
| `Del`    | `~~`     | `DEL.as-md`    |
| `Link`   | `[]()`   | `A.as-md`      |
| `Image`  | `![]()`  | `IMG.as-md`    |
| `!TEXT`  | `xxxx`   | `SPAN.as-md`   |