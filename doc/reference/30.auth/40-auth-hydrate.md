---
title: 混合认证机制
module: login
keywords: [混合认证, hydrate, for_hydrate, site, 域用户登录, 系统会话, sysSessionStore]
source:
  - login/WnLoginApiMaker.java
  - login/session/WnSessionStoreProxy.java
  - web/filter/WnCheckSession.java
  - impl/box/cmd/cmd_login.java
  - api/box/WnBoxContext.java
related: [README, 20-auth-session, 30-auth-in-domain, 10-auth-user-role]
---

# 混合认证机制

前两种模式各有尴尬：

- 系统用户（`for_sys`）能进域主目录操作，但你的站点用户不是系统用户
- 域内用户（`for_domain`）有自己的账户体系，但登录后出不了站点

**混合模式（hydrate）把两者接起来**：域内用户登录，拿的是一个**系统会话**。

## 工作方式

`WnLoginApiMaker.forHydrate()` 的组装规则（源码注释写得很直白）：

- **会话**：无视站点配置，强制采用系统会话存储（Ioc 里的 `sysSessionStore`）——
  会话进 `/var/session/`，与系统用户会话同池管理
- **用户/角色**：仍按站点配置（`accounts` / `roles`），域内用户的密码与角色照旧
- 创建会话时会打上 `site` 属性——系统会话带上「它属于哪个站点」的标记，
  之后 `sysLoginApi` 取到这个会话，就自动用对应站点的用户/角色存储来解析身份

一句话：**会话是系统的，人是站点的**。

## 谁在用它

混合模式是系统里最常见的组装方式：

- `login` 命令（`cmd_login`）——控制台里切换/登录身份
- `WnCheckSession`（Web 过滤器）——校验 HTTP 请求的系统会话
- `AppModule` / `WnBoxContext` —— App 与命令沙箱的执行上下文

也就是说，HTTP 请求走进来，过滤器用混合模式验会话；会话里带着 `site`，
身份解析就落到对应站点的用户库——系统用户和域内用户在同一条链路上各得其所。

## 登录后的世界

域内用户凭混合会话登录成功，就进入了**域主目录**（`/home/$站点域`）——
可以执行命令、读写自己有权限的对象、调 regapi。权限判定走标准的
对象权限体系，域主再通过 `@角色` 给协作成员细分权限。

这就是「用域账户使用 Walnut」的完整路径。

## 注意事项

- 混合会话出现在 `/var/session/`，系统管理员可见——站点用户量大会话也多，
  `se-sys-du` 与会话过期机制照单适用
- 会话的 `site` 属性是链路的关键，自定义会话创建逻辑时不要丢掉它

## 扩展阅读

- 会话管理 — 系统会话的完整机制
- 域内用户存储与认证 — 站点用户库的配置
