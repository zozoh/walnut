---
title: 动态密码 OTP
module: security.otp
keywords: [动态密码, OTP, 邮箱验证码, 一发一验, WnOTPService, WnOTPBean]
source:
  - security/otp/
related: [README, 20-auth-session, 10-email-mailx, 70-auth-captcha]
---

# 动态密码 OTP 概述

**动态密码 OTP**（短信/邮箱送达）确认「本人持有这个手机号/邮箱」：
注册、找回密码、动态码登录这类敏感操作前，往这个账号发一个随机码，
能对上才算数。挡机器人是 captcha 服务的事，见 70-auth-captcha，
本文只讲 OTP。

OTP 不依赖会话——验证发生时，往往还没登录呢。所以核心约束只有一个：
**一个账号在同一时刻只能进行一种验证**，我们把它叫做「一发一验」。

## 要解决的问题

一个对外开放注册的 Walnut，会立刻面对这个麻烦：

- **冒名者**：知道了某人的手机号/邮箱，不等于拥有它。注册、找回密码这类
  敏感操作前，往这个账号发一个随机码，能对上才算数。这就是 OTP。

而 OTP 落到邮箱这条路上，还有一个工程问题：邮件怎么发？SMTP 服务器、发件人
别名、模板从哪来？我们不想在 OTP 逻辑里再写一遍邮件发送，
**答案就是复用 `mailx` 已经成熟的那套约定**。

## 解决思路

`WnOTPService` 是动态密码的门面：调用方给一个配置名和运行时
（`WnSystem` 或 `WnIo + sessionVars`）得到实例，之后只管调
`create / save / send / check` 这些组合函数。实例怎么组装，全看配置。

```bash
|-- WnOTPService 配置驱动的组装器（sender/builder/store）
|-- WnOTPSender 发送接口
|-- WnEmailOTPSender 邮件基类
|-- WnSysEmailOTPSender 系统实现
配置：~/.otp/{name}.json5 + ~/.mailx/
```

动态密码的一生三步走：**生成 → 存储 → 发送**，下面分节讲。

# OTP 动态密码

## 配置：`~/.otp/{name}.json5`

配置放在系统用户（root）家目录下的 `~/.otp/`。`default` 是配置名，
将来要支持多套配置，就是再加一个 `~/.otp/{name}.json5`。
一个文件把**三个角色的装配**（sender / builder / store）全定了：
每段都是 `type` + `config`——`type` 决定实现类，`config` 是该实现类的
设置。整个文件解析成配置类树 `WnOtpConfig`：

```json5
// → WnOtpConfig
{
  // → WnOtpSenderConfig
  // 发送器：type 决定实现类
  //   EMAIL → WnSysEmailOTPSender
  sender : {
    type   : "EMAIL",
    // → WnOtpEmailSenderConfig（邮件发送设置）
    config : {
      // 邮件通道：引用 ~/.mailx/{channel}.json 里的 smtp 段
      channel  : "default",

      // 邮件标题模板，支持 ${value} 等变量
      subject  : "【Walnut】您的动态验证码",

      // 语言目录：模板文件位于 ~/.mailx/i18n/{lang}/
      lang     : "en-us",

      // 模板文件名：相对 ~/.mailx/i18n/{lang}/ 的文件名
      tmpl     : "OTP.html",

      // 发送日志的方式：syslog = 直接写系统日志（目前唯一支持的方式）
      writeLog : "syslog",

      // 模板文件额外需要补充的上下文变量（可留空）：
      // 渲染时合并进模板上下文，与内置的 otp 变量一起可用
      // （内置变量 ${otp.value} / ${otp.account} / ${otp.duration} ...
      //  来自 WnOTPBean）
      vars     : {
        siteName : "MySite",
        helpUrl  : "https://example.com/help"
      }
    }
  },

  // → WnOtpBuilderConfig
  // 生成器：type 决定实现类
  //   num  → WnNumberOTPBuilder（n 位数字码）
  //   char → WnCharOTPBuilder（n 位字符码）
  builder: {
    type   : "num",
    // → WnOtpNumBuilderConfig（位数与有效期，缺省 6 位 300 秒）
    config : {
      n        : 6,
      duration : 300
    }
  },

  // → WnOtpStoreConfig
  // 存储器：type 决定实现类
  //   redis → WnRedisOTPStore
  store  : {
    type   : "redis",
    // → WnOtpRedisStoreConfig
    config : {
      // Redis 连接配置名：~/.redis/{name}.json
      name : "default",

      // 存储键前缀，不设置则默认 otp:{服务配置名}:
      // 前缀是在同一个 Redis 里区分场景的机制
      prefix : "xxx",

      // 校验重试上限，超过即删码作废
      maxRetryTimes : 3
    }
  }
}
```

## 生成 OTP

生成被抽象成 `WnOTPBuilder` 接口：`make(type, account)` 返回一个 `WnOTPBean`。
类型（短信/邮箱）和账号由调用方给，码值随机产生，有效期在构建生成器时定死。

已有的两个实现：

- **`WnNumberOTPBuilder`**：生成 n 位数字码
- **`WnCharOTPBuilder`**：生成 n 位字符码

`WnOTPBean` 就是动态密码的全部信息：

- **type**：类型（`SMS` / `EMAIL`），决定走哪条发送通道
- **account**：账号，手机号或邮箱——既是存储键也是收件人
- **value**：码值
- **duration**：有效期（秒）
- **retryTimes / sendAt**：已重试次数、发送时间

## 存储 OTP

存储被抽象成 `WnOTPStore` 接口：`save` / `remove` / `get` / `check`。
默认实现是 `WnRedisOTPStore`——直接存 Redis：

- **键**：`{prefix}{账号}:{类型}`。**前缀来自 store.config.prefix**
  （`WnOtpRedisStoreConfig`），不设置则默认 `otp:{服务配置名}:`。
  **前缀是在同一个 Redis 里区分场景的机制**：不同域、不同业务各建一个
  store 实例、各配各的前缀，同一个账号在不同场景下的动态密码互不覆盖，
  键也绝不会撞——比如 `otp:walnut:abc@xyz.com:EMAIL` 与
  `otp:shop:abc@xyz.com:EMAIL` 是两条完全独立的记录
- **值**：`WnOTPBean` 的 JSON 串；TTL 就是码的有效期（秒），
  到点 Redis 自动回收，不用自己清过期
- **重试上限**：默认 3 次，store.config 里 `maxRetryTimes` 可改。
  `check` 时超过上限直接删码作废；值不符则累加 `retryTimes` 存回去；
  值对才返回 `WnOTPBean`

「一发一验」在存储层兜底：同账号再次 `save` 会直接覆盖旧码，
所以并发获取不会生成出两个有效的码，只是先拿到那个作废。

将来如果要做基于对象存储的实现（`WnOTPStore` 的另一实现），存放位置
等细节随那个实现一起定，不在发送配置里预留键。

## 发送 OTP

发送被抽象成一个极简的接口 `WnOTPSender`：给它一个 `WnOTPBean`，
它负责送达，返回 `AjaxReturn`。至于怎么送——短信走短信通道，
邮件走邮件通道——由实现类决定。以后扩展 SMS 或其他动态密码实现，
只是多一个 `WnOTPSender` 实现类的事。

### 通过邮件发送

目前唯一的发送方式，拆成两层：

- **`WnEmailOTPSender`（抽象基类）**：把「发邮件」这件事固化成模板方法——
  构建邮件、取 SMTP 配置、投递、写日志，四步的顺序写死，
  每步留出抽象方法给子类。
- **`WnSysEmailOTPSender`（系统实现）**：构造时传入 `WnOtpEmailSenderConfig`
  （由 `WnOTPService` 从 sender.config 解析），把这一切拼装起来。

这样做的好处是：换一个邮件来源（比如从数据库读模板、接第三方发信服务），
只写一个新的 `WnOTPSender` 实现即可，校验、存储、重试逻辑一概不动。

#### 模板目录与变量

线上 `~/.mailx/` 的目录形态是这样的，OTP 模板就放在它的 `i18n/{lang}/` 里，
和其他的模板（通知、账单等）放在一起，统一管理：

```bash
~/.mailx/
|-- default.json
|-- channel-1.json
|-- channel-2.json
|-- i18n/
|   |-- en-us/
|   |   |-- MAL.html
|   |   |-- NOTI_ERROR.html
|   |   |-- OTP.html
|   |   |-- post-invoice.html
```

**模板变量来自 `WnOTPBean`**：发送器把 OTP 信息整体放进模板上下文的
`otp` 变量里，常用的有：

- `${otp.value}`：验证码本身
- `${otp.account}`：收件账号（同时它就是收件人地址）
- `${otp.duration}`：有效期（秒）
- `${otp.sendAt}`：发送时间

此外 sender.config 里的 `vars` 段会**合并进模板上下文**——
站点名、帮助链接这类业务常量写在那里，模板里 `${siteName}` 直接用。

渲染用的是 `WnTmplX`，和 `mailx @tmpl` 是同一套模板引擎——
你在 mailx 里怎么写模板，在这里就怎么写。

#### 发送约定

SMTP 发送配置**从 `~/.mailx/` 目录里读取**，OTP 自己不存任何 SMTP 账号
密码：sender.config 里的 `channel` 对应 `~/.mailx/{channel}.json`。
一个通道配置文件里，与发送相关的 `smtp` 段长这样：

```json
{
  smtp: {
    host     : "smtp.example.com",   // SMTP 服务地址
    port     : 465,                  // 端口
    strategy : "SMTPS",              // 发送策略（SMTP / SMTPS / SMTP_TLS）
    account  : "no-reply@example.com",
    password : "********",
    from     : "no-reply@example.com", // 发件人，不设置则默认用 account
    alias    : "Walnut",             // 发件人显示名
    lang     : "en-us",              // 默认模板语言
    mailTo   : [],                   // 发信时固定追加的收件人
    mailCc   : [],                   // 固定抄送
    mailBcc  : []                    // 固定密送
  }
}
```

发送时逐条对应：

- 发件人、别名、固定收件人/抄送/密送，全部来自上面这个 `smtp` 段
- 收件人自动填 `otp.account`——OTP 本来就只发给正在验证的那个账号，
  配置里不提供覆盖项
- 标题取配置里的 `subject`，正文取 `~/.mailx/i18n/{lang}/{tmpl}`，
  用 OTP 变量渲染后构建成 `WnSmtpMail`
- 投递交给 `WnMailPosting.send(smtp, mail)`，成功返回 `null`，
  失败返回包装好的 `WebException`
- 无论成败都调用 `writeLog(otp, result)`；目前 `writeLog` 只支持
  `syslog` 一种方式——直接把发送结果写进系统日志。
  将来要落库、发审计通知，在这里扩展即可

# 主要代码类

- `WnOTPService` — 服务门面：按 `~/.otp/{name}.json5` 的 sender/builder/store
  三段配置组装三个角色，对外只暴露组合函数 `type()` / `create(account)` /
  `send(otp)` / `save(otp)` / `remove(account)` / `get(account)` /
  `check(account, value)`（验证通过即删除，一次性消费）
- `WnOtpConfig` / `WnOtpSenderConfig` / `WnOtpEmailSenderConfig` /
  `WnOtpBuilderConfig` / `WnOtpNumBuilderConfig` / `WnOtpStoreConfig` /
  `WnOtpRedisStoreConfig` — 配置类树：`~/.otp/{name}.json5` 解析成
  `WnOtpConfig`，各段 config 再解析成对应实现类的设置
- `WnOTPSender` — OTP 发送接口，`send(WnOTPBean)` 返回 `AjaxReturn`
- `WnEmailOTPSender` — 邮件 OTP 发送抽象基类，固化「构建→取配置→投递→写日志」四步
- `WnSysEmailOTPSender` — 系统实现：构造时必须传入 `WnOtpEmailSenderConfig`
  （由 `WnOTPService` 从 sender.config 解析）；通道取自 `~/.mailx/`
- `WnOTPBuilder` — OTP 生成接口，`make(type, account)` 返回 `WnOTPBean`
- `WnNumberOTPBuilder` / `WnCharOTPBuilder` — 数字码 / 字符码生成器
- `WnOTPStore` — OTP 存储接口：save / remove / get / check；现有实现 `WnRedisOTPStore`
- `WnOTPBean` — 动态码信息：类型、账号、码值、有效期、重试次数
- `WnSmtpMail` / `WnMailPosting` — mailx 的邮件对象与投递器，OTP 直接复用

# 扩展阅读

- 70-auth-captcha — 人机验证码服务，以及 OTP 与 captcha 在登录场景里的协作流程
- 70.functionate/10-email-mailx — mailx 的 SMTP 配置、i18n 模板目录与模板邮件机制
- 20-auth-session — 动态码登录成功后建会话
