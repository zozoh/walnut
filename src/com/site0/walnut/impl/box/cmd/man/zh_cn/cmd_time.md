# 命令简介

`time` 计量命令执行时间

# 用法

```bash
time [-t 次数] 命令...
  -t    # 重复执行次数（用于取平均耗时）
```

# 示例

```bash
# 计量一次命令的耗时
time ls ~/data

# 重复 10 次取平均
time -t 10 ls ~/data
```
