# 命令简介

`output` 输出内容，支持延迟、间隔、批量

与 `echo` 类似，主要用于测试管道行为（如异步输出、缓慢输出场景）。

# 用法

```bash
output [-delay 毫秒] [-n 条数] [-interval 毫秒] [-tei] 内容...
  -delay      # 延迟多少毫秒后输出
  -n          # 输出多少条信息
  -interval   # 每条信息的时间间隔（毫秒）
  -e          # 输出到错误流
  -t          # 每条信息前加时间戳
  -i          # 输出每条信息的序号
```

# 示例

```bash
# 直接输出
output hello

# 延迟 500ms 输出
output -delay 500 hello

# 每秒输出一条，共 5 条
output -n 5 -interval 1000 hello

# 输出到错误流
output -e hello
```
