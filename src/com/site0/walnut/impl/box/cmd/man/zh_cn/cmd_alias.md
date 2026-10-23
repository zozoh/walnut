# 命令简介

`alias` 定义带参数的别名命令

别名保存在会话中，展开后执行真实命令并原样转发额外参数。

# 用法

```bash
alias            # 显示当前已存在的别名
alias -p         # 同上
alias -r 名称     # 移除一个别名
alias 名称=命令   # 定义别名
```

# 示例

```bash
# 显示已存在的别名
alias -p

# 移除别名
alias -r qq

# 定义别名
alias qq="ll -A"

# 执行别名（实际执行 ll -A /home）
qq /home
```
