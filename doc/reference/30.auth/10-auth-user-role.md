---
title: 系统用户、角色与密码存储
module: login.usr
keywords: [系统用户, 用户组, 角色, 密码, salt, sha1, adduser, usermod, passwd, /sys/usr]
source:
  - login/usr/
  - login/role/
  - login/UserRace.java
  - impl/box/cmd/cmd_adduser.java
  - impl/box/cmd/cmd_passwd.java
related: [README, 20-auth-session, 0-auth-pvg, ../10.concepts/02-domain-home.md]
---

# 系统用户、角色与密码存储概述

系统用户是 Walnut 的「居民」：登录控制台、拥有自己的域（主组）。
在 一切皆对象 的视角下，**一个用户就是一个对象**。

## 用户对象：`/sys/usr/$nm`

每个用户一个文件，元数据就是账户信息：

```js
{
  id : "u6..8r",        // 唯一 ID
  nm : "xiaobai",       // 登录名（唯一）
  phone : "139..",      // 唯一，可登录
  email : "x@y.z",      // 唯一，可登录
  passwd : "加盐散列",   // 密码（见下文）
  salt   : "随机盐",
  grp  : "xiaobai",     // 主组 = 域名
  role : "user",        // 业务角色名
  home : "/home/xiaobai",
  open : "wn.console",  // 登录后打开的应用
  // OAuth / 微信绑定
  oauth_github : "xxx",
  wx_gh_site0  : "OpenId",
  wx_unionid   : "unionid",
  // 时间戳
  ct : AMS, lm : AMS, login : AMS
}
```

登录名、手机、邮箱三个键都唯一——找回账户用哪一个都行。

## 密码怎么存

不存明文，也不存可逆加密。密码的存储形式是：

```java
// util/Wn.java
passwd_stored = sha1( rawPassword + salt )
```

`salt` 是每个用户独立的随机串。验证登录时，用同样的公式重算一遍再比对——
就算两张表里的密码相同，散列值也因盐而异；系统里任何时刻都不出现明文密码。

顺带一提：用户对象被输出（`me`、API 返回）时，`passwd` / `salt` 字段只会输出
`true` 作为占位标记，真实值永不外泄（`WnSimpleUser` 的序列化规则）。

## 用户组与角色

注册即建同名主组（`grp = 用户名`），用户的主目录就是 `/home/$nm`——
这就是 Domain 与 Home 里域的来历。

组内成员关系存在组对象上，组成员有一个**组角色**数值：

- `-1` — 黑名单（整组数据对这人不可见）
- `0` — 非组员
- `1` — 管理员
- `10` — 组员
- `100` — 预备（待审批）

特殊组：`root` 是系统管理组，`op` 是运维组（部分特权命令要求 op 组管或 root 组成员）。

## 存储后端：Std 与 Sql

用户/角色的存储接口是 `WnUserStore` / `WnRoleStore`，两种实现：

- **Std（默认）**：用户就是 `/sys/usr/` 下的标准对象，角色是组元数据——
  零配置，开箱即用
- **Sql**：站点级配置声明 `sqlFetch` 后，用户/角色改从 SQL 表读写
  （海量用户的场景）

对调用方完全透明，`WnUserStoreMaker` 按配置自动选择。

## 常用命令

```bash
adduser xiaobai -p 123456       # 创建账户（自动建组、建主目录、跑初始化脚本）
usermod xiaobai -E '{"phone":"139.."}'   # 修改元数据
passwd xiaobai                  # 改密码（自动换盐）
whoami / me                     # 当前身份与元数据
grp list                        # 组操作（man grp）
role                            # 角色管理（man role）
```

## 初始化脚本

用户创建后、每次登录后，系统可以自动执行脚本：

```bash
/sys/setup/usr/create/    # 创建后执行（按文件名排序）
/sys/setup/usr/login/     # 每次登录后执行
   abc/                   # init 类型为 abc 的用户专用（user.init 字段）
```

支持命令模板和 `.js`（交给 jsc 执行）。

## 注意事项

- `passwd` / `salt` 永远不出系统；排查登录问题先看「用户名+密码」再怀疑时钟与盐
- 组角色 `100`（预备）的用户能看到组但做不了事，适合「注册待审批」流程

## 扩展阅读

- 会话管理 — 登录成功之后发生什么
- 标准对象的权限体系 — `m` / `pvg` 如何守门
