# 过滤器简介

`@explain` 将当前条目内容（XML）作为模板，展开指定的变量集

> 具体的语法参见 `f1-ooml-explain-syntax.md`

# 用法

```bash
ooml @explain
  [-in ~/path/to.json]       # 指定变量集文件路径，如果没有，则从标准输入读取
  [-debug word/document.xml] # 仅仅输出指定实体的模板解析结构
                             # 如果仅写 -debug 相当于 word/document.xml
```

# 示例

```bash
# 指定一个上下文变量的文件
ooml demo.docx @explain -in ~/xxx.json @pack output.docx

# 读取一个上下文变量
o id:xxx | ooml demo.docx @explain @pack output.docx

# 读取一个上下文变量并进行转换
o id:xxx | jsonx @translate -mapping -only -f ~/xx.json | ooml demo.docx @explain @pack output.docx
```