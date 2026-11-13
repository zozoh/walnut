---
caseID: CASE-201
title: 普通 JSON 接口返回 200 与约定内容
source: ATESET-09 / wn-doc 50.regapi/10-api-object §场景速查（JSON 接口）
contracts:
  - /api/demo/ping
expected: inline
---

# CASE-201 普通 JSON 接口返回 200 与约定内容

## 前置

- 夹具由脚本里的 `atlib.fixture()` 铺：`atest/atest.init` 用
  `@API .regapi/api/ping 'cross/json'` 声明接口，内容是 `echo` 一行 JSON；
  重复执行不重复建对象。
- 接口对外地址 = `<AT_URL>/api/<测试域>/<接口名>`（本任务测试域是 `demo`），
  用标准库 HTTP 直接调，不经命令通道。
- 快捷元数据 `json` 给接口加了 `Content-Type: text/json`（值里带 `json`）。

## 步骤

1. 铺夹具：`atlib.fixture()` —— 把 `atest.init` 里的 `@API` 对象铺进域；
2. 调接口：`atlib.http("/api/demo/ping")`，拿到状态码、响应头与响应体；
3. 从响应体解析出 `ok` 与 `msg`，连同状态码、`Content-Type` 一起 `emit()`。

## 期望

```json
{
  "status": 200,
  "ctype": {"$regex": "json"},
  "ok": true,
  "msg": "pong"
}
```

## 证据

1. `evidence/summary.txt`：判定结果与 `atlib.note()` 记录；
2. `evidence/http/01-GET-….txt`：原始响应（状态行 + 响应头 + body）；
3. `evidence/result.json`：脚本交给 runner 的 actual；
4. `evidence/stdout.txt` / `evidence/stderr.txt`：脚本原始输出（含每次 HTTP 的状态行）。
