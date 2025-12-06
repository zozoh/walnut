# 过滤器简介

`@raw_msg` 是一个测试工具，将纯文本的邮件消息解析，并展示到控制台中

# 用法

```bash
mailx @raw_msg
[/path/to/mime_txt1, ...]     # 指定多个文件对象，读取 MAIL MIME TEXT 
[-meta '{...}']               # @see `man mailx imap`
[-decrypt]                    # @see `man mailx imap`
[-content xxx]                # @see `man mailx imap` 
[-header]                     # @see `man mailx imap`
```

# 示例

```bash
# 解析一个邮件消息
mailx @raw_msg ~/tmp/test.mime.txt
```
