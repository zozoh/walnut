---
title: 会话机制
module: login.session
keywords: [会话, session, 票据, ticket, 多级登录, su, exit, 过期]
source:
  - login/session/
  - login/WnLoginApi.java
related: [accounts, auto-login, ../10.concepts/04-command-model.md]
---

# 会话机制

登录成功即创建**会话对象**（不是内存 Session）：`/var/session/${TICKET}`。一切身份与权限都挂会话，HTTP 请求凭 cookie 中的票据找回会话。

## 会话元数据

```js
{
  id   : "45..8a",     // 会话 ID
  nm   : "54..8m",     // 票据（会定期变化，客户端只认它）
  expi : 1721000000000, // 过期绝对毫秒
  uid  : "u6..8r", unm : "xiaobai",
  by_tp  : "web_passwd",   // 创建场景：
                          // web_passwd 密码 / web_vcode 动态码
                          // wx_gh_xxx 微信 / microapp 小程序
                          // session 子会话（by_val=父会话ID）
  by_val : "xiaobai"
}
```

文件内容：JSON 环境变量（见 [会话与环境变量](../10.concepts/03-session-env.md)）。

## 多级登录（会话树）

像 Linux 的 `su`：

```bash
login superman     # 当前会话下创建子会话（需 op 组管或 root 组成员）
                   # 输出新会话（10 秒有效），客户端走 /u/ajax/chse 换票据
exit               # 退回父会话；没有父会话则登出
```

- 会话间以 `p_se_id` 串成树；touch 子会话会连带 touch 整条父链（防止子会话活着父会话过期）。
- 每个会话独立过期；子会话可用 `se-tmp-du`（默认 10 秒）这种短时令牌。

## 生命周期

- **创建**：登录成功即建；时长取 `se-sys-du`（默认 3600s），临时会话 `se-tmp-du`（默认 10s）
- **触摸**：每次请求自动顺延 expi
- **过期**：过期对象机制自动删除（见 [expiration](../20.io-storage/expiration.md)）；登录时也会顺带清理

## 票据与安全

- 客户端只保存**票据（nm 字段）**，通过 cookie `SEID` 传递。
- 校验三元组 `id+uid+grp`；票据会轮换，旧票据即刻失效。
- 后台线程使用 `Wn.SET_BG` 类型的长会话（`auth.getSessionByUserNameAndType("root", Wn.SET_BG)`），不与用户会话混用。

## 编程接口

```java
WnSession se = auth.createSession(user, Wn.SET_SYS, du);   // 建
auth.touchSession(se);                                      // 摸
auth.logout(se);                                            // 销
auth.checkSession(ticket);                                  // 验
```

JS / 命令里：`sys.se` 当前会话；`session` 命令查看管理。

## 场景：API 里识别「谁」

HTTP API 的上下文自动带：

```bash
http-se-id / http-se-ticket     # 系统会话
http-se-me-name / http-se-me-group
http-se-vars                    # 会话环境变量
```

站点会话另有 `http-www-*` 一组（见 [auth-history](../50.regapi/auth-history.md)）。

## 注意事项

- 会话即对象：可以 `o /var/session/xxx -cqn` 查看（需权限），但**不要手工改票据字段**。
- 多标签页共享 `SEID` cookie，一端 `exit` 全端失效。
- 自动登录不建会话树，是独立签名机制，见 [auto-login](auto-login.md)。
