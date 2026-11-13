# at-run — ATEST 运行器（v2）

一条命令把 ATEST 串起来：发现用例 → 校验格式 → 解析实例 → 复用会话 →
逐例执行 → 写 JSON 报告。它是"绿灯可信"的物理载体：人只认它的退出码与报告。

v2（ATESET-04）在 v1 之上补了**表达力**与**证据**：期望支持
`$ignore` / `$range` / `$regex` 三个 matcher，diff 给出嵌套路径，
每例产 `summary.txt` 与 `objects/`，报告只增不改（新增 `schema` 与 `evidence`）。
脚本侧另给薄库 [`atlib.py`](atlib.py)（`wn` / `snap` / `emit` / `note`）。

与 [`at-init`](../at-init/) 分工：`at-init` 铺骨架，**本工具只跑**；用例格式由
ATESET-02 定稿（模板 [`bin/at-init/tmpl/CASE-001.md`](../at-init/tmpl/CASE-001.md)），
实例与会话由 [`wn-cli`](../wn-cli/) 负责，本工具只读实例登记、只复用会话。

> 契约来源：ATESET-03 任务书 §脚本契约 / §报告（T1 定稿，硬接口）。
> 本 README 是实现口径的补充，**不改变**那两处定义。

## 配置：项目根 `atest.json`

**唯一配置源**（不再有 `ATEST.md`）。`at-init` 生成，人可直接编辑：

```json
{
  "version": 1,
  "project": "myproj",
  "root": "atest",
  "instance": "L1",
  "test_domain": "demo",
  "credentials_file": ".at/credentials.json",
  "init_template": "atest/atest.init",
  "sql_datasource": "default"
}
```

- `root`：集成测试根目录；`cases/` / `scripts/` / `expect/` / `baseline/` /
  `report/` 都在它下面；
- `instance`：`L1` / `L2`，缺省来源；为空时执行退出 2；
- `test_domain`：会话名 `<级别>-<域>-00`；
- `init_template`：夹具模板路径（v1 只读取并在 stdout 回显；渲染归 ATESET-05）；
- 文件缺失、不是合法 JSON、或无 `instance` → 退出码 2，不猜（`--check` 也一样）。

CLI 覆盖：`--root` / `--instance` / `--domain`。

## 用法

```bash
# 只校验用例格式，不执行（0 错 = 退出 0）
python bin/at-run/at-run.py --check
python bin/at-run/at-run.py --check bin/at-init/tmpl/CASE-001.md
python bin/at-run/at-run.py --check atest/cases/

# 全量跑（级别 / 测试域 / 端点都按 atest.json 与 .wn-instances.json 解析）
python bin/at-run/at-run.py

# 只跑一例 / 一个套件；遇到红灯即停
python bin/at-run/at-run.py --only CASE-001
python bin/at-run/at-run.py --only CASE-001,CASE-002 --stop-on-fail
python bin/at-run/at-run.py --suite smoke

# 指定项目目录 / 实例级别 / 测试域 / 根目录
python bin/at-run/at-run.py --dir D:/work/myproj --instance L2 --domain mytest
python bin/at-run/at-run.py --root spec --check
```

| 参数 | 说明 |
|------|------|
| `--check` | 只校验，不执行；可用位置参数指定文件或目录（缺省 `<dir>/<root>/cases`） |
| `--dir DIR` | 项目目录（默认当前目录） |
| `--root NAME` | 集成测试根目录；缺省读 `atest.json` 的 `root`，再缺省 `atest` |
| `--instance L1\|L2` | 实例级别；缺省读 `atest.json` 的 `instance` |
| `--domain NAME` | 覆盖 `atest.json` 的 `test_domain`（会话名 `<级别>-<域>-00`） |
| `--instances FILE` | 实例登记文件；缺省 `$WN_INSTANCES` → 从 `<dir>` 逐级向上 → 用户目录 |
| `--session-dir DIR` | 会话池目录；缺省登记文件所在目录的 `.at/client/session`（工作区级） |
| `--only IDS` | 只跑这些 CASE-ID（逗号分隔，可重复） |
| `--suite NAMES` | 只跑这些 suite（逗号分隔，可重复；缺省全部） |
| `--stop-on-fail` | 第一个红灯即停；**blocked 不算红** |
| `--timeout SEC` | 单例脚本超时（默认 120 秒） |

> `--dir` / `--root` / `--domain` / `--instances` / `--session-dir` / `--timeout`
> 是实现层参数（任务书只列了 `--suite` / `--only` / `--stop-on-fail` /
> `--instance`，原文是"参数含"）。`--domain` 的用途：`atest.json` 里
> `test_domain` 还没改成实际测试域时，不改文件也能跑。

退出码：`0` 全绿（`failed == 0`，**blocked 不算红**）；`1` 有红 / 用法错误
（参数、`--check` 有错、`--only` 指向不存在的用例）；`2` 环境不可用
（`atest.json` 或实例登记缺失/不合法、端点连不上、会话缺失或失效）。

stdout 是**单行 JSON**（提示与逐例进度进 stderr）：

```json
{"ok":true,"suite":"default","root":"atest","instance":"L1","test_domain":"demo",
 "init_template":"atest/atest.init","instance_source":".../atest.json",
 "session":"L1-demo-00","total":1,"passed":1,"failed":0,"blocked":0,"skipped":0,
 "report":".../atest/report/default/20261113T113351.json",
 "latest":".../atest/report/default/latest.json","exit":0}
```

`skipped` = `--stop-on-fail` 提前结束时未执行的用例数（它们**不进报告**，但一定
出现在这个字段与 stderr 摘要里，不静默）。

`--check` 的 stdout 还带 `root`；它读同一份配置（缺 `atest.json` 或 `instance`
为空同样退出 2），只是不执行。

## 实例与会话（只连不管）

- **级别**：`--instance` → `atest.json` 的 `instance` → 退出 2。
- **端点**：`.wn-instances.json` 的 `{级别: {host, port}}`。查找顺序
  `--instances` → `$WN_INSTANCES` → 从 `<dir>` 逐级向上 → `~/.wn-instances.json`；
  显式给出的文件**必须存在**（静默回退会跑错实例）。
- **探活**：TCP 连接端点，3 秒不通即退出 2；**不尝试拉起实例**（1/2 级实例都由
  开发者手工管理）。
- **会话**：工作区池里的 `<级别>-<测试域>-00`。存在则复用，并核对会话里的 `base`
  与本次端点一致（不一致拒绝复用，防 L1/L2 混跑）；缺失或失效则退出 2 并给出登录
  命令——**本工具不代登录、不建域**。会话过期（401）由 `wn-cli` 当场删除缓存文件。

## 脚本契约（T1 定稿）

runner 执行 `<root>/scripts/<CASE-ID>.py`（argv 传参，**绝不经 shell**；
`subprocess.run([...])`），工作目录 = 项目目录，注入：

| 变量 | 含义 |
|---|---|
| `AT_LEVEL` / `AT_URL` | 实例级别与端点（如 `L1` / `http://127.0.0.1:8080`） |
| `AT_SESSION_DIR` | 会话池目录（工作区级） |
| `AT_DOMAIN` | `atest.json` 的 `test_domain`（或 `--domain`） |
| `AT_WNCLI` | `wn-cli.py` 绝对路径（脚本统一经它操作实例） |
| `AT_CASE_ID` / `AT_OUT` | 用例 ID / 证据目录（runner 预建） |

另外把 `bin/at-run` 放进子进程 `PYTHONPATH`（**不是**新的 `AT_*` 契约变量），
脚本因此能 `from atlib import wn, snap, emit, note`（见下文薄库一节）。

脚本 stdout **最后一行**必须是单行 JSON：

- 成功：`{"ok": true, "actual": {...}}` —— `actual` 是要比的结果对象；
- 失败：`{"ok": false, "error": "..."}`。

判定口径：脚本只造数据、取快照、产 `actual`，**不做断言**；断言由 runner 按用例
md 的 `## 期望` 统一做（防"实现即预期"）。退出码：脚本 `0` 正常；`2` = 脚本报
环境不可用（记 **blocked**，不算红）；其它非 0 或超时 = **failed**。

脚本里取机器可读结果请用 `o ... @json -cqn`（裸 `@json` 是 HOCON 风格，不是严格
JSON；`-cqn` 才是紧凑标准 JSON）。

## 期望 matcher（T1 定稿）

`## 期望` 里某个字段的值可以是 matcher 对象（`$` 前缀与普通字段区分）：

```json
{
  "nm": "atest-dir",
  "ct": {"$range": ["$t0", "$t1"]},
  "name": {"$regex": "^CASE-\\d+$"},
  "tmp_field": {"$ignore": true}
}
```

| matcher | 语义 | 合法值 |
|---|---|---|
| `{"$ignore": true}` | 跳过该键（等价于不写，但显式声明"我知道它不稳定"）；键不存在也算通过 | 只能是 `true` |
| `{"$range": ["$t0", "$t1"]}` | 实际值须是数字且落在闭区间内；`$t0` / `$t1` 由 runner 在用例**开始前 / 结束后**各取一次毫秒时间戳，脚本的本地时钟不参与 | 两个元素：`"$t0"` / `"$t1"` / 数字 |
| `{"$regex": "..."}` | 实际值须是字符串且正则命中（Python `re.search`；要整串匹配自己写 `^...$`） | 能编译的字符串 |

- 三者**互斥**：一个 matcher 对象只能有一个键，不能和普通字段混用；
- 不认识 `$xxx`、值类型不对、正则编译失败、把 matcher 放在期望**根**上 →
  `--check` 报 `expected-invalid`（`--check` 也会校验已存在的
  `expect/<CASE-ID>.json`；文件缺失仍留给执行时记 blocked）；
- matcher 显式写在默认忽略的键上（如 `ct`）时**以 matcher 为准**——
  这正是"时间区间"用例的写法；写普通字面值仍按 `volatile-field` 报错。

## 脚本侧薄库 `atlib.py`

脚本不必重写样板；runner 把 `bin/at-run` 放进子进程 `PYTHONPATH`，直接：

```python
from atlib import wn, snap, emit, note

wn("o ~ @create atest-dir -race DIR -upsert")   # 自动带 --level/--se/--url/--session-dir
obj = snap("~/atest-dir")                        # 落 evidence/objects/，返回 dict
note("准备完成")                                 # 写进 summary.txt（runner 会保留）
emit({"nm": obj["nm"], "race": obj["race"]})     # 输出契约那一行 JSON 并退出 0
```

| 函数 | 行为 |
|---|---|
| `wn(cmd, timeout=90)` | 经 `wn-cli cmd` 执行；非 0 退出码 → `SystemExit(2)`（记 blocked） |
| `snap(path, name=None)` | `o <path> @json -cqn` → dict；落 `<AT_OUT>/objects/<路径>.json`（`/` 换 `__`）；取不到即抛 |
| `emit(actual)` | 打印 `{"ok":true,"actual":...}` 后退出 0——**之后不要再往 stdout 写任何东西** |
| `note(text)` | 追加一行 `[script] …` 到 `summary.txt`；runner 重写 summary 时原样保留 |

atlib 只依赖标准库与 `AT_*` 环境变量；失败一律 `SystemExit(2)` 且原因进 summary，
所以脚本里不用再包 try/except。手工跑脚本时自己设
`PYTHONPATH=<walnut>/bin/at-run` 并补齐 `AT_*`（正常路径永远是经 at-run）。

## 判定与证据

- **只比 expected 里出现的键**（子集匹配）；数组按顺序、等长逐项比，diff 路径
  形如 `items[1].nm`；
- 忽略 `id` / `pid` / `ct` / `lm` / `sha1`（任意深度）与以 `_` 开头的键；
  显式写了 matcher 的键以 matcher 为准；
- 失败时报告带 `diff: [{path, expected, actual}]`（`actual: null` 表示缺键）；
  matcher 失败时 `expected` 里是**代入后**的值（如 `{"$range": [t0, t1]}` 的毫秒数）；
- 每例证据目录 `<report>/<timestamp>/<CASE-ID>/`：
  - `summary.txt`：一句话结论 + diff 前三处 + 复现命令 + L1 日志不可用声明 +
    脚本 `note()` 记录（**每例都有**，失败时先看它）；
  - `objects/`：脚本 `snap()` 抓的对象 JSON（按路径命名）；
  - `cmd.txt` / `env.txt` / `stdout.txt` / `stderr.txt` / `result.json` /
    `note.txt` / `clock.txt`（`$t0` / `$t1` 的毫秒与本地时间）；
- `blocked` 的来源：缺脚本（未实现）、缺期望文件或基线（`expected: file` /
  `baseline`）、期望文件/基线不是合法 JSON（记 blocked 而非红，也不中断其余用例）。
  基线缺失不算红（基线由 08 的审核者建立，必须可移植）。

## 基线（必须可移植）

`expected: baseline` 的期望值放在单一、可移植的路径：

```text
<root>/baseline/<CASE-ID>.json      # 如 atest/baseline/CASE-101.json
```

- 基线建立后要能**反复执行**：同一份基线在 L1 / L2、在别人的机器上都应通过。
  不同实例结果不同，本身就是用例或产品的问题，**不用"两套基线"消化**；
- 所以基线只收**与环境无关**的值：`id` / `pid` / `ct` / `lm` / `sha1`、绝对路径
  （如 `ph`）、会话名/机器名等一律不写进基线；时间类约束用用例 `## 期望` 里的
  `$range` 表达，不要往基线里塞执行时刻；
- 缺基线仍是 **blocked**（不算红），提示里给完整路径；本工具不建基线、不改基线（08）；
- `--check` 不要求基线存在（尚未建立是常态），但期望声明与内容矛盾会报
  `expected-mismatch`。

## 报告

```text
<root>/report/<suite>/<timestamp>.json     # 本次报告（JSON 是唯一源）
<root>/report/<suite>/latest.json          # 同内容（覆盖写）
<root>/report/<suite>/<timestamp>/<CASE-ID>/  # 每例证据目录
```

```json
{"schema":"atest-report/2","suite":"default","timestamp":"2026-11-13T12:22:24+08:00",
 "instance":"L1","instance_source":".../atest.json","total":2,"passed":1,"failed":1,
 "blocked":0,
 "cases":[{"caseID":"CASE-101","status":"passed","duration_ms":391,
           "evidence_dir":"...","diff":[],
           "evidence":["clock.txt","cmd.txt","env.txt",
                       "objects/~__atest04-range.json","result.json",
                       "stderr.txt","stdout.txt","summary.txt"]},
          {"caseID":"CASE-104","status":"failed","duration_ms":855,
           "evidence_dir":"...",
           "diff":[{"path":"items[1].nm","expected":"atest-b","actual":"atest-c"}],
           "evidence":["clock.txt","cmd.txt","env.txt","note.txt","objects/…",
                       "result.json","stderr.txt","stdout.txt","summary.txt"]}]}
```

- `schema` 是 v2 新增的顶层键（`"atest-report/2"`）；03 的 9 个顶层键与每例 5 个键
  **保持不变**，`evidence` 追加在每例的最后；
- `evidence` 是相对 `evidence_dir` 的文件清单（正斜杠、排序稳定），指向真实文件；
- `status` 只有 `passed` / `failed` / `blocked`。`instance_source` 为 `cli`
  （`--instance` 生效）或 `atest.json` 的绝对路径。`<root>/report/` 已在项目
  `.gitignore` 里（`at-init` 追加），报告不进库。

## `--check` 的错误码

| 错误码 | 含义 |
|---|---|
| `filename-mismatch` | 文件名（去 `.md`）≠ `caseID` |
| `caseid-invalid` | 不匹配 `^CASE-[A-Z0-9-]{1,32}$` |
| `missing-field` | 缺 5 个必填之一（`caseID` / `title` / `source` / `contract` / `expected`） |
| `expected-invalid` | `expected` 不是 `inline` / `file` / `baseline`；或期望值里的 matcher 不合法（不认识 / 混用 / 类型不对 / 正则编译失败 / 放在根上） |
| `contract-too-generic` | `contract` 只写命令族（`o` / `jsc` 等），不具体 |
| `missing-section` | 缺 `## 前置` / `## 步骤` / `## 期望` / `## 证据` |
| `assertion-in-steps` | `## 步骤` 里出现断言词（检查/确认/断言/判失败…） |
| `expected-mismatch` | 声明与内容矛盾（inline 无 json 块；file/baseline 却贴了 json 块） |
| `expected-invalid-json` | `## 期望` 的 json 块（或已存在的 `expect/<CASE-ID>.json`）解析失败 |
| `volatile-field` | 期望里写死 `id` / `pid` / `ct` / `lm` / `sha1`（写 matcher 不算） |
| `caseid-duplicate` | 多个用例文件用同一个 `caseID` |

`--check` 对目录是**扫全部 `*.md`**（缺 front-matter 的 md 会被报错，这样
`<root>/cases/` 里混进草稿不会静默漏检）。执行前也会对选中的用例做同一套校验：
有格式错误就**整体不执行**（退出 1），不出"半绿"报告。

## 已知边界

- 比较仍是**子集**语义（只比期望里出现的键），不做整棵对象树的等价判断；
  三个 matcher 之外的自定义比较交给用例脚本自己算；
- 不做全量快照（只存脚本显式 `snap()` 的路径）；不渲染 `<root>/atest.init`（05）；
  不写 Markdown 报告（07）；不建基线（08）；**不并发**执行；
- 脚本固定 `<CASE-ID>.py`（任务书的契约）；其它语言留给后续任务；
- 超时只杀直接子进程（脚本自己起的 `wn-cli` 子进程不保证一起终止）；
- 用例级 `instance` 与本次运行级别不一致时记 **blocked**（附原因），不会在一次
  运行里混跑两级实例；
- 全 blocked 的运行退出码仍是 `0`（按"blocked 不算红"的约定）；报告里
  `passed=0` / `blocked=n` 与 stderr 摘要是唯一警示；
- 子进程注入 `PYTHONUTF8=1` / `PYTHONIOENCODING=utf-8`（Windows 控制台 cp936 会把
  中文证据拆坏）、`PYTHONDONTWRITEBYTECODE=1`（脚本 import `atlib` 不往仓库里落
  `__pycache__`）与 `PYTHONPATH`（指向 `bin/at-run`，供 `import atlib`）；
  不新增 `AT_*` 契约变量。
