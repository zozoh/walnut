# 命令简介

`mkdir` 创建目录

默认要求父目录已存在，加 `-p` 自动创建缺失的父目录。

# 用法

```bash
mkdir [-pocnql] [-noexists] 路径...
  -p         # 自动创建父目录
  -o         # 输出创建的对象（JSON，默认无输出）
  -noexists  # 已存在时不报错（配合 -p 使用）
  -cqn       # 输出 JSON 的格式开关（见 cmd_output）
```

# 示例

```bash
# 创建目录
mkdir ~/data

# 自动创建多级父目录
mkdir -p ~/data/2026/10

# 已存在不报错
mkdir -p -noexists ~/data

# 创建并输出对象信息
mkdir -o ~/data
```
