# 命令简介

`export` 查看或设置会话环境变量

不带参数显示当前会话的全部环境变量；带 `键=值` 参数则逐个设置
（值支持 `${变量}` 展开），并持久化到会话存储。

# 用法

```bash
export                # 显示全部环境变量
export 键=值 [键=值...]  # 设置一个或多个变量
```

# 示例

```bash
# 查看全部环境变量
export

# 设置变量（立即持久化）
export MY_DB=mysql

# 值里引用其他变量
export BACKUP_DIR=~/backup/${MY_DB}
```
