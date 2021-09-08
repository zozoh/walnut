# 过滤器简介

`@explain` 将当前条目内容（XML）作为模板，展开指定的变量集

> 具体的语法参见 `f1-ooml-explain-syntax.md`

# 用法

```bash
ooml @explain
  [-i ~/path/to.json]     # 指定变量集文件路径，如果没有，则从标准输入读取
```

# 示例

```bash
ooml demo.docx @checkout word/document.xml @explain -i ~/xxx.json
```