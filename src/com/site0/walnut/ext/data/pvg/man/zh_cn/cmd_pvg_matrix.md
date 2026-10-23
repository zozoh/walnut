# 过滤器简介

`@matrix` 输出权限矩阵。

# 用法

```bash
pvg [权限矩阵路径] @matrix [-json] [-cqn] [-t 样式] [-ibase N]
       # 权限矩阵路径    # 不给则从管道读取矩阵 JSON
  -json    # JSON 模式输出，默认为表格模式
  -cqn     # JSON 模式下的格式化开关
  -t       # 是/否的输出字符样式；不给时表格模式相当于 Yes:N/A，JSON 模式为 true/false
  -ibase   # 表格模式的行序号起始数字，默认 1
```

`-t` 模式的含义：

- 不给定：表格 `Yes`/`--`，JSON `true`/`false`
- `"Yes:N/A"`：表格 `Yes`/`N/A`，JSON `"Yes"`/`"N/A"`
- `"true:false"`：表格 `true`/`false`，JSON `true`/`false`
- `"On:Off"`：表格 `On`/`Off`，JSON `"On"`/`"Off"`
- `"Yes"`：是输出 `Yes`，否输出空（JSON 为 `undefined`）
- `":No"`：是输出空，否输出 `No`

# 示例

```bash
# 打印权限矩阵表格
pvg ~/my-pvg.json @matrix
#  # | Roles | Action-A | Action-B | Action-C | Action-D | Action-E
#  1 | Role0 | Yes      | --       | --       | --       | --
#  2 | Role1 | Yes      | --       | Yes      | Yes      | --
#  ...

# JSON 形式输出
pvg ~/my-pvg.json @matrix -json
# {
#   "Role1" : {
#     "Action-A" : true,
#     "Action-B" : true,
#     ...
#   }
# }
```
