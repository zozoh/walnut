---
caseID: CASE-103
title: 按元数据条件查询并按名字排序
source: ATESET-06 / man cmd_o §@query
contracts:
  - cmd_o @create
  - cmd_o @query
  - cmd_o @json
expected: inline
---

# CASE-103 按元数据条件查询并按名字排序

## 前置

- 夹具由 `atlib.fixture()` 铺好 `~/atest-lab/docs/`；本例在该目录下建 4 个文件：
  3 个带 `tag: q103`、1 个带 `tag: other` 作干扰项。
- 建对象统一带 `-upsert`，重复执行不会多出对象。
- 查询固定 `-sort '{nm:1}'`，不依赖后端默认顺序；`-l` 让结果始终是数组。

## 步骤

1. 铺夹具：`atlib.fixture()`；
2. 建 3 个命中对象：`o @create -p ~/atest-lab/docs -upsert '{"nm":"c103-a","tag":"q103"}'`，
   `c103-b` / `c103-c` 同形；
3. 建 1 个干扰对象：`o @create -p ~/atest-lab/docs -upsert '{"nm":"c103-x","tag":"other"}'`；
4. 查询：执行 `o ~/atest-lab/docs @query '{"tag":"q103"}' -sort '{nm:1}' -l @json -cqn`；
5. 取结果的 `nm` 列表作为 actual。

## 期望

命中集合的名字有序列表（干扰对象 `c103-x` 不出现）：

```json
{
  "nm": ["c103-a", "c103-b", "c103-c"]
}
```

## 证据

1. `evidence/summary.txt`：判定结果与 `atlib.note()` 记录；
2. `evidence/objects/~__atest-lab__docs__c103-b.json`：命中对象之一的快照；
3. `evidence/stdout.txt` / `evidence/stderr.txt`：4 条建对象命令与查询命令的原始输出；
4. `evidence/result.json`：脚本交给 runner 的 actual（`nm` 列表）。
