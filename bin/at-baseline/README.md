# at-baseline — ATEST 基线工具

把"一次跑出来的 actual"变成**可移植的期望快照**：清洗掉环境字段、带上来源哈希，
并能在用例改动后告诉你"基线该重新审核了"。

`expected: baseline` 的期望值放在**单一**路径：

```text
<root>/baseline/<CASE-ID>.json      # 如 atest/baseline/CASE-101.json
```

同一份基线在 L1 / L2、在别人的机器上都要能过——两边结果不同本身就是用例或产品的
bug，**不用"两套基线"消化**（2026-11-13 用户裁定）。

## 为什么录制与审核分开

让用例作者自己录基线，等于把"当前实现"固化成"预期"。所以本工具只做**机械动作**：

```text
at-run --record                     → <root>/report/<ts>.actual.json
at-baseline --from <actual.json>    → <root>/baseline/<CASE-ID>.json（清洗 + meta）
at-baseline --audit                 → 用例 md / 夹具模板变了就报 stale
```

"这个值在业务上对不对"由**审核者**判断并签 `meta.reviewer`，工具不判断。

## 用法

```bash
# 1) 录一轮 actual（不比较期望）
python bin/at-run/at-run.py --record

# 2) 清洗落盘（已有基线缺 --force 会拒绝，一个文件都不写）
python bin/at-baseline/at-baseline.py --from atest/report/20261113T150908.actual.json

# 3) 只录/只刷其中几例，并签名
python bin/at-baseline/at-baseline.py --from <file> --only CASE-101,CASE-102 --reviewer 智群

# 4) 复核：用例 md 或 atest.init 改过 → stale（退出码 1）
python bin/at-baseline/at-baseline.py --audit
```

| 参数 | 说明 |
|------|------|
| `--from FILE` | `at-run --record` 产的 `<ts>.actual.json`（与 `--audit` 二选一） |
| `--audit` | 扫全部基线，比对来源哈希；有问题退出 1 |
| `--dir DIR` | 项目目录（默认当前目录） |
| `--root NAME` | 集成测试根目录；缺省读 `<dir>/atest.json` 的 `root`，再缺省 `atest` |
| `--only IDS` | 只处理这些 CASE-ID（逗号分隔，可重复；只用于 `--from`） |
| `--reviewer NAME` | 写进 `meta.reviewer` 的审核者标识（默认 `unreviewed` = 未审核） |
| `--force` | 基线已存在时覆盖（默认拒绝；覆盖 = 期望换了，要重新复核） |

`--from` 会先校验**每个**用例：md 存在、`expected: baseline`、夹具模板可读；
有一条不满足就整体不写（不留半套基线）。

## 基线格式

```json
{
  "meta": {
    "case": "CASE-101",
    "source_hash": "c98a92e31e265e2d",
    "instance": "L1",
    "recorded_at": "2026-11-13T15:09:08+08:00",
    "reviewer": "unreviewed",
    "dropped": ["pid"]
  },
  "expected": { "nm": "case101-dir", "race": "DIR" }
}
```

- **runner 只读 `expected`**：`meta` 不是断言，改 hash/审核者不会改变判定；
- 没有 `expected` 键的**裸对象**（手写基线）整份当期望，向后兼容——但 `--audit`
  会报 `no-hash`（它没有来源哈希）；
- `instance` / `recorded_at` 取自 actual 的 `_meta`（录制时的实例与时间）；
  `--from` 只搬事实，不猜。

## 清洗规则（定稿，工具执行）

基线只收与**环境无关**的值。规则三条：

| 规则 | 处理 | 留痕 |
|------|------|------|
| 丢掉键 `id` / `pid` / `ct` / `lm` / `sha1` / `ph` / `mnt` / `ln` / `c` / `m` / `g` / `d0` / `d1`（任意深度） | 整键删除 | `meta.dropped` 记字段路径（嵌套用 `a.b`，数组用 `a[0]`） |
| 丢掉**值里含绝对路径**的键（`/home/…`、`/Users/…`、`C:\…`、`file://…`、UNC `\\host\…`） | 整键删除；数组元素删该元素 | `meta.dropped` 记 `字段 (absolute-path)` |
| 其余原样保留 | —— | —— |

- `meta.dropped` 是审核者的**复核入口**：如果业务字段被洗掉了，会在这里露出来
  （清洗太狠 = 基线变空 = 假绿）；
- 键名判定在**任意深度**生效（对象快照里的 `pid`、数组元素里的绝对路径都会被处理）；
- 时间类断言与基线互斥：`expected: baseline` 的用例 `## 期望` 里不能贴 json 块，
  `$range` matcher 没有落点。时间不进基线，看证据（`clock.txt` / 对象快照）。

## 来源哈希

```text
source_hash = sha256("md\n" + 用例 md + "\x00" + "init\n" + 夹具模板 + "\x00")[:16]
```

- **必须含 `atest.init`**：夹具变了，期望往往也跟着变；
- CRLF 先归一成 LF，同一份内容在 Windows / Linux checkout 上哈希相同；
- 公式在 `bin/at-run/at-run.py` 与 `bin/at-baseline/at-baseline.py` 各有一份
  （两个工具互不 import），**改一处必须改另一处**；`at-run --check` 与
  `--audit` 用同一公式对账，两边不一致会在第一次 `--check` 就暴露。

## 审核流程

1. 执行者：`--record` + `--from`（机械动作，`meta.reviewer` 先是 `unreviewed`）；
2. 审核者：逐条看 `expected`——这个值是否代表"业务上正确"，而不是"当前实现碰巧如此"；
   顺手复核 `meta.dropped`，确认没把业务字段洗掉；
3. 审核者签名：把 `meta.reviewer` 改成自己的标识（或 `--from --reviewer` 重录）；
   有疑问的在报告里提出，**不改基线**；
4. 之后用例 md / `atest.init` 一改 → `--audit`（或 `at-run --check`）报 stale →
   回到 2。重新录制再走一遍 1~3。

## `--audit` 的状态

| status | 含义 |
|--------|------|
| `ok` | 基线哈希与当前用例 md + 夹具模板一致 |
| `stale` | 用例 md / 夹具模板改过：基线待复核（重新审核后 `--from --force`） |
| `no-hash` | 基线没有 `meta.source_hash`（手写裸对象）：改了用例也发现不了 |
| `orphan` | 基线对应的用例 md 不见了（基线该删，或用例该补） |
| `invalid` | 基线读不了 / 不是合法 JSON / 顶层不是对象 |
| `hash-skipped` | 夹具模板缺失或不可读，算不了哈希 |

`--audit` 输出是**单行 JSON**（`checked` / `stale` / `results`），stale 时退出 1；
人读摘要进 stderr。退出码：`0` 全部对得上；`1` 有 stale / 用法错误；
`2` 环境不可用（`atest.json`、用例目录、基线目录、actual 文件缺失或坏档）。

## 边界与坑

- **不判断值对不对**：工具只清洗、只对账；"业务上正确"由审核者签；
- **不自动覆盖**：已有基线缺 `--force` 直接报错（退出 1），一个文件都不写；
- **不做跨用例继承**：一份基线只服务一个 CASE-ID；
- **不清理基线**：删了对应用例后基线变 `orphan`，删不删由人决定（工具不代删）；
- **清洗后基线可能变空**（实际值全是易变字段）→ `expected: {}` 对任何实际值都通过，
  是**假绿**；`meta.dropped` 全是被洗字段时，审核者应当判"这例不适合基线"；
- 基线文件 UTF-8 无 BOM、LF、两空格缩进（`json.dumps(indent=2)`）；
  报告目录 `atest/report/` 已在 `.gitignore` 里，**基线进库**。
