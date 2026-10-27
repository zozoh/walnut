---
title: 站点鉴权、权限断言与历史记录
module: 50.regapi
keywords: [http-www-home, http-www-auth, pvg-setup, pvg-assert, preload, history, histest]
source: [ext/sys/httpapi/HttpApiModule.java, ext/data/pvg/]
related: [10-api-object, 40-cache, ../30.auth/20-auth-session.md, ../30.auth/0-auth-pvg.md]
---

# 站点鉴权、权限断言与历史记录概述

一个受保护的接口，在处理链上要回答三个问题：

- **你是谁？** —— 站点会话注入（`http-www-*`）
- **你能做吗？** —— 权限断言（`pvg-*`）
- **你做了什么？** —— 历史记录（`history`）

三个都是 API 级机制：不写代码，在 API 对象上声明元数据即生效。

## 站点会话注入（http-www-home）

API 声明自己属于某个站点后，系统会按请求带的票据恢复站点会话，并把用户信息注入请求元数据：

```bash
o /home/demo/api/order/list -e '
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

> 站点登录本身由 webx 命令完成（见 过滤命令）；这里只负责「拿着票据认出用户」。

## 权限断言（pvg-setup + pvg-assert）

在站点会话的基础上做动作级权限检查，底层是 业务权限 的 `BizPvgService`：

```bash
o /home/demo/api/order/del -e '
  http-www-home:/home/demo/website;
  pvg-assert:["order:del|order:admin"];
  http-output:json'
```

- `pvg-assert`：字符串数组，每项是一组 `动作|动作`（`|` 表“或”），多项之间是“与”；当前用户的站点角色任意命中一组才放行，否则 `e.api.forbid`；
- `pvg-setup`：权限表对象路径；不设置时回落到站点 auth 选项里的 `pvg_setup`（站点目录对象上的 SnakeCase 键）。权限表格式见 业务权限。

> ⚠️ **当前源码疑似有守卫条件写反**：`__do_www_check_pvg` 里的
> `if (!apc.isNeedWWWAuth || null != apc.authOptions) return;` 意味着
> 正常配置了站点（`authOptions` 非空）时断言被直接跳过——`pvg-assert`
> 目前实际不生效。修复源码前，不要依赖它做权限控制。

## preload：执行前预加载

`preload` 可以是单个对象或数组；逐项用 `test` 匹配请求，命中的第一项生效（先中先得）：

```json5
{
  preload : [{
    test    : { "http-qs-tp" : "hot" },         // 匹配条件（AutoMatch 语法）
    appends : { "http-qs-limit" : "50" },       // 先注入/覆盖请求元数据
    run     : { "hotCfg" : "o race:CONFIG id:hotCfg -l | jsonx @get" }
  },{
    run : { "dftCfg" : "o race:CONFIG id:dft -l | jsonx @get" }
  }]
}
```

- `test`：匹配请求元数据；缺省总匹配；
- `appends`：NutMap，先做 `Wn.explainObj` 模板渲染后并入请求元数据
  （占位符可用 `${http-qs-*}`、`${params.*}`、`${http-www-me-*}` 等），
  `run` 的命令模板里可以直接引用；
- `run`：**「结果键 → 命令模板」的映射**，不是查询条件。每个条目的命令模板
  用请求元数据渲染后以域账号身份执行，stdout 的结果存入请求元数据的对应键：

  - 键 `...` —— 结果（须为 JSON 对象）整个并入请求元数据；
  - 键 `xxx` —— 结果按 JSON 解析后存为 `xxx`；
  - 键 `!xxx` —— 纯文本约定：结果**不做 JSON 解析**，原样以字符串存入 `xxx`；
  - 键 `!...` —— 跳过不执行。

  命令执行异常不会中断请求，只打警告日志。全部结束后请求元数据会记入
  `api-preloaded:true/false` 标志。

## history / histest / hisname / hismetas

```json5
{
  history : [ { "race":"ORDER_HIS", "oid":"${params.oid}", "uid":"${http-www-me-id}" } ],
  histest : { "http-method" : "^POST$" },   // 记录历史前的请求条件
  hisname : "order"                          // 历史配置名，默认 _history
}
```

> ⚠️ **当前状态：写库逻辑未启用。** 源码 `_record_history` 中，读取
> `~/.domain/history/<hisname>.json` 配置之后，实际插入历史记录的代码全部被注释
> （`TODO 这里需要将历史记录插入到指定的数据表里`）。也就是说 `history`/`hismetas`
> 相关元数据目前**不会真正写历史表**，配置 `~/.domain/history/*.json` 缺失时只会打警告。
> 接入前请确认这部分代码是否已恢复。

## 动态校验语法（AutoMatch）

`cache-test.match`、`pvg-assert`、`preload.test`、`histest` 共用 Nutz `AutoMatch` 语法：

- `abc` — 等值
- `^正则` / `!^正则` — 正则匹配 / 取反
- `*` — 通配
- `~~abc` — 忽略大小写
- `1..100` — 数值区间
- `null` — 键不存在或值为 null
- `""` / `[BLANK]` — 空串
- `true` / `false` — 布尔

数组 = AND（全都要满足）；对象多键 = AND；想表达 OR 用 `pvg-assert` 的
`a|b` 或拆多条 `cache-test`。

## 执行顺序

站点会话注入、权限断言、preload 都是请求处理链上的一环，
整条链路的先后关系见 README 的「一次请求的完整旅程」。

# 延伸阅读

- API 对象 — 元数据全集
- 会话 / 对象的权限体系
- 缓存 — 同样的匹配语法
