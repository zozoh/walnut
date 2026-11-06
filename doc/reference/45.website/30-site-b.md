---
title: B 类站：管理后台的自定义登录页
module: web
keywords: [B类站, 登录页, 自定义登录, /a/login, WnLoginPage, wn_www_grp]
source:
  - web/module/AppModule.java
  - web/bean/WnLoginPage.java
  - login/site/WnLoginSite.java
related: [10-domain-mapping, 20-site-a, 40-site-c, 30-auth-in-domain]
---

# B 类站概述

**B 类站今天的定位是管理后台的自定义登录页。**

一个域的管理后台域名（比如 `admin.foo.com`），访客打开它的 `/a/login/`，
看到的是这个域定制的登录界面，而不是系统默认的登录页。
登录认证依然走域自己的用户体系，与界面无关。

## 解决思路

B 类记录不改路径：过滤器只把 `wn_www_grp`（域）和 `wn_www_site`（站点路径）
注入请求，原样放行。访客访问 `/a/login/**` 时，`AppModule` 拿这两个属性
渲染登录页：

```text
admin.foo.com/a/login/
  │  ① 域名记录 {domain:foo, site:~/www, tp:B} 注入属性
  ▼
AppModule.login_page
  ② 有会话 → 直接进默认应用
  ③ 没会话 → 按 wn_www_grp 找域用户，渲染定制登录页
```

## 域名记录配置

```bash
echo '{}' > /domain/admin.foo.com
o /domain/admin.foo.com -e 'domain:foo; site:~/www; tp:B'
```

- **`domain`**：登录页归属的域
- **`site`**：站点路径。一个字段两用——既是登录页目录的候选，
  又是登录站点配置的定位依据（见 [域内用户存储与认证](../30.auth/30-auth-in-domain.md)）

## 登录页的查找顺序

`WnLoginPage`（`web/bean/`）按下面的顺序找登录页，先找到谁用谁：

1. **域用户配置的路径**：`site`（即注入的 `wn_www_site`）；没写就用域用户的
   `LOGIN_PAGE` 元数据。是 `http(s)://` 开头就直接 302 过去；否则按域主目录
   展开成登录页目录
2. **`/etc/hosts.d/<host>/pages`**：按域名细分的登录页目录，找不到就用
   `/etc/hosts.d/default/pages`
3. **内置登录页**：JAR 里的 `login.html`，动态渲染（永远按当前登录模式生成）

细节：

- 目录里的 `index.html` 是登录页入口；请求可带 `pg` 参数换页
- 文件带 `tp:"html"` 元数据时，内容先过 `WnTmpl` 模板渲染再输出——
  模板变量包括系统配置、域用户信息、`DOMAIN`（主组名）等
- 第一层里声明的目录不存在或不是目录，只打日志，回退到下一层
- `pg=sys` 时不查 `/etc/hosts.d`，直接进内置登录页

## 登录与认证

登录页只是皮，认证走站点的用户库：站点配置（`accounts` / `roles`）定义
域内用户住在哪里，`WnLoginSite` 负责解析。整条机制见
[域内用户存储与认证](../30.auth/30-auth-in-domain.md)；登录页上的验证码、
OTP 等登录模式配置见 [captcha 与 OTP 的登录协同](../30.auth/62-auth-with-captcha-otp.md)。

## 注意事项

- B 类不改路径，所以 `admin.foo.com` 上的 `/api/...` 与 C 类同理不会被重写
- `site` 一个字段两用（登录页目录 + 登录站点配置），两个机制共用同一条 `/domain` 记录
- 登录页目录按域主目录解析（`~/` 与相对路径都相对 `/home/<域>`）

# 延伸阅读

- [域名转发](10-domain-mapping.md) — B 分支的注入机制
- [域内用户存储与认证](../30.auth/30-auth-in-domain.md) — 登录站点与用户库
- [captcha 与 OTP 的登录协同](../30.auth/62-auth-with-captcha-otp.md) — 登录页的验证码 / OTP 配置
- [C 类站](40-site-c.md) — 同一套域名记录的另一半
