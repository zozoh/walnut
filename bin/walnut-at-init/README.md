# walnut-at-init — 工作目录 .at 脚手架

生成 `doc/reference/102.auto-test/40-at-project.md` 约定的测试工作目录。

```bash
# 在当前工作目录生成 .at/（--name 必填：智能体自己的代号）
python walnut-at-init.py --name big-dad

# 刷新骨架（不覆盖已有会话与 whoami.json）
python walnut-at-init.py --name big-dad --force D:/work/myproj
```

生成内容：`.at/whoami.json`、`.at/bin/`（工具包装脚本 cmd+sh）、
`.at/walnut/conf/`（级别 3 样例）、`.at/client/session/`、`.at/_ottl/`、
`ATEST.md` / `LOCAL_REF.md` 样例，并向 `.gitignore` 追加敏感条目。

退出码：`0` 成功；`1` 代号不合法；`2` `.at` 已存在且未加 `--force`。
