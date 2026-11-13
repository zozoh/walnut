# at-gc — ATEST 报告归档

`atest/report/` 每跑一次就多一组文件；本工具**只保留最近 N 次**，更早的压成
一个 zip 后删除原件；`archive/` 里放久了的 zip 也会按 `--archive-days`
（**默认保留 30 天**）清掉。`at-run` 跑完会自动调用它，日常不用手敲。

```text
python bin/at-gc/at-gc.py [--dir <项目>] [--root NAME] [--keep N]
                          [--archive-days N] [--dry-run]
```

## 一次执行在盘上有两种形态

判定顺序：**先看 `<ts>/report.json`，没有再退回 `<ts>.json`**。

```text
新一代（ATESET-16 起）              旧一代（2026-11 之前，盘上已有 70 份）
atest/report/<ts>/report.json       atest/report/<ts>.json
atest/report/<ts>/report.md         atest/report/<ts>.md
atest/report/<ts>/actual.json       atest/report/<ts>.actual.json
atest/report/<ts>/cases/<CASE-ID>/… atest/report/<ts>/<CASE-ID>/…   （证据）
```

- 新一代：`<ts>/report.json` 存在 → **整个 `<ts>/` 目录**算一次执行；
- 旧一代：`<ts>.json` 存在 → `<ts>.json` + 同名前缀的 `.md` / `.actual.json`
  + `<ts>/` 证据目录算一次执行；
- `latest.json` / `latest.md` 是"最近一次的副本"，**任何形态都永不归档**。

## 保留规则

- 按时间戳（`YYYYMMDDTHHMMSS`，同一秒重复跑会带 `-2` 序号）排序；
- 最新 `N` 次原样保留。`N` = `--keep` > `atest.json` 的 `report_keep` > `3`；
- 更早的每个 `<ts>` 打成一个 `atest/report/archive/<ts>.zip`；
- 正在写的报告**跳过**：
  - 新形态缺 `<ts>/report.json`（`at-run` 最后才写主报告）；
  - 旧形态缺 `<ts>.json`；
  - 主报告 json 的 mtime 在 **5 分钟内**；
- 名字不是时间戳、也不是 `latest.*` / `archive/` 的顶层条目：跳过并计入
  `skipped`（`reason: "invalid-name"`），不删。

## zip 里的布局（统一成新一代）

归档不保留"旧形态"的扁平命名；两种形态解出来都是**一次执行**：

| 原来件 | zip 内路径 |
|---|---|
| 新形态 `<ts>/` 下的全部文件 | 相对 `<ts>/` 的路径（`report.json`、`cases/<CASE-ID>/…`） |
| 旧形态 `<ts>.json` | `report.json` |
| 旧形态 `<ts>.md` | `report.md` |
| 旧形态 `<ts>.actual.json` | `actual.json` |
| 旧形态 `<ts>/<CASE-ID>/…` | `cases/<CASE-ID>/…` |

即：把 `archive/<ts>.zip` 解到 `atest/report/<ts>/`，得到的正是新一代的一次执行。
同一 `<ts>` 若新旧散件并存（极端情况），全部收进同一个 zip。

## 安全规则（先压缩、校验通过再删）

1. 写临时文件 `archive/<ts>.zip.tmp`；
2. 用 `zipfile` 逐成员**比对内容**（不只是大小）：成员齐全、内容逐字节一致、
   `testzip()` 通过；
3. 通过后才 `os.replace` 成正式 zip，再删原件；
4. 任何一步失败：zip 不留半份（临时文件删掉），**原件一个不删**，退出码 1。

- **幂等**：成功归档的 `<ts>` 已无原件，重复运行不会重复压缩；
- **断点续删**：如果"校验通过、删到一半"被中断，下次运行会核对已有 zip 覆盖
  剩余原件，然后把删除补完（不会重复压缩）；覆盖不上则报错、原件保留；
- **`--dry-run`**：只扫描与打印计划，不建 `archive/`、不写 zip、不删文件，
  前后文件列表与 mtime 完全不变；
- 不跟随目录符号链接（`os.walk` 默认），zip 内路径统一 `/`。

## 过期归档清理（默认保留 30 天）

归档动作**之后**，扫描 `<root>/report/archive/` 的直接子项，`mtime` 早于
`--archive-days N` 天的 `.zip` 删除，并逐个打印：

```text
at-gc: 删除过期归档 20260101T000001.zip（31 天前）
```

- `--archive-days` 缺省 **30**（即 `archive/` 只留最近一个月）；
  **`0` = 不清理**（一个都不删）；
- 括号里的天数 = 该 zip 实际存放天数（`int((now - mtime) / 86400)`），不是阈值；
- 安全边界：只删 `archive/` **直接子项**里、扩展名为 `.zip` 的**普通文件**；
  **目录、符号链接、非 `.zip` 一律不动**（`archive/` 下的子目录不会被递归）；
- 归档与清理的顺序固定：先归档、再清理。刚写出的 zip `mtime` 是现在，不会被
  同一轮误删；`--dry-run` 只打印 `at-gc: [dry-run] 删除过期归档 …`，
  不删任何文件（JSON 的 `deleted_archives` 是"将删除"清单）；
- 删除失败（权限 / 占用）记进 `errors`：该 zip 保留、`ok=false`、退出码 1；
  其余归档不受影响。

## 参数

| 参数 | 说明 |
|------|------|
| `--dir DIR` | 项目目录（默认当前目录） |
| `--root NAME` | 集成测试根目录；缺省读 `<dir>/atest.json` 的 `root`，再缺省 `atest` |
| `--keep N` | 保留最近 N 次（非负整数）；缺省读 `atest.json` 的 `report_keep`，再缺省 `3` |
| `--archive-days N` | 归档后删除 `archive/` 下 mtime 早于 N 天的 `.zip`；缺省 `30`，`0` = 不清理 |
| `--dry-run` | 只打印计划，不动任何文件 |

## 输出

stderr 是人读摘要（保留几个、归档哪些、跳过为什么）；stdout 最后一行单行 JSON：

```json
{"ok": true, "kept": 3,
 "archived": [{"ts": "20260101T000001",
               "zip": "atest/report/archive/20260101T000001.zip",
               "files": ["atest/report/20260101T000001.json",
                         "atest/report/20260101T000001.md",
                         "atest/report/20260101T000001/"],
               "bytes": 3551, "form": "flat"}],
 "freed_bytes": 73930,
 "deleted_archives": [{"name": "20251201T000001.zip", "days": 31}],
 "deleted_bytes": 4521,
 "dry_run": false, "keep": 3,
 "skipped": [{"ts": "20260101T000007", "reason": "in-flight",
              "detail": "主报告 5 分钟内有改动（可能正在写）"}],
 "errors": []}
```

- `kept`：原样保留的报告数（不是文件数）；
- `archived[].files`：这次归档的**顶层原件**（相对项目根；目录形态只有一项，
  以 `/` 结尾），`bytes` 是原件总字节；
- `archived[].form`：`"dir"`（新一代目录）/ `"flat"`（旧一代散件）；
- `freed_bytes`：本次删除的原件总字节（`--dry-run` 时是"将释放"）；
- `deleted_archives[]`：本轮删掉的过期归档，`name` = `archive/` 下的文件名，
  `days` = 实际存放天数；`--dry-run` 时是"将删除"清单（一个都没删）；
- `deleted_bytes`：这些过期 zip 的总字节（`--dry-run` 时是"将释放"）；
- `skipped`：`invalid-name` / `in-flight`；
- `errors`：归档失败 / 过期归档删除失败的 `<ts>` 或文件名与原因；非空时
  `ok=false`、退出 1。

退出码：`0` 好（含无事可做）/ `1` 用法或 IO 错（有 `errors`、`--keep` 非法）/
`2` 环境不可用（`atest.json` 或 `report/` 目录缺失/坏档）。

## 与 at-run 的集成

`at-run` 写完报告（含 `--record`）后自动调用 `at-gc`：

- 用同一个 `--dir` 和 `--root`，保留数读 `atest.json` 的 `report_keep`；
- 本次报告刚写完，mtime 在 5 分钟内 → 一定被跳过，**不会删掉本次报告**；
- **不传 `--archive-days`**，即按默认 **30 天**清理过期归档：`at-run` 无需改动；
- `at-gc` 失败只在 stderr 警告，**不改变 `at-run` 的退出码**；
- `at-run` 的 stdout JSON 多一个 `archived`（本次归档数，0 = 没触发/没归档）；
- `at-run --no-gc` 关闭自动归档（也就不会删过期归档）。

## 已知边界

- **`at-report` 还不支持直接读 zip**：归档后的人读视图要从 zip 解出来再跑；
- 不做远端归档、不清理基线/用例、不动实例；
- 并发：两个 `at-run` 同时收尾时，`at-gc` 的"扫描 → 删除"没有全局锁；
  正在写（5 分钟内 / 缺主报告）的报告会被跳过，但极端时间窗仍可能撞车，
  建议别在同一个项目目录并发跑。
