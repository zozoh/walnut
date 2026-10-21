---
title: 系统账户与域账户
module: login
keywords: [账户, 用户组, 角色, 域账户库, usr, role, grp]
source:
  - login/usr/
  - login/role/
  - login/WnLoginApi.java
related: [session, privilege, domain-and-home, auto-login]
---

# 系统账户与域账户

Walnut 有两套账户体系，服务两类场景：

| | 系统账户 | 域账户（站点用户库） |
|---|---------|---------------------|
| 存哪 | `/sys/usr/`（每用户一文件） | `~/accounts/`（域内 ThingSet 或映射表） |
| 谁管理 | root / 系统服务 | 域主（站点运营者） |
| 登录到什么 | Walnut 系统（域 Shell / 管理界面） | 域自己的站点/App |
| 能做什么 | 操作自己的域和系统资源 | 只能访问该站点的业务接口 |

**桥接**：域账户可凭站点票据创建系统会话（混合登录），登录后进入域主目录——这就是「用域账户使用 Walnut」的路径（`/a/auth_login_by_domain_*`）。

## 系统账户

### 用户元数据（`/sys/usr/$nm`）

```js
{
  id : "u6..8r",        // 唯一 ID
  nm : "xiaobai",       // 登录名（唯一）
  phone : "139..",      // 唯一，可登录
  email : "x@y.z",      // 唯一，可登录
  passwd : "加盐SHA",   // 密码
  salt   : "随机盐",
  grp  : "xiaobai",     // 主组 = 域
  role : "user",        // 业务角色名
  home : "/home/xiaobai",
  open : "wn.console",  // 登录后打开的应用
  // OAuth / 微信
  oauth_github : "xxx",
  wx_gh_site0  : "OpenId",
  wx_unionid   : "unionid",
  // 时间戳
  ct : AMS, lm : AMS, login : AMS
}
```

### 用户组与角色（`/sys/grp/` 与 `/sys/role/`）

- 注册即建同名主组；组内成员关系存 `ID{uid,grp,role}` 元数据。
- 组角色值：`-1` 黑名单 / `0` 非组员 / `1` 管理员 / `10` 组员 / `100` 预备（待审批）。
- `root` 组是系统管理组，`op` 组是运维组（部分特权命令要求 op 组管或 root 组成员）。

```bash
adduser xiaobai -p 123456       # 创建账户（自动建组、建主目录、跑初始化脚本）
usermod xiaobai -E '{"phone":"139.."}'   # 修改元数据
passwd xiaobai                  # 改密码
grp list                        # 组操作（man grp）
role                            # 角色操作（man role）
```

### 初始化脚本

```
/sys/setup/usr/create/    # 用户创建后执行（按文件名排序）
/sys/setup/usr/login/    # 每次登录后执行
   abc/                  # init 类型为 abc 的用户专用（user.init 字段）
```

支持命令模板和 `.js`（交给 jsc）。

## 域账户

站点目录（`~/www` 或任意标记 `accounts` 元数据的目录）关联账户库：

```js
// 站点目录元数据
{
  accounts : "~/accounts",     // 账户库（用户数据集）
  roles    : "~/roles"         // 业务角色库（可选）
}
```

域账户特征：
- 登录名/手机/邮箱唯一；密码加盐；业务角色是**字符串名**（admin/member/...），不是数字。
- 特殊角色 `others`：登录后固定加载，用 `@others` 给对象设默认权限。
- 支持组织结构：`organization.json`（G 组/P 职位树），用户 `job/dept` 字段，`belong` + `pvg:{"+节点": 0755}` 按组织授权。

域登录 API 的构建：`WnLoginApiMaker.forHydrate().make(io, env, options)` 按站点目录的 `accounts/roles` 元数据动态组装（Std 本地实现或 Sql 实现）。

## 命令速查

| 命令 | 作用 |
|------|------|
| `adduser` / `usermod` / `passwd` | 系统账户管理 |
| `whoami` / `me` | 当前身份与元数据 |
| `grp` / `role` | 组与角色管理 |
| `login <user>` | 切换身份（需特权） |
| `vcode` / `captcha` | 验证码体系（见 [vcode-captcha](vcode-captcha.md)） |

## 注意事项

- 登录名/手机/邮箱三个键都唯一——找回账户可用任一。
- 域账户体系由站点目录元数据驱动，**不用的站点不用配** `accounts`。
- 弃用的 Thing 概念曾承载域账户库存储；新实现请用通用对象或 SQL 映射表承载账户数据。
