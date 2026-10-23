# 命令简介

`cmds` 列出系统中全部可用命令

用于确认某命令是否存在（比 `which` 更可靠，内置命令没有 `which`）。

# 用法

```bash
cmds [每行数量]
```

# 示例

```bash
# 列出全部命令（每行 10 个）
cmds

# 每行 50 个
cmds 50

# 检查某命令是否存在
cmds | grep sqlx
```
