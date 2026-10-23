# 命令简介

`count` 统计目录下的子对象数量

不带参数统计当前目录。默认不计隐藏对象。

# 用法

```bash
count [-A] [-tp 类型] [-match '{条件}'] [目录...]
  -A       # 包含隐藏对象
  -tp      # 只统计指定类型（如 txt）
  -match   # 只统计匹配条件的对象
```

# 示例

```bash
# 当前目录（非隐藏）对象数量
count

# 上一级目录
count ..

# 包含隐藏对象
count -A

# 统计 txt 类型
count -tp txt

# 按条件统计
count -match '{tp:"txt"}'
```
