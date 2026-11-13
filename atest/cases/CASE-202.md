---
caseID: CASE-202
title: 令牌接口不带令牌 403、带令牌 200
source: ATESET-09 / wn-doc 50.regapi/10-api-object §api-access-token
contracts:
  - /api/demo/secret
  - /api/demo/secret?at=s3cr3t-atest
expected: inline
---

# CASE-202 令牌接口不带令牌 403、带令牌 200

## 前置

- 夹具声明 `.regapi/api/secret`：元数据 `api-access-token: "http-qs-at"`。
  值是**请求元数据键**（不是裸参数名）：源码 `ApiMetaFiller.checkApiAccessToken`
  直接拿它去查请求元数据，写 `"at"` 会永远取不到值而恒 403
  （wn-doc 的 `"at"` 示例与源码不符，以源码为准）。
- 令牌文件走默认位置 `~/.domain/api_access_token`，内容 `s3cr3t-atest`。
- 源码口径：客户端没带、带错、令牌文件不存在，一律 403。
- 403 是**预期响应**，不是环境故障：脚本照常 `emit()`；连不上才是
  `SystemExit(2)`（runner 记 blocked）。

## 步骤

1. 铺夹具：`atlib.fixture()` —— 建接口对象与令牌文件；
2. 裸调：`atlib.http("/api/demo/secret")`，取回状态码；
3. 带令牌调：`atlib.http("/api/demo/secret?at=s3cr3t-atest")`，取回状态码；
4. 把两次状态码 `emit()` 出去。

## 期望

```json
{
  "without_token": 403,
  "with_token": 200
}
```

## 证据

1. `evidence/summary.txt`：判定结果与两次请求的状态记录；
2. `evidence/http/*.txt`：两次原始响应（含 403 的响应头；
   Jetty 的错误页 body 不稳定，不写进期望）；
3. `evidence/result.json`：脚本交给 runner 的 actual；
4. `evidence/stdout.txt` / `evidence/stderr.txt`：脚本原始输出。
