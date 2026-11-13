# at-report — ATEST 报告视图（JSON → Markdown）

把 [`at-run`](../at-run/) 产出的 JSON 报告渲染成**人只看一眼**的 Markdown：
总览、逐例表格、红灯的 diff 与证据、与上一份的趋势、一节留空的「建议」。

一条命令、纯读、可重复：**只读 JSON 报告与证据里的 `summary.txt`，不执行用例、不连实例**。

```text
<root>/report/<timestamp>.json   →   <root>/report/<timestamp>.md + latest.md
```

Markdown 由工具生成，不让模型手写——省 token、可复现、格式稳定。

## 用法

```bash
# 最近一次（<root>/report/latest.json）
python bin/at-report/at-report.py

# 显式写 latest，等价于上一条
python bin/at-report/at-report.py latest

# 指定某一份报告（给复核/复盘用）
python bin/at-report/at-report.py atest/report/20261113T145425.json

# 别的项目 / 别的根目录
python bin/at-report/at-report.py --dir D:/work/myproj --root spec
```

| 参数 | 说明 |
|------|------|
| `REPORT` | JSON 报告路径；`latest`（缺省）读 `<root>/report/latest.json`。相对路径按 `--dir` 解析 |
| `--dir DIR` | 项目目录，默认当前目录 |
| `--root NAME` | 集成测试根目录名；缺省读 `<dir>/atest.json` 的 `root`，再缺省 `atest` |

退出码（沿用 `bin/` 工具语义）：

| 码 | 含义 |
|----|------|
| `0` | 生成成功，且报告**无红**（`blocked` 不算红） |
| `1` | 生成成功但报告**有红**；或用法错误（参数 / `--root` 不合法） |
| `2` | 环境不可用：`atest.json` 或报告 JSON 缺失、JSON 损坏、目录不可写 |

stdout 先打印两行人读摘要，**最后一行是单行 JSON**（机器解析用）：

```json
{"ok":true,"source":"atest/report/latest.json","report":"atest/report/20261113T145550.md","latest":"atest/report/latest.md","timestamp":"20261113T145550","total":4,"passed":4,"failed":0,"blocked":0,"trend":{"previous":"20261113T145446","new_red":[],"fixed":[],"still_red":[],"added":[],"removed":[]},"exit":0}
```

- `report` / `latest` / `source` 都是**相对项目根**的路径，可直接贴进文档；
- `trend.previous` 是上一份的运行 ID（首份为 `null`）；
- 出错时只有一行 JSON：`{"ok": false, "error": "..."}`。

## 报告结构

结构在 ATESET-07 任务书里定稿；改格式先改任务书，再改本工具。

```markdown
# ATEST 报告 · 20261113T145550

- 实例：L1（来源 cli）　测试域：demo　根目录：atest
- 时间：2026-11-13T14:55:50+08:00
- 结果：4 绿 / 0 红 / 0 blocked，耗时 4.8s，退出码 0

## 用例

| CASE-ID | 状态 | 耗时 | 摘要 |
|---|---|---|---|
| CASE-101 | ✅ passed | 1.0s | — |
| CASE-102 | ❌ failed | 88ms | diff: content |

## 红灯

### CASE-102 diff: content
- 期望：`"hello"`　实际：`"hello "`
- 证据：`atest/report/20261113T145550/CASE-102/summary.txt`、`…/objects/…json`
- 复现：`python bin/at-run/at-run.py --dir . --root atest --instance L1 --domain demo --only CASE-102`

## 趋势（对比 20261113T145446）

- 新红：无　修好：CASE-104　仍红：无

## 建议

（人/智能体补充；工具不写）
```

几条固定规则：

- **总览**：实例/来源、测试域、根目录；报告时间、几绿几红几 blocked、耗时（各例
  `duration_ms` 之和）、退出码（`failed == 0` → `0`，否则 `1`，与 `at-run` 一致）；
- **用例**：表格行数 = `cases` 数；状态徽标 ✅ / ❌ / ⛔；摘要列失败看 diff 路径、
  blocked 看 `summary.txt` 首行结论、通过为 `—`；
- **红灯**：**只在有 `failed` 时出现**（`blocked` 单列在表格里，不算红）；
  - 有 diff：`### <CASE-ID> diff: <path>`，每处一行「期望 / 实际」；多处时逐行带 path；
  - 无 diff（如脚本没输出结果 JSON）：`### <CASE-ID>（无 diff）` + `summary.txt` 的结论；
  - 证据清单：`summary.txt` 在前、`objects/` 次之，其余按名字；
  - 复现命令：`at-run --only <CASE-ID>`，可在项目根直接复制执行；
- **趋势**：只与**同目录**上一份比，见下节；
- **建议**：固定留空，工具永远不写内容——由人或智能体补充。

## 趋势怎么算

「上一份」= 与源 JSON **同目录**、文件名符合 `<YYYYMMDD>T<HHMMSS>[-n].json`、
运行 ID 严格小于当前份的**最大**那份；`latest.json` 永远排除（它只是副本）。

| 分类 | 判定 |
|------|------|
| 新红 | 当前 `failed`，上一份不是 `failed`（上一份缺失也算） |
| 修好 | 上一份 `failed`，当前不是 `failed` |
| 仍红 | 两份都是 `failed` |
| 新增 / 消失 | 只在一份里出现的 CASE-ID；有才显示这一行 |

首份（同目录没有更早的报告）显示 `- 无上一份报告可比（首份）`；上一份 JSON 坏了
只降级成一行提示，**不阻塞**本次报告生成。

## 路径与可移植性

- 证据路径一律**相对项目根**：`evidence_dir`（at-run 写成绝对路径）在渲染时转成
  `atest/report/<ts>/<CASE-ID>/…`，报告整体挪走也不失效；
- 万一 `evidence_dir` 不在项目根下（人为构造的报告），原样保留绝对路径，不猜；
- 报告文件名：源文件是 `<timestamp>.json` 就直接用；是 `latest.json` 或自定义名时，
  由 JSON 的 `timestamp` 字段（ISO 时间）推出 `<YYYYMMDD>T<HHMMSS>`；
- 重复生成同一份会**覆盖**同名 `.md` 与 `latest.md`（纯函数，幂等）。

> `latest.md` 的含义是「**最近一次生成的**报告」，不是「最新时间戳的报告」：
> 用位置参数回放旧 JSON 时它也会被刷新。想保留旧视图请先复制走。

## 读哪些 JSON 字段

| 来源 | 字段 |
|------|------|
| 报告顶层 | `schema`（v2 = `atest-report/2`；无此键的 v1 也接受）、`timestamp`、`instance`、`instance_source`、`cases` |
| `cases[]` | `caseID`、`status`（`passed` / `failed` / `blocked`）、`duration_ms`、`evidence_dir`、`diff[{path, expected, actual}]`、`evidence[]` |
| `atest.json` | `root`（`--root` 可覆盖）、`test_domain`（拼复现命令用） |

顶层 `total` / `passed` / `failed` / `blocked` **只作参考**：表格与总览一律按 `cases`
重新计数，避免报告字段自相矛盾时误导。`diff` 里的值按 JSON 原样展示，超长截断为
`…`（不改报告本身）。

## 已知边界

- 不做 HTML / 网页、不接 CI、不自动分析失败原因（只陈列证据，判断留给人）；
- 不重新执行用例、不探活实例；`blocked` 不算红，整份全 blocked 时退出码仍是 `0`；
- 趋势只比上一份，不做历史聚合（需要趋势图时用 `at-gc` 之后的归档另做）；
- 截图 / 二进制证据不内嵌，只给路径；
- 中文列宽不做 padding 对齐（Markdown 表格本来就不保证等宽，别为对齐破坏 diff）；
- 报告与 `latest.md` 都在 `<root>/report/` 下，该目录已由 `at-init` 写进 `.gitignore`；
- 生成物 UTF-8 无 BOM、LF；Windows 控制台用 UTF-8 输出，避免中文乱码。
