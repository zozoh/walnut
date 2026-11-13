---
caseID: CASE-104
title: 改元数据后按路径读回新值
source: ATESET-06 / man cmd_o §@update
contracts:
  - cmd_o @update
  - cmd_o @json
expected: baseline
---

# CASE-104 改元数据后按路径读回新值

## 前置

- 夹具由 `atlib.fixture()` 铺好 `~/atest-lab/hello.txt`；本例只改它的元数据
  （`owner` / `seq`），不改内容。
- `@update` 每次执行都会刷新 `lm`；它是易变字段，基线只锁 `nm` / `owner` / `seq`
  （`lm` 的值看证据快照，不写进期望）。
- 重复执行写的是同一组元数据，结果一致；无独立清理脚本。

## 步骤

1. 铺夹具：`atlib.fixture()` —— `hello.txt` 已存在时跳过内容写入，元数据照常合并；
2. 改元数据：在命令通道执行 `o ~/atest-lab/hello.txt @update '{"owner":"walnut","seq":7}'`；
3. 读回：执行 `o ~/atest-lab/hello.txt @json -cqn`，对象快照落 `evidence/objects/`。

## 期望

只比基线 `atest/baseline/CASE-104.json` 的 `expected` 里出现的键：

- `nm` = `hello.txt`；
- `owner` = `walnut`；
- `seq` = `7`。

`lm` 是 `@update` 刷新的时间戳，属易变字段，**不进基线**（写死就没法在别的实例上过，
基线会被录成垃圾）；它仍在证据的 `objects/` 快照与 `clock.txt` 里，需要时人工核对。

## 证据

1. `evidence/summary.txt`：判定结果、时间区间与 `atlib.note()` 记录；
2. `evidence/objects/__atest-lab__hello.txt.json`：改完元数据后的对象快照；
3. `evidence/stdout.txt` / `evidence/stderr.txt`：update 与读回的原始输出；
4. `evidence/result.json`：脚本交给 runner 的 actual。
