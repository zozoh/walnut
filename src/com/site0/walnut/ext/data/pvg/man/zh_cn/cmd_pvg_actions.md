# 过滤器简介

`@actions` 输出权限矩阵的动作列表。

# 用法

```bash
pvg [权限矩阵路径] @actions [-json] [-cqn]
       # 权限矩阵路径    # 不给则从管道读取矩阵 JSON
  -json    # JSON 模式输出，默认为文本模式
  -cqn     # JSON 模式下的格式化开关
```

# 示例

```bash
# 打印权限矩阵的动作列表
pvg ~/my-pvg.json @actions
# Action-A, Action-B, Action-C, Action-D

# JSON 形式输出
pvg ~/my-pvg.json @actions -json
# ["Action-A", "Action-B", "Action-C"]
```
