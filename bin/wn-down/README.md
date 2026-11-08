# wn-down — 温柔停止 Walnut 实例

先 admin-port 发 `stop`（触发 WnSetup destroy），超时后 taskkill 兜底。
契约见 `{WN_DOC_HOME}/reference/102.auto-test/30-at-setup.md`（`{WN_DOC_HOME}` = 工作区同级 `../wn-doc/`）。

```bash
python wn-down.py --dist D:/workspace/git/github/zozoh/walnut/dist/wn4j --port 8080
# → {"action":"stop","gentle":true,"pid":12345}
```

归属核对：运行锁里的 `agent`/`work_path` 必须是当前身份（`--who` 或
`.at/whoami.toml`），否则拒绝停止（退出码 2）。人类手动启动的实例
（锁里 agent 为空）不受此限。

退出码：`0` 停止成功；`1` 用法错误；`2` 拒绝停止或锁缺失。
