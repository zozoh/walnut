# 过滤器简介

`@abs` 输出路径的绝对形式。

- 不给参数时相当于 `pwd`
- 给一个参数时输出该路径的绝对路径（单行）
- 给多个参数时以表格展示每个路径及其绝对路径

# 用法

```bash
path @abs [-bish] [-ibase N] [路径...]
  -b       # 表格带边框
  -i       # 表格显示行号
  -s       # 表格尾部显示汇总行（total N paths）
  -h       # 表格显示表头
  -ibase   # 行号起始值，默认 0
```

# 示例

```bash
# 当前目录（等同 pwd）
path @abs

# 单个路径
path @abs ~/data

# 多个路径（带表头的表格）
path @abs ~/a ~/b -h

# 带边框、行号并汇总
path @abs ~/a ~/b ~/c -bish
```
