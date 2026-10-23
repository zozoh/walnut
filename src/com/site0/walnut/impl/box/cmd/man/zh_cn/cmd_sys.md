# 命令简介

`sys` 查看系统级信息

默认输出全局配置（`/etc/sysconf`）；可选输出节点运行时、启动配置、进程环境变量。

# 用法

```bash
sys [-cqn] [-nice] [-runtime|-setup|-sysenv]
  -runtime    # 节点运行时：启动时间、存活时长、当前时间等
  -setup      # 启动配置（web.properties 全部键值）
  -sysenv     # 进程环境变量
  -nice       # 以文本表格输出（默认 JSON）
  -cqn        # JSON 格式开关：-c 紧凑、-n 含 null、-q 键名加引号
```

# 示例

```bash
# 查看全局配置
sys

# 查看节点运行了多久
sys -runtime

# 表格形式查看启动配置
sys -setup -nice
```
