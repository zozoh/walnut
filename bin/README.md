# bin/ — 智能体自动化测试的公共工具集

这里存放**所有智能体共享**的 Walnut 测试工具。每个工具一个目录
（`<工具名>/<工具名>.py` + 同目录 `README.md`），纯标准库 Python 3、
JSON 输出、退出码有语义。直接用仓库里的同一份，**不要复制到自己的
工作目录**；项目私有的临时脚本放工作目录 `.at/_ottl/`，打磨成熟后
再晋升回这里。

| 工具 | 一句话职责 |
|------|-----------|
| `wn-ping` | 探活：`GET /a/login_mode`，判定实例存活与 `agent_ready` |
| `wn-up` | 启动或复用实例：探活 → 复用/拉起 → 等就绪 → 可选 init-node（级别 2 由开发者执行，智能体仅用于级别 3） |
| `wn-down` | 温柔停止实例：核对运行锁归属，连带关闭私有 redis |
| `wn-cli` | 主力客户端：`login / cmd / obj / mnt / check-node / init-node` |
| `walnut-at-init` | 工作目录 `.at/` 脚手架（懒惰模式，绝不重置已有积累） |
| `at-init` | 项目 `atest/` 脚手架：ATEST 骨架、`atest.json` 配置与实例登记样例（只管 `atest/`，不管 `.at/`） |
| `at-run` | 运行器：校验用例 → 解析实例/会话 → 逐例执行 → JSON 报告；`--record` 录 actual 供建基线 |
| `at-baseline` | 基线：`--from` 把 actual 清洗成可移植基线（来源哈希 + `meta.dropped` 留痕），`--audit` 查"用例改了、基线没重审" |
| `at-report` | 报告视图：JSON → Markdown（总览 / 用例表格 / 红灯 diff 与证据 / 趋势），并刷新 `latest.md` |
| `at-gc` | 报告归档：保留最近 N 次（`report_keep`，默认 3），更早的压成 `report/archive/<ts>.zip`，校验通过才删原件；`at-run` 跑完自动调用 |
| `at-doctor` | 环境自检：Python / atest.json / 用例 / 实例登记 / 可达 / 会话 / 工具 / 报告目录共 8 项，一项命令判断"这台机器能不能跑" |

行为契约（参数/退出码/输出格式）见
`{WN_DOC_HOME}/reference/102.auto-test/30-at-tools.md`（`{WN_DOC_HOME}` = 工作区同级 `../wn-doc/`）；
操作套路（先查命令表、纯用命令、找不到就问）见
`{WN_DOC_HOME}/skill/use-wn-cli.skill.md`。
