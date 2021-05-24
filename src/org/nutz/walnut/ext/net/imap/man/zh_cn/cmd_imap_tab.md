# 过滤器简介

`tab` 将上下文的邮件采用表格形式输出
 

# 用法

```bash
imap @tab
  [subject,from]  # 指定了表格的列，可支持的列名为：
                  #  - num
                  #  - subject
                  #  - folder
                  #  - from
                  #  - to
                  #  - reply
                  #  - mime/contentType
                  #  - content
                  #  - breif
                  #  - attachment
                  #  - ac/attachmentCount
  [-bish]         # 表格的格式化方式
```
