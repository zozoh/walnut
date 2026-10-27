---
title: 域内用户存储与认证
module: login.site
keywords: [域用户, 站点用户, accounts, roles, 用户库, 业务角色, for_domain, WnLoginSite]
source:
  - login/site/
  - login/WnLoginApiMaker.java
  - login/usr/WnUserStoreMaker.java
  - login/role/WnRoleStoreMaker.java
related: [README, 30-auth-in-domain, 40-auth-hydrate, 10-auth-user-role]
---

# 域内用户存储与认证概述

系统用户是 Walnut 的居民；但你的站点/App 有自己的用户——注册用户、会员、客户。
这批**域内用户**与系统用户是两套体系：他们不需要 Walnut 账户，
也碰不到 Walnut 的控制台，只在你站点的业务接口里活动。

## 站点与用户库

一个「站点」就是一份登录配置：可以是一个 JSON 文件，也可以是一个目录的元数据。
核心是两个键，指向站点的用户库与角色库：

```js
// 站点目录（如 ~/www）的元数据
{
  accounts : "~/accounts",     // 用户库位置（标准对象目录或 SQL 映射表）
  roles    : "~/roles"         // 业务角色库（可选）
}
```

- 是 JSON 文件：`io.readJson` 读内容作为 `WnLoginOptions`
- 是目录：元数据直接映射为 `WnLoginOptions`

`WnLoginSite`（`login/site/`）负责解析站点配置，组装出该站点的 `WnLoginApi`。

## 登录接口的组装：`for_domain`

站点认证用 `WnLoginApiMaker.forDomain()` 组装：

```java
WnLoginApi auth = WnLoginApiMaker.forDomain()
    .make(io, sessionVars, options);   // options 来自站点配置
```

组装逻辑（`WnUserStoreMaker` / `WnRoleStoreMaker` / `WnSessionStoreMaker`）：

- 配置里声明了 `sqlFetch` → 用户/角色走 **SQL 存储**（`WnSqlUserStore`，海量用户场景）
- 否则走 **Std 存储**：用户就是用户库目录下的标准对象（密码盐值规则与系统用户相同：
  `sha1(密码 + 盐)`）

也就是说：**域内用户同样是对象**，只是不住在 `/sys/usr/`，而住在站点自己的目录里。

## 域内用户的特征

与系统用户的关键差异：

- **业务角色是字符串名**（`admin` / `member` / `operator`...），不是系统组的数字角色
- 特殊角色 `others`：登录后固定加载，常用 `@others` 给对象设「默认权限」
- 支持组织结构：`organization.json`（G 组 / P 职位树），用户带 `job` / `dept` 字段，
  配合 `belong` + `pvg:{"+节点": 0755}` 按组织节点授权
- 登录名 / 手机 / 邮箱唯一，同样可用手机或邮箱登录

## 与系统用户的关系

域内用户登录到的是**站点**，不是 Walnut 系统：会话独立、权限边界在站点目录内。
如果希望域内用户登录后能像系统用户一样进入域主目录操作——那就是
混合认证机制 要解决的问题。

## 注意事项

- 站点配置由域主（站点运营者）维护，系统管理员不需要介入
- 不用的站点不用配 `accounts`——没有用户库元数据的目录不会被当作站点
- 海量用户（百万级）请把用户库配置成 SQL 映射表，对象目录承载不了那个量级

## 扩展阅读

- 混合认证机制 — 域内用户如何获得系统会话
- 系统用户、角色与密码存储 — 系统账户体系
- 三层权限体系 — `@角色` 授权语法
