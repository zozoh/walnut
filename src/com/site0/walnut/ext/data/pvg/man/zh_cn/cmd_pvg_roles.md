命令简介
=======

`pvg roles` 输出权限矩阵的角色列表

用法
=======

```bash
pvg [PvgPath] roles
    [-json]        # 【选】是否为 JSON 模式输出，默认为文本模式
    [-cqn]         # 【选】JSON 模式下的格式化参数
```

示例
=======

```bash
# 打印权限矩阵的角色列表
demo:> pvg ~/my-pvg.json roles
Role1, Role2, Role3

# 输出权限矩阵角色列表的 JSON 形式
demo:> pvg ~/my-pvg.json roles -json
["Role1", "Role2", "Role3"]
```
    
    
