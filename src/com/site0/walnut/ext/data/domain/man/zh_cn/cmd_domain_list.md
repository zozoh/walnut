`domain list` 列出系统中的所有域

# 用法

```bash
domain @list
  [{..}]        # 【选】过滤条件
  [-limit 100]  # 一次最大查询限度，默认 100
  [-skip 0]     # 跳过多少记录
  [-pager]      # 显示分页信息
  [-sort]       # 排序方式，默认  {nm:1}
  [-json]       # 以JSON格式输出
  [-cqn]        # JSON 模式下的格式化参数
```

# 示例

```bash
# 列出所有域（表格模式）
domain @list

# 以JSON格式列出所有域
domain @list -json -cqn
```