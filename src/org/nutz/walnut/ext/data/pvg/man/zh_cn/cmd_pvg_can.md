命令简介
=======

`pvg can` 根据权限矩阵检查给定权限

用法
=======

```bash
pvg [PvgPath] can
    [Role1]        # 【必】角色名称
    [Action-A ..]  # 【必】新密码
    [-ajax]        # 【选】是否为 ajax 输出
    [-cqn]         # 【选】ajax 模式下的 JSON格式化参数
```
  
示例
=======

```bash
# 是否具备某个行为的权限
demo:> pvg ~/my-pvg.json can Role1 Action-B
true

# 是否全部具备某几个行为的权限
demo:> pvg ~/my-pvg.json can Role1 Action-B Action-C
false

# 是否具备某几个行为中至少一个的权限
demo:> pvg ~/my-pvg.json can -or Role1 Action-B Action-C
true

# 返回结果用 Ajax 格式包裹
demo:> pvg ~/my-pvg.json can Role1 Action-B Action-C -ajax -cqn
{"ok":true,"data":{"Action-B":true,"Action-C":true}}

# 返回结果用 Ajax 格式包裹
demo:> pvg ~/my-pvg.json can Role1 Action-B Action-E -ajax -cqn
{"ok":false,"data":{"Action-B":true,"Action-E":false}}
```
    
    
