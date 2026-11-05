---
title: captcha 与 OTP 的登录协同
module: security.captcha-otp
keywords:
  [
    多租户,
    登录模式,
    系统权鉴,
    域权鉴,
    配置目录,
    WamOcmInfo,
    get_ocm_info,
  ]
source:
  - web/module/app/WamOcmInfo.java
  - web/module/app/WamOTPSend.java
  - web/module/app/WamCaptcha.java
  - web/module/AppModule.java
related: [README, 60-auth-otp, 61-auth-captcha]
---

# captcha 与 OTP 的登录协同概述

给系统装上人机验证码（captcha，见 [人机验证码 captcha](61-auth-captcha.md)）和动态密码（OTP，见
[动态密码 OTP](60-auth-otp.md)）并不麻烦，麻烦的是 Walnut 是个多租户系统：系统用户（root / demo……）
各有各的域，每个域有自己的用户库，也各有各的验证码、OTP、邮件配置。captcha 服务
与 OTP 服务本身都不关心这些配置放在谁家——**放哪、用哪份，是本文要讲的协同规则**。

## 配置放在谁家

系统用户 `demo` 的域内有一个账号 `xiaobai`，配置目录的布局：

```bash
# 系统账号存放的位置
/sys/usr/
|-- demo        # 系统账户，用户名为 demo，元数据 email:"demo@qq.com"

# 系统 root 用户的主目录，配置所有系统用户的验证码与 OTP
/root/
|-- .redis/     # Redis 连接配置目录
|-- .mailx/     # 邮件配置目录
|-- .captcha/   # 验证码配置目录
|-- .otp/       # OTP 配置目录

# 系统域用户 demo 的主目录，配置 demo 域内账号的验证码与 OTP
/home/demo/
|-- .redis/     # Redis 连接配置目录
|-- .mailx/     # 邮件配置目录
|-- .captcha/   # 验证码配置目录
|-- .otp/       # OTP 配置目录
```

## 两种逻辑上的登录模式

一次登录请求带两个标识：`domain`（域，系统用户名，可为空）和 `account`（登录名）。
按这两个标识判断用户打算用哪套权鉴：

- **系统权鉴登录**：`domain` 为空，或 `account == domain`。只有系统用户库里才有与域
  同名的账号——`demo` 是系统用户，`demo` 域自己的用户库里只有 `xiaobai` 这类子
  账号。账号与域同名时，意图必然是用系统权鉴接口登录这个系统用户本身。
- **域权鉴登录**：声明了 `domain`，且 `account != domain`。意图是用域站点自己的用户
  库登录。

**两种模式的判断只看 `domain` / `account` 两个字符串，与用户对象的类型无关。**
这一点是有教训的：域站点经 `WnLoginApiMaker.forHydrate()` 解析出来的域用户，race
也是 `SYS`——用 `user.isSysUser()` 分不出两种模式。所以模式判断收敛在
`AppModule.get_ocm_info(domain, account)` 一处，全系统只此一份口径。

## 配置目录的取用规则

模式定了，captcha / OTP / mailx 三个服务的**配置主目录**跟着定。服务类自己不再
持有任何权鉴接口，它们拿到的只有一个 `WamOcmInfo`，全部信息从里面取：

- `confHomePath`：配置主目录。captcha / OTP 的配置文件（`~/.captcha/`、`~/.otp/`、
  `~/.mailx/`）都从这里展开。
- `user`：操作用户，用来取邮箱等元数据。captcha 阶段用户可能还不存在（譬如注册
  场景），解析不到时置 `null`，由调用方决定要不要报错。
- `domain` / `account`：本次请求的原文，供服务类记录票据、拼报错信息等。

其中 `confHomePath` 的取用规则：

- **系统权鉴**：取 root 用户的家目录——root 的主目录就是全部系统级配置的所在地。
- **域权鉴**：取域用户（系统用户 `demo` 本身）的家目录，即 `/home/demo/`。

配套的**登录接口**也按模式取用（`AppModule.get_auth(domain, account)`）：

- **系统权鉴**：直接用系统默认权鉴接口 `auth()`。
- **域权鉴**：用 `WnLoginSite` 模式构建域站点自己的用户库接口（与域站点登录同一
  套路）。

注意配置主目录与登录接口是**两条独立的分支**：配置主目录永远经系统权鉴解析（域
本身是系统用户），登录接口才分系统 / 域两套。

拿 `demo` 账号登录 `demo` 域（`domain="demo", account="demo"`）来说：这是系统
权鉴模式——配置主目录取 `/root/`，OTP 发到 `demo` 系统用户的元数据邮箱，而不是
误取 `/home/demo/` 下的域配置。「同名账号到底算谁的」正是服务漂移最容易出错的地
方。

## 端到端流程

```bash
客户端
|-- 登录界面：账号、密码、OTP 输入框，旁边一个「获取 OTP」按钮

第 1 轮：人机校验
|-- 点击「获取 OTP」，先请求 captcha：POST /a/captcha/get/{domain}?account=...
|-- 渲染验证码，用户拖拽 / 点选
|-- POST /a/captcha/verify/{domain}
|   请求体：{id, domain, account, captcha, data}
|-- 通过 → 拿到绑定 account 的临时票据 ticket（一次性，带过期）

第 2 轮：发送 OTP
|-- POST /a/otp/send/{domain}?ticket=...&account=...
|-- 服务端先按 (domain, account) 构建 WamOcmInfo：配置主目录、用户、邮箱
|   一次到位；之后消费 ticket（checkTicket）→ 生成 → 存储 → 发送 OTP 到邮箱

第 3 轮：登录提交
|-- POST /a/auth/login/{domain}，带账号、密码、otp_val
|-- 服务端按同样规则取配置目录与登录接口，OTP 一次性消费校验
```

三轮请求，`(domain, account)` 都是同一对；每一轮服务端第一件事都是构建
`WamOcmInfo`，之后的 captcha / OTP 逻辑只认这个对象，不再各自去碰权鉴。

## 代码落点

- `AppModule.get_ocm_info(domain, account)` — 通用入口：按登录模式一次性解析出
  `WamOcmInfo`。captcha / otp 相关的入口函数（`captcha_get`、`captcha_verify`、
  `otp_send`）都从这里起步。
- `AppModule.get_auth(domain, account)` — 按登录模式返回登录接口，供
  `get_ocm_info` 解析用户、以及登录提交类入口使用。
- `WamOcmInfo` — 操作上下文对象：`confHomePath` / `user`（可空）/ `domain` /
  `account`。
- `WamCaptcha` / `WamOTPSend` — captcha / OTP 发送的服务类，只接收 `WamOcmInfo`
  而**不持有任何权鉴接口**：配置目录取 `ocm.confHomePath`，用户取 `ocm.user`
  （OTP 发送时为空报 `e.otp.account.noexists`，邮箱为空报
  `e.otp.account.no_email`）。
- `WamSysLoginByPassword` / `WamDomainLoginByPassword` — 两个登录服务类：操作
  上下文由入口函数经 `get_ocm_info(domain, account)` 构建传入（与 captcha /
  otp 入口同一套路），OTP 校验配置目录取 `ocm.confHomePath`，不再各自维护
  OTP 配置目录逻辑。域登录入口先解析站点拿到 `domain` 再调
  `get_ocm_info`，站点只解析一次。

## 默认系统登录页

我们把默认登录页的认证选项集中在
`ROOT/etc/hosts.d/default/pages/index.js` 的 `SIGN_IN_CONFIG` 常量中：

```js
const SIGN_IN_CONFIG = {
  domain: undefined,
  mode: "PASSWD_OR_OTP",
  otp: undefined,
  captcha: undefined,
  otpCooldown: 60,
};
```

- **mode**：`PASSWD_AND_OTP` 同时显示密码和 OTP；`PASSWD_OR_OTP`
  提供密码 / OTP 切换，初始选择密码；`OTP_ONLY` 仅显示 OTP；
  `PASSWD_ONLY` 保留原有密码登录。未选择的认证字段不参与提交。
- **domain**：仅指定 Captcha / OTP 接口的域，默认不指定，走系统权鉴。
  此页仍向 `/a/sys_login_by_passwd` 提交，不因此变成域账号登录页。
- **otp / captcha**：配置名，默认不指定，由服务器选择。
- **otpCooldown**：发送成功后的客户端冷却秒数，`0` 表示不启用冷却。

**前端 mode 必须与服务器的 `app-module-sys-login` 策略匹配；
修改页面常量不会修改后端认证要求。**

页面直接引用 `tac/tac.min.js` 和 `tac/tac.css`，不需要 Vue 或构建步骤。
点击 Get OTP 前需填写账号，双重认证模式还需填写密码。页面先弹出
TAC 人机验证码，通过后用绑定该次账号的 ticket 发送 OTP；发送成功才开始冷却，
失败会在表单中提示。可以使用关闭按钮或 Escape 退出验证码。

最终登录保持原生 POST 与服务器跳转行为，参数使用 `name`、`passwd`、
`otp`、`otp_val`，只提交当前选择的认证方式。账号和 OTP 去掉前后空白，
密码保留原值。

## JAR 内置兜底登录页

当宿主没有可用的登录页时，系统还可以使用
`src/com/site0/walnut/web/module/login.html`。它作为 classpath 资源随 JAR
发布，由 `WnLoginPage.getBuiltinLoginView(vars)` 使用 `WnTmplX` 动态渲染。

**兜底页不依赖宿主静态资源：页面样式、登录脚本和 TAC JS/CSS 全部内嵌。**

页面只保留文字标题及表单，没有 LOGO、背景图片、外部字体，也不依赖
jQuery 或 lodash。Captcha 图片由同源认证接口返回；验证码图标、计算 worker
等使用 SDK 的内嵌数据。内嵌 SDK 不执行外部脚本路径推导，禁用态样式也不加载外部图片。

这份 HTML 的 `login-script` 段同样有独立的 `SIGN_IN_CONFIG`：
`mode` 由 `${app-module-sys-login?PASSWD_AND_OTP}` 读取服务端配置，
未指定时采用 `PASSWD_AND_OTP`；`otpCooldown: 60`，其余三项不指定。
四种模式、验证码、OTP 发送与冷却、提交字段规则和前述默认页一致。
默认页的常量与兜底页的模板配置分别维护，兜底页仍使用系统登录接口。

HTML 中的 JavaScript 模板字符串必须写成 `$${expression}`，包括内嵌 TAC
SDK 中的插值。`WnTmplX` 会将它原样输出成客户端所需的 `${expression}`；
只有需要服务端替换的 `mode` 保留普通 `${...}`。否则请求地址、错误详情、
倒计时和验证码界面都会被模板引擎错误替换。

更新 TAC 时，需要同步检查这份内嵌副本的脚本初始化和资源引用，不能直接加入
`script src` 或外部样式引用，否则会失去托底能力。

# 扩展阅读

- [动态密码 OTP](60-auth-otp.md) — 动态密码 OTP 服务：生成、存储（Redis）、邮件发送
- [人机验证码 captcha](61-auth-captcha.md) — 人机验证码服务：接口抽象与 tianai 实现
