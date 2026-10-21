---
title: 业务权限模型
module: ext.data.pvg
keywords: [业务权限, 角色, 动作, pvg命令, WnPvgMoreService, 权限矩阵]
source:
  - ext/data/pvg/
related: [privilege, auth-history, api-object]
---

# 业务权限模型

基础权限（mode/pvg）守数据安全，但没法表达「运营能不能审核文章」这类业务动作。业务权限模型用一张**角色 × 动作**的矩阵解决：

```bash
 Roles   | view | create | audit | export
---------+------+--------+-------+-------
 guest   |  ✓   |        |       |
 member  |  ✓   |   ✓    |       |
 operator|  ✓   |   ✓    |   ✓   |
 admin   |  ✓   |   ✓    |   ✓   |   ✓
```

**与基础权限的本质区别**：它是**被动的**——不由 Io 层强制执行，而是服务/命令/脚本**主动询问**「这个角色能不能做这个动作」。

## 数据格式

一个 JSON 对象（推荐存 `~/.pvg/我的业务.json`）：

```js
{
  "guest"   : { "view": true },
  "member"  : { "view": true, "create": true },
  "operator": { "view": true, "create": true, "audit": true },
  "admin"   : { "view": true, "create": true, "audit": true, "export": true }
}
```

来源不限：文件内容、对象元数据、API 请求结果均可，只要能还原成这个 Map。

## 命令行用法

```bash
# 能否做某动作（返回 true/false）
pvg ~/my-pvg.json can operator audit
# → true

# 多个动作全部具备（AND）
pvg ~/my-pvg.json can operator view audit
# → true

# 多个动作至少一个（OR）
pvg ~/my-pvg.json can -or operator export audit
# → true

# Ajax 包装（API 场景，输出每个动作的判定明细）
pvg ~/my-pvg.json can operator view export -ajax -cqn
# → {"ok":false,"data":{"view":true,"export":false}}
```

子命令：`can` / `roles` / `actions` / `matrix`（见 `man pvg` 各篇）。

## 服务类（Java / JS）

```java
WnPvgMoreService pvg = ...;
pvg.can("operator", "audit");            // 单个
pvg.can("operator", "view", "audit");      // 全部
pvg.canOr("operator", "export", "audit");  // 任一
```

```js
// JS 里通过 exec 调用
var re = sys.exec("pvg ~/my-pvg.json can " + role + " " + action);
var ok = "true" === re.trim();
```

## 典型集成：regapi 入口校验

API 对象上挂两个元数据（详见 [auth-history](../50.regapi/auth-history.md)）：

```js
// 站点目录或 API 对象上
pvg_setup : "~/.domain/pvg.json"    // 权限表位置（站点用 SnakeCase！）
// API 对象上
pvg-assert: ["view", "create|audit"]  // 数组间 AND，元素内 | 分隔 OR
```

不满足直接 403。

## 应用场景

- HTTP API 的访问控制（如上）
- 管理界面的菜单/侧边栏显隐
- 命令脚本里的动作分支：`pvg can` 失败就退出
- 审批流：角色升级自动获得新动作

## 注意事项

- 角色名是**字符串**（域账户的业务角色），与系统组角色数字（1/10/100）是两套体系，勿混用。
- 矩阵里未声明的动作默认**不允许**（缺失 ≠ 允许）。
- 权限表改动即时生效（每次读取），无需刷新。
