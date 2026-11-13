# at-run — ATEST 运行器（v2）

一条命令把 ATEST 串起来：发现用例 → 校验格式 → 解析实例 → 复用/自动建立会话 →
逐例执行 → 写 JSON 报告。它是"绿灯可信"的物理载体：人只认它的退出码与报告。

v2（ATESET-04）在 v1 之上补了**表达力**与**证据**：期望支持
`$ignore` / `$range` / `$regex` 三个 matcher，diff 给出嵌套路径，
每例产 `summary.txt` 与 `objects/`，报告只增不改（新增 `schema` 与 `evidence`）。
脚本侧另给薄库 [`atlib.py`](atlib.py)（`wn` / `http` / `fixture` / `snap` / `emit` / `note`）。

ATESET-05 补上**夹具**：静态布局写进 `<root>/atest.init`，用例脚本用
`atlib.fixture()` 调 Walnut 自己的 `app init` 铺进域（`-conf` 指定上传后的模板，
`-script off`）；非幂等用例再带清理脚本，执行顺序固定为 **清理 → 初始化 → 用例**
（见下文「夹具与清理」）。

> 用例格式 v2（2026-11-13）：必填 `contracts` 有序数组；不再有 `instance` /
> `suite` 字段，报告也不再按 suite 分目录。权威定义见 ATESET-02 任务书。

与 [`at-init`](../at-init/) 分工：`at-init` 铺骨架，**本工具只跑**；用例格式由
ATESET-02 定稿（模板 [`bin/at-init/tmpl/CASE-001.md`](../at-init/tmpl/CASE-001.md)），
实例与会话由 [`wn-cli`](../wn-cli/) 负责，本工具只读实例登记，会话不存在时按需
自动登录（见下文「实例与会话」）。

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
- `init_template`：夹具模板路径（缺省 `<root>/atest.init`）；`atlib.fixture()` 用它，
  `--check` 会警告缺失 / 异常大 / 外部引用（外部引用只是审阅提示，`app init` 支持）；
- `credentials_file`：凭据文件位置（缺省 `.at/credentials.json`）；只在**需要自动
  登录**时读（会话还在就不读，手工登录过的项目不受影响）；
- 文件缺失、不是合法 JSON、或无 `instance` → 退出码 2，不猜（`--check` 也一样）。

CLI 覆盖：`--root` / `--instance` / `--domain` / `--creds`。

## 用法

```bash
# 只校验用例格式，不执行（0 错 = 退出 0）
python bin/at-run/at-run.py --check
python bin/at-run/at-run.py --check bin/at-init/tmpl/CASE-001.md
python bin/at-run/at-run.py --check atest/cases/

# 全量跑（级别 / 测试域 / 端点都按 atest.json 与 .wn-instances.json 解析）
python bin/at-run/at-run.py

# 录制：跑全套并另写 <ts>/actual.json（不比较期望；喂给 bin/at-baseline 建基线）
python bin/at-run/at-run.py --record
python bin/at-run/at-run.py --record --only CASE-101,CASE-102

# 只跑一例 / 一个套件；遇到红灯即停
python bin/at-run/at-run.py --only CASE-001
python bin/at-run/at-run.py --only CASE-001,CASE-002 --stop-on-fail

# 整批执行前先跑套件级清理 <root>/cleanup.py
python bin/at-run/at-run.py --reset

# 指定项目目录 / 实例级别 / 测试域 / 根目录 / 凭据文件
python bin/at-run/at-run.py --dir D:/work/myproj --instance L2 --domain mytest
python bin/at-run/at-run.py --creds .at/temp-creds.json
python bin/at-run/at-run.py --root spec --check

# 本次跑完不归档旧报告（排查报告目录时用）
python bin/at-run/at-run.py --no-gc
```

| 参数 | 说明 |
|------|------|
| `--check` | 只校验，不执行；可用位置参数指定文件或目录（缺省 `<dir>/<root>/cases`） |
| `--record` | 跑全套并另写 `<root>/report/<ts>/actual.json`；只验脚本契约，**不比较期望** |
| `--dir DIR` | 项目目录（默认当前目录） |
| `--root NAME` | 集成测试根目录；缺省读 `atest.json` 的 `root`，再缺省 `atest` |
| `--instance L1\|L2` | 实例级别；缺省读 `atest.json` 的 `instance` |
| `--domain NAME` | 覆盖 `atest.json` 的 `test_domain`（会话名 `<级别>-<域>-00`） |
| `--instances FILE` | 实例登记文件；缺省 `$WN_INSTANCES` → 从 `<dir>` 逐级向上 → 用户目录 |
| `--session-dir DIR` | 会话池目录；缺省登记文件所在目录的 `.at/client/session`（工作区级） |
| `--creds FILE` | 凭据文件；缺省读 `atest.json` 的 `credentials_file`，再缺省 `.at/credentials.json`。只在需要自动登录时读 |
| `--only IDS` | 只跑这些 CASE-ID（逗号分隔，可重复） |
| `--stop-on-fail` | 第一个红灯即停；**blocked 不算红** |
| `--reset` | 整批执行前先跑 `<root>/cleanup.py`；失败则全批记 **blocked**（不算红） |
| `--timeout SEC` | 单例脚本 / 清理脚本超时（默认 120 秒） |
| `--no-gc` | 跑完不自动调用 `at-gc` 归档旧报告（缺省会自动归档，见「报告」） |

> `--dir` / `--root` / `--domain` / `--instances` / `--session-dir` / `--timeout`
> 是实现层参数（任务书只列了 `--only` / `--stop-on-fail` / `--instance`，
> 原文是"参数含"）。`--domain` 的用途：`atest.json` 里
> `test_domain` 还没改成实际测试域时，不改文件也能跑。

退出码：`0` 全绿（`failed == 0`，**blocked 不算红**）；`1` 有红 / 用法错误
（参数、`--check` 有错、`--only` 指向不存在的用例）；`2` 环境不可用
（`atest.json` 或实例登记缺失/不合法、端点连不上、缺凭据、启动时自动登录失败）。

stdout 是**单行 JSON**（提示与逐例进度进 stderr）：

```json
{"ok":true,"root":"atest","instance":"L1","test_domain":"demo",
 "init_template":"atest/atest.init","instance_source":"atest.json",
 "session":"L1-demo-00","session_created":false,"reset":true,"cleanup":"ok",
 "total":1,"passed":1,"failed":0,"blocked":0,"skipped":0,"archived":0,
 "report":".../atest/report/20261113T113351/report.json",
 "latest":".../atest/report/latest.json","exit":0}
```

`skipped` = `--stop-on-fail` 提前结束时未执行的用例数（它们**不进报告**，但一定
出现在这个字段与 stderr 摘要里，不静默）。

`archived` = 本次跑完 `at-gc` 归档的旧报告数（`0` = 没触发/没归档）：
报告写完后自动调用 `at-gc`，保留最近 `atest.json` 的 `report_keep` 次（缺省 3）；
本次报告刚写完（5 分钟内），一定被跳过，不会删掉；`at-gc` 失败只在 stderr
警告、**不改变本次退出码**；`--no-gc` 关闭。

`--record` 另写一份 actual（同一次执行的产物；`_meta` 是录制元信息，其余键 =
CASE-ID → 脚本 actual）：

```json
{"_meta":{"schema":"atest-actual/1","timestamp":"2026-11-13T15:09:08+08:00",
          "instance":"L1","root":"atest","report":"atest/report/<ts>/report.json",
          "recorded":4,"unrecorded":[]},
 "CASE-101":{"nm":"case101-dir","race":"DIR","pid":"…"}}
```

- `_meta` 以 `_` 开头，`at-baseline` 不当用例读；`unrecorded` = 跑了但没拿到
  `actual` 的用例（脚本失败 / blocked），不静默；
- 报告里多出 `mode: "record"` 与 `actual`：这次 `passed` **只代表脚本契约通过**，
  不是验收绿——验收用普通运行；
- 录制模式不因"缺期望"而 blocked：`expected: baseline` 还没建基线时正该录这一轮
  （没有可比的期望会把原因写进证据 `note.txt`）。

`--check` 的 stdout 还带 `root`；它读同一份配置（缺 `atest.json` 或 `instance`
为空同样退出 2），只是不执行。

## 实例与会话（会话自动建立）

- **级别**：`--instance` → `atest.json` 的 `instance` → 退出 2。
- **端点**：`.wn-instances.json` 的 `{级别: {host, port}}`。查找顺序
  `--instances` → `$WN_INSTANCES` → 从 `<dir>` 逐级向上 → `~/.wn-instances.json`；
  显式给出的文件**必须存在**（静默回退会跑错实例）。
- **探活**：TCP 连接端点，3 秒不通即退出 2；**不尝试拉起实例**（1/2 级实例都由
  开发者手工管理）。
- **会话**：工作区池里的 `<级别>-<测试域>-00`，决定见下：

  | 场景 | 行为 |
  |---|---|
  | 会话文件存在 | 直接复用；核对 `base` 与本次端点一致（不一致拒绝复用，防 L1/L2 混跑）。**不看凭据文件**——手工登录过的项目照常跑 |
  | 会话文件不存在 + 有凭据 | 自动登录一次后跑（`session_created: true`） |
  | 会话文件不存在 + 没凭据 | 退出 2，错误信息同时给两条路：建凭据文件（附 JSON 形状）或先手工 `wn-cli … login` 一次 |
  | 执行中 401（某例 `wn-cli` 退出码 3） | 自动**重新登录一次并重跑该例**；仍失败才记该例 **failed**（不是 blocked） |

  - 凭据来源：`--creds` → `atest.json` 的 `credentials_file` →
    `.at/credentials.json`，形状 `{"usr":"…","passwd":"…"}`；**该文件不进 git**。
  - 登录失败（启动时）→ 退出 2，错误信息带上 `wn-cli` 的原始输出；执行中重登失败
    → 该例 failed，原因同样带原始输出，且本批不再反复重登。
  - 401 重跑时首次输出留 `evidence/stdout.attempt1.txt` /
    `stderr.attempt1.txt`，外加 `evidence/relogin.txt` 说明；`$t0` / `$t1`
    重置为真正产出数据的那一次。
  - `--check` **不建会话、不读凭据**（只看格式，不碰实例）。
- **`session_created`**：stdout 摘要里的 `true` = 本次新建了会话（自动登录），
  `false` = 复用已有会话——人一眼能确认工具没有悄悄换身份。

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
环境不可用（记 **blocked**，不算红；即使没来得及输出结果 JSON，也按 blocked，
例如 `atlib.fixture()` / `snap()` 的 `_die`）；其它非 0 或超时 = **failed**。

脚本里取机器可读结果请用 `o ... @json -cqn`（裸 `@json` 是 HOCON 风格，不是严格
JSON；`-cqn` 才是紧凑标准 JSON）。

## 夹具与清理（ATESET-05）

静态布局**不写进用例命令**：`<root>/atest.init` 声明"世界该有什么"，
用例脚本调 `atlib.fixture()`，由 Walnut 自己的 `app init` 铺进域；变量在
`<root>/vars.json`。动态数据（每例新增的业务记录）仍写脚本。

> **由 `app init` 执行**：`fixture()` 把模板上传到 `~/.atest/init/<name>.init`，
> 再跑 `app init -conf ~/.atest/init/<name>.init -script off`（有变量时加
> `-vars '<json>'`）。模板里可以使用 `app init` 的**全部指令**：
> `@DIR` / `@FILE` / `@API` / `@HOME` / `@ENV` / `@INCLUDE` 与
> `%TMPL` / `%COPY` / `%EXPLAIN`。atlib 不再维护自己的模板子集。
>
> `-script` 默认 **off**：脚本测试要收尾脚本时自己在用例脚本里调
> （收尾脚本名 / 内容由模板作者约定）。

```text
<root>/atest.init                 静态布局模板（app init 语法）
<root>/vars.json                  可选；对象，项目级变量（缺省 {}）
<root>/cleanup.py                 可选；套件级清理（at-run --reset 时先跑）
<root>/scripts/<CASE-ID>.clean.py 可选；用例级清理（该例执行前跑）
```

> `at-init` 目前只复制 `atest.init`；需要变量时把
> `bin/at-init/tmpl/vars.json` 复制成 `<root>/vars.json` 再改（模板里的变量都有
> 默认值，不复制也能跑）。

### `atlib.fixture()`

```python
from atlib import fixture, snap, emit

fixture()                       # 默认读 atest.json 的 root / init_template
obj = snap("~/.atest/demo")     # 模板铺好的对象
emit({"atest-owner": obj.get("atest-owner")})
```

`fixture(root=None, name=None, vars=None, timeout=90)`：

- 读 `<root>/atest.json` 的 `root` / `init_template`，定位模板与
  `<root>/vars.json`（JSON 对象，缺省 `{}`）；`vars` 是调用方默认值，
  项目 vars.json 覆盖同名键；
- 把模板 `obj save-text` 到 `~/.atest/init/<name>.init`（覆盖写；`name`
  缺省为模板文件名）；有变量时把合并后的 JSON `obj save-text` 到
  `~/.atest/vars.json`，并作为 `-vars` 传给 `app init`；
- 执行 `app init -conf ~/.atest/init/<name>.init -script off`——模板支持
  `@DIR` / `@FILE` / `@API` / `@HOME` / `@ENV` / `@INCLUDE` 与
  `%TMPL` / `%COPY` / `%EXPLAIN`（`${var}` / `${var?默认值}`、
  `%COPY >` 外部文件引用等 `app init` 语义都可用）；
- **幂等**由 `app init` 自身保证：目标文件已存在且内容非空时跳过写入，
  目录/元数据按模板合并；同一模板连跑两次安全、对象不重复；
- 失败（模板缺失 / vars.json 非法 / 上传失败 / `app init` 非 0）→
  `SystemExit(2)`，runner 记 **blocked**，原因与 `app init` 的原始输出进
  `summary.txt` 与 stderr；
- 返回 `{"name", "template", "vars", "entries", "counts"}`（后两项由
  `app init` 输出统计，供日志参考）。

### 清理约定

| 脚本 | 何时跑 | 用途 |
|---|---|---|
| `<root>/cleanup.py` | `at-run --reset` 时、整批用例之前 | 把套件共用的脏数据清掉 |
| `<root>/scripts/<CASE-ID>.clean.py` | 该用例执行前 | 非幂等用例的定点清理 |

- 两个脚本都用 `atlib`（`wn()` 直接可用；清理脚本也可 `note()` 留证）；
- **不许删域、不许删数据目录**：只删自己造的对象 / 清表，禁止 `o ~ @delete -r`、
  删 `/home/<域>` 或 `/app` 这类动作；
- 没有清理脚本视为"该用例幂等"，直接跑；
- 清理失败 → 该例 **blocked**（不判红），证据进该例 `clean/summary.txt`；
  `--reset` 的套件清理失败 → 全批 **blocked**，证据在 `<run>/cases/_reset/`；
- 执行顺序固定：**清理 → 初始化（fixture）→ 用例**；`$t0` 在清理成功后才取。

### `--check` 的夹具警告（不阻塞）

`--check` 会检查 `<root>/atest.init`（或 `atest.json` 的 `init_template`），
问题只进 `warnings`、不影响 `ok` / 退出码。其中 `init-template-external-ref` /
`init-template-include` 是**审阅提示**：`app init` 本身支持这些写法，只是
外部引用越多、模板越难审。

| 警告码 | 含义 |
|---|---|
| `init-template-missing` | 模板缺失（`fixture()` 运行时会 blocked） |
| `init-template-not-file` | 路径不是文件 |
| `init-template-unreadable` | 读不了（编码 / 权限 / IO） |
| `init-template-too-large` | 超过 256 KiB（太大难审；大内容先上传再引用） |
| `init-template-external-ref` | `%COPY >` / `%TMPL >` / `%EXPLAIN >` 等外部文件引用 |
| `init-template-include` | `@INCLUDE`（也是外部文件引用） |

### `--check` 的基线警告（不阻塞）

`--check` 对每个 `expected: baseline` 的用例校验基线**存在 + 来源哈希**，
问题只进 `warnings`、不影响 `ok` / 退出码：

| 警告码 | 含义 |
|---|---|
| `baseline-missing` | 基线还没建（该例执行时记 blocked；建法见 `bin/at-baseline/README.md`） |
| `baseline-invalid` | 基线读不了 / 不是合法 JSON / 顶层不是对象 |
| `baseline-no-hash` | 基线缺 `meta.source_hash`（手写的裸对象基线）：改了用例也发现不了 |
| `baseline-stale` | 用例 md / 夹具模板已改，基线待复核（哈希对不上） |
| `baseline-hash-skipped` | 夹具模板缺失/不可读，算不了哈希（另见 `init-template-*` 警告） |

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
| `http(path, method="GET", headers=None, data=None, timeout=30)` | 用标准库 `urllib` 真打 `AT_URL` 上的接口，返回 `(status, headers, body_text)`；4xx/5xx **不抛**、**不跟随重定向**；连不上 → `SystemExit(2)`（记 blocked） |
| `fixture(root=None, name=None, vars=None)` | 上传模板 / 变量，调 `app init -conf ... -script off` 铺进域；失败即抛（记 blocked） |
| `snap(path, name=None)` | `o <path> @json -cqn` → dict；落 `<AT_OUT>/objects/<路径>.json`（`/` 换 `__`）；取不到即抛 |
| `emit(actual)` | 打印 `{"ok":true,"actual":...}` 后退出 0——**之后不要再往 stdout 写任何东西** |
| `note(text)` | 追加一行 `[script] …` 到 `summary.txt`；runner 重写 summary 时原样保留 |

atlib 只依赖标准库与 `AT_*` 环境变量；失败一律 `SystemExit(2)` 且原因进 summary，
所以脚本里不用再包 try/except。手工跑脚本时自己设
`PYTHONPATH=<walnut>/bin/at-run` 并补齐 `AT_*`（正常路径永远是经 at-run）。

### `atlib.http()`：真 HTTP 通道（ATESET-09）

夹具里用 `@API` 声明接口对象（`.regapi/api/<name>`，一个文件一个接口），用例脚本用
真 HTTP 调 `<AT_URL>/api/<测试域>/<name>`——**不绕命令通道**，测的是对外契约：

```python
from atlib import fixture, http, emit, note

fixture()
st, hdrs, body = http("/api/demo/ping")      # 302/403 也照常返回
emit({"status": st, "ctype": hdrs.get("Content-Type", "")})
```

- 返回 `(status, headers, body_text)`：`headers` 是 dict，`get()` **大小写不敏感**
  （HTTP 头名本来就不区分大小写）；body 按响应 `charset` 解码，缺省 UTF-8；
- **不跟随重定向**：`http-resp-code: 302` 的接口原样返回 302，`Location` 在 headers 里
  ——跟过去就测不到跳转本身了；
- **4xx/5xx 不抛**：`api-access-token` 的 403 是被测行为，不是脚本失败；
- **连不上 → `SystemExit(2)`**（runner 记 **blocked**）：实例挂了与"接口返回错误码"
  是两件事，不允许混成红灯或绿灯；
- 每次调用的原始响应落 `<AT_OUT>/http/NN-<方法>-<URL>.txt`
  （状态行 + 全部响应头 + body），进该例证据清单，供人工复核；
- `data` 传 dict 走 url-encoded 表单，传 str / bytes 原样发送，None 表示无请求体；
- 断言仍不在这里做：脚本只把可观察值 `emit()` 出去，判定交给 runner 按 `## 期望`。

## 判定与证据

- **只比 expected 里出现的键**（子集匹配）；数组按顺序、等长逐项比，diff 路径
  形如 `items[1].nm`；
- 忽略 `id` / `pid` / `ct` / `lm` / `sha1`（任意深度）与以 `_` 开头的键；
  显式写了 matcher 的键以 matcher 为准；
- 失败时报告带 `diff: [{path, expected, actual}]`（`actual: null` 表示缺键）；
  matcher 失败时 `expected` 里是**代入后**的值（如 `{"$range": [t0, t1]}` 的毫秒数）；
- 每例证据目录 `<root>/report/<timestamp>/cases/<CASE-ID>/`：
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

文件格式（`meta` 是溯源信息，**runner 只读 `expected`**）：

```json
{
  "meta": {"case": "CASE-101", "source_hash": "c98a92e31e265e2d",
           "instance": "L1", "recorded_at": "2026-11-13T15:09:08+08:00",
           "reviewer": "unreviewed", "dropped": ["pid"], "matcherized": []},
  "expected": {"nm": "case101-dir", "race": "DIR"}
}
```

- 基线建立后要能**反复执行**：同一份基线在 L1 / L2、在别人的机器上都应通过。
  不同实例结果不同，本身就是用例或产品的问题，**不用"两套基线"消化**；
- 所以基线只收**与环境无关**的值：`id` / `pid` / `sha1` / `ph` / `mnt` / `ln` /
  `c` / `m` / `g` / `d0` / `d1` 与含绝对路径的值一律不进基线（由 `at-baseline`
  清洗，并记进 `meta.dropped`）；
- 时间类字段 `ct` / `lm` / `expi` **不丢也不写死**：`at-baseline` 把数值转成
  `{"$range": ["$t0", "$t1"]}`（记进 `meta.matcherized`），"运行期被写入过"仍可断言；
- **基线里的 `expected` 与用例 md 内联期望使用同一套 matcher**（比较器只看
  `expected` 这个结构）：`$range` / `$regex` / `$ignore` 在基线里同样生效；
- 建法：`--record` 录 actual → `bin/at-baseline --from <actual.json>` 清洗落盘；
  已有基线缺 `--force` 不覆盖（防误刷），`--audit` 比对来源哈希报"待复核"；
- 缺基线仍是 **blocked**（不算红），提示里给完整路径；本工具不建/不改基线；
- `--check` 不要求基线存在，但对存在性/哈希给 warning（见上表）；期望声明与内容
  矛盾仍报 `expected-mismatch`。

## 报告

```text
<root>/report/<timestamp>/report.json   # 本次报告（JSON 是唯一源）
<root>/report/<timestamp>/report.md     # at-report 生成的人读视图
<root>/report/<timestamp>/actual.json   # 仅 --record：{CASE-ID: actual} + _meta
<root>/report/<timestamp>/cases/<CASE-ID>/  # 每例证据目录
<root>/report/latest.json               # 同内容完整副本（覆盖写，不是指针）
<root>/report/latest.md                 # at-report 刷新的最近视图
<root>/report/archive/<timestamp>.zip   # 旧报告（at-gc 归档；保留最近 report_keep 次）
```

```json
{"schema":"atest-report/2","timestamp":"2026-11-13T12:22:24+08:00",
 "instance":"L1","instance_source":"atest.json","reset":true,"cleanup":"ok",
 "total":2,"passed":1,"failed":1,"blocked":0,
 "cases":[{"caseID":"CASE-101","status":"passed","duration_ms":391,
           "evidence_dir":"atest/report/20261113T172335/cases/CASE-101","diff":[],
           "evidence":["clock.txt","cmd.txt","env.txt","objects/…",
                       "result.json","stderr.txt","stdout.txt","summary.txt"]},
          {"caseID":"CASE-104","status":"failed","duration_ms":855,
           "evidence_dir":"atest/report/20261113T172335/cases/CASE-104",
           "diff":[{"path":"items[1].nm","expected":"atest-b","actual":"atest-c"}],
           "evidence":["clock.txt","cmd.txt","env.txt","note.txt","objects/…",
                       "result.json","stderr.txt","stdout.txt","summary.txt"]}]}
```

- `schema` 是 v2 新增的顶层键（`"atest-report/2"`）；03 的顶层键与每例 5 个键
  **保持不变**（用例格式 v2 去掉 `suite`，报告也不再按 suite 分目录）；
  ATESET-05 追加 `reset` / `cleanup`（`cleanup` = `skipped` / `missing` / `ok` / `failed`）；
- 每例多出 `clean/`（用例级清理证据）与 `cleanup.txt`（套件清理失败时的整批证据）；
- `evidence` 是相对 `evidence_dir` 的文件清单（正斜杠、排序稳定），指向真实文件；
- `status` 只有 `passed` / `failed` / `blocked`。
- **报告内路径一律相对项目根 + `/`**：`evidence_dir` 形如
  `atest/report/<ts>/cases/CASE-101`，`instance_source` 为 `cli`（`--instance`
  生效）或相对路径 `atest.json`——报告是可搬运的产物，不该出现盘符与反斜杠。
- **stdout 摘要的 `report` / `latest` / `actual` 是绝对路径**（给人复制粘贴），
  与报告内的相对路径**有意不同**；别把它当 bug。`<root>/report/` 已在项目
  `.gitignore` 里（`at-init` 追加），报告不进库。
- 写完后自动调用 `bin/at-gc` 归档旧报告：保留最近 `report_keep` 次（缺省 3），
  更早的压成 `archive/<timestamp>.zip`（校验通过才删原件）。本次报告与
  `latest.*` 永不归档；`--no-gc` 关闭；归档失败只在 stderr 警告，退出码不变。

## `--check` 的错误码

| 错误码 | 含义 |
|---|---|
| `filename-mismatch` | 文件名（去 `.md`）≠ `caseID` |
| `caseid-invalid` | 不匹配 `^CASE-[A-Z0-9-]{1,32}$` |
| `missing-field` | 缺必填（`caseID` / `title` / `source` / `contracts` / `expected`）；或 `contracts` 不是非空数组 |
| `removed-field` | 出现已移除的 `instance` / `suite`（实例是运行期信息；用 `--only` 选批） |
| `expected-invalid` | `expected` 不是 `inline` / `file` / `baseline`；或期望值里的 matcher 不合法（不认识 / 混用 / 类型不对 / 正则编译失败 / 放在根上） |
| `contract-too-generic` | `contracts` 某项只写命令族（`o` / `jsc` 等），不具体；要子命令 / `/` 端点 / `.js` 脚本 |
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
- 不做全量快照（只存脚本显式 `snap()` 的路径）；夹具由 `app init` 执行
  （`-conf` + `-vars`，`-script off`）；不挂 `mnt`；不写 Markdown 报告（07）；
  不建基线（`--record` 只录 actual，清洗/落盘归 `bin/at-baseline`）；**不并发**执行；
- 夹具模板的解析/渲染全部交给 `app init`，atlib 只负责上传与调用；
  `-script` 默认 off（收尾脚本由用例自己调）；
- 清理脚本的约束是**约定**，工具不做静态检查：不许删域 / 删数据目录，只删自己造的对象；
- `--reset` 的套件清理失败把全批记 blocked（退出码仍 0，按"blocked 不算红"）；
  每例的 `clean/` 证据会进报告 `evidence` 清单；
- 清理脚本与用例脚本共用一个 `--timeout`；超时只杀直接子进程；
- 脚本固定 `<CASE-ID>.py`（任务书的契约）；其它语言留给后续任务；
- 超时只杀直接子进程（脚本自己起的 `wn-cli` 子进程不保证一起终止）；
- 用例格式 v2 不再有 `instance` / `suite`；`instance` 是运行期信息，
  由 `atest.json` / `--instance` 指定，`suite` 用 `--only` 代替；
- 全 blocked 的运行退出码仍是 `0`（按"blocked 不算红"的约定）；报告里
  `passed=0` / `blocked=n` 与 stderr 摘要是唯一警示；
- 会话透明化：会话缺失 → 按需自动登录（会话存在时连凭据文件都不读）；执行中 401
  只重登**一次**并重跑该例，仍失败记 **failed**（不是 blocked）；重登失败的原因在
  本批内记住，不会每个用例都反复试错；
- 子进程注入 `PYTHONUTF8=1` / `PYTHONIOENCODING=utf-8`（Windows 控制台 cp936 会把
  中文证据拆坏）、`PYTHONDONTWRITEBYTECODE=1`（脚本 import `atlib` 不往仓库里落
  `__pycache__`）与 `PYTHONPATH`（指向 `bin/at-run`，供 `import atlib`）；
  不新增 `AT_*` 契约变量。
