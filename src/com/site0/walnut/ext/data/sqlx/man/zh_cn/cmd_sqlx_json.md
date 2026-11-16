# 过滤器简介

`@json` 控制 `sqlx` 最终结果的 JSON 输出格式。

# 用法

```bash
sqlx @json
  [-cqn]              # JSON 格式化方式，如 -cqn 紧凑输出
  [-as auto|raw|obj]  # 结果处理方式
                      # raw  : 默认，原样输出
                      # auto : 单元素数组自动展开；隐藏批量执行细节
                      # obj  : 强制取数组/集合的第一个元素
```

- `-as obj` 适合确定只有一条结果的查询
- 批量 `@exec` 的结果是 `SqlExecResult`；`-as auto` 会隐藏内部批量细节

# 示例

```bash
# 紧凑输出查询结果
sqlx @vars '{filter:{st:1}}' @query pet.select @json -cqn

# 只输出第一条
sqlx @vars '{id:"xxx"}' @query pet.fetch @json -as obj

# 批量插入结果
cat ~/pets.json | sqlx @vars -as list @exec pet.insert @json -as auto
```
