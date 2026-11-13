# at-doctor — ATEST 环境自检

一条命令回答"**这台机器现在能不能跑 at-run**"。8 项检查逐项给 `ok` / `warn` /
`fail`，并附一句可执行的说明（差什么、在哪、怎么补）。

```text
python bin/at-doctor/at-doctor.py [--dir <项目>] [--instance L1|L2]
```

典型用法：README / 文档看完了但跑不起来；换了一台机器；刚改过端口或会话；
先跑它，再决定去修哪一块。

## 8 项检查

| id | 检查 | 判据 | fail 通常意味着 |
|---|---|---|---|
| `python` | 解释器 | 当前 Python ≥ 3.10 | 换 `python3` 或升级 |
| `config` | `atest.json` | 存在、合法 JSON、`root` 指向的目录存在 | 先跑 `bin/at-init`，或修 `root` |
| `cases` | 用例 | `at-run --check` 的 `error_count = 0`（warn 不算失败） | 用例 md 有格式错误，按 error 码修 |
| `instances` | 实例登记 | `.wn-instances.json` 可解析，目标级别有合法 `host` + `port` | 建/补登记文件，或级别写错 |
| `reachable` | 实例可达 | `wn-ping` 返回 `alive:true` **且** `agent_ready:true` | 实例没起、端口错、登录模式不允许纯密码 |
| `session` | 会话或凭据 | 会话文件 `<级别>-<域>-00` 存在，**或**凭据文件可用（`usr` / `passwd` 非空） | 先手工登录一次，或配 `.at/credentials.json` |
| `tools` | 工具齐全 | `bin/` 下 `at-run` / `at-report` / `at-baseline` / `wn-cli` 都在 | 仓库不完整或 `bin/` 被挪动 |
| `report` | 报告目录 | `<root>/report/` 可写（缺失时尝试创建并实测写入） | 目录只读 / 磁盘满 / 权限问题 |

约束：

- 只检查，**不登录、不执行用例**；不改 `atest.json` / 实例登记 / 会话；
- 除"创建缺失的 `<root>/report/` + 写一个临时探针文件"外不落盘，探针用完即删；
- 依赖项缺失时不中断：后续项会给出"依赖 xxx 项失败"，而不是抛异常；
- `warn` 表示能跑但有隐患（例如报告目录原本缺失、由本工具创建）。

## 解析规则

- 实例登记查找顺序与 `at-run` 一致：`$WN_INSTANCES` → 从 `--dir` 逐级向上 →
  用户目录；
- 会话目录：`$WN_SESSION_DIR` → 实例登记文件所在目录的
  `.at/client/session`（工作区级）→ 项目目录；
- 凭据文件：`atest.json` 的 `credentials_file`（缺省 `.at/credentials.json`，
  相对项目目录解析）；
- 实例级别：`--instance` > `atest.json` 的 `instance`；都没有时只在依赖它的
  项上报 fail。

## 输出

stderr 逐行人读：

```text
at-doctor: python     ok   Python 3.12.3（要求 ≥ 3.10）
at-doctor: config     ok   atest.json 合法；root=atest，目录存在；instance=L1
at-doctor: cases      ok   at-run --check：10 个用例文件，0 错误
at-doctor: instances  ok   L2 = 127.0.0.1:7709（.wn-instances.json）
at-doctor: reachable  ok   http://127.0.0.1:7709 alive，agent_ready（system=PASSWD_ONLY / domain=PASSWD_ONLY）
at-doctor: session    ok   会话 L2-demo-00 存在（.at/client/session/L2-demo-00.toml；直接复用，不读凭据）
at-doctor: tools      ok   at-run / at-report / at-baseline / wn-cli 都在 ...\walnut\bin
at-doctor: report     ok   可写：atest/report
at-doctor: 8 项：8 ok / 0 warn / 0 fail —— 可以跑 at-run（exit 0）
```

stdout 最后一行单行 JSON：

```json
{"ok": true,
 "checks": [{"id": "python", "status": "ok", "detail": "Python 3.12.3（要求 ≥ 3.10）"},
            {"id": "session", "status": "fail", "detail": "会话 L2-demo-00 不存在（…），凭据 … 也不可用（文件不存在）：二者之一必须有"}],
 "failures": 1}
```

- `ok` = `failures == 0`（`warn` 不影响）；
- `failures` = `status == "fail"` 的项数；
- 每项的 `detail` 就是给人/智能体看的下一步线索。

退出码：`0` 没有 fail（warn 允许）/ `1` 有 fail / `2` 工具自身不可用
（`--dir` 不存在或不是目录）。

## 常见 fail 怎么修

| 项 | 先做什么 |
|---|---|
| `config` | 跑 `python bin/at-init/at-init.py`；`root` 要与实际目录一致 |
| `cases` | 跑 `python bin/at-run/at-run.py --check` 看错误码（`--help` 有全集） |
| `instances` | 在工作区根放 `.wn-instances.json`（形状见 `bin/at-run/README.md`） |
| `reachable` | 确认实例在跑、端口与登记一致；`wn-ping --url <url>` 单独复核 |
| `session` | 配 `.at/credentials.json`（`{"usr":"…","passwd":"…"}`，不进 git），或先手工 `wn-cli … login` 一次 |
| `report` | 检查目录权限 / 磁盘；`<root>` 不存在时先跑 `at-init` |

## 已知边界

- 只探活 `GET /a/login_mode` + 文件检查，不做真实登录、不发业务命令；
- 不校验会话 ticket 是否过期（执行中 401 由 `at-run` 自动重登）；
- 不检查 Java / Maven / 实例版本——ATEST 跑测试不依赖它们；
- 不修改任何配置：发现问题只报告，修复由人或上层流程决定。
