---
caseID: CASE-102
title: 写入文本后 cat 读回内容完全一致
source: ATESET-06 / man cmd_echo、cmd_cat
contracts:
  - cmd_echo -n 重定向写文本
  - cmd_cat ~/atest-lab/docs/case102-note.txt
expected: inline
---

# CASE-102 写入文本后 cat 读回内容完全一致

## 前置

- 夹具由 `atlib.fixture()` 铺好 `~/atest-lab/docs/`；本例只写一个文件
  `~/atest-lab/docs/case102-note.txt`，内容固定，重复执行覆盖同一路径。
- `echo -n` 不追加换行；`cat` 的输出可能带行尾换行，脚本取回后 `strip()` 再交给 runner。
- 期望按字符串完全相等比较，不做包含/前缀比较。

## 步骤

1. 铺夹具：`atlib.fixture()`；
2. 写内容：在命令通道执行 `echo -n 'hello from atest case102' > ~/atest-lab/docs/case102-note.txt`；
3. 读回：执行 `cat ~/atest-lab/docs/case102-note.txt`，去掉首尾空白后作为 actual。

## 期望

文件内容字符串完全相等：

```json
{
  "content": "hello from atest case102"
}
```

## 证据

1. `evidence/summary.txt`：判定结果与 `atlib.note()` 记录；
2. `evidence/objects/~__atest-lab__docs__case102-note.txt.json`：写入后的对象快照（含 `len`）；
3. `evidence/stdout.txt` / `evidence/stderr.txt`：写与读两条命令的原始输出；
4. `evidence/result.json`：脚本交给 runner 的 actual（`content` 已 trim）。
