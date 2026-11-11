# 过滤器简介 

`@tmpl` 修改上下文中的邮件内容模板名称
 
> 模板路径相对于 `~/.mailx/i18n/<lang>/`，`<lang>` 取配置的 `smtp.lang`，
> 默认 `zh-cn`。模板对象的 `subject` 元数据会覆盖邮件标题。

# 用法

```bash
@tmpl
  [Name]      # 模板名称
  [-html]     # 表示正文内容为 HTML
```


# 示例

```bash
mailx default @tmpl login -html
```

