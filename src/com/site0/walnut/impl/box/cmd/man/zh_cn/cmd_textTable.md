# 命令简介

`textTable` 把 JSON 数据渲染成文本表格

从管道读取 JSON 对象数组，默认输出对齐的文本表格。

# 用法

```bash
[上游命令] | textTable [-t|-tmpl 模板|-V]
  -t         # 显式指定表格输出（默认行为）
  -tmpl      # 按给定模板输出每一行
  -V         # 仅输出值
```

# 示例

```bash
# 查询结果渲染为表格
sqlx @query mydao 'select * from t_pet' | textTable

# 管道里任何 JSON 数组都可以
cat ~/data.json | textTable
```
