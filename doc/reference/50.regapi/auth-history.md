---
title: 站点鉴权、权限断言与历史记录
module: 50.regapi
keywords: [http-www-home, http-www-auth, pvg-setup, pvg-assert, preload, history, histest]
source: [ext/sys/httpapi/HttpApiModule.java, ext/data/pvg/]
related: [api-object, cache, ../30.auth/session.md, ../30.auth/biz-privilege.md]
---

# 站点鉴权、权限断言与历史记录

三个 API 级的高级机制：**站点会话注入**（谁）、**权限断言**（能干什么）、**preload / history**（执行前后干预）。

## 站点会话注入（http-www-home）

API 声明自己属于某个站点后，系统会按请求带的票据恢复站点会话，并把用户信息注入请求元数据：

```bash
o /home/demo/api/order/list -e '
  http-api:true;
  http-www-home:/home/demo/website;
  http-www-ticket:http-qs-ticket;   # 从哪个参数读票据，默认 http-qs-ticket
  http-www-auth:true;                # 无会话即拒绝（默认随 http-www-home 一起生效）
  http-output:json'
```

- `http-www-home`：站点目录路径（值支持模板占位）；也可以是 JSON 文件的 auth 选项（文件模式）；
- `http-www-auth`：为 true 时无有效会话抛 `e.api.forbid`；
- 会话票据从 `http-www-ticket` 指定的参数读取（默认 `http-qs-ticket`）。

注入的元数据（命令/JS 中直接可用）：

- **`http-www-se-ticket`**：站点会话票据
- **`http-www-me-id` / `http-www-me-nm`**：用户 ID / 登录名
- **`http-www-me`**：用户完整元数据 JSON
- **`http-www-me-phone` / `http-www-me-email` / `http-www-me-mainGroup`**：手机 / 邮箱 / 主域

> 站点登录本身由 webx 命令完成（见 [过滤命令](../40.commands/03_cmd_filter.md)）；这里只负责「拿着票据认出用户」。

## 权限断言（pvg-setup + pvg-assert）

在站点会话的基础上做动作级权限检查，底层是 [业务权限](../30.auth/biz-privilege.md) 的 `BizPvgService`：

```bash
o /home/demo/api/order/del -e '
  http-api:true;
  http-www-home:/home/demo/website;
  pvg-assert:["order:del|order:admin"];
  http-output:json'
```

- `pvg-assert`：字符串数组，每项是一组 `动作|动作`（`|` 表“或”），多项之间是“与”；当前用户的站点角色任意命中一组才放行，否则 `e.api.forbid`；
- `pvg-setup`：权限表对象路径；不设置时回落到站点 auth 选项里的 `pvg_setup`（站点目录对象上的 SnakeCase 键）。权限表格式见 [业务权限](../30.auth/biz-privilege.md)。

## preload：执行前预加载

`preload` 可以是单个对象或数组；逐项用 `test` 匹配请求，命中的第一项生效（先中先得）：

```js
{
  preload : [{
    test    : { "http-qs-tp" : "hot" },        // 匹配条件（AutoMatch 语法）
    appends : { "http-qs-limit" : "50" },      // 先注入/覆盖请求元数据
    run     : { "race" : "CONFIG", "id" : "hotCfg" }  // 再执行查询，结果并入上下文
  },{
    run : { "race" : "CONFIG", "id" : "dft" }
  }]
}
```

- `test`：匹配请求元数据；缺省总匹配；
- `appends`：NutMap，先做 `Wn.explainObj` 模板渲染后并入请求元数据（占位符可用 `${http-qs-*}`、`${params.*}`、`${http-www-me-*}` 等）；
- `run`：一个 WnQuery 条件，执行结果并入上下文。

**键前缀 `!` 纯文本约定**：在 `appends`/`run` 的值里，以 `!` 开头的字符串表示“不渲染模板、直接取原文”（渲染管线约定，源码注释可见）。

## history / histest / hisname / hismetas

```js
{
  history : [ { "race":"ORDER_HIS", "oid":"${params.oid}", "uid":"${http-www-me-id}" } ],
  histest : { "http-method" : "^POST$" },   // 记录历史前的请求条件
  hisname : "order"                          // 历史配置名，默认 _history
}
```

> ⚠️ **当前状态：写库逻辑未启用。** 源码 `_record_history` 中，读取 `~/.domain/history/<hisname>.json` 配置之后，实际插入历史记录的代码全部被注释（`TODO 这里需要将历史记录插入到指定的数据表里`）。也就是说 `history`/`hismetas` 相关元数据目前**不会真正写历史表**，配置 `~/.domain/history/*.json` 缺失时只会打警告。接入前请确认这部分代码是否已恢复。

## 动态校验语法（AutoMatch）

`cache-test.match`、`pvg-assert`、`preload.test`、`histest` 共用 Nutz `AutoMatch` 语法：

| 写法 | 含义 |
|---|---|
| `abc` | 等值 |
| `^正则` / `!^正则` | 正则匹配 / 取反 |
| `*` | 通配 |
| `~~abc` | 忽略大小写 |
| `1..100` | 数值区间 |
| `null` | 键不存在或值为 null |
| `""` / `[BLANK]` | 空串 |
| `true` / `false` | 布尔 |

数组 = AND（全都要满足）；对象多键 = AND；想表达 OR 用 `pvg-assert` 的 `a|b` 或拆多条 `cache-test`。

## 执行顺序备忘

```text
1. 定位 API 对象（_ANY 通配）
2. api-access-token 校验
3. http-www-* 站点会话注入 → 无会话且 http-www-auth → 403
4. pvg-setup/pvg-assert 角色断言 → 403
5. cache-test 缓存命中 → 直接返回
6. preload
7. 执行命令 / JS
8. http-output 组装响应
9. _record_history（当前为 TODO）
```

## 延伸阅读

- [API 对象](api-object.md) — 元数据全集
- [会话](../30.auth/session.md) / [业务权限](../30.auth/biz-privilege.md)
- [缓存](cache.md) — 同样的匹配语法
