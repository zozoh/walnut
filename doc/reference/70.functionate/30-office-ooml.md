---
title: Office 文档解析与模板渲染（ooml）
module: 70.functionate
keywords: [ooml, docx, xlsx, Office, 模板渲染, OOXML]
source: [ext/media/ooml/, ooml/]
related: [README, 40-sheet-csv.md]
---

# Office 文档解析与模板渲染（ooml） 概述
Word / Excel 本质上是「zip 包 + 一串 XML」，即 OOXML 格式。
`ooml` 命令直接操作这个结构，提供两类能力：

- **解析**：把 xlsx 读成 workbook / sheet / rows 数据，导出 JSON 或目录
- **模板渲染**：把 docx 的 XML 当模板做变量展开，再重新打包成新文档
  ——这就是「根据模板生成 Word」的实现方式

底层解析库在 `src/com/site0/walnut/ooml/`（基于 docx4j），
`ooml` 命令是它的命令行包装。

## 解析 Excel

```bash
# 看整个工作簿的结构
ooml ~/abc.xlsx @xlsx @workbook

# 看第 0 个工作表
ooml ~/abc.xlsx @xlsx @sheet 0

# 读出行数据（JSON 输出，可管道给其他命令）
ooml ~/abc.xlsx @xlsx @sheet 0 @rows

# 把某个工作表导出成目录（每行一个文件）
ooml ~/abc.xlsx @xlsx @sheet 0 @exportsheet ~/outdir
```

## 模板渲染 Word

一个 docx 模板就是普通 Word 文档，在需要变量位置写上 `ooml` 的模板语法。
渲染时 `@explain` 做变量展开，`@pack` 重新打包：

```bash
# 变量来自 JSON 文件
ooml demo.docx @explain -in ~/xxx.json @pack output.docx

# 变量来自管道：先查出数据，转换后喂给模板
o id:xxx | jsonx @translate -mapping -only -f ~/xx.json \
  | ooml demo.docx @explain @pack output.docx
```

`@explain` 的模板语法详见 `man cmd_ooml_explain`。

## 典型场景：对账单生成

```bash
# 从数据库查出账单数据 → 渲染 Word 模板 → 作为邮件附件发出
sqlx 'SELECT * FROM t_bill WHERE uid = @uid' -vars '{uid:"u123"}' \
  | ooml ~/tmpl/bill.docx @explain @pack ~/out/bill-u123.docx \
  && mailx default @to u123@x.com @sub "你的对账单" @at ~/out/bill-u123.docx
```

整条链路全是命令拼接，没有写一行 Java。
