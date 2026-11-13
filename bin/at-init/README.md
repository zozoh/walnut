# at-init — ATEST 项目骨架脚手架

在项目里生成**运行时集成测试（ATEST）**的 `atest/` 骨架、`atest.json` 配置与
实例登记样例。一条命令、可重复执行、不覆盖已有内容。

根目录默认 `atest`（`--root` 可改）。**为什么不叫 `test/`**：项目自己的测试框架
可能已经占用它（Maven 的 `testSourceDirectory`、Gradle 的 `src/test` 等），
集成测试另起一个根。

**配置只有 `atest.json` 一份**（不再生成 `ATEST.md`）：`project` / `root` /
`instance` / `test_domain` / `credentials_file` / `init_template` /
`sql_datasource` 都在里面——智能体读它，人也能直接看和改。

与 [`walnut-at-init`](../walnut-at-init/) 分工：后者管 `.at/`（身份 / 会话 /
私有配置），**本工具只管 `<root>/` 与 `atest.json`**；检测到 `.at/`
缺失时只提示，不代建。

## 用法

```bash
# 当前目录（项目名取目录名，根目录 atest）
python bin/at-init/at-init.py

# 指定目录与项目名
python bin/at-init/at-init.py --dir D:/work/myproj --name myproj

# 集成测试根目录改名（目录与 atest.json 的 root 一起变）
python bin/at-init/at-init.py --root spec

# 只算不写（先看要生成什么）
python bin/at-init/at-init.py --dry-run

# 刷新实例登记样例（不动用户内容）
python bin/at-init/at-init.py --force
```

| 参数 | 说明 |
|------|------|
| `--dir DIR` | 目标项目目录，默认当前目录 |
| `--name NAME` | 项目名，写进 `atest.json` 的 `project`；默认取目录名 |
| `--root NAME` | 集成测试根目录名，默认 `atest`；须匹配 `^[A-Za-z0-9._-]+$` 且不是 `.` / `..`，非法退出码 `1` |
| `--force` | 只刷新 `.wn-instances.example.json`（内容有差异时）；`atest.json` 只补缺失键，不覆盖已有值 |
| `--dry-run` | 只输出计划，不写任何文件 |

退出码：`0` 成功（含"全部 kept"）；`1` 用法错误（参数 / 项目名 / 根目录名不合法）；
`2` 环境问题（目标目录不存在、模板缺失、写入失败、`atest.json` 不是合法 JSON）。

stdout 是**单行 JSON**（适合机器解析，提示进 stderr）：

```json
{"ok":true,"dry_run":false,"dir":"...","project":"myproj","root":"atest","created":[...],"appended":[...],"refreshed":[...],"kept":[...],"warnings":[...]}
```

## 生成物

```text
<项目>/
|-- .at/                       # 归 walnut-at-init（本工具只提示，不创建）
|-- atest.json                 # 唯一配置（机器可读，人也能看）
|-- .wn-instances.example.json # 实例登记样例（真身由开发者维护）
`-- atest/                     # 根目录名由 --root 决定
    |-- atest.init             # 静态布局模板（夹具；05 消费）
    |-- cases/                 # 用例 <CASE-ID>.md
    |-- scripts/               # 脚本 <CASE-ID>.<ext>
    |-- expect/                # 独立期望文件 <CASE-ID>.json（expected: file）
    |-- baseline/              # 基线 <CASE-ID>.json
    `-- report/                # 报告输出（gitignore）
```

进库：`atest.json`、`atest/atest.init`、`atest/cases/`、`atest/scripts/`、
`atest/expect/`、`atest/baseline/`、`.wn-instances.example.json`。
不进库：`atest/report/`、`.wn-instances.json`（本机配置）——两条都已追加进
项目 `.gitignore`（带 `# at-init:` 标记，重复执行不重复追加）。
**迁移**：已有 `.gitignore` 里的旧行 `/test/report/` 会被替换成 `/<root>/report/`，
其余行不动（`refreshed` + `warnings` 里会说明）。

> 生成**只创建空目录与占位文件**，不往 `cases/` / `scripts/` / `expect/` 里塞示例用例：
> 用例格式由 ATESET-02 定稿，模板在 [`tmpl/`](tmpl/) 里照抄改名即可。
> `atest/atest.init` 是项目的夹具文件，生成一次后由人编辑，`--force` 不碰它。

## atest.json（唯一配置）

```json
{
  "version": 1,
  "project": "myproj",
  "root": "atest",
  "instance": "L2",
  "test_domain": "atest",
  "credentials_file": ".at/credentials.json",
  "init_template": "atest/atest.init",
  "sql_datasource": "default"
}
```

| 键 | 必填 | 默认 | 含义 |
|----|------|------|------|
| `version` | 是 | `1` | 配置格式版本 |
| `root` | 是 | `atest` | 集成测试根目录（`--root` 同步） |
| `project` | 否 | 目录名 / `--name` | 项目名 |
| `instance` | 否 | `L2` | 跑哪一级实例：`L1` 快速回路 / `L2` 完整回归与基线；端口在 `.wn-instances.json`，**不写这里** |
| `test_domain` | 否 | `atest` | 本项目专用测试域（只增不减，跨用例复用） |
| `credentials_file` | 否 | `.at/credentials.json` | 凭据文件位置（gitignore） |
| `init_template` | 否 | `<root>/atest.init` | 静态布局模板（必填语义，缺省已填好） |
| `sql_datasource` | 否 | `default` | 域内 `.dao` 配置名，数据操作走 `sqlx` |

- 首次运行按上表写全；已存在时**只补缺失的键**，不覆盖你已写的值
  （`--force` 同样不碰）；
- `instance` 为空时 `at-run` 会退出 2；`root` 与 `--root` 不一致时
  `warnings` 会提示（仍不覆盖）；
- 文件不是合法 JSON、或顶层不是对象时退出码 `2`，不猜、不改。

## 幂等与 `--force`

| 文件 | 首次运行 | 再次运行 | `--force` |
|------|----------|----------|-----------|
| `<root>/` 各级目录 | `created` | `kept` | 同左 |
| `<root>/atest.init` | `created` | `kept` | **不覆盖** |
| `.gitkeep` 占位 | `created` | `kept` | 同左 |
| `atest.json` | `created` | `kept` | 只补缺键，**不覆盖已有值** |
| `.wn-instances.example.json` | `created` | `kept` | 内容有差异时**刷新** |
| `.gitignore` 的 `# at-init:` 块 | `created` / `appended` | `kept`（旧的 `/test/report/` 会迁移） | 同左 |

`cases/`、`scripts/`、`expect/`、`baseline/` 里的用户内容**在任何情况下都不动**。

## 实例登记

`.wn-instances.example.json` 只是**样例**，本工具**不写**真正的
`.wn-instances.json`：它属于开发者的机器级配置，由开发者放到工作区根或用户目录。
解析顺序与 `wn-cli` 一致：`--instances` → `$WN_INSTANCES` → 从当前目录逐级向上 →
`~/.wn-instances.json`。详见 ATESET 计划 DRAFT §3.5。

## 模板

| 模板（[`tmpl/`](tmpl/)） | 用途 |
|--------------------------|------|
| `atest.init` | 静态布局模板骨架（`{{root}}` 占位；注释示例，由 05 定稿语法） |
| `CASE-001.md` | 用例模板（本身即合法用例，照抄改名即可）：front-matter 5 个必填（`caseID` / `title` / `source` / `contract` / `expected`）+ 正文四节（前置 / 步骤 / 期望 / 证据） |
| `CASE-001-bad.md` | 反例：7 类常见错误逐条标注"为什么错"；只当教材，**别复制进 `atest/cases/`** |
| `CASE-001.py` | 脚本骨架：契约未定稿前**退出 2 并说明未实现**，不假装通过 |
| `wn-instances.example.json` | 生成到项目根的 `.wn-instances.example.json` |

`atest.init` 里的 `{{root}}` 在生成时替换成实际根目录；其余模板（`CASE-001*.md`、
`CASE-001.py`）默认按 `atest/` 写路径，改根目录后照抄时把开头换成你的根目录名即可。

## 已知边界

- 编码：生成物一律 UTF-8 无 BOM；新文件用 LF，改写已有文件时沿用其原换行
  （Windows 上 `core.autocrlf=true` 检出成 CRLF 也不会让配置比较失真）；
- `atest.json` 只补表里的键；其它键原样保留（JSON 会被按两空格缩进重写——
  只在确有缺键时才写盘）；
- 不校验 `instance` 是否在 `.wn-instances.json` 里登记（那是 `at-run` 的事）；
- 不生成、不读取、不修复 `ATEST.md`：它已被 `atest.json` 取代；
- `.at/` 里由 `walnut-at-init` 生成的东西不在本工具职责内。
