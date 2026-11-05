# wn-up — 启动或复用 Walnut 实例

探活 → 复用或启动 → 等就绪。契约见 `doc/reference/102.auto-test/30-at-tools.md`。

```bash
# 级别 2：独立实例 + 构建默认 conf
python wn-up.py --dist D:/workspace/git/github/zozoh/walnut/dist/wn4j
# → {"ok":true,"action":"start","pid":12345,"app_port":8080,"admin_port":8081,...}
# 已存在则 → {"ok":true,"action":"reuse",...}

# 级别 3：私有 conf（把 .at/walnut/conf 传进去）
python wn-up.py --dist .../dist/wn4j --conf D:/work/myproj/.at/walnut/conf --port 8090

# 指定代号与运行锁目录
python wn-up.py --dist ... --who big-dad --pid-dir D:/work/myproj/.at/run
```

退出码：`0` 成功/复用；`1` 用法错误（含缺 whoami）；`2` 端口被非 Walnut
程序占用；`4` 启动失败或等待超时。

身份取自 `--who` 或 `./.at/whoami.json`，经环境变量写入运行锁
`run/pid-<port>.json`。
