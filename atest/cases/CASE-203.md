---
caseID: CASE-203
title: 声明 http-resp-code 302 的接口跳转到指定 URL
source: ATESET-09 / wn-doc 50.regapi/10-api-object §http-resp-code
contracts:
  - /api/demo/go
expected: inline
---

# CASE-203 声明 http-resp-code 302 的接口跳转到指定 URL

## 前置

- 夹具声明 `.regapi/api/go 'http302'`（快捷元数据 = `http-resp-code: 302`），
  内容是 `echo -n 'http://example.com/atest'`；302 模式下命令 stdout 就是跳转目标 URL。
- 用 `echo -n`（不带行尾换行）：带换行时服务端 `sendRedirect` 会把换行变成
  `Location` 的**尾随空格**，那样"跳转到指定 URL"就没被真正钉住。
- `atlib.http()` **不跟随重定向**：urllib 默认会跟到目标站点，那样测到的就不是
  这个接口的跳转行为了。

## 步骤

1. 铺夹具：`atlib.fixture()` —— 建 `http-resp-code:302` 的接口对象；
2. 调接口：`atlib.http("/api/demo/go")`，拿到 302 与 `Location`；
3. 把状态码与 `Location` 一起 `emit()`。

## 期望

```json
{
  "status": 302,
  "location": "http://example.com/atest"
}
```

## 证据

1. `evidence/summary.txt`：判定结果与 `Location` 记录；
2. `evidence/http/*.txt`：原始响应（302 状态行 + `Location` 头，body 为空）；
3. `evidence/result.json`：脚本交给 runner 的 actual；
4. `evidence/stdout.txt` / `evidence/stderr.txt`：脚本原始输出。
