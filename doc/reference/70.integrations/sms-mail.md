---
title: 短信与邮件
module: 70.integrations
keywords: [sms, mailx, 短信, 邮件, 模板邮件, SMTP, IMAP, 云片]
source: [ext/net/sms/, ext/net/mailx/]
related: [xapi, ../60.sched-async/notify.md, ../40.commands/03-cmd-filter.md]
---

# 短信与邮件

两个最常用的触达通道。命令细节见 网络命令，本文侧重配置与落地模式。

## 短信（sms）

**配置**：`~/.sms/config_<provider>`（JSON）。当前仅内置云片（Yunpian）实现：

```js
{
  apikey  : "abc1234567890",
  header  : "【文尔科技】",    // 强制加头部（运营商要求）
  lang    : "zh-cn",
  timeout : 5000
}
```

`lang` 与 `timeout` 是所有 provider 的公共字段，其余字段由各 provider 实现解释。

```bash
sms send -r 13800001111 -t signup 'code:3421'          # 模板消息（模板存 ~/.sms/）
sms send -r 13800001111 -t i18n:signup 'code:3421'    # i18n 模板（~/.sms/i18n/<lang>/）
sms send -header 【XX】 -r 13800001111 自定义正文        # 自定义消息
```

多供应商思路：`config_<provider>` 一个文件一个供应商，命令里用 `-provider`（默认 Yunpian）切换。

## 邮件（mailx）

**配置**：`~/.mail/<ConfigName>.json`——SMTP/POP3/IMAP 账户（支持 SSL、S/MIME 加密签名）。

**发送**（过滤器管道，20 个子手册页）：

```bash
mailx default \
    @to zozoh@example.com \
    @cc boss@example.com \
    @sub '订单创建通知' \
    @tmpl order_created \
    @vars '{orderId:"W20261021001", amount:"128.00"}' \
    -quiet
```

- `@tmpl <名称>`：正文用 `~/.mail/tmpl/<名称>` 目录下的模板（`man mailx tmpl`）；
- `@vars`：模板变量；`@load` 从文件/对象加载变量；
- `@content`：直接给正文（`-html` 富文本）；`@set @fs` 管理附件。

**接收**：

```bash
mailx default @imap -since 7d -json     # IMAP 拉最近 7 天
mailx default @fs ~/mails               # 虚拟文件系统方式收信
```

## 落地模式

**多账户**：`.mail/billing.json`（账单）、`.mail/alert.json`（告警），命令第一个参数选账户：

```bash
mailx alert @to ops@example.com @sub '服务告警' @content 'CPU 90%'
```

**模板邮件**：`@tmpl <名称> [-html]` 指定模板（模板存放位置与文件结构以 `man mailx tmpl` 及发送实现源码为准），配合 `@vars` 填变量。

**root 统一配置**：触达类配置（apikey、SMTP 密码）建议集中在 root 的 `~/.sms`、`~/.mail`，普通用户通过任务身份（cron 的 `user`）借用，或复制配置到自己的家目录。

## 注意事项

- 短信必须带签名头（如【XX科技】），否则运营商拦截；
- 邮件发送注意频率，批量邮件建议经 mq 队列限速发送；
- 模板变量做 HTML 转义，防注入。

## 延伸阅读

- 网络命令 — sms / mailx 完整过滤器列表
- 通知与提醒 — 告警模式
