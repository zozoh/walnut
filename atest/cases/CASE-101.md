---
caseID: CASE-101
title: 在 atest-lab 下建目录后按路径读回
source: ATESET-06 / man cmd_o §@create
contracts:
  - cmd_o @create
  - cmd_o @json
expected: inline
---

# CASE-101 在 atest-lab 下建目录后按路径读回

## 前置

- 夹具由脚本里的 `atlib.fixture()` 铺：`~/atest-lab/`、`~/atest-lab/docs/`、
  `~/atest-lab/hello.txt`（内容里的 `who` 来自 `atest/vars.json`）；重复执行不覆盖已有内容。
- 本例新增的唯一数据是 `~/atest-lab/case101-dir`（目录），命令带 `-upsert`，可重复执行，无独立清理脚本。
- 读回走 `o <path> @json -cqn`：`-cqn` 不能省，裸 `@json` 是 HOCON 风格（键不加引号），脚本没法 `json.loads`。

## 步骤

1. 铺夹具：`atlib.fixture()` —— 按 `atest/atest.init` 建好 `~/atest-lab/` 与 `docs/`；
2. 建目录：在命令通道执行 `o @create -p ~/atest-lab -race DIR -upsert '{"nm":"case101-dir"}'`
   （级别与会话由 runner 注入；手工复跑时在 wn-cli 前补 `--level L1`）；
3. 读回：执行 `o ~/atest-lab/case101-dir @json -cqn`，对象快照落 `evidence/objects/`。

## 期望

只比下面 json 里出现的键；`pid` 是父目录 id，本批显式声明不比较：

```json
{
  "nm": "case101-dir",
  "race": "DIR",
  "pid": {"$ignore": true}
}
```

## 证据

1. `evidence/summary.txt`：判定结果与 `atlib.note()` 记录；
2. `evidence/objects/__atest-lab__case101-dir.json`：读回的对象快照；
3. `evidence/stdout.txt` / `evidence/stderr.txt`：脚本原始输出（含每一步 `wn` 命令）；
4. `evidence/result.json`：脚本交给 runner 的 actual。
