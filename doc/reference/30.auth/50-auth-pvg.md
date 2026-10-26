---
title: 标准对象的权限体系
module: api.io
keywords: [权限, mode, pvg, 组角色, admin, member, others, 三层权限, 业务权限, 判定流程]
source:
  - api/io/WnSecurity.java
  - impl/io/WnSecurityImpl.java
  - core/bean/WnIoObj.java
  - ext/data/pvg/
related: [README, 10-auth-user-role, ../10.concepts/01-std-obj.md, ../50.regapi/auth-history.md]
---

# 标准对象的权限体系

权限长在对象自己身上（见 一切皆对象 的 `m` 与 `pvg`），
分三层，从底到上：

```bash
第 1 层  mode    ：对象自带的三段八进制（admin/member/others）
第 2 层  pvg     ：元数据扩展 ACL（精确到用户/角色）
第 3 层  业务权限 ：角色×动作矩阵，被动查询
```

前两层由 Io 层**强制**——每一次读写都校验；第三层是服务/命令主动询问。

## 第 1 层：mode

```bash
  admin    member   others
[r][w][x] [r][w][x] [r][w][x]
```

与 Unix 神似但有一个关键差异：**第一段不是 owner 而是 admin**（属组管理员）。
对象没有 owner 概念，只有 `c`（创建者，仅记录）和 `g`（所属组）。
设计动机是强调**组内协作**——一个域的数据由多人共同维护。

- `r` 读不到 = 对象对你**不可见**（`ls` 都看不到）——「看不见」是 Walnut 的默认隐藏策略
- `w` 含元数据修改。想让对象「只能改元数据不能改内容」？建一个可写的 LINK 指向它
- `x` 对文件 = 可执行，对目录 = 可进入

```bash
chmod 755 ~/docs            # rwxr-xr-x
chmod -R 700 ~/secret       # 递归
chown xiaobai:demo ~/a.txt  # 改创建者/属组
```

**判定顺序**：admin（属组管理员）→ member（组员）→ others。
组角色数值的含义见 系统用户、角色与密码存储。

## 第 2 层：pvg 元数据（例外授权）

```js
{
  pvg: {
    "xiaobai" : 5,      // 指定用户，八进制位（5 = r-x）
    "@operator": 7      // @开头 = 业务角色名
  }
}
```

- **向上查祖先**：对象自己没有 `pvg` 就问父目录，父有就按父的
- 常见用法：给协作目录开放给特定伙伴 `o ~/shared -u 'pvg:{"partner_id":7}'`

## 第 3 层：业务权限（角色 × 动作）

「运营能不能审核文章」这类问题，mode/pvg 回答不了——它们守的是数据，
不是动作。业务权限用一张**角色 × 动作**的矩阵回答：

```js
// 存成一个 JSON（推荐 ~/.pvg/我的业务.json）
{
  "guest"   : { "view": true },
  "member"  : { "view": true, "create": true },
  "operator": { "view": true, "create": true, "audit": true },
  "admin"   : { "view": true, "create": true, "audit": true, "export": true }
}
```

- 它是**被动的**：不由 Io 层强制执行，而是脚本/API **主动询问**（`pvg` 命令或服务类）
- 未声明的动作默认**不允许**（缺失 ≠ 允许）

```bash
pvg ~/my-pvg.json can operator audit          # → true
pvg ~/my-pvg.json can -or operator export audit  # 任一
pvg ~/my-pvg.json can operator view export -ajax -cqn  # Ajax 包装（API 场景）
```

regapi 入口校验：`pvg_setup`（权限表位置）+ `pvg-assert`（要求的动作清单，
数组间 AND、元素内 `|` 分隔 OR），不满足直接 403——详见 auth-history。

## 判定流程（简化）

```bash
请求操作对象 o（身份 user）
  → user 是 root？→ 直接放行
  → o.pvg 或其祖先 pvg 命中 user / @角色？→ 按 pvg 位判定
  → 按 o.m 三段：admin? member? others? 取位判定
```

实现入口：`WnSecurity`（接口）/ `WnSecurityImpl`；链接对象每次访问重新校验目标权限（防权限变更滞后）。

## 操作历史（协作审计）

针对组数据的操作可记录在 `/grp/$grp/history`：

```js
{ tp:"uhis", his_usr:"e4ca..", his_app:"console",
  his_cmd:"echo abc > ~/abc.txt", his_pwd:"~" }
```

数量/期限超限自动裁剪。

## 注意事项

- `pvg` 优先于 `mode`，但 root 永远凌驾
- 组角色 `-1`（黑名单）生效时，整组数据对黑名单用户不可见
- 角色名是**字符串**（域账户业务角色），与系统组角色数字（1/10/100）是两套体系，勿混用

## 扩展阅读

- 标准对象 — `m` / `pvg` 字段定义
- 系统用户、角色与密码存储 — 组角色数值语义
