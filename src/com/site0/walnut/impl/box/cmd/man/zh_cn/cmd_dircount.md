# 命令简介

`dircount` 统计目录的子对象数量，支持按深度遍历

对每个给定目录输出一条 JSON 记录（`id`/`name`/`path`/`count`）。

# 用法

```bash
dircount [-A] [-tp 类型] [-match '{条件}'] [-deep 深度] [目录...]
  -A       # 包含隐藏对象
  -tp      # 只统计指定类型
  -match   # 只统计匹配条件的对象
  -deep    # 遍历深度（默认 1）
```

# 示例

```bash
# 当前目录（非隐藏）对象数量
dircount

# 包含隐藏对象
dircount -A

# 统计 txt 类型
dircount -tp txt

# 按条件统计
dircount -match '{tp:"txt"}'

# 2 层深度统计
dircount -deep 2
```
