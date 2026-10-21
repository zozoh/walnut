---
title: 网络与通信命令
module: 40.commands
keywords: [curl, wget, httpc, xapi, mailx, sms, ftp, webx, whoisx, icp, ip2region, mqttc]
source: [ext/net/, impl/box/cmd/]
related: [command-model, ../70.integrations/xapi.md, ../70.integrations/sms-mail.md]
---

# 网络与通信命令

Walnut 内置了 HTTP 客户端、邮件、短信、FTP、MQTT 等网络能力，源码在 `src/com/site0/walnut/ext/net/`，`curl` / `wget` 在 `impl/box/cmd/`。

> 参数细节一律以 `man <命令>` 为准。

## 命令一览

| 命令 | 用途 | 手册 |
|---|---|---|
| `curl` / `wget` | 内置的 HTTP 下载（类 shell 习惯） | ✅ |
| `httpc` | 通用 HTTP 客户端（过滤器管道） | ✅（10 子页） |
| `xapi` | 第三方平台 API 调用框架 | ✅（含子页） |
| `mailx` | 邮件收发（SMTP/POP3/IMAP，附件、模板） | ✅（20 子页） |
| `sms` | 短信发送与查询 | ✅ |
| `ftp` | FTP 客户端（ls/upload/download/mv/rm） | ✅（5 子页） |
| `webx` | 站点用户自助操作（登录/改密/绑定手机邮箱） | ✅ |
| `whoisx` | 域名 WHOIS 友好查询 | ✅ |
| `icp` | ICP 备案查询 | ✅ |
| `ip2region` | IP 归属地/运营商查询 | ✅ |
| `mqttc` | MQTT 客户端（pub/sub） | ✅ |

## curl / wget

语法接近 Linux 习惯，支持 http/https 下载、管道输出：

```bash
# 下载到文件
curl -o ~/page.html https://example.com/
wget https://example.com/big.zip -O ~/big.zip

# 输出到标准流，交给后续命令
curl -s https://api.example.com/data | json -c
curl -s https://example.com/page.html | dom @find 'a'
```

## httpc：HTTP 客户端

过滤器管道风格的 HTTP 客户端，比 curl 更适合脚本化组装请求：

```bash
httpc [{method}] {URL} [-rhH] [@FILTER ...]

httpc post https://api.example.com/v1/users \
    @header 'Authorization: Bearer xxx' \
    @body '{name:"walnut",age:1}' \
    @timeout 5000 \
    @output ~/resp.json

httpc https://example.com/img.png @output ~/img.png
```

- 第一个参数是 `get/post/...` 表示方法；只给 URL 时按上下文自动判断 GET/POST（`@method` 也可指定）；
- `-r` 跟随重定向；`-h` 输出含响应头；`-H` 只看响应头；
- 过滤器：`@body @header @method @multipart @output @params @proxy @query @timeout`（各有独立手册页，如 `man httpc multipart`）。

## xapi：第三方 API 框架

`xapi` 是 Walnut 调用第三方平台（微信、支付宝、阿里云等）的统一入口，专家配置决定每个 API 的密钥、URL、请求组装方式：

```bash
xapi info              # 列出所有可用 API
xapi send <api名> ...    # 发送请求
xapi req <api名> ...     # 查看将发出的请求对象（调试用，不真正发送）
xapi ak <api名>          # 检查密钥是否可用
xapi wxjssdk <url>       # 生成微信 JSSDK 签名配置
```

机制与专家配置写法详见 [集成：xapi](../70.integrations/xapi.md)。

## mailx：邮件

配置存 `~/.mail/<ConfigName>.json`（SMTP/POP3/IMAP 账户）。过滤器型命令：

```bash
# 发送模板邮件
mailx default \
    @to zozoh@example.com \
    @sub '您的订单已创建' \
    @tmpl ~/mailtmpl/order_created \
    @vars '{orderId:"W20261021001", amount:128.00}' \
    -quiet

# IMAP 收信
mailx default @imap -since '7d' -json
```

过滤器分组：收信（`@fs @imap @raw_msg`）、发送头部（`@to @cc @bcc @fto @fcc @fbcc @sub`）、内容（`@content @tmpl ...`）、附件与变量（`@set @vars @load @json @trans`）等，共 20 个子页。

## sms：短信

配置存 `$HOME/.sms/config_<provider>`（如 `config_yunpian`，含 `apikey / header / lang / timeout`）：

```bash
# 发送自定义消息（-header 头部，-r 手机号，正文为位置参数）
sms send -header 【nutz科技】 -r 13800001111 机器AABBCCDDEEFF流量异常500mb

# 发送模板消息（模板存 ~/.sms/<名称>，-t 指定模板名，正文为模板变量）
sms send -r 13800001111 -t signup 'code:3421'
```

> 短信配置通常由 root 统一维护，普通用户通过 `noti` 通知体系间接触发（见 [通知](../60.sched-async/notify.md)）。

## ftp

配置存 `~/.ftp/<名称>/ftpconf`（host/port/username/token/pathPrefix）：

```bash
ftp ls 配置名 /path
ftp upload 配置名 /local/file /remote/path
ftp download 配置名 /remote/file ~/local/
```

## webx / whoisx / icp / ip2region

```bash
# 站点用户自助：登录、会话、改密、绑定手机/邮箱、验证码
webx ~/www/site @auth -name zozoh -passwd 'xxxx'
webx ~/www/site @passwd -oldpass oldPwd -newpass newPwd

# 域名注册信息（JSON 输出）
whoisx nutzam.com

# ICP 备案查询
icp beian.miit.gov.cn

# IP 归属地
ip2region query 123.45.67.89
```

## mqttc：MQTT

配置目录 `~/.mqttc/<名称>/`（conf 为 JSON 配置，message 缓存订阅消息，handler 放 jsc 消息处理器）：

```bash
mqttc load mybroker           # 加载/查看配置
mqttc pub mybroker /topic/a 'hello'
mqttc sub mybroker /topic/a   # 订阅（可用 handler/msgin.jsc 处理消息）
mqttc unsub mybroker /topic/a
```

## 典型场景

- **API 里调外部服务**：`.regapi` → `xapi send` 或 `httpc`；
- **注册发验证邮件**：`webx @auth` + `mailx @tmpl`；
- **批量发短信**：`cron` 定时任务里 `sms send`；
- **设备数据接入**：MQTT `sub` + handler 写库。

## 注意事项

- 外部请求都要设超时（`httpc @timeout`、`sms` 配置 timeout），避免任务线程挂死；
- 密钥类配置（`.mail`、`.sms`、xapi accessKey）不要放在普通用户目录；
- `xapi req` 先看不发，是排查第三方对接问题的第一步。

## 延伸阅读

- [集成：xapi](../70.integrations/xapi.md) — ThirdXApi 框架机制
- [集成：短信与邮件](../70.integrations/sms-mail.md)
- [通知](../60.sched-async/notify.md) — noti 通知队列
