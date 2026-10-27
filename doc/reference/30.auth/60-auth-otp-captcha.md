---
title: 验证码与 OTP
module: security.otp
keywords: [验证码, captcha, OTP, 动态码, 邮箱验证码, WnCaptchaApi, 一发一验]
source:
  - security/otp/
  - security/captcha/
  - ext/data/vcode/
related: [README, 10-auth-user-role, 20-auth-session, 10-email-mailx]
---

# 验证码与 OTP 概述

登录前后的辅助认证有两道：**图形验证码 captcha** 区分人和机器，
**动态码 OTP**（短信/邮箱送达）确认「本人持有这个手机号/邮箱」。

这两件事都不依赖会话——验证发生时，往往还没登录呢。所以核心约束只有一个：
**一个账号在同一时刻只能进行一种验证**，我们把它叫做「一发一验」。

## 要解决的问题

一个对外开放注册的 Walnut，会立刻面对两类麻烦：

- **机器人**：批量注册、撞库、刷短信。敏感操作前必须先过一道人机校验，
  这就是 captcha——默认采用**拖拽式**：服务端生成一道验证题，
  序列化成字符串交给客户端，由客户端绘制出拖拽界面。
- **冒名者**：知道了某人的手机号/邮箱，不等于拥有它。注册、找回密码这类
  敏感操作前，往这个账号发一个随机码，能对上才算数。这就是 OTP。

而 OTP 落到邮箱这条路上，还有一个工程问题：邮件怎么发？SMTP 服务器、发件人
别名、模板从哪来？我们不想在 OTP 逻辑里再写一遍邮件发送，
**答案就是复用 `mailx` 已经成熟的那套约定**。

## 解决思路

整体分两套各自独立的服务，由业务方按需编排。

captcha 服务（security/captcha）——挡机器人：

```bash
|-- WnCaptchaBuilder 按配置名构建
|-- WnCaptchaApi 生成/校验验证码
|-- WnCaptchaObj 验证码的序列化载体
配置：~/.captcha/{name}.json
```

OTP 发送（security/otp）——确认账号归属：

```bash
|-- WnOTPService 配置驱动的组装器（sender/builder/store）
|-- WnOTPSender 发送接口
|-- WnEmailOTPSender 邮件基类
|-- WnSysEmailOTPSender 系统实现
配置：~/.otp/{name}.json5 + ~/.mailx/
```

下面分两节把两套服务讲清楚，最后一节看它们怎么协作。

# OTP 动态密码

动态密码的一生三步走：**生成 → 存储 → 发送**，下面分三节讲。

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

- **键**：`{prefix}{账号}:{类型}`，前缀默认 `otp:{域}:`，
  可在 Redis 配置的 `setup` 里用 `prefix` 改。**前缀是在同一个 Redis 里
  区分场景的机制**：不同域、不同业务各建一个 store 实例、各配各的前缀，
  同一个账号在不同场景下的动态密码互不覆盖，键也绝不会撞——
  比如 `otp:walnut:abc@xyz.com:EMAIL` 与 `otp:shop:abc@xyz.com:EMAIL`
  是两条完全独立的记录
- **值**：`WnOTPBean` 的 JSON 串；TTL 就是码的有效期（秒），
  到点 Redis 自动回收，不用自己清过期
- **重试上限**：默认 3 次，`setup` 里 `maxRetryTimes` 可改。
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
- **`WnSysEmailOTPSender`（系统实现）**：从 `~/.otp/default.json5`
  读配置，把这一切拼装起来。

这样做的好处是：换一个邮件来源（比如从数据库读模板、接第三方发信服务），
只写一个新的 `WnOTPSender` 实现即可，校验、存储、重试逻辑一概不动。

#### 配置：`~/.otp/default.json5`

`WnOTPService` 的配置放在系统用户（root）家目录下的 `~/.otp/`。
`default` 是配置名，将来要支持多套配置，就是再加一个 `~/.otp/{name}.json5`。
一个配置文件分两块：**sender / builder / store 三段角色装配** +
**邮件发送设置**（`sender.config` 指向的 `~/.otp/{config}.json5`，通常就是
本文件自己）。

```json5
{
  // ------- 角色装配：WnOTPService 按这三段组装 sender / builder / store -------

  // 发送器：type 决定实现类
  //   EMAIL → WnSysEmailOTPSender
  // config 是邮件发送配置名，即 ~/.otp/{config}.json5
  sender : {
    type   : "EMAIL",
    config : "default"
  },

  // 生成器：type 决定实现类
  //   num  → WnNumberOTPBuilder（n 位数字码）
  //   char → WnCharOTPBuilder（n 位字符码）
  // duration 是码的有效期（秒），缺省 300
  builder: {
    type     : "num",
    n        : 6,
    duration : 300
  },

  // 存储器：type 决定实现类
  //   redis → WnRedisOTPStore
  // config 是 Redis 连接配置名（~/.redis/{config}.json）
  // domain 是键前缀里的域（otp:{domain}:），缺省用本配置名
  store  : {
    type   : "redis",
    config : "default"
  },

  // ------- 邮件发送设置（sender.config 所指文件里的这部分） -------

  // 邮件通道：引用 ~/.mailx/{channel}.json 里的 smtp 段
  channel  : "default",

  // 邮件标题模板，支持 ${value} 等变量
  subject  : "【Walnut】您的动态验证码",

  // 语言目录：模板文件位于 ~/.mailx/i18n/{lang}/
  lang     : "en-us",

  // 模板文件名：相对 ~/.mailx/i18n/{lang}/ 的文件名
  tmpl     : "OTP.html",

  // 发送日志的方式：syslog = 直接写系统日志（目前唯一支持的方式）
  writeLog : "syslog"
}
```

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

**模板变量来自 `WnOTPBean`**，常用的有：

- `${value}`：验证码本身
- `${account}`：收件账号（同时它就是收件人地址）
- `${duration}`：有效期（秒）
- `${sendAt}`：发送时间

渲染用的是 `WnTmplX`，和 `mailx @tmpl` 是同一套模板引擎——
你在 mailx 里怎么写模板，在这里就怎么写。

#### 发送约定

SMTP 发送配置**从 `~/.mailx/` 目录里读取**，OTP 自己不存任何 SMTP 账号
密码：`~/.otp/default.json5` 里的 `channel` 对应 `~/.mailx/{channel}.json`。
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

# Captcha 服务

captcha 单独一套服务类，放在 `security/captcha` 包里。
现在只定**接口骨架**，拖拽题怎么生成、答案怎么判定，这些细节晚点再设计。

## 接口三角色

- **`WnCaptchaBuilder`**：按配置名构建验证码接口，相当于工厂——
  `builder.build("default")` 拿到一个 `WnCaptchaApi`
- **`WnCaptchaApi`**：验证码服务的统一入口，提供**生成**与**校验**两个能力
- **`WnCaptchaObj`**：一份验证码的载体，`toString()` 序列化成字符串交给客户端，
  `fromString()` 从字符串还原——客户端拿到什么、回传什么，全靠它定义

## 配置：`~/.captcha/`

配置放在 root 家目录的 `~/.captcha/`，一个文件就是一种验证码方式：

```bash
~/.captcha/
|-- default.json     # 拖拽式验证码的配置，包括图片策略等配置信息
|-- ...              # 我们以后还可以支持更多验证码方式
|-- ...              # 譬如选择汉字，计算数学题的，等等
```

## 生成与校验的分工

一次完整的验证码交互（生成 + 校验）是这样分工的：

```bash
服务器：生成部分
|-- WnCaptchaBuilder.build("default") 获取一个 WnCaptchaApi
|-- WnCaptchaApi 生成验证码对象 WnCaptchaObj
|-- WnCaptchaObj 的 toString() 将自己变成字符串
|-- 服务器将字符串返回给客户端

客户端
|-- 根据服务器返回的字符串，绘制验证码界面
|-- 接收用户输入，保留信息

服务器：校验部分
|-- WnCaptchaBuilder.build("default") 获取一个 WnCaptchaApi
|-- WnCaptchaApi 校验用户输入，返回校验结果
```

**注意一个留白**：验证码的状态（题目、答案、有效期）存在哪、怎么保证
「一发一验」，属于「细节后补」的部分——但 `WnCaptchaObj` 的序列化格式
必须能承载这些信息，因为它是客户端与服务器的唯一契约。

# OTP 与 captcha 的协作

两个服务各自独立，靠业务编排串联。以「OTP 登录」场景为例，全流程如下：

```bash
客户端
|-- 展示登录界面：用户名、密码、OTP 输入框，旁边一个「获取 OTP」按钮

第 1 轮：获取 OTP 之前，先过人机校验
|-- 用户点击「获取 OTP」，向服务器请求 captcha
服务器
|-- 进入 captcha 生成流程，返回 captcha 字符串
客户端
|-- 根据字符串绘制拖拽验证码界面
|-- 用户完成拖拽，点击登录

第 2 轮：登录提交
|-- 客户端将用户名、密码、验证码（拖拽结果）一起发送给服务器
服务器
|-- 进入 captcha 校验流程，先校验验证码
|-- 校验失败 → 直接返回失败（不碰用户名密码）
|-- 校验通过 → 继续验证用户名、密码
|-- 用户名密码不通过 → 返回登录失败
|-- 全部通过 → 触发 OTP 发送（WnOTPSender），等待用户回填
```

OTP 本身的发送时机由具体业务决定（登录场景里，用户名密码通过后再发码，
可以避免「验证码被用来探测账号是否存在」）。

# 主要代码类

- `WnCaptchaBuilder` — captcha 工厂接口，`build(name)` 按 `~/.captcha/{name}.json` 构建
- `WnCaptchaApi` — captcha 服务统一接口：生成与校验（当前为空骨架）
- `WnCaptchaObj` — 验证码载体：`toString()` 序列化给客户端，`fromString()` 还原
- `WnOTPService` — 服务组装器：按 `~/.otp/{name}.json5` 的 sender/builder/store
  三段配置创建实例，对外暴露 `sender` / `builder` / `store` 三个 public 字段
- `WnOTPSender` — OTP 发送接口，`send(WnOTPBean)` 返回 `AjaxReturn`
- `WnEmailOTPSender` — 邮件 OTP 发送抽象基类，固化「构建→取配置→投递→写日志」四步
- `WnSysEmailOTPSender` — 系统实现：从 `~/.otp/default.json5` 读配置，通道取自 `~/.mailx/`
- `WnOTPBuilder` — OTP 生成接口，`make(type, account)` 返回 `WnOTPBean`
- `WnNumberOTPBuilder` / `WnCharOTPBuilder` — 数字码 / 字符码生成器
- `WnOTPStore` — OTP 存储接口：save / remove / get / check；现有实现 `WnRedisOTPStore`
- `WnOTPBean` — 动态码信息：类型、账号、码值、有效期、重试次数
- `WnSmtpMail` / `WnMailPosting` — mailx 的邮件对象与投递器，OTP 直接复用

# 扩展阅读

- 70.functionate/10-email-mailx — mailx 的 SMTP 配置、i18n 模板目录与模板邮件机制
- 20-auth-session — 动态码登录成功后建会话
- 10.concepts/01-std-obj — 验证码对象的 expi 过期自动回收机制
