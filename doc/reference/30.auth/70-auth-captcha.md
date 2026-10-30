---
title: 人机验证码 captcha
module: security.captcha
keywords:
  [
    验证码,
    captcha,
    人机校验,
    行为验证码,
    WnCaptchaApi,
    WnCaptchaAnswerStore,
    WnCaptchaTicketStore,
    tianai,
  ]
source:
  - security/captcha/
related: [README, 60-auth-otp]
---

# 人机验证码 captcha 概述

**人机验证码 captcha** 干一件事：区分人和机器。批量注册、撞库、刷短信的
机器人，必须在敏感操作前被拦下来。OTP 负责「确认账号归属」，captcha 负责
「挡机器人」，两者独立成服务、由业务编排串联（协作流程见本文最后一节，
OTP 本身见 60-auth-otp）。

它也不依赖会话——验证发生时，往往还没登录呢。

## 要解决的问题

一个对外开放注册的 Walnut，会立刻面对这个麻烦：

- **机器人**：批量注册、撞库、刷短信——不拦下来，短信账单先爆，
  数据库里也会灌满假账号。敏感操作前必须有一道**人机校验**，
  这就是 captcha 存在的意义。

## 解决思路

我们不自己造验证码轮子：滑块轨迹校验、点选坐标比对、答案缓存、
容错判定，都有成熟的行为验证码库可选。但库只是**实现的选择**——
Walnut 自己定义一套抽象的接口，把具体库藏在接口后面：

```bash
|-- WnCaptchaBuilder       # 按配置构建验证码
|-- WnCaptchaApi           # 生成/校验验证码
|-- WnCaptchaObj           # 验证码的序列化载体
|-- WnCaptchaAnswerStore   # 答案的临时存取
|-- WnCaptchaTicketStore   # 临时票据的签发与校验
配置：~/.captcha/{name}.json
```

当前默认的实现选型是 [tianai-captcha](https://doc.captcha.tianai.cloud/)
（dromara 开源，Apache-2.0），它的接入方式见文末「tianai 实现」章节。
将来换库或自研，业务代码不用动。

# Captcha 服务

## 接口角色

- **`WnCaptchaBuilder`**：按配置名构建验证码接口，相当于工厂——
  `builder.build("default")` 读取 `~/.captcha/default.json`，
  组装出底层引擎（tianai 的 `ImageCaptchaApplication`）与
  `WnCaptchaAnswerStore`、`WnCaptchaTicketStore`，
  把它们注入一个 `WnCaptchaApi` 实现类返回。
  业务代码不直接接触引擎
- **`WnCaptchaApi`**：验证码服务的统一入口，是**业务逻辑面对的
  高级接口**：
  - `generate()` 生成一份验证码，返回 `WnCaptchaObj`，
    同时把标准答案临时存入 `WnCaptchaAnswerStore`
  - `check(id, data)` 校验用户提交的行为数据，通过即作废该验证码
  - `createTicket(account)` 校验通过后，签发一张**临时票据**
    （ticket → account 存入 `WnCaptchaTicketStore`，一次性，带过期）
  - `checkTicket(ticket, account)` 业务提交时校验票据：取出映射、
    比对 account、验过即作废
- **`WnCaptchaObj`**：一份验证码的载体，`toString()` 输出序列化
  JSON，`fromString()` 从字符串还原；web 层会把它包进 `AjaxReturn`
  的 `data` 再返回客户端。吐出的 JSON 结构由具体实现定义
- **`WnCaptchaAnswerStore`**：答案的**临时存取**接口——生成时 `save`，
  校验时 `get` 比对、`remove` 作废，带过期时间。答案由生成它的节点
  在校验时消费，实现类可以是节点本地缓存，也可以是 Redis
- **`WnCaptchaTicketStore`**：临时票据的**签发与校验**接口——
  `createTicket(account)` 写入 ticket → account 映射，
  `checkTicket(ticket, account)` 比对账号并作废。票据在业务提交时
  才被消费，那次请求可能落到集群里另一个节点，所以实现类必须是
  多节点共享的存储（Redis）

## 配置：`~/.captcha/`

配置放在 root 家目录的 `~/.captcha/`，一个文件就是一种验证码方式：

```bash
~/.captcha/
|-- default.json     # 默认验证码
|-- click.json       # 另一种验证码方式
|-- ...
```

> 注意：`~` 是**会话变量 `HOME`** 的简写，不是 IO 层认的路径。工厂读配置前
> 会先用 `Wn.normalizePath(ph, vars)` 把它常规化成标准绝对路径——
> 譬如域 `demo` 的 `~/.captcha/default.json` 会变成
> `/home/demo/.captcha/default.json`，再交给 WnIo 读取。

配置文件的字段语义由具体实现解释。下面是当前默认实现（tianai）
的配置示例：

```json5
{
  // 验证码类型，取值由实现定义（tianai：SLIDER / ROTATE / CONCAT /
  // WORD_IMAGE_CLICK / POW）
  type: "SLIDER",

  // 答案缓存过期时间（毫秒），到点作废需重新获取
  expire: 120000,

  // 校验容错档位：1-10 的整数，数值越大越宽松（越容易通过）。
  // 这是抽象尺度，具体多大误差算通过，由验证码实现类自行理解
  tolerant: 5,

  // 滑块干扰：生成迷惑性缺口
  obfuscate: false,

  // 背景图目录：Walnut 对象存储里的一个图片目录，里面放一组图片，
  // 每次生成验证码时随机取一张做背景（建议 600x360）。
  // 实现类会把目录读成图片列表并缓存（默认 WnSimpleCache 10 分钟），
  // bgImgCache 可调整缓存毫秒数，0 表示完全不缓存
  bgImgDir: "~/.captcha/bg",
  bgImgCache: 600000,

  // 点选验证码的字体；留空表示本验证码不支持文字点选类型
  fontPath: "~/.captcha/font/simhei.ttf",

  // 临时票据的过期时间（毫秒）
  ticketExpire: 600000,

  // 答案与票据的存储配置（当前仅支持 redis，连接配置引用
  // ~/.redis/{name}.json，与 OTP 的存储选择一致）
  store: {
    type: "redis",
    name: "default", // Redis 连接配置名
    prefix: "captcha:default:", // 键前缀，留空则 captcha:{服务配置名}:
                               // 同时约束引擎内部键：tianai 引擎的缓存键本是
                               // 自己拼的 {captcha}:{id}，工厂组装时经
                               // TACBuilder.prefix 对齐到这里，不同域/配置的
                               // 键空间才不互撞
  },
}
```

## 生成与校验的分工

一次完整的验证码交互（生成 + 校验）是这样分工的：

```bash
服务器：生成部分
|-- WnCaptchaBuilder.build("default") 获取一个 WnCaptchaApi
|-- WnCaptchaApi.generate() 生成 WnCaptchaObj，答案存入 WnCaptchaAnswerStore
|-- WnCaptchaObj 的 toString() 将自己变成字符串
|-- 服务器用 AjaxReturn 包裹后返回客户端：{ok:true, data: ...}

客户端
|-- 根据服务器返回的字符串，绘制验证码界面
|-- 接收用户输入，采集行为数据（拖拽轨迹、点选坐标……）
|-- 将 {id, data: 行为数据} 发送给服务器

服务器：校验部分
|-- WnCaptchaBuilder.build("default") 获取一个 WnCaptchaApi
|-- WnCaptchaApi.check(id, data)：取出答案比对，通过即作废
|-- 通过 → 继续业务；不通过 → 客户端刷新重来
```

**服务器吐出什么**：web 层把 `WnCaptchaObj.toString()` 的结果放进
`AjaxReturn`（`org.nutz.web.ajax.AjaxReturn`）的 `data` 字段返回：

```json
{
  "ok": true,
  "data": {
    /* WnCaptchaObj 的 JSON */
  }
}
```

`data` 的结构由具体实现定义，但都包含 `id`（本次验证码的唯一
标识）与渲染所需的全部数据，客户端拿到即可绘制，零额外请求。

统一用 `AjaxReturn` 包裹（成功 `{ok:true, data:...}`，失败
`{ok:false, errCode, msg}`）有一个直接的好处：验证码服务自己出
问题（配置缺失、Redis 不通、生成异常……）时，客户端看到的是
`ok:false` + 错误码，可以和「校验不通过」明确区分开——
客户端不至于把服务故障误当成「拖拽没对齐」让用户反复重试。

**客户端提交什么**：`{id, data}`。`id` 对应生成的那份验证码，
`data` 是用户行为的采集结果。服务器据此从 `WnCaptchaAnswerStore`
取出标准答案比对。

三个老问题，在抽象层面都有了落点：

- **答案存哪**：`WnCaptchaAnswerStore`，带过期
- **一发一验**：每次生成一个新 `id`，天然互不干扰；校验通过即作废
- **防重放**：作废之外，业务侧还要用临时票据把校验结果和业务动作
  绑在一起（见下）

## 临时票据：把校验结果和业务动作绑在一起

一次 `check` 通过只证明「刚才那次拖拽是人做的」，距离业务动作真正
执行往往还隔着几次交互（登录场景里，校验通过后还要填 OTP）。所以
校验通过后，服务器**不返回验证码的 `id`**，而是签发一张**临时票据**：

```bash
|-- check 通过 → createTicket(account)
|--   在 WnCaptchaTicketStore 里生成 ticket → account 的映射（一次性，带过期）
|--   同样用 AjaxReturn 包裹后回给客户端：{ok:true, data:{ticket:"..."}}
|-- 业务提交时带上 ticket + account（如登录请求里的 captchaTicket + 用户名）
|-- 服务器执行业务前 checkTicket(ticket, account)：
|--   取出映射、比对 account，验过立即作废，才放行
```

这一步是必须的：没有它，攻击者可以跳过拖拽、直接调用业务接口；
有了票据，人机校验的结论被**绑定到具体账号**上，机器批量重放
别人的 ticket 也过不了 account 比对。

# OTP 与 captcha 的协作

两个服务各自独立，靠业务编排串联。以「OTP 登录」场景为例，全流程如下：

```bash
客户端
|-- 展示登录界面：用户名、密码、OTP 输入框，旁边一个「获取 OTP」按钮

第 1 轮：获取 OTP 之前，先过人机校验
|-- 用户点击「获取 OTP」，向服务器请求 captcha
服务器
|-- WnCaptchaApi.generate()，以 {ok:true, data: WnCaptchaObj JSON} 返回
客户端
|-- 渲染验证码界面，用户完成拖拽/点选
|-- 客户端将 {id, 行为数据} 提交给服务器
服务器
|-- WnCaptchaApi.check(id, data) 校验
|-- 通过 → createTicket(account) 签发临时票据，
|--   以 {ok:true, data:{ticket}} 回给客户端
|--   校验不通过 → {ok:false}，客户端刷新重来

第 2 轮：登录提交
|-- 客户端将用户名、密码、ticket 一起发送给服务器
服务器
|-- 先 checkTicket(ticket, 用户名)，失败 → 直接返回（不碰用户名密码）
|-- captcha 通过 → 验证用户名、密码
|-- 用户名密码不通过 → 返回登录失败
|-- 全部通过 → 触发 OTP 发送（WnOTPService.send），等待用户回填
```

OTP 本身的发送时机由具体业务决定（登录场景里，用户名密码通过后再发码，
可以避免「验证码被用来探测账号是否存在」）。

# tianai 实现

当前默认实现选型 [tianai-captcha](https://doc.captcha.tianai.cloud/)
（dromara 开源，Apache-2.0）：滑块、旋转、滑动还原、文字点选、
工作量证明（POW）五种行为验证码，答案缓存、过期、容错、轨迹校验
都是现成的。下面是我们的抽象角色与 tianai 的对应关系。

## 角色映射

- **`WnCaptchaBuilder`** → 构建 tianai 引擎 `ImageCaptchaApplication`
  （线程安全，单例；非 Spring 环境用 `TACBuilder` 链式组装），
  并按配置创建 `WnCaptchaAnswerStore` 与 `WnCaptchaTicketStore`
  实现，一起注入 `WnCaptchaApi` 实现类（如 `WnTianaiCaptchaApi`）
- **`WnCaptchaApi`** → 业务面对的高级接口，由实现类包装
  `ImageCaptchaApplication` + 两个 Store：
  - `generate()` → `generateCaptcha(type)`，返回 `ApiResponse<ImageCaptchaVO>`
  - `check(id, data)` → `matching(id, ImageCaptchaTrack)`
  - `createTicket` / `checkTicket` → 不经过 tianai，直接读写
    `WnCaptchaTicketStore`（ticket → account 映射）；tianai 的
    `secondaryVerification(id)` 与我们自己的票据重复，
    实现里不用它
- **`WnCaptchaObj`** → 包装 `ImageCaptchaVO`，`toString()` 即其 JSON
- **`WnCaptchaAnswerStore`** → 包装 tianai 的 `CacheStore`
  （官方有 Redis 实现；Walnut 侧用已有的 `Wedis` 接 Redis，
  与 OTP 的存储选择一致——多节点共享、重启不丢）。
  答案由生成它的节点在校验时消费，若追求极致性能，
  也可以换成节点本地缓存的实现
- **`WnCaptchaTicketStore`** → 不经过 tianai，Walnut 侧直接用
  `Wedis` 接 Redis 实现（票据在业务提交时才被消费，可能跨节点，
  必须共享存储）
- **背景图/模板图**：tianai 用 `ResourceStore` 管理，
  Walnut 实现一个基于 `WnIo` 的 ResourceStore，从 `bgImgDir`
  的图片列表中随机取图；模板图直接用库内置的

## 配置字段的对应

`~/.captcha/{name}.json` 的字段是抽象尺度，由实现类翻译成
底层库的语言。tianai 实现的翻译方式：

- **`type`** → 验证码类型常量：`SLIDER` / `ROTATE` / `CONCAT` /
  `WORD_IMAGE_CLICK` / `POW`
- **`expire`** → 答案缓存过期（毫秒）。我们的模型里一个配置文件就是
  一种验证码，不同类型的过期各写各的文件，无需按类型再分
- **`tolerant`** → tianai 的容错是 0~1 的小数（默认 0.02）；
  实现类按 `tolerant * 0.005` 线性换算（档位 1-10 对应 0.5%~5%），
  档位越高容错越大
- **`obfuscate`** → 滑块干扰缺口
- **`bgImgDir`** → 经 Walnut 基于 WnIo 的 ResourceStore 读取的背景图
  目录（建议 600x360），随机取图；目录列表按 `bgImgCache`
  缓存（WnSimpleCache，0 为不缓存）
- **`fontPath`** → 文字点选的字体，同样经 WnIo ResourceStore 读取；
  留空则拒绝生成 `WORD_IMAGE_CLICK` 类型的验证码

## 服务器吐出什么（tianai 口径）

`ImageCaptchaVO` 的 JSON 放在 `AjaxReturn` 的 `data` 里返回：

```json
{
  "ok": true,
  "data": {
    "id": "…",
    "type": "SLIDER",
    "data": { "backgroundImage": "base64…", "templateImage": "base64…" }
  }
}
```

`data` 随类型而异：滑块是背景图 + 滑块图的 base64，点选是带提示文字的图，
POW 是挑战参数 `[difficulty, saltLen, count]`（默认 `[4,16,4]`）。
图片全部 base64 内联，客户端零额外请求——官方 tac SDK 认的就是
这个格式，可直接对接（`new TAC({requestCaptchaDataUrl, validCaptchaUrl,
bindEl, validSuccess})`）。

## 客户端提交什么（tianai 口径）

提交的请求体本身就是一个裸 JSON 对象：

```json5
{
  // Captcha 的标识 ID
  id: "SLIDER_...",
  // 提供验证码服务的域名
  domain: "demo",
  // 用户的登录名
  account: "xxx",
  // Captcha 的配置名称，如果为空，实现类类会自行理解，
  // 通常会理解为 default
  captcha: null,
  // 该验证码客户端信息
  // 譬如 SDK 采集的轨迹：
  // - 滑块/旋转：拖动过程的坐标时间序列
  // - 文字点选：点击坐标序列
  // - POW：求解出的 nonce
  data: { trackList: [] },
}
```

服务器拿 id 从 CacheStore 取出标准答案，与轨迹比对（带容错）。

## 非 Spring 环境

Walnut 不是 Spring 应用，tianai 提供链式 Builder：

```java
ImageCaptchaApplication app = TACBuilder.builder()
        .addDefaultTemplate()          // 内置模板
        .addResource(...)              // 自定义背景图
        .expire(...)
        .setCacheStore(...)            // 接我们的 Wedis CacheStore
        .build();
```

# 主要代码类

- `WnCaptchaBuilder` — captcha 工厂接口：`build(name)` 按 `~/.captcha/{name}.json`
  组装引擎与两个 Store，注入 WnCaptchaApi 实现类
- `WnCaptchaApi` — captcha 服务统一接口：生成与校验，业务直接使用
- `WnCaptchaObj` — 验证码载体：`toString()` 序列化给客户端，`fromString()` 还原
- `WnCaptchaAnswerStore` — 答案临时存取接口：`save` / `get` / `remove`，带过期
- `WnCaptchaTicketStore` — 临时票据接口：`createTicket` 签发、`checkTicket` 校验并作废
- `WnSysCaptchaBuilder` — 工厂实现：读配置、组装 tianai 引擎与两个 Redis
  Store；`build(name)` 每次现场组装，不做缓存（`security/captcha/impl`）
- `WnTianaiCaptchaApi` — tianai 版 WnCaptchaApi 实现：包装
  ImageCaptchaApplication + 两个 Store（`security/captcha/impl/tianai`）
- `WnTianaiCaptchaObj` — tianai 版 WnCaptchaObj：包装 ImageCaptchaVO
- `WnRedisCaptchaAnswerStore` — 答案的 Redis 实现：一身二任，同时实现
  tianai 的 CacheStore（引擎校验数据走同一套键空间）
- `WnRedisCaptchaTicketStore` — 票据的 Redis 实现：ticket → account 映射，
  一次性消费
- `WnIoCaptchaResourceStore` — tianai ResourceStore 实现：背景图/字体从
  对象存储随机读取（WnSimpleCache 缓存目录列表），模板委托内置本地存储
- `WnIoResourceProvider` — tianai ResourceProvider 实现（类型 `wnio`）：
  从对象存储打开资源流
- `ImageCaptchaApplication` — tianai 的验证码引擎：生成、轨迹匹配

# 扩展阅读

- 60-auth-otp — 动态密码 OTP 服务：生成、存储（Redis）、邮件发送的完整机制
