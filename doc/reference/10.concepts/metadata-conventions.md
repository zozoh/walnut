---
title: 元数据约定速查
module: core
keywords: [元数据, mnt, expi, pvg, thumb, lbls, d0, d1, 约定]
source:
  - api/io/WnObj.java
  - core/bean/WnIoObj.java
related: [everything-is-obj, domain-and-home, mount-mapping, privilege]
---

# 元数据约定速查

Walnut 的模块间协作靠**元数据约定**：看懂这些键，就能看懂任何域的配置。按用途分组速查（`*` 表示前缀族）。

## 存储映射

- **`mnt`** — 值示例：`dao(demo/t_news)://lbm(A)` — 说明：把子树映射到其他索引器/桶 — 详见：[mount-mapping](../20.io-storage/mount-mapping.md)
- **`data`** — 值示例：`1f76...` — 说明：对象在桶管理器内的数据段 ID — 详见：[io-model](../20.io-storage/io-model.md)
- **`expi`** — 值示例：`1720000000000` — 说明：过期绝对毫秒，到期自动删除 — 详见：[expiration](../20.io-storage/expiration.md)

## 权限与安全

- **`mode`(`m`)** — 值示例：`448` — 说明：三段八进制权限（admin/member/others）
- **`pvg`** — 值示例：`{xiaobai:5,@op:7}` — 说明：扩展 ACL，可指定用户或 `@角色`
- **`api-access-token`** — 值示例：`http-qs-at=~/.domain/token` — 说明：regapi 访问密钥校验规则

## 展示与分类

| 键 | 值示例 | 说明 |
|----|--------|------|
| `tp` | `jpg` / `wnml` | 类型后缀，决定 MIME 与默认缩略图 |
| `mime` | `image/jpeg` | 内容类型 |
| `thumb` | `id:5k..5k` | 缩略图对象引用 |
| `lbls` | `["bug","A"]` | 自由标签数组，可查询 |
| `d0` / `d1` | `home` / `demo` | 一/二级目录名，加速范围查询 |
| `title` / `brief` | — | 显示名与摘要（业务通用） |

## HTTP API（regapi）专用 `*`

- **`http-header-*`** — 说明：固定写入响应头 — 详见：[api-object](../50.regapi/api-object.md)
- **`http-qs-*` / `http-cookie-*` / `http-header-*`（请求对象上）** — 说明：请求参数注入 — 详见：[request-object](../50.regapi/request-object.md)
- **`http-resp-code` / `http-dynamic-header` / `http-cross-origin`** — 说明：响应行为控制 — 详见：[api-object](../50.regapi/api-object.md)
- **`cache-test` / `cache-duration` / `cache-finger-key` / `cache-redirect`** — 说明：缓存策略 — 详见：[cache](../50.regapi/cache.md)
- **`preload` / `history` / `pvg-setup` / `pvg-assert`** — 说明：预加载/埋点/权限 — 详见：[auth-history](../50.regapi/auth-history.md)

## 任务与调度

- **`tp: "cron_task"`**：定期任务对象（内容=命令模板）
- **`tp: "schedule_task"`**：分钟计划任务（cron 展开产物）
- **`tp: "task"`**：后台任务堆栈项
- **`cron`**：WnCron 表达式

## 典型对象长什么样

```js
// 一个映射到 SQL 的业务对象
{
  id: "4k2...", pid: "8m1...", nm: "t_news", race: "DIR",
  c: "demo", g: "demo", m: 448,
  mnt: "dao(demo/t_news)://lbm(news-data)",
  ct: 1720000000000, lm: 1720000000000,
  // 以下为业务自定义元数据
  title: "新闻表", category: "news"
}
```

## 使用建议

- 业务字段直接用**有意义的英文名**放对象上，不要另建元数据包一层。
- 命名避免与上表系统键冲突；系统键全小写，业务键可以用驼峰区分。
- 查询高频字段记得利用 `lbls`/`d0`/`d1` 这些已索引的结构键。
