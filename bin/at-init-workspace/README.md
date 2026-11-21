# at-init-workspace — 工作目录 .at 脚手架

生成 `{WN_DOC_HOME}/public/atest/reference/40-at-project.md` 约定的测试工作目录
（`{WN_DOC_HOME}` = 工作区同级 `../wn-doc/`）。

```bash
# 在当前工作目录生成 .at/（客户端骨架）
python at-init-workspace.py

# 刷新骨架（不覆盖已有会话）
python at-init-workspace.py --force D:/work/myproj

# 级别 3 私有服务器（需要 build.rs.json）
python at-init-workspace.py --with-server --rs build.rs.json D:/work/myproj
```

生成内容：`.at/client/session/`、`.at/AGENTS.md`、`LOCAL_REF.md` 样例，
并向 `.gitignore` 追加敏感条目。身份（"我是谁"）由 wn-memo 管理，
不在 `.at/` 里；`--name` 仅作旧命令兼容（已弃用）。
（集成测试配置 `atest.json` 与 `atest/` 骨架由 `bin/at-init-cases/at-init-cases.py` 生成，本工具不再写 `ATEST.md`。）

> **级别 2 的数据家固定在工作区根** `.at/walnut/{buck,swap}` 与 `.at/redis/data`
> （构建时烘焙）。若把本工具跑在**工作区根**并加 `--with-server`，级别 3 的
> conf 默认会指向同一份 buck/swap——两个实例的 mongo-db 不同时共桶会报
> `LostBucket`。这种情况请在 `build.rs.json` 里显式指定独立的 `buck`/`swap`
> 目录，或把级别 3 工作目录放到别处。

退出码：`0` 成功；`1` 代号不合法；`2` `.at` 已存在且未加 `--force`。
