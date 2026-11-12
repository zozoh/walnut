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
| `at-init` | 项目 `test/` 脚手架：ATEST 骨架、`ATEST.md` 标记段与实例登记样例（只管 `test/`，不管 `.at/`） |

行为契约（参数/退出码/输出格式）见
`{WN_DOC_HOME}/reference/102.auto-test/30-at-tools.md`（`{WN_DOC_HOME}` = 工作区同级 `../wn-doc/`）；
操作套路（先查命令表、纯用命令、找不到就问）见
`{WN_DOC_HOME}/skill/use-wn-cli.skill.md`。
