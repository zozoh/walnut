# at-init — ATEST 项目骨架脚手架

在项目里生成**运行时集成测试（ATEST）**的 `test/` 骨架、`ATEST.md` 的标记段
与实例登记样例。一条命令、可重复执行、不覆盖已有内容。

与 [`walnut-at-init`](../walnut-at-init/) 分工：后者管 `.at/`（身份 / 会话 /
私有配置），**本工具只管 `test/` 与 `ATEST.md` 的标记段**；检测到 `.at/`
缺失时只提示，不代建。

## 用法

```bash
# 当前目录（项目名取目录名）
python bin/at-init/at-init.py

# 指定目录与项目名
python bin/at-init/at-init.py --dir D:/work/myproj --name myproj

# 只算不写（先看要生成什么）
python bin/at-init/at-init.py --dry-run

# 模板升级后刷新样例段（不动用户内容）
python bin/at-init/at-init.py --force
```

| 参数 | 说明 |
|------|------|
| `--dir DIR` | 目标项目目录，默认当前目录 |
| `--name NAME` | 项目名，写进 `ATEST.md` 的 `project:` 段；默认取目录名 |
| `--force` | 刷新**样例文案**（`ATEST.md` 标记段、`.wn-instances.example.json`）；标记段里你设定的字段值**保留**（见下） |
| `--dry-run` | 只输出计划，不写任何文件 |

退出码：`0` 成功（含"全部 kept"）；`1` 用法错误（参数 / 项目名不合法）；
`2` 环境问题（目标目录不存在、模板缺失、写入失败、`ATEST.md` 标记段损坏）。

stdout 是**单行 JSON**（适合机器解析，提示进 stderr）：

```json
{"ok":true,"dry_run":false,"dir":"...","project":"myproj","created":[...],"appended":[...],"refreshed":[...],"kept":[...],"preserved":["instance"],"warnings":[...]}
```

`preserved` 只在 `--force` 真刷新标记段时出现，列出被保留下来的用户字段。

## 生成物

```text
<项目>/
|-- .at/                       # 归 walnut-at-init（本工具只提示，不创建）
|-- ATEST.md                   # 集成测试标记段（instance / 测试域 / ...）
|-- .wn-instances.example.json # 实例登记样例（真身由开发者维护）
`-- test/
    |-- atest.init             # 静态布局模板（夹具；05 消费）
    |-- cases/                 # 用例 <CASE-ID>.md
    |-- scripts/               # 脚本 <CASE-ID>.<ext>
    |-- expect/                # 独立期望文件 <CASE-ID>.json（expected: file）
    |-- baseline/              # 基线 <CASE-ID>.json
    `-- report/                # 报告输出（gitignore）
```

进库：`ATEST.md`、`test/atest.init`、`test/cases/`、`test/scripts/`、`test/expect/`、
`test/baseline/`、`.wn-instances.example.json`。
不进库：`test/report/`、`.wn-instances.json`（本机配置）——两条都已追加进
项目 `.gitignore`（带 `# at-init:` 标记，重复执行不重复追加）。

> 生成**只创建空目录与占位文件**，不往 `cases/` / `scripts/` / `expect/` 里塞示例用例：
> 用例格式由 ATESET-02 定稿，模板在 [`tmpl/`](tmpl/) 里照抄改名即可。
> `test/atest.init` 是项目的夹具文件，生成一次后由人编辑，`--force` 不碰它。

## 幂等与 `--force`

| 文件 | 首次运行 | 再次运行 | `--force` |
|------|----------|----------|-----------|
| `test/` 各级目录 | `created` | `kept` | 同左 |
| `test/atest.init` | `created` | `kept` | **不覆盖** |
| `.gitkeep` 占位 | `created` | `kept` | 同左 |
| `ATEST.md` 标记段 | `created` / `appended` | `kept` | 段内**刷新说明文字**、补齐缺失字段；**用户设定的字段值保留**（见下） |
| `.wn-instances.example.json` | `created` | `kept` | 内容有差异时**刷新** |
| `.gitignore` 的 `# at-init:` 块 | `created` / `appended` | `kept` | 同左 |

`cases/`、`scripts/`、`expect/`、`baseline/` 里的用户内容**在任何情况下都不动**。

`ATEST.md` 已存在但没有标记段时，追加段（`appended`）而不是重写文件。

`--force` **保留**这些字段已写在文件里的值（空值视为缺失，用模板默认值补上）：

`project`（未给 `--name` 时）· `instance` · `test_domain` · `credentials_file` · `sql_datasource`

即：你把 `instance` 改成 `L1` 做快速回路，之后跑 `--force` 仍然是 `L1`——不会被
样例默认的 `L2` 静默改回去。标记段里**其它**人工改动（说明文字、注释、额外行）
会被模板刷掉，那些请写在段外。段内容与"模板 + 你的字段值"一致时不报差异；
不一致且没加 `--force` 时保持 `kept` 并在 `warnings` 里提示。

## ATEST.md 的字段

`<!-- at-init:begin -->` ~ `<!-- at-init:end -->` 之间是工具维护段，字段：

| 字段 | 含义 |
|------|------|
| `project` | 项目名（`--name`，缺省目录名） |
| `instance` | `L1` / `L2`：跑哪一级实例（端口在 `.wn-instances.json` 里，**不写这里**） |
| `test_domain` | 本项目专用测试域（只增不减，跨用例复用） |
| `credentials_file` | 凭据文件位置（gitignore） |
| `init_template` | 静态布局模板，默认 `test/atest.init`（必填） |
| `sql_datasource` | 域内 `.dao` 配置名，数据操作走 `sqlx` |

生成时 `instance` 默认 `L2`（完整回归与基线在 2 级实例上做）；只在 1 级
快速回路时改成 `L1`。

## 实例登记

`.wn-instances.example.json` 只是**样例**，本工具**不写**真正的
`.wn-instances.json`：它属于开发者的机器级配置，由开发者放到工作区根或用户目录。
解析顺序与 `wn-cli` 一致：`--instances` → `$WN_INSTANCES` → 从当前目录逐级向上 →
`~/.wn-instances.json`。详见 ATESET 计划 DRAFT §3.5。

## 模板

| 模板（[`tmpl/`](tmpl/)） | 用途 |
|--------------------------|------|
| `ATEST.md` | 手册骨架 + 工具维护段（`{{project}}` 占位） |
| `atest.init` | 静态布局模板骨架（注释示例，由 05 定稿语法） |
| `CASE-001.md` | 用例模板（本身即合法用例，照抄改名即可）：front-matter 5 个必填（`caseID` / `title` / `source` / `contract` / `expected`）+ 正文四节（前置 / 步骤 / 期望 / 证据） |
| `CASE-001-bad.md` | 反例：7 类常见错误逐条标注"为什么错"；只当教材，**别复制进 `test/cases/`** |
| `CASE-001.py` | 脚本骨架：契约未定稿前**退出 2 并说明未实现**，不假装通过 |
| `wn-instances.example.json` | 生成到项目根的 `.wn-instances.example.json` |

## 已知边界

- 编码：生成物一律 UTF-8 无 BOM；新文件用 LF，改写已有文件时沿用其原换行
  （Windows 上 `core.autocrlf=true` 检出成 CRLF 也不会让标记段比较失真）；
- 只解析标记段里的 `- key: value` 行来拿用户值；`--force` 保留上表那几个字段的
  值，其余人工改动（说明文字、注释、额外行）会被模板刷掉，请改在段外；
- 不校验 `instance` 是否在 `.wn-instances.json` 里登记（那是 `at-run` 的事）；
- 旧项目里由 `walnut-at-init` 生成的 `ATEST.md`（`walnut_url` / `walnut_level` /
  `walnut_dist`）与新的两级实例模型并存：本工具只追加自己的段，不改旧段，
  旧字段的清理等文档任务（ATESET-11/13）统一处理。
