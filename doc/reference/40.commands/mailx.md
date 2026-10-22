---
title: mailx 命令：邮件收发一体化
module: 40.commands
keywords: [mailx, 邮件, SMTP, IMAP, 模板邮件, 附件, S/MIME]
source: [ext/net/mailx/]
related: [net-cmd, ../70.integrations/sms-mail.md, tmpl, ../60.sched-async/notify.md]
---

# mailx 命令：邮件收发一体化

`mailx` 是 Walnut 的邮件命令：SMTP 发信、IMAP/文件系统收信、模板邮件、附件、S/MIME 加密签名。过滤器型命令——像搭积木一样组装一封邮件再发送。

- 源码：`src/com/site0/walnut/ext/net/mailx/`
- 手册：`man mailx` 及 20 个子页（`man mailx tmpl`、`man mailx imap` 等）

## 设计思路

1. **多账户**：一个配置名一个账户（`~/.mail/<ConfigName>.json`），发送/接收都在同一账户上下文；
2. **构建即管道**：收件人 → 标题 → 正文/模板 → 变量 → 附件，每个 `@过滤器` 修改邮件上下文，最后隐式发送；
3. **模板邮件**：正文用模板 + 变量渲染（底层是 [tmpl](tmpl.md) 引擎），业务通知与视觉稿解耦；
4. **收信抽象**：IMAP 直接收；`@fs` 把 S3 等存储桶映射目录当收件箱（适配 AWS SES 落 S3 的形态）。

## 配置

`~/.mail/<ConfigName>.json`：SMTP/POP3/IMAP 账户参数（服务器、端口、账号、密码/授权码、SSL、S/MIME 证书等，字段见 `man mailx`）。

## 适用场景

- **业务通知邮件**：注册确认、订单状态、账单（模板 + 变量）
- **告警**：cron 任务失败通知（见 [通知](../60.sched-async/notify.md)）
- **收信处理**：客服邮箱自动分拣：`@imap` 拉取 → 命令处理
- **敏感邮件**：S/MIME 加密/签名发送

## 发信：过滤器分组

```bash
mailx [ConfigName] [-lang zh-cn] [-quiet] [-json] [-cqn] [-ajax] [[@filter ...]...]
```

- **收件人** — 过滤器：`@to @cc @bcc` — 作用：添加接收者（可多个）
- **** — 过滤器：`@fto @fcc @fbcc` — 作用：从文件/目录批量添加（`-match` 过滤）
- **标题** — 过滤器：`@sub` — 作用：邮件标题
- **内容** — 过滤器：`@content` — 作用：正文（`-html` 富文本）
- **** — 过滤器：`@tmpl` — 作用：正文用模板（`@vars` 提供变量）
- **变量** — 过滤器：`@vars @load @set` — 作用：模板变量：直接给 / 从文件读 / 从对象元数据映射
- **附件** — 过滤器：`@fs` 等 — 作用：附件与 MIME 处理（见子页）
- **其他** — 过滤器：`@trans @meta @raw_msg @json` — 作用：转换脚本、元数据、原始消息、JSON 输出

## 示例

### 模板邮件（业务通知主力）

```bash
mailx default \
    @to zozoh@example.com \
    @sub '订单创建通知' \
    @tmpl order_created \
    @vars '{orderId:"W20261021001", amount:"128.00", items:[{nm:"A",nb:2}]}' \
    -quiet
```

模板内可用 `${}` 占位、`${#loop}` 循环渲染 item 列表。

### 从对象数据发信（批量）

```bash
# @set 从目录对象取收件人（-mapping 字段映射，-content 取文件内容）
o ~/customers @query '{lv:"vip"}' -l \
  @tmpl 'mailx default @to ${email} @sub "VIP 答谢" @content "...";\n' | run
```

### 富文本 + 多收件人

```bash
mailx default @to a@x.com b@x.com @cc boss@x.com \
    @sub '周报' @content '<h1>本周数据</h1><p>...</p>' -html -quiet
```

### 静默与输出

```bash
mailx default @to a@x.com @sub hi @content xx -quiet   # 只发不输出
mailx default @to a@x.com @sub hi @content xx -json    # 输出发送结果 JSON（供 API 用）
```

## 收信

```bash
# IMAP 拉最近 7 天
mailx default @imap -since 7d -json

# 文件系统收信（S3/SES 形态）：读取配置 fs 段（home/inbox 等）
mailx default @fs ~/incoming -json
```

## 注意事项

- 批量发送（>100 封）建议经 [消息队列](../60.sched-async/mq.md) 限速投递，避免触发服务商频率限制；
- SMTP 密码用授权码（QQ/163/Gmail 等不开明文的，开启 SMTP 授权）；
- 模板变量渲染前做 HTML 转义（`@trans` 可挂转换脚本）；
- `-quiet` 与 `-json` 二选一：API 场景要返回值就去掉 `-quiet`。

## 延伸阅读

- [网络命令组](net-cmd.md) — sms 等其他触达通道
- [tmpl](tmpl.md) — 模板语法
- [集成：短信与邮件](../70.integrations/sms-mail.md)
