# wn-up — 启动或复用 Walnut 实例

探活 → 复用或启动 → 等就绪。契约见 `{WN_DOC_HOME}/reference/102.auto-test/30-at-tools.md`
（`{WN_DOC_HOME}` = 工作区同级 `../wn-doc/`）。

**归属约定**：级别 1/2 的共享实例由开发者启动；智能体发现它们不在线时
直接退出测试流程向主人报告，**不调用本工具拉起**。智能体只在级别 3
（自己的私有实例）场景使用本工具。

```bash
# 级别 2 共享实例：开发者启动（构建默认 conf）
python wn-up.py --dist D:/workspace/git/github/zozoh/walnut/dist/wn4j
# → {"ok":true,"action":"start","pid":12345,"app_port":7709,"admin_port":7710,...}
# 已存在则 → {"ok":true,"action":"reuse",...}

# 级别 3：智能体自己的私有 conf（把 .at/walnut/conf 传进去）
python wn-up.py --dist .../dist/wn4j --conf D:/work/myproj/.at/walnut/conf --port 8090

# 指定代号与运行锁目录
python wn-up.py --dist ... --who big-dad --pid-dir D:/work/myproj/.at/run
```

退出码：`0` 成功/复用；`1` 用法错误（含缺 whoami）；`2` 端口被非 Walnut
程序占用；`4` 启动失败或等待超时。

身份取自 `--who` 或 `./.at/whoami.toml`（兼容旧 `whoami.json`），经环境变量写入运行锁
`run/pid-<port>.json`。
