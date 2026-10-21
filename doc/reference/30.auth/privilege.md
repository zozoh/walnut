---
title: 三层权限模型
module: api.io
keywords: [权限, mode, pvg, 组角色, admin, member, others, WnSecurity]
source:
  - api/io/WnSecurity.java
  - impl/io/WnSecurityImpl.java
  - core/bean/WnIoObj.java
related: [everything-is-obj, accounts, biz-privilege]
---

# 三层权限模型

数据权限分三层，从底到上：

```bash
第 1 层  mode    ：对象自带的三段八进制（admin/member/others）
第 2 层  pvg     ：元数据扩展 ACL（精确到用户/角色）
第 3 层  业务权限 ：角色×动作 JSON 表，被动查询（见 biz-privilege.md）
```

前两层由 Io 层**强制**（每次读写都校验）；第三层是服务/命令主动询问。

## 第 1 层：mode

```
  admin    member   others
[r][w][x] [r][w][x] [r][w][x]
```

- 第一段是 **admin**（属组管理员），不是 Unix 的 owner——对象由全组协作维护。
- `r` 读不到 = 对象对你**不可见**（ls 都看不到）。
- `w` 含元数据修改。想让对象「只能改元数据不能改内容」？建一个可写的 LINK 指向它。
- `x` 对文件=可执行，对目录=可进入。

```bash
chmod 755 ~/docs            # rwxr-xr-x
chmod -R 700 ~/secret       # 递归
chown xiaobai:demo ~/a.txt  # 改创建者/属组
```

**判定顺序**：先看你是不是 admin（属组管理员）→ 再看你是不是 member（组员）→ 否则 others。

## 第 2 层：pvg 元数据

```js
{
  pvg: {
    "xiaobai" : 5,      // 指定用户
    "@operator": 7      // @角色（业务角色名）
  }
}
```

- 数字是八进制权限位（5=r-x, 7=rwx）。
- **向上查祖先**：对象自己没有就问父目录，父有就按父的。
- 常见用法：给协作目录开放给特定伙伴：`o ~/shared -u 'pvg:{"partner_id":7}'`。

## 判定流程（简化）

```bash
请求读对象 o（身份 user）
  → user 是 root？→ 直接放行
  → o.pvg 或其祖先 pvg 命中 user/@角色？→ 按 pvg 位判定
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

- 「看不见」是 Walnut 的默认隐藏策略——`r` 是最低可见门槛。
- `pvg` 优先于 `mode`，但 root 永远凌驾。
- 组角色（-1 黑名单）生效时，整组数据对黑名单用户不可见。
- 业务级「能不能做某动作」的判定属于第三层，见 [biz-privilege](biz-privilege.md)。
