---
title: Email 收发（mailx）
module: 70.functionate
keywords: [mailx, email, smtp, imap, 模板邮件, 附件]
source: [ext/net/mailx/]
related: [README, 30-office-ooml.md]
---

# Email 收发（mailx） 概述
业务系统几乎都需要发邮件：注册验证、通知推送、对账单。
`mailx` 命令把收发 Email 做成了一个过滤器命令——
SMTP 发信、IMAP 收信，支持富文本、附件和模板渲染。

## 账户配置

发信前先在 `~/.mailx/` 目录下放一个 JSON 配置文件：

```json5
// ~/.mailx/default.json
{
  "smtp": {
    "host": "smtp.exmail.qq.com",
    "port": 465,
    "account": "noreply@demo.com",
    "password": "xxxxxx",
    "from": "Demo 系统"
  }
}
```

- 配置文件名就是命令里的第一个参数，`mailx ics ...` 使用 `~/.mailx/ics.json`，
  省略时默认 `default.json`
- 还可以有 `pop3` / `imap` / `fs` 段，分别对应收信协议和虚拟文件系统配置
- 模板文件放在 `~/.mailx/tmpl/<lang>/` 目录下（lang 取配置里的 `lang` 字段）

## 发送

过滤器链执行完后**自动发送**，不用任何「发送」指令：

```bash
# 最简单的发信：收件人 + 标题 + 正文
mailx default @to a@b.c @sub "你好" @content "hello world"

# 带附件
mailx default @to a@b.c @sub "报表" @at ~/report.xlsx

# 模板发信：-html 表示 HTML 正文，@vars 传入模板变量
mailx default @to a@b.c @tmpl login -html @vars '{name:"zozoh"}'
```

收件人、抄送、密送分别是 `@to` / `@cc` / `@bcc`，可以重复或一次给多个。

## 接收（IMAP）

`@imap` 把收件箱当数据源读取，收取的邮件存成对象：

```bash
# 收取未读邮件到 ~/mymail/，附件单独存放
mailx default @imap -flags '!SEEN' -to '~/mymail/${msg_id}' \
  -at '~/mymail/${msg_id}/${file_name}'

# 只看邮件头，不落盘也不标记已读（调试用）
mailx default @imap -json -cqn
```

## 常用过滤器速查

- `@to` / `@cc` / `@bcc` — 收件人
- `@sub` — 标题
- `@content` — 正文
- `@tmpl` — 模板发信
- `@at` — 附件
- `@vars` / `@set` / `@meta` / `@json` — 模板变量
- `@imap` — IMAP 收信
- `@load` — 加载已有邮件对象继续编辑

> ⚠️ `cmd_mailx` 的 man 主文档（`man/zh_cn/cmd_mailx.md`）部分内容过期：
> 示例里的 `@send` / `@sign` / `@encrypt` 等过滤器在源码中并不存在，
> 以本篇列出的过滤器为准。
