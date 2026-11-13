---
caseID: CASE-204
title: 接口写对象后副作用可在对象树里读到
source: ATESET-09 / wn-doc 50.regapi/10-api-object §场景速查（命令模板）
contracts:
  - /api/demo/write
expected: inline
---

# CASE-204 接口写对象后副作用可在对象树里读到

## 前置

- 夹具声明 `.regapi/api/write`，内容是一条
  `o @create -p ~/atest-lab -upsert '{"nm":"api-side-effect.txt","content":"from-api"}'`：
  调一次接口就在 `~/atest-lab/api-side-effect.txt` 造一个文件对象。
- `{"nm":…, "content":"from-api"}` 里的 `content` 是**元数据**、不是文件正文：
  读回用对象快照看元数据，不去 `cat` 文件内容。
- `-upsert` 让同一接口连跑多次不撞"已存在"，用例可重复执行。
- 副作用对象由**接口自己**创建（不是脚本先造好再去调接口）。

## 步骤

1. 铺夹具：`atlib.fixture()` —— `~/atest-lab/` 由模板建好；
2. 调接口：`atlib.http("/api/demo/write")`，拿到状态码；
3. 读副作用对象：`atlib.snap("~/atest-lab/api-side-effect.txt")`，
   快照落 `evidence/objects/`；
4. 把状态码、`nm` 与 `content` 元数据一起 `emit()`。

## 期望

```json
{
  "status": 200,
  "nm": "api-side-effect.txt",
  "content": "from-api"
}
```

## 证据

1. `evidence/summary.txt`：判定结果与副作用对象记录；
2. `evidence/objects/~__atest-lab__api-side-effect.txt.json`：副作用对象快照；
3. `evidence/http/*.txt`：接口的原始响应（含接口回传的对象 JSON）；
4. `evidence/result.json` / `stdout.txt` / `stderr.txt`：脚本结果与原始输出。
