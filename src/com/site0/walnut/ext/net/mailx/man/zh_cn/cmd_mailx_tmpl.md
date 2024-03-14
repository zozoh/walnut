# 过滤器简介 

`@tmpl` 修改上下文中的邮件内容模板名称
 

# 用法

```bash
@msg
  [Name]      # 模板名称
  [-html]     # 表示正文内容为 HTML
```


# 示例

```bash
sendmail @tmpl login -html
```

