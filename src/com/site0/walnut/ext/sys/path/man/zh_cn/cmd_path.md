# 命令简介

`path` 路径工具：输出路径的绝对形式

# 用法

```bash
path @abs [-bish] [路径...]
```

支持的处理器：

```bash
@abs    # 输出绝对路径（多个时表格展示）
```

# 示例

```bash
# 当前目录（等同 pwd）
path @abs

# 单个路径
path @abs ~/data

# 多个路径（表格）
path @abs ~/a ~/b -h
```
