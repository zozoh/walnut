---
title: 会话管理
module: login.session
keywords: [会话, session, 票据, ticket, 多级登录, su, exit, 过期, se-sys-du]
source:
  - login/session/
  - login/WnLoginApi.java
related: [README, 10-auth-user-role, 40-auth-hydrate, ../10.concepts/03-session-env.md]
---

# 会话管理概述

登录成功即创建一个**会话对象**（不是内存里的 Session，就是一个普通对象）：
`/var/session/${TICKET}`。之后的每一次操作，身份与权限都挂在会话上；
HTTP 请求凭 cookie 里的票据找回它。

既然是对象，它就自动享有对象的一切待遇：过期自动删除、可查询、可授权
（expi 机制见 标准对象）。

## 会话元数据

```js
{
  id   : "45..8a",      // 会话 ID
  nm   : "54..8m",      // 票据（会定期变化，客户端只认它）
  expi : 1721000000000, // 过期绝对毫秒
  uid  : "u6..8r", unm : "xiaobai",
  by_tp  : "web_passwd",   // 创建场景：
                          // web_passwd 密码 / web_vcode 动态码
                          // wx_gh_xxx 微信 / microapp 小程序
                          // session 子会话（by_val=父会话ID）
  by_val : "xiaobai"
}
```

文件内容：JSON 环境变量（见 会话与环境变量）。

## 生命周期

- **创建**：登录成功即建；时长取 `se-sys-du`（默认 3600 秒），临时会话用 `se-tmp-du`（默认 10 秒）
- **触摸**：每次请求自动顺延 `expi`
- **过期**：到期由过期对象机制自动删除；登录时也会顺带清理

## 多级登录（会话树）

像 Linux 的 `su`——不换浏览器、不开隐身窗口，就在当前会话里套一层身份：

```bash
login superman     # 当前会话下创建子会话（需 op 组管或 root 组成员）
                   # 输出新会话（10 秒有效），客户端走 /u/ajax/chse 换票据
exit               # 退回父会话；没有父会话则登出
```

- 会话间以 `p_se_id` 串成树；touch 子会话会连带 touch 整条父链（防止子会话还活着父会话先过期）
- 每个会话独立过期；子会话可用 `se-tmp-du` 这种短时令牌

## 票据与安全

- 客户端只保存**票据（`nm` 字段）**，通过 cookie `SEID` 传递
- 校验三元组 `id + uid + grp`；票据会轮换，旧票据即刻失效
- 后台线程使用 `Wn.SET_BG` 类型的长会话（如 root 的后台任务），不与用户会话混用

## 在 API 里识别「谁」

HTTP 请求的上下文自动带出一组环境变量：

```bash
http-se-id / http-se-ticket       # 系统会话
http-se-me-name / http-se-me-group
http-se-vars                      # 会话环境变量
```

站点会话（域内用户的会话）另有 `http-www-*` 一组。

## 编程接口

```java
WnSession se = auth.createSession(user, Wn.SET_SYS, du);   // 建
auth.touchSession(se);                                      // 摸
auth.checkSession(ticket);                                  // 验
auth.logout(se);                                            // 销
```

JS / 命令里：`sys.se` 是当前会话；`session` 命令查看与管理。

## 注意事项

- 会话即对象：可以 `o /var/session/xxx -cqn` 查看（需权限），但**不要手工改票据字段**
- 多标签页共享 `SEID` cookie，一端 `exit` 全端失效

## 扩展阅读

- 会话与环境变量 — `sys.se` 与环境变量体系
- 混合认证机制 — 域内用户的会话如何挂进系统会话存储
